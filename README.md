# Meaningful Playlists
Meaningful Playlists is a Spring Boot app that turns keywords into Spotify playlists — inspired by the [Bee Movie](https://open.spotify.com/playlist/7MIV5i0fIKeAmbwWPMtJJK) playlist.

## Description
The application lets users:
   - Authenticate via Spotify
   - Search tracks using text prompts
   - Generate and save custom playlists
   - Speed up queries with Redis-based caching

## Prerequisites
   - Docker and Docker Compose
   - Spotify Developer Account

## Spotify Configuration
   - Create an application on [Spotify Developer Dashboard](https://developer.spotify.com/dashboard)
   - Get `CLIENT_ID` and `CLIENT_SECRET`
   - Configure the redirect URI as `https://your-domain.com/spotify/callback`

## Running the Application
1. **Expose the HTTP port**

   To make the app accessible externally (e.g. for the spotify callback), you need to expose port `9000`.

   You can do that using [ngrok](https://ngrok.com): 

   ```bash
   ngrok http 9000
   ```
2. **Update the `.env-local` file with the required properties**

3. **Run docker compose command:**
   ```bash
   cd docker && docker-compose up --build
   ```
4. **All done!**

   You can now start creating **meaningful playlists** by visiting the `/spotify` endpoint of the exposed URL.

5. **Explore the API via Swagger**

   Once running, navigate to `/swagger-ui/index.html` to interact with the API documentation.
