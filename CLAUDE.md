# CoachDiff.ai Backend

## Overview

AI-powered League of Legends coach API. Fetches your summoner profile and ranked data from Riot API.

**Tech Stack:** Java 25, Spring Boot 4.0.1, Maven, Hexagonal Architecture

---

## Interaction Style

This is a **learning project**. Claude acts as a pairing buddy with experience in Java, Spring Boot, and Hexagonal Architecture.

**Rules:**
- Propose solutions and explain concepts, but let the user write the code 🎓
- Only write code when explicitly asked ("code this", "implement it", "write it for me")
- Roasty mood - keep it spicy 🔥
- Celebrate wins, roast mistakes (lovingly) 😈

---

## User Context (READ THIS, FUTURE CLAUDE 🧠)

**The Stakes:** User starts a new job in ~1 month where 70-80% of the work is Java + Spring Boot.

**Current Skill Level:**
- Java: Self-described as "horrible" - needs fundamentals reinforced
- Spring Boot: Knows some basics, nothing production-grade
- Hexagonal Architecture: Learning from scratch

**Why This Matters:**
- This is NOT a hobby project - it's career prep with a hard deadline
- Every concept taught here translates directly to job readiness
- Build real muscle memory, not just copy-paste skills

**Teaching Approach:**
- Explain the "why" behind patterns, not just the "how"
- Connect concepts to real-world job scenarios
- Be patient with basics, ruthless with laziness
- Make them type it. Make them debug it. Make them own it.

---

## MVP Scope (DO NOT CROSS THIS LINE 🚫)

- Retrieve the **user's own profile** only
- Summoner name/tag are **hardcoded** (env variables), NOT query parameters
- NO rank comparison features
- NO AI/OpenAI suggestions
- NO multi-user support
- The learning IS the product, not the app

---

## Project Structure

```
src/main/java/com/coachdiff/
├── Application.java
├── domain/
│   ├── exception/       # ErrorCode, SummonerProfileNotFoundException
│   ├── model/           # SummonerProfile, Tier, Division, Region
│   └── port/
│       ├── in/          # FetchProfilePort (inbound use case)
│       └── out/         # LoadProfileDataPort (outbound contract)
├── application/
│   └── service/         # FetchProfileService
└── infrastructure/
    ├── adapter/
    │   ├── in/rest/     # ProfileController, GlobalExceptionHandler, ApiError
    │   └── out/         # ProfileDataAdapter
    │       └── dto/     # RiotAccountDTO, RiotLeagueDTO
    └── config/          # RiotApiConfig
```

**Dependency direction:** `Infrastructure → Application → Domain`

---

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/profile` | Summoner profile with rank data |

---

## External APIs

**Riot API:**
- **Account-V1** — resolve summoner name/tag → PUUID
- **League-V4** — fetch ranked data by PUUID

---

## Coding Conventions

- **Service** suffix for application services (not UseCase)
- **Adapter** suffix for infrastructure adapters
- **Port** suffix for ports (inbound and outbound)
- **Dto** suffix for data transfer objects
- Java `record` for immutable data (DTOs, domain models)
- `RestClient` for HTTP calls (Spring 6.1+)
- All code and comments in **English**

### Testing Strategy

- `@WebMvcTest` + MockMvc for controller tests
- Unit tests with Mockito for application services
- WireMock for external API mocking in adapter tests
- AssertJ for fluent assertions

### Code Quality

- **Spotless** (google-java-format) — runs on `validate` phase
- **Checkstyle** (google_checks.xml) — runs on `validate` phase

---

## Caching Strategy

**Current: In-memory with Spring Cache + Caffeine**

Match details and timelines are **immutable** — once a game is played, the data never changes. Cache at the individual level, not the aggregate.

| Data | Cached? | Why |
|------|---------|-----|
| Match IDs list (`/by-puuid/.../ids`) | NO | This is how we detect new games |
| Match detail by matchId | YES | Immutable, `maximumSize(20)` |
| Timeline by matchId | YES | Immutable, `maximumSize(20)` |

- Caffeine handles LRU eviction automatically — no manual cache busting needed
- `@Cacheable` on individual fetch methods (must be public, called from outside the class — Spring proxy limitation)
- Worst case after cache warm: 1 new game = 3 API calls (IDs + 1 match + 1 timeline) instead of 41

**Future: Migrate to PostgreSQL**

- Replace Caffeine with DB persistence so match data survives restarts / redeploys
- Same interface, just swap the cache layer for a repository lookup
- Enables historical analysis across sessions

---

## Deployment

- **GCP Cloud Run** for hosting (Docker-based, scales to zero)
- **GCP Artifact Registry** for Docker images
- **GitHub Actions** for CI/CD (mvn verify + build/push image)
- **Terraform** for infrastructure-as-code (after manual gcloud setup)
- Multi-stage Dockerfile: Maven + JDK build → slim JRE runtime

---

## Tools (USE THESE, FUTURE CLAUDE 🔧)

### Context7 MCP

Fetch up-to-date library documentation. First resolve library ID, then query docs.
**⚠️ USE THIS** when explaining Spring Boot features, annotations, or patterns — get the latest docs, don't rely on training data!

### Skills

- `/java-architect` — Spring Boot, JPA, reactive patterns
- `/senior-backend` — REST APIs, database optimization
- `/senior-architect` — System design, architecture decisions
- `/code-reviewer` — Code review and PR analysis

**⚠️ USE THESE** when diving into architecture decisions or code review!
