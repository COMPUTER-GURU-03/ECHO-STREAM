# Implementation Plan - MOVIE DB

MOVIE DB is a premium, cinematic entertainment application built with Jetpack Compose and Kotlin. It provides a high-end streaming experience with real-time metadata from TMDB, personal library synchronization via Supabase, and a secure video playback architecture.

> [!NOTE]
> This application is being implemented as a **native Android app (Kotlin/Compose)**. While the original brief referenced Flutter/Dart, we are translating all requirements (Supabase schema, TMDB integration, and Luxury Dark design) into a high-performance native Android codebase.

### User Review & Critical Decisions

> [!IMPORTANT]
> The following decisions were confirmed during the Phase 1 interview:
> - **Visual Aesthetic**: Luxury Dark (Deep black `#08090D`, Electric Indigo accents, Glassmorphism).
> - **Authentication**: Email and Password only (via Supabase Auth).
> - **Primary Navigation**: Bottom Navigation Bar (Home, Discover, Library, Profile).

- **TMDB API Key**: You must provide a TMDB API Key in the AI Studio Secrets panel.
- **Supabase Configuration**: You must provide your Supabase URL and Anon Key in the Secrets panel.
- **Video Playback**: The app expects a Supabase Edge Function to proxy Google Drive playback URLs for security.

---

### 1. Overview & Core Concept

MOVIE DB is designed for movie enthusiasts who want a personalized, synchronized entertainment dashboard. It bridges the gap between high-quality metadata (TMDB) and private video collections (Google Drive via Supabase).

**Key Workflows**:
- **Discovery**: Users browse trending and popular content with high-quality posters and trailers.
- **Library Management**: Users save titles to their watchlist, which syncs across devices.
- **Synchronized Progress**: "Continue Watching" resumes movies and TV shows from the exact second they left off.
- **Secure Playback**: High-performance video player with resume capability and quality selection.

---

### 2. User Experience & Visual Design

- **Luxury Dark Theme**:
    - **Palette**: Background `#08090D`, Surface `#101218`, Accent `Electric Indigo` (`#675CFF`).
    - **Glassmorphism**: Translucent overlays for navigation bars and title headers using `Modifier.blur` and semi-transparent surfaces.
    - **Typography**: **Inter** (Display & Body) for a clean, editorial feel.
- **Key Screens**:
    - **Splash/Auth**: Clean, dramatic entrance with logo and simple login/signup.
    - **Home**: Cinematic hero banner with horizontal rails for categories.
    - **Title Details**: Immersive backdrop with blurred details, cast list, and season selector for TV.
    - **Player**: Minimalist, gesture-enabled controls with a "Resume" prompt.
- **Motion**: Staggered list reveals and smooth shared-element-like transitions between posters and details.

---

### 3. Key Product Decisions & Trade-Offs

- **Supabase Kotlin SDK**:
    - *Approach*: Use the official `io.github.jan-tennert.supabase` libraries.
    - *Why*: It provides first-class support for Postgrest (database), GoTrue (auth), and Realtime (progress sync).
- **Video Source Abstraction**:
    - *Approach*: A `VideoRepository` that returns a streamable URI.
    - *Why*: Allows swapping Google Drive/Edge Functions for other providers (Cloudflare, etc.) in the future without touching the UI.
- **TMDB Proxy Architecture**:
    - *Approach*: All metadata requests flow through **Supabase Edge Functions**.
    - *Why*: Ensures TMDB API keys are never exposed in the Android APK. Provides a secure, server-side layer for caching and request validation.

---

### 4. Technical Architecture & Data Strategy

```unicode
┌─────────────────────────────────────────────────────────┐
│                   Android App (Compose)                 │
│  ┌──────────────┐  ┌──────────────┐  ┌───────────────┐  │
│  │  Home View   │  │ Details View │  │  Player View  │  │
│  └──────┬───────┘  └──────┬───────┘  └───────┬───────┘  │
│         │                 │                  │          │
│  ┌──────┴─────────────────┴──────────────────┴───────┐  │
│  │                  ViewModel Layer                  │  │
│  │   (StateFlow, Navigation, Error Handling)         │  │
│  └──────┬─────────────────┬──────────────────┬───────┘  │
└─────────┼─────────────────┼──────────────────┼──────────┘
          │                 │                  │
┌─────────┴────────┐  ┌─────┴────────────┐  ┌──┴───────────┐
│  TMDB Service    │  │ Supabase Service │  │ Video Repos. │
│ (Function Proxy) │  │ (Auth/Database)  │  │ (Edge Proxy) │
└──────────────────┘  └──────────────────┘  └──────────────┘
          │
          ▼
┌─────────────────────────────────────────────────────────┐
│                Supabase Edge Functions                  │
│       (TMDB_READ_ACCESS_TOKEN protected secret)         │
└───────────────────┬─────────────────────────────────────┘
                    │
                    ▼
          ┌───────────────────┐
          │     TMDB API      │
          └───────────────────┘
```

**Data Model Entities**:
- `Title`: Metadata from TMDB mapped to Supabase `titles`.
- `Profile`: User data from Supabase `profiles`.
- `WatchProgress`: Real-time position tracking synced to `watch_progress`.
- `VideoAsset`: Metadata for authorized streams from `video_assets`.

**State Mapping**:
- `HomeUiState`: `Loading`, `Success(categories)`, `Error`.
- `DetailsUiState`: `Loading`, `Success(metadata, casts, related)`, `Error`.
- `PlayerState`: `Buffering`, `Playing`, `Paused`, `PositionUpdated`.

---

### 5. Implementation Roadmap

- **Phase 1: Foundation**: Set up Gradle with Supabase/Ktor/Retrofit. Implement Luxury Dark Theme and Navigation.
- **Phase 2: Auth**: Supabase Email/Password login, persistence, and profile creation.
- **Phase 3: TMDB Discovery**: Fetch Trending/Popular categories. Implement Hero banners and horizontal rails.
- **Phase 4: Details & Library**: Detailed title view, season/episode lists, and "Add to Watchlist" logic (Postgrest).
- **Phase 5: Sync & Player**: Implement `Media3/ExoPlayer`. Hook up `watch_progress` heartbeat and resume logic.
- **Phase 6: Polish**: Inter font integration, animations, and edge-to-edge support.
