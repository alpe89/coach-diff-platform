# Match Metrics Guide

## Sources
Compiled from coaching frameworks (LS, VeigarV2, SkillCapped), stat platforms (OP.GG, U.GG, Mobalytics), and Riot API documentation.

---

## Core Principle
> Early gold leads + low deaths + objective conversion explain the majority of climb potential.

Vanity stats (raw KDA, total damage, multikills) are often misleading. Focus on **efficiency** and **differentials**.

---

## Riot API Endpoints Used

### Match-V5 (`/lol/match/v5/matches/{matchId}`)
End-of-game summaries. Contains: kills, deaths, assists, gold, damage, CS, vision, objectives, etc.

### Timeline-V5 (`/lol/match/v5/matches/{matchId}/timeline`)
Per-minute snapshots + discrete events. Contains: gold/xp/cs at each minute, positions (x,y), kill events, ward events, item purchases, skill level-ups.

---

## Metric Tiers (by impact on improvement)

### S-Tier (Strongest predictors)
| Metric | Source | Calculation | Why |
|--------|--------|-------------|-----|
| Gold Diff @10/15 | Timeline (participantFrames.totalGold) | PlayerGold@T - OpponentGold@T | Strongest single predictor of winning |
| CS/Min | Match (totalMinionsKilled + neutralMinionsKilled) | TotalCS / GameMinutes | Most consistent skill expression |
| CS Diff @10/15 | Timeline (participantFrames.minionsKilled) | PlayerCS@T - OpponentCS@T | Direct laning skill measure |
| XP Diff @10/15 | Timeline (participantFrames.xp) | PlayerXP@T - OpponentXP@T | If gold is high but XP low = bad recalls/over-roaming |
| Deaths (total + timing) | Match + Timeline events | Count + timestamps | Grey screen = biggest resource loss |
| Damage Per Gold | Match (totalDamageDealtToChampions / goldEarned) | ChampDmg / GoldEarned | Efficiency > raw damage. Separates good from great. |

### A-Tier (Highly influential)
| Metric | Source | Calculation | Why |
|--------|--------|-------------|-----|
| Kill Participation | Match | (Kills + Assists) / TeamKills | Map presence + teamfight awareness |
| Damage Share | Match | PlayerDmg / TeamDmg | Role execution check. ADC/Mid should be >25-30% |
| Gold Share | Match | PlayerGold / TeamGold | Compare with Damage Share to detect dead weight |
| Vision Score/Min | Match (visionScore / gameDuration) | VS / GameMinutes | Map control + objective setup |
| Objective Participation | Timeline events | PlayerObjEvents / TeamObjEvents | Macro awareness |
| Gold Per Minute | Match | GoldEarned / GameMinutes | Overall economy efficiency |

### B-Tier (Useful optimization)
| Metric | Source | Calculation | Why |
|--------|--------|-------------|-----|
| Solo Kills | Match | Direct field | 1v1 mechanical skill |
| Turret Plate Gold | Match | Direct field | Lane dominance indicator |
| Damage Taken % | Match | PlayerDmgTaken / TeamDmgTaken | Positioning/tankiness check |
| Control Wards Placed | Match | Direct field | Vision investment |
| Wards Killed | Match | Direct field | Vision denial |
| Crowd Control Time | Match | Direct field | Utility contribution |

### C-Tier (Vanity / misleading alone)
- Raw KDA (without context)
- Raw kill count
- Total damage (without gold context)
- Multikills

---

## MVP MatchAggregate (Current Implementation)
What we're building NOW. Aggregated averages over last 20 matches.

### From Match-V5:
- gamesAnalyzed, wins, losses, winRate (derived)
- **Combat:** avgKills, avgDeaths, avgAssists, avgKda, avgSoloKills, avgDamagePerMinute, avgTotalDamageToChampions, avgDamagePerGold, avgTeamDamagePercentage, avgDamageTakenPercentage, avgKillParticipation
- **Economy:** avgGoldPerMinute, avgCsPerMinute
- **Objectives:** avgDamageToTurrets, avgDamageToObjectives, avgTurretPlatesTaken
- **Vision:** avgVisionScorePerMinute, avgWardsPlaced, avgWardsKilled, avgControlWardsPlaced

### From Timeline-V5:
- **Economy:** avgCsAt10, avgGoldAt15, avgLaneMinionsFirst10Min

### Currently MISSING from MVP (should consider adding):
- avgDeathsPreLaning (deaths before 14min — from Timeline)
- avgXpAt10 / avgXpAt15 (XP tracking — from Timeline)
- avgGoldAt10 (we have @15 but @10 is also S-tier)
- avgDamageShare (team context — from Match)
- avgGoldShare (team context — from Match)
- avgObjectiveParticipation (from Timeline events)

---

## Future Features (POST-MVP)

### Phase 2: Differential Metrics (requires opponent data)
- CSD@10/15 (CS vs lane opponent)
- GoldDiff@10/15 (Gold vs lane opponent)
- XPDiff@10/15 (XP vs lane opponent)
- These are the MOST valuable metrics but require identifying the lane opponent from the match data

### Phase 3: Role-Specific Metrics
- **Top:** TP value score, split push pressure, 1v1 kill rate
- **Jungle:** Objective control rate, gank success rate, early path efficiency, counter-jungle gold
- **Mid:** Roam success rate, lane priority time
- **ADC:** Damage per death (DPD = ChampDmg/Deaths), positioning error rate
- **Support:** Vision denial rate (wardsCleared/enemyWardsPlaced), roam impact

### Phase 4: Advanced Timeline Analysis
- Isolation deaths (death position far from allies — x,y coordinate analysis)
- Recall timing efficiency (gold held when recalling vs item breakpoints)
- Objective setup quality (vision placed before dragon/baron spawn)
- Forward % (time spent past river midline — positional heatmaps)
- Teamfight detection (clustered kill events)
- Pressure conversion (kills -> towers/dragons/baron rate)

### Phase 5: Consistency & Meta Metrics
- Consistency score (std deviation of CSPM, deaths, gold@10 across games)
- Tempo score (time dead + recalling + walking without purpose)
- Win condition alignment (resources invested toward team win condition)
- Champion-specific benchmarks (compare stats vs champion averages)
- Rank-relative performance (compare vs rank average)

### Phase 6: AI Coaching
- Automated strength/weakness identification
- Personalized improvement priorities using the weighted model:
  - 30% Early Economy, 25% Death Efficiency, 20% Objective Participation, 15% Role-Specific, 10% Vision
- Game-by-game trend analysis

---

## Important Implementation Notes
- **Always normalize by game duration** (per-minute metrics) — a 45min game ≠ 25min game
- **Match-V5 regional routing:** match + timeline endpoints use regional (europe/americas), NOT platform (euw1)
- **Rate limits:** Dev key = 20 req/s, 100 req/2min. Use Resilience4j RateLimiter.
- **Participant identification:** Find the summoner's participantId in the match data by matching PUUID, then extract their stats
- **Lane opponent identification (future):** Match by role + team side (Blue Top vs Red Top)
- **Timeline frames are per-minute** (frame[0] = 0:00, frame[10] = 10:00, frame[15] = 15:00)
