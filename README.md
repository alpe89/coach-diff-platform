# CoachDiff.ai

AI-powered League of Legends coach API. Analyzes your ranked performance with profile data and match aggregation stats.

## Tech Stack

- **Java 25** (preview features enabled)
- **Spring Boot 4.0.1**
- **Maven**
- **PostgreSQL** (Neon in production, Docker Compose locally)
- **Hexagonal Architecture** (Ports & Adapters)

## Prerequisites

- Java 25+
- Maven 3.9+
- Docker & Docker Compose
- [Riot API Key](https://developer.riotgames.com)

## Getting Started

### 1. Set up environment variables

Copy `.env.example` to `.env` and fill in your values:

```bash
cp .env.example .env
```

### 2. Start the database

```bash
docker compose up -d
```

### 3. Run the app

```bash
mvn spring-boot:run
```

### 4. Create an account

```bash
curl -X POST http://localhost:8080/api/account \
  -H "Content-Type: application/json" \
  -d '{"email":"you@example.com","name":"YourGameName","tag":"1234","role":"ADC","region":"EUW1"}'
```

### 5. Test it

```bash
curl -H "X-User-Email: you@example.com" http://localhost:8080/api/profile
curl -H "X-User-Email: you@example.com" http://localhost:8080/api/matches
```

## API

All endpoints except `POST /api/account` require the `X-User-Email` header.

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/profile` | Summoner profile with rank data |
| GET | `/api/matches` | Match aggregation stats (last 20 ranked games, current season) |
| GET | `/api/account` | Get account by email |
| POST | `/api/account` | Create account |
| PATCH | `/api/account` | Update account fields |
| DELETE | `/api/account` | Delete account |

### Example — Profile

```json
{
  "name": "Alpe",
  "tag": "1989",
  "tier": "GOLD",
  "division": "II",
  "lp": 45,
  "wins": 120,
  "losses": 110,
  "gamesPlayed": 230,
  "winRate": 0.522,
  "profileIconUrl": "https://ddragon.leagueoflegends.com/cdn/16.3.1/img/profileicon/6298.png"
}
```

### Example — Match Aggregation

```json
{
  "gamesAnalyzed": 20,
  "wins": 12,
  "losses": 8,
  "winRate": 0.6,
  "avgKills": 7.45,
  "avgDeaths": 4.9,
  "avgAssists": 7.05,
  "avgKda": 4.21,
  "avgCsPerMinute": 6.54,
  "avgVisionScorePerMinute": 0.79
}
```

## Project Structure

```
src/main/java/com/coachdiff/
├── domain/                  # Business logic — no framework dependencies
│   ├── exception/           # Domain exception hierarchy
│   ├── model/               # Profile, Account, MatchRecord, MatchAggregate, Tier, Division, Role, Region
│   └── port/
│       ├── in/              # Inbound ports (use cases)
│       └── out/             # Outbound ports (driven adapters)
├── application/
│   └── service/             # Use case implementations
└── infrastructure/
    ├── adapter/
    │   ├── in/rest/         # REST controllers + exception handlers
    │   └── out/             # Riot API clients/adapters + persistence (match + account)
    └── config/              # RestClient, cache, rate limiter configuration
```

### Architecture

**Hexagonal Architecture** (Ports & Adapters):

- **Domain** — Pure business logic with zero framework dependencies
- **Ports** — Interfaces defining how the domain talks to the outside world
- **Adapters** — Implementations connecting ports to real infrastructure (REST, Riot API, PostgreSQL)

Dependency direction: `Infrastructure → Application → Domain`

## Code Quality

```bash
# Run all checks (format + lint + tests + coverage)
mvn verify

# Coverage report
open target/site/jacoco/index.html

# Format code (Spotless + google-java-format)
mvn spotless:apply
```

## Deployment

**Platform:** Google Cloud Platform

- **Cloud Run** — hosting (scales to zero)
- **Artifact Registry** — Docker images
- **Secret Manager** — API keys and DB credentials
- **Neon** — PostgreSQL (serverless)

**CI/CD:** GitHub Actions

- Push to `main` → runs `mvn verify`
- Push tag `v*` → builds, pushes image, deploys to Cloud Run

### Docker

```bash
# Build image
docker build -t coach-diff .

# Run locally with env file
docker run -p 8080:8080 --env-file .env coach-diff
```
