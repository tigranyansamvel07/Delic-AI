# Delic-AI

Spring Boot coursework project for AI-assisted music discovery.

## Configuration

Set these environment variables before running the application:

```text
DB_URL=jdbc:mysql://localhost:3306/coursework
DB_USERNAME=root
DB_PASSWORD=your_database_password
SPOTIFY_CLIENT_ID=your_spotify_client_id
SPOTIFY_CLIENT_SECRET=your_spotify_client_secret
LASTFM_API_KEY=your_lastfm_api_key
LASTFM_API_SECRET=your_lastfm_api_secret
OLLAMA_BASE_URL=http://localhost:11434
OLLAMA_MODEL=gemma3:4b
```

## Run

```bash
./mvnw spring-boot:run
```

On Windows:

```powershell
.\mvnw.cmd spring-boot:run
```
