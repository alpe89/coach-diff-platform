# CoachDiff.ai — Project Plan

> AI-powered League of Legends coaching platform. Full roadmap from foundation to replay analysis.
>
> Last updated: 2026-02-27

---

## Status Legend
- ✅ **Done** — Implemented, tested, deployed
- 🔧 **Active** — Currently in progress
- ⬜ **Planned** — Not started yet

---

## Phase 1: Foundation ✅

### 1.1 Profile Endpoint ✅
Fetch summoner identity + ranked data by combining three Riot APIs.
- Account-V1: resolve summoner name + tag → PUUID
- Summoner-V4: profile icon URL
- League-V4: tier, division, LP, wins/losses
- `GET /api/profile` returns unified `Profile` domain model
- StructuredTaskScope (Java 25) for parallel API calls

### 1.2 Match Aggregation Endpoint ✅
Fetch last 20 ranked matches, compute aggregate stats (combat, economy, objectives, vision).
- Match-V5: match details + timeline data
- Service orchestration: check DB → fetch missing from Riot → save new → compute aggregate
- Timeline parsing for early-game metrics (CS@10, Gold@15, lane minions first 10min)
- `GET /api/matches` returns `MatchAggregate` domain model

### 1.3 PostgreSQL Persistence ✅
Match records persisted with composite key (matchId + puuid).
- Neon (serverless Postgres) in production
- Docker Compose (`postgres:17-alpine`) for local dev
- Testcontainers for tests
- Flyway migrations for schema management (V1-V6)

### 1.4 Structured Logging ✅
ECS format for GCP Cloud Logging, DEBUG level on `com.coachdiff`.

### 1.5 Season Scoping ✅
`SEASON_START_EPOCH` config filters matches to current season only.

### 1.6 Remake Filtering ✅
Games under 10 minutes excluded before saving to DB.

---

## Phase 2: Data Enrichment ✅

### 2.1 Role + Champion on MatchRecord ✅
Flyway migration (V4) adding `role` and `champion_name` columns.
Domain model, entity, and adapter updated to track which role/champion was played.

### 2.2 Champion Breakdown in Aggregation ✅
Per-champion stats alongside overall aggregate. Champion name tracked but NOT aggregated at top level.

### 2.3 Role-Based Filtering ✅
Only aggregate games matching user's coaching role. All matches saved to DB regardless of role — filtering happens at aggregation time.

---

## Phase 3: Account Domain ✅

### 3.1 Account CRUD ✅
Full create/read/update/delete for Account (name, tag, coaching role, region).
- `ManageAccountPort` (inbound port) → `AccountService` → `AccountPersistencePort` (outbound port)
- `AccountController` with REST endpoints
- `AccountEntity` with JPA persistence

### 3.2 Wire Account into Endpoints ✅
`X-User-Email` header on all endpoints replaces hardcoded env vars.
Account loaded from DB, provides summoner name/tag/region for Riot API calls.

### 3.3 Cache Cleanup ✅
Removed `@Cacheable` from match/timeline (not needed with DB persistence).
Caffeine cache remains only on Riot Account (PUUID) lookups.

---

## Phase 4: Security & Permissions 🔧

### 4.1 Permission Domain Model ✅
`Permission` enum with key-based mapping (`BASE_USE`, `COACH_USE`).
Static `KEY_LOOKUP` map for O(1) `fromKey()` resolution.

### 4.2 JSONB Permissions on Account ✅
Flyway migration (V7) adding `permissions` JSONB column with default `{"base_use": true}`.
`AccountEntity` uses `@JdbcTypeCode(SqlTypes.JSON)` for Hibernate 6+ native JSONB support.
`toDomain()`/`fromDomain()` converts between `Map<String, Boolean>` (entity) and `Map<Permission, Boolean>` (domain).

### 4.3 SecurityConfig Foundation ✅
Spring Security configured: CSRF disabled, stateless sessions, CORS with env-based allowed origins, HTTP Basic/form login disabled.
`auth.anyRequest().authenticated()` — but no authentication mechanism wired yet.

