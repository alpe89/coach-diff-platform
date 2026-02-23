#!/usr/bin/env python3
"""
One-time benchmark data crawler for CoachDiff.

Crawls ranked matches from the Riot API, extracts per-participant stats
tagged by rank and role, and aggregates them into benchmark values.

Usage:
    RIOT_API_KEY=RGAPI-xxx python scripts/benchmark_crawler.py

Output:
    scripts/benchmark_raw.csv         — raw per-participant data (~12k rows)
    scripts/benchmark_aggregated.csv  — median + mean by tier × role (40 rows)
    scripts/benchmark_insert.sql      — ready-to-paste Flyway migration SQL
"""

import csv
import os
import random
import statistics
import sys
import time
from collections import deque

import requests

# ---------------------------------------------------------------------------
# Config
# ---------------------------------------------------------------------------

RIOT_API_KEY = os.environ.get("RIOT_API_KEY")
if not RIOT_API_KEY:
    print("ERROR: Set RIOT_API_KEY environment variable")
    sys.exit(1)

PLATFORM_URL = "https://euw1.api.riotgames.com"  # League-V4, Summoner-V4
REGIONAL_URL = "https://europe.api.riotgames.com"  # Match-V5

QUEUE_ID = 420  # Ranked Solo/Duo
SUMMONERS_PER_TIER = 20
MATCHES_PER_SUMMONER = 20
SEASON_START_EPOCH = 1736294400

HEADERS = {"X-Riot-Token": RIOT_API_KEY}

TIERS = ["IRON", "BRONZE", "SILVER", "GOLD", "PLATINUM", "EMERALD", "DIAMOND"]
DIVISIONS = ["I", "II", "III", "IV"]

METRICS = [
    "cs_per_min",
    "kda",
    "gold_per_min",
    "dpm",
    "vision_score_per_min",
    "kill_participation",
]

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))

# ---------------------------------------------------------------------------
# Rate limiter — token bucket for 100 req / 120s + 20 req/s burst
# ---------------------------------------------------------------------------

call_timestamps: deque[float] = deque()
WINDOW_SIZE = 100
WINDOW_SECONDS = 120.0
MIN_INTERVAL = 0.055  # ~18 req/s, slight margin under 20 req/s burst

request_count = 0


def rate_limited_get(url: str, params: dict | None = None) -> requests.Response:
    global request_count
    now = time.time()

    # Enforce burst limit
    if call_timestamps:
        elapsed = now - call_timestamps[-1]
        if elapsed < MIN_INTERVAL:
            time.sleep(MIN_INTERVAL - elapsed)

    # Enforce sliding window (100 req / 120s)
    if len(call_timestamps) >= WINDOW_SIZE:
        oldest = call_timestamps[0]
        wait = WINDOW_SECONDS - (time.time() - oldest)
        if wait > 0:
            print(f"  [rate limit] sleeping {wait:.1f}s (window full)")
            time.sleep(wait)
        call_timestamps.popleft()

    call_timestamps.append(time.time())
    request_count += 1

    resp = requests.get(url, headers=HEADERS, params=params, timeout=15)

    if resp.status_code == 429:
        retry_after = int(resp.headers.get("Retry-After", 10))
        print(f"  [429] Rate limited, retrying after {retry_after}s...")
        time.sleep(retry_after)
        return rate_limited_get(url, params)

    return resp


# ---------------------------------------------------------------------------
# Phase 1: Collect raw data
# ---------------------------------------------------------------------------


def fetch_league_entries(tier: str) -> list[dict]:
    """Fetch ranked summoner entries for a tier. Returns list of entry dicts."""
    entries = []

    if tier == "MASTER_PLUS":
        for apex_tier in ["masterleagues", "grandmasterleagues", "challengerleagues"]:
            url = f"{PLATFORM_URL}/lol/league/v4/{apex_tier}/by-queue/RANKED_SOLO_5x5"
            resp = rate_limited_get(url)
            if resp.status_code == 200:
                data = resp.json()
                entries.extend(data.get("entries", []))
            else:
                print(f"  WARN: {apex_tier} returned {resp.status_code}")
        return entries

    for div in DIVISIONS:
        url = f"{PLATFORM_URL}/lol/league/v4/entries/RANKED_SOLO_5x5/{tier}/{div}"
        resp = rate_limited_get(url, params={"page": 1})
        if resp.status_code == 200:
            entries.extend(resp.json())
        else:
            print(f"  WARN: {tier} {div} returned {resp.status_code}")

    return entries


