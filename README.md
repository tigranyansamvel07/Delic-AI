# DelicAI 🎵
### Discover your next favourite song.

DelicAI is an AI-assisted music discovery web application built with **Java and Spring Boot**. Enter a song or artist, and DelicAI uses a local language model to help identify tracks with a similar sound, mood, style, and rhythm. It then enriches the recommendations with music metadata, album artwork, and links to streaming platforms.

The project combines a server-rendered web interface, external music APIs, and locally hosted AI models to explore how AI can make music discovery more personal.

## ✨ Features

- **AI-assisted recommendations** — asks an Ollama language model to rank candidate tracks by musical style, genre, mood, tempo, rhythm, and audience.
- **Multi-source track lookup** — searches music metadata using iTunes, Last.fm, and Spotify, with fallback logic when a source does not return a result.
- **Album artwork and track details** — displays track titles, artists, and available cover images.
- **Listen elsewhere** — provides search or track links for Spotify, YouTube, and Apple Music.
- **Account flow** — includes sign-up and log-in pages backed by a MySQL database.
- **Interactive results page** — select tracks from the recommendation list, inspect their artwork, and navigate between results.
- **Ollama chat endpoints** — includes a separate API for generating normal or streamed text responses from a local Ollama model.

## 🧭 How it works

1. **Enter a starting point.** Search for a song or artist through the DelicAI interface.
2. **Resolve the input track.** The application looks up the submitted query to identify the track and artist.
3. **Find candidate songs.** It requests similar-track data from Last.fm and enriches candidate results with metadata where possible.
4. **Let AI rank candidates.** The configured Ollama model receives the original track and the candidate list, then returns a JSON list of songs it considers most similar.
5. **Enrich the recommendations.** DelicAI searches its music sources for the recommended tracks and collects artwork and outbound listening links.
6. **Explore the results.** Browse the resulting list and open a track on a supported music platform.

Recommendations depend on the candidate tracks returned by the connected services, the AI model's output, and the metadata those services provide. Results may vary by query and API availability.

## 🧰 Tech stack

| Area | Technologies |
| --- | --- |
| Backend | Java 17, Spring Boot 3 |
| Web & API | Spring MVC, Spring WebFlux, REST controllers |
| Frontend | HTML, CSS, JavaScript, Thymeleaf |
| Database | MySQL, Spring Data JPA, Hibernate |
| AI | Ollama with a local language model (default: `gemma3:4b`) |
| Music data | iTunes Search, Last.fm API, Spotify Web API |
| JSON | Jackson |
| Build | Maven Wrapper |

## 🏗️ Project structure

```text
src/
└── main/
    ├── java/org/tl/coursework_ahh/
    │   ├── controllers/    # Web pages and AI API endpoints
    │   ├── models/         # User, Track, and History entities
    │   ├── repositories/   # Spring Data JPA repositories
    │   └── services/       # Music lookup, AI analysis, and user logic
    └── resources/
        ├── templates/      # Thymeleaf pages
        ├── static/
        │   ├── css/        # Page styles
        │   └── js/         # Interactive results page
        └── application.properties
```

## ⚙️ Requirements

Before starting, install or prepare:

- **Java 17** or later compatible with the project
- **MySQL** server
- **Ollama**, running locally
- The Ollama model configured for recommendations (default: `gemma3:4b`)
- API credentials for the integrations you intend to use: Spotify and Last.fm

You can install the default model with:

```bash
ollama pull gemma3:4b
```

Make sure Ollama is running and reachable at the configured base URL. The application currently has a separate manual chat service that uses the `llama2` model, so pull that model too if you plan to use those chat endpoints:

```bash
ollama pull llama2
```

## 🚀 Getting started

### 1. Clone the repository

```bash
git clone https://github.com/tigranyansamvel07/Delic-AI.git
cd Delic-AI
```

### 2. Create a MySQL database

For example, create a database named `coursework`:

```sql
CREATE DATABASE coursework;
```

The application uses Hibernate's `ddl-auto=update` setting to create or update entity tables on startup. This is convenient for local development; review the schema-management strategy before using the application in production.

### 3. Configure environment variables

The application reads its database, music API, and Ollama configuration from environment variables. Defaults are provided for local development in `application.properties`.

