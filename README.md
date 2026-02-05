# CoachDiff.ai

AI-powered League of Legends coach API. Fetches your summoner profile and ranked data from Riot API.

## Tech Stack

- **Java 25**
- **Spring Boot 4.0.1**
- **Maven**
- **Hexagonal Architecture** (Ports & Adapters)

## Prerequisites

- Java 25+
- Maven 3.9+
- [Riot API Key](https://developer.riotgames.com)

## Getting Started

### Local Development

1. Edit `src/main/resources/application-local.yml` with your Riot API credentials:

```yaml
coach-diff:
  default-summoner:
    name: "your-summoner-name"
    tag: "your-tag"

riot:
  api:
    key: "your-riot-api-key"
```

2. Run with the `local` profile:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

3. Test it:

```bash
curl http://localhost:8080/api/profile
```

The API will be available at `http://localhost:8080`.

### Production / CI

Set environment variables instead:

```bash
export RIOT_API_KEY=your-riot-api-key
export RIOT_SUMMONER_NAME=your-summoner-name
export RIOT_SUMMONER_TAG=your-tag
mvn spring-boot:run
```

## API

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/profile` | Returns summoner profile with rank data |

### Example Response

```json
{
  "name": "Alpe",
  "tag": "1989",
  "region": "EUW1",
  "tier": "GOLD",
  "division": "II",
  "lp": 45,
  "wins": 120,
  "losses": 110
}
```

## Project Structure

```
src/main/java/com/coachdiff/
├── domain/                  # Business logic — no framework dependencies
│   ├── exception/           # Custom exceptions + error codes
│   ├── model/               # SummonerProfile, Tier, Division, Region
│   └── port/
│       ├── in/              # Inbound ports (use cases)
│       └── out/             # Outbound ports (driven adapters)
├── application/
│   └── service/             # Use case implementations
└── infrastructure/
    ├── adapter/
    │   ├── in/rest/         # REST controllers + exception handlers
    │   └── out/             # Riot API adapter + DTOs
    └── config/              # RestClient configuration
```

### Architecture

This project follows **Hexagonal Architecture** (Ports & Adapters):

- **Domain** — Pure business logic with zero framework dependencies
- **Ports** — Interfaces defining how the domain talks to the outside world
- **Adapters** — Implementations connecting ports to real infrastructure (REST, external APIs)

Dependency direction: `Infrastructure → Application → Domain`

## Code Quality

```bash
# Run tests
mvn test

# Format code (Spotless + google-java-format)
mvn spotless:apply

# Check formatting + linting
mvn validate
```