def fetch_match_ids(puuid: str) -> list[str]:
    """Fetch recent ranked match IDs for a PUUID."""
    url = f"{REGIONAL_URL}/lol/match/v5/matches/by-puuid/{puuid}/ids"
    params = {
        "queue": QUEUE_ID,
        "start": 0,
        "count": MATCHES_PER_SUMMONER,
        "startTime": SEASON_START_EPOCH,
    }
    resp = rate_limited_get(url, params=params)
    if resp.status_code == 200:
        return resp.json()
    print(f"  WARN: match list for {puuid[:8]}... returned {resp.status_code}")
    return []


def fetch_match_detail(match_id: str) -> dict | None:
    """Fetch full match details."""
    url = f"{REGIONAL_URL}/lol/match/v5/matches/{match_id}"
    resp = rate_limited_get(url)
    if resp.status_code == 200:
        return resp.json()
    print(f"  WARN: match {match_id} returned {resp.status_code}")
    return None


def extract_participant_data(match: dict, tier_label: str) -> list[dict]:
    """Extract metrics for all 10 participants in a match."""
    info = match.get("info", {})
    game_duration_s = info.get("gameDuration", 0)
    game_duration_min = game_duration_s / 60.0

    # Skip remakes
    if game_duration_min < 10:
        return []

    rows = []
    for p in info.get("participants", []):
        role = p.get("teamPosition", "")
        if role not in ("TOP", "JUNGLE", "MIDDLE", "BOTTOM", "UTILITY"):
            continue

        challenges = p.get("challenges", {})

        cs = p.get("totalMinionsKilled", 0) + p.get("neutralMinionsKilled", 0)
        cs_per_min = round(cs / game_duration_min, 2) if game_duration_min > 0 else 0

        rows.append(
            {
                "tier": tier_label,
                "role": role,
                "match_id": match.get("metadata", {}).get("matchId", ""),
                "champion": p.get("championName", ""),
                "cs_per_min": cs_per_min,
                "kda": round(challenges.get("kda", 0), 2),
                "gold_per_min": round(challenges.get("goldPerMinute", 0), 1),
                "dpm": round(challenges.get("damagePerMinute", 0), 1),
                "vision_score_per_min": round(
                    challenges.get("visionScorePerMinute", 0), 3
                ),
                "kill_participation": round(
                    challenges.get("killParticipation", 0), 3
                ),
            }
        )

    return rows


def collect_tier_data(tier_label: str) -> list[dict]:
    """Collect all raw participant data for a single tier."""
    print(f"\n{'='*60}")
    print(f"TIER: {tier_label}")
    print(f"{'='*60}")

    # Step 1: Get league entries
    print(f"  Fetching league entries...")
    entries = fetch_league_entries(tier_label)
    print(f"  Got {len(entries)} entries")

    if len(entries) < SUMMONERS_PER_TIER:
        print(f"  WARN: Only {len(entries)} entries, using all of them")
        sample = entries
    else:
        sample = random.sample(entries, SUMMONERS_PER_TIER)

    # Step 2: Fetch match IDs (puuid is included directly in league entries)
    all_match_ids: set[str] = set()
    summoner_count = 0

    for entry in sample:
        puuid = entry.get("puuid")
        if not puuid:
            continue

        match_ids = fetch_match_ids(puuid)
        new_ids = set(match_ids) - all_match_ids
        all_match_ids.update(match_ids)
        summoner_count += 1
        print(
            f"  Summoner {summoner_count}/{len(sample)}: "
            f"{len(match_ids)} matches, {len(new_ids)} new "
            f"(total unique: {len(all_match_ids)})"
        )

    # Step 3: Fetch match details and extract data
    print(f"  Fetching {len(all_match_ids)} unique match details...")
    all_rows = []
    for i, match_id in enumerate(all_match_ids, 1):
        match = fetch_match_detail(match_id)
        if match:
            rows = extract_participant_data(match, tier_label)
            all_rows.extend(rows)
        if i % 25 == 0:
            print(f"    {i}/{len(all_match_ids)} matches processed ({len(all_rows)} rows)")

    print(f"  DONE: {len(all_rows)} participant rows from {len(all_match_ids)} matches")
    return all_rows


def phase1_collect() -> str:
    """Run Phase 1: collect raw data from all tiers. Returns CSV path."""
    all_tiers = TIERS + ["MASTER_PLUS"]
    all_rows = []

    for tier in all_tiers:
        rows = collect_tier_data(tier)
        all_rows.extend(rows)

    csv_path = os.path.join(SCRIPT_DIR, "benchmark_raw.csv")
    fieldnames = ["tier", "role", "match_id", "champion"] + METRICS

    with open(csv_path, "w", newline="") as f:
        writer = csv.DictWriter(f, fieldnames=fieldnames)
        writer.writeheader()
        writer.writerows(all_rows)

    print(f"\n{'='*60}")
    print(f"Phase 1 complete: {len(all_rows)} rows written to {csv_path}")
    print(f"Total API calls: {request_count}")
    return csv_path