| Variable | Purpose | Default |
| --- | --- | --- |
| `DB_URL` | JDBC URL for MySQL | `jdbc:mysql://localhost:3306/coursework` |
| `DB_USERNAME` | MySQL username | `root` |
| `DB_PASSWORD` | MySQL password | Empty |
| `SPOTIFY_CLIENT_ID` | Spotify application client ID | Empty |
| `SPOTIFY_CLIENT_SECRET` | Spotify application client secret | Empty |
| `LASTFM_API_KEY` | Last.fm API key | Empty |
| `LASTFM_API_SECRET` | Last.fm API secret | Empty |
| `OLLAMA_BASE_URL` | Ollama server base URL | `http://localhost:11434` |
| `OLLAMA_MODEL` | Model configured for the AI music flow | `gemma3:4b` |
| `LASTFM_API_BASE_URL` | Last.fm API endpoint | `https://ws.audioscrobbler.com/2.0/` |

Use your own credentials and keep them out of source control. Set the environment variables in your terminal or IDE run configuration before launching the app.

**PowerShell example:**

```powershell
$env:DB_URL="jdbc:mysql://localhost:3306/coursework"
$env:DB_USERNAME="root"
$env:DB_PASSWORD="your_database_password"
$env:SPOTIFY_CLIENT_ID="your_spotify_client_id"
$env:SPOTIFY_CLIENT_SECRET="your_spotify_client_secret"
$env:LASTFM_API_KEY="your_lastfm_api_key"
$env:LASTFM_API_SECRET="your_lastfm_api_secret"
$env:OLLAMA_BASE_URL="http://localhost:11434"
$env:OLLAMA_MODEL="gemma3:4b"
```

**macOS / Linux example:**

```bash
export DB_URL="jdbc:mysql://localhost:3306/coursework"
export DB_USERNAME="root"
export DB_PASSWORD="your_database_password"
export SPOTIFY_CLIENT_ID="your_spotify_client_id"
export SPOTIFY_CLIENT_SECRET="your_spotify_client_secret"
export LASTFM_API_KEY="your_lastfm_api_key"
export LASTFM_API_SECRET="your_lastfm_api_secret"
export OLLAMA_BASE_URL="http://localhost:11434"
export OLLAMA_MODEL="gemma3:4b"
```

> **Note:** The primary recommendation service currently configures its Ollama URL and model directly in its implementation (`http://localhost:11434` and `gemma3:4b`). The environment variables are declared in application configuration, but may not affect that service until it is wired to use those properties.

### 4. Run the application

On macOS or Linux:

```bash
./mvnw spring-boot:run
```

On Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

Once Spring Boot starts, open [http://localhost:8080](http://localhost:8080) in your browser.

## 🔌 API endpoints

The project also exposes a small Ollama-backed API:

| Method | Endpoint | Purpose |
| --- | --- | --- |
| `POST` | `/api/ai/chat` | Generate a single response from the configured manual chat service |
| `GET` | `/api/ai/chat/stream?message=...` | Stream a response as Server-Sent Events (SSE) |
| `GET` | `/test` | Simple Ollama connectivity check |

Example request to the non-streaming chat endpoint:

```bash
curl -X POST http://localhost:8080/api/ai/chat \
  -H "Content-Type: application/json" \
  -d '{"message":"Recommend a few upbeat songs for a workout."}'
```

The chat endpoints currently use the `llama2` model in `ManualOllamaService`, separately from the `gemma3:4b` model used by the recommendation flow.

## 🗃️ Data model

The code defines three main JPA entities:

- **User** — account email, username, and password fields.
- **Track** — track name, artist, album-cover URL, and Spotify, YouTube, and Apple Music links.
- **History** — a user ID and submitted search query.

The History entity and repository are present in the project. Search history persistence and a complete history user interface may still need further wiring in the current implementation.

## 🔐 Security notes

This repository is a coursework / learning project. Before deploying it publicly, review the authentication and security implementation carefully. In particular, passwords are currently handled directly by the application code rather than through a production-ready authentication system. Use password hashing, server-side validation, secure session handling, and proper secret management before accepting real user accounts. Never commit API credentials or database passwords.

## 🛠️ Troubleshooting

- **Database connection fails:** confirm MySQL is running, the database exists, and `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` are correct.
- **AI request fails:** confirm Ollama is running at the configured local URL and that the required model is downloaded.
- **Recommendations are empty or incomplete:** check API credentials, external service availability, and application logs. Not every suggested song will have a matching metadata result.
- **Spotify lookup fails:** verify the Spotify client ID and client secret are set correctly.
- **Results differ between searches:** recommendations are generated from changing API data and probabilistic model output.

## 📌 Project status

DelicAI is a coursework project and may require additional configuration and hardening before production use. Some integrations have fallbacks, but external API availability and credentials can affect functionality.

## 📄 Licence

No licence is currently specified in this repository. Unless a licence is added, reuse and redistribution are not explicitly granted.