### 4.4 Google OAuth2 Resource Server ⬜
**NEXT STEP.** Wire up `spring-boot-starter-oauth2-resource-server` so the API validates Google JWTs.
- Add dependency to `pom.xml`
- Configure JWT issuer URI in `application.yml`
- Update `SecurityConfig` with `.oauth2ResourceServer(oauth2 -> oauth2.jwt(...))`
- Extract user email from JWT claims (replace `X-User-Email` header)
- Update controller tests to mock security context

### 4.5 Permission-Based Authorization ⬜
Use permissions from Account to gate access to specific features.
`BASE_USE` = can use profile/matches endpoints. `COACH_USE` = can use LLM coaching (future).

---

## Phase 5: Benchmarks ⬜

### 5.1 Rank Benchmark Data ⬜
Static reference data: role + rank + metric → expected values.
Compiled from community sources (OP.GG, U.GG tier averages).
- Flyway migration for benchmark table (V5/V6 already created table + seed data)
- `BenchmarkPersistencePort` + `BenchmarkPersistenceAdapter` (already implemented)
- Domain model for benchmark comparison
- Endpoint or inclusion in match aggregate response

---

## Phase 6: LLM Coach (MVP) ⬜

### 6.1 LLM Analysis Layer ⬜
Separate endpoint that sends aggregate + benchmark data to an LLM for coaching feedback.
- Prompt builder in domain layer (constructs coaching context from stats)
- API-based (Claude or OpenAI) — NOT embedded model
- Spring AI framework for LLM integration
- `POST /api/coaching` or similar endpoint

### 6.2 Coach Response Caching ⬜
Hash aggregate + benchmark input, persist coaching response in DB.
Invalidate when new match data detected (aggregate changes).

---

## Phase 7: Post-MVP Exploration ⬜

### 7.1 Replay Parsing ⬜
Explore feasibility of parsing `.rofl` replay files for granular play-by-play analysis.
Would enable advanced metrics not available from API alone (positioning, mouse patterns, reaction times).

### 7.2 Differential Metrics ⬜
CS/Gold/XP diffs vs lane opponent (requires opponent identification from match data).

### 7.3 Role-Specific Metrics ⬜
Per-role specialized stats (see [Match Metrics Guide](match-metrics-guide.md) Phase 3).

### 7.4 Advanced Timeline Analysis ⬜
Isolation deaths, recall efficiency, objective setup quality, positional heatmaps (see [Match Metrics Guide](match-metrics-guide.md) Phase 4).

---

## Key Architecture Decisions

| Decision | Rationale |
|----------|-----------|
| Hexagonal Architecture | Clean separation of concerns, testable domain, swappable infrastructure |
| Save ALL matches, filter at aggregation | Avoids re-fetching from Riot API when coaching role changes |
| Remake filtering before save | Games < 10min are noise, not worth storing |
| Benchmark data is static | Compiled from community sources, not live API. Simpler and cheaper. |
| Permissions as JSONB | Flexible, no join table needed, easy to extend |
| `Account.withUpdates()` wither | Avoids 7-arg constructors for partial updates on records |
| OAuth2 Resource Server (not client) | API receives tokens from frontend, validates them. Doesn't issue tokens. |

---

## Lessons Learned (Gotchas)

- Apple Silicon (ARM) Docker images won't run on Cloud Run (amd64). Use `--platform linux/amd64` for local builds. Not needed in CI (GitHub Actions runners are amd64).
- `gcloud --set-env-vars` REPLACES all env vars, `--update-env-vars` MERGES. Critical difference.
- Docker `--env-file` does NOT strip quotes from values. Use bare `KEY=value` format.
- `--enable-preview` needed at THREE levels: compiler plugin, surefire plugin, runtime JVM flags (Dockerfile + spring-boot plugin). Easy to miss.
- DDragon CDN versions ≠ Riot seasonal versions. Check `https://ddragon.leagueoflegends.com/api/versions.json` for the real ones.
- Datasource env vars: `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD` (Spring Boot auto-binds these).
- Neon requires SSL: `?sslmode=require&channel_binding=require` in JDBC URL.
- Flyway migration naming: `V{N}__{description}.sql` (double underscore).

---

## Reference Docs
- [Match Metrics Guide](match-metrics-guide.md) — full taxonomy of metrics, tiers, what to track and when
- [CLAUDE.md](../CLAUDE.md) — project instructions, coding conventions, deployment details