# ---------------------------------------------------------------------------
# Phase 2: Aggregate
# ---------------------------------------------------------------------------


def phase2_aggregate(raw_csv_path: str):
    """Read raw CSV, compute median + mean by tier × role, output results."""

    # Read raw data
    with open(raw_csv_path, newline="") as f:
        reader = csv.DictReader(f)
        rows = list(reader)

    print(f"\nPhase 2: Aggregating {len(rows)} rows...")

    # Group by (tier, role)
    groups: dict[tuple[str, str], list[dict]] = {}
    for row in rows:
        key = (row["tier"], row["role"])
        groups.setdefault(key, []).append(row)

    # Compute aggregates
    agg_rows = []
    for (tier, role), group_rows in sorted(groups.items()):
        agg = {"tier": tier, "role": role, "sample_size": len(group_rows)}
        for metric in METRICS:
            values = [float(r[metric]) for r in group_rows if float(r[metric]) > 0]
            if values:
                agg[f"median_{metric}"] = round(statistics.median(values), 3)
                agg[f"avg_{metric}"] = round(statistics.mean(values), 3)
            else:
                agg[f"median_{metric}"] = 0
                agg[f"avg_{metric}"] = 0
        agg_rows.append(agg)

    # Write aggregated CSV
    agg_csv_path = os.path.join(SCRIPT_DIR, "benchmark_aggregated.csv")
    agg_fields = ["tier", "role", "sample_size"]
    for metric in METRICS:
        agg_fields.extend([f"median_{metric}", f"avg_{metric}"])

    with open(agg_csv_path, "w", newline="") as f:
        writer = csv.DictWriter(f, fieldnames=agg_fields)
        writer.writeheader()
        writer.writerows(agg_rows)

    print(f"Aggregated CSV: {agg_csv_path} ({len(agg_rows)} rows)")

    # Generate SQL INSERT
    sql_path = os.path.join(SCRIPT_DIR, "benchmark_insert.sql")
    median_cols = [f"median_{m}" for m in METRICS]
    avg_cols = [f"avg_{m}" for m in METRICS]
    all_cols = ["tier", "role"] + median_cols + avg_cols

    with open(sql_path, "w") as f:
        f.write(
            f"INSERT INTO benchmark ({', '.join(all_cols)})\nVALUES\n"
        )
        value_lines = []
        for row in agg_rows:
            vals = [f"'{row['tier']}'", f"'{row['role']}'"]
            for col in median_cols + avg_cols:
                vals.append(str(row[col]))
            value_lines.append(f"  ({', '.join(vals)})")
        f.write(",\n".join(value_lines))
        f.write(";\n")

    print(f"SQL output: {sql_path}")

    # Print summary table
    print(f"\n{'='*60}")
    print("SUMMARY (medians)")
    print(f"{'='*60}")
    print(
        f"{'Tier':<13} {'Role':<8} {'N':>5} {'CS/m':>6} {'KDA':>6} "
        f"{'Gold/m':>7} {'DPM':>7} {'VS/m':>6} {'KP':>6}"
    )
    print("-" * 72)
    for row in agg_rows:
        print(
            f"{row['tier']:<13} {row['role']:<8} {row['sample_size']:>5} "
            f"{row['median_cs_per_min']:>6} {row['median_kda']:>6} "
            f"{row['median_gold_per_min']:>7} {row['median_dpm']:>7} "
            f"{row['median_vision_score_per_min']:>6} "
            f"{row['median_kill_participation']:>6}"
        )


# ---------------------------------------------------------------------------
# Main
# ---------------------------------------------------------------------------

if __name__ == "__main__":
    print("CoachDiff Benchmark Crawler")
    print(f"API Key: {RIOT_API_KEY[:12]}...")
    print(f"Platform: {PLATFORM_URL}")
    print(f"Region: {REGIONAL_URL}")
    print(f"Season start: {SEASON_START_EPOCH}")
    print(f"Sampling: {SUMMONERS_PER_TIER} summoners × {MATCHES_PER_SUMMONER} matches per tier")

    start = time.time()
    raw_csv = phase1_collect()
    phase2_aggregate(raw_csv)
    elapsed = time.time() - start

    print(f"\nTotal time: {elapsed / 60:.1f} minutes")
    print(f"Total API calls: {request_count}")
