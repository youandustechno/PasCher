# Project Plan

Add an option for the video player to use fullscreen in the PasCher app.

## Project Brief

# Project Brief: PasCher - Fullscreen Video Support

Add a fullscreen mode to the video player in the PasCher app.

## Objective
Enable users to watch movies in a dedicated fullscreen mode, hiding system UI and maximizing the video area.

## Technical Details
- **Toggle:** Add a fullscreen button to the video player controls or as a separate UI element.
- **System UI:** Hide status and navigation bars when in fullscreen.
- **Orientation:** Automatically switch to landscape (or allow user to toggle) when entering fullscreen.
- **Navigation:** Support back navigation to exit fullscreen.
- **Compose Integration:** Use state-driven UI to transition between embedded and fullscreen modes.
- **Adaptive Support:** Ensure fullscreen works correctly on both phones and tablets/foldables.

## Implementation Steps
**Total Duration:** 18m 54s

### Task_1_DataNetworking: Set up the data layer including Retrofit for movie API integration, Room for local storage, and DataStore for user preferences. Define domain models for Movie and Subscription.
- **Status:** COMPLETED
- **Updates:** Successfully set up the data layer. Defined Movie, SubscriptionPlan, and UserSubscription models. Implemented Room for local storage, Retrofit for networking, and DataStore for user preferences. Created MovieRepository and SubscriptionRepository. Updated compileSdk and targetSdk to 35. Build passes.
- **Acceptance Criteria:**
  - Retrofit and Room configured
  - Data repository implemented
  - Movie and Subscription models created
  - Build passes

### Task_2_NavigationDiscovery: Implement Navigation 3 and the Movie Discovery UI. Use Material 3 Adaptive layouts (List-Detail) to support various screen sizes. Integrate Coil for high-quality image loading.
- **Status:** COMPLETED
- **Updates:** Implemented Navigation 3 state-driven structure. Created Movie Discovery UI using Material 3 Adaptive ListDetailPaneScaffold. Integrated Coil for image loading. Added search functionality. Set up AppContainer for DI. App is edge-to-edge. Build passes.
- **Acceptance Criteria:**
  - Navigation 3 state-driven structure implemented
  - Movie Catalog screen with adaptive layout
  - Search functionality working
  - Images loading correctly with Coil
- **Duration:** 2m 15s

### Task_3_PlaybackDetails: Build the Movie Details screen and integrate Media3 ExoPlayer to handle high-quality video streaming with standard playback controls.
- **Status:** COMPLETED
- **Updates:** Implemented Movie Details screen showing metadata and integrated Media3 ExoPlayer for video streaming. Supported navigation to Movie Details. Build passes.
- **Acceptance Criteria:**
  - Movie Details screen showing metadata
  - Media3 ExoPlayer integrated with playback controls (play/pause, seek)
  - Video streaming functional
- **Duration:** 2m 4s

### Task_4_SubscriptionPayment: Implement the Subscription Management UI showcasing tiered plans and a secure 'Pay Now' payment gateway simulation.
- **Status:** COMPLETED
- **Updates:** Implemented Subscription Management UI with tiered plans (Free, Basic, Premium). Created a 'Pay Now' simulation flow. Persisted subscription status using DataStore. Integrated Subscription screen into the Navigation 3 backstack. Build passes.
- **Acceptance Criteria:**
  - Subscription tier selection UI implemented
  - 'Pay Now' transaction flow completed
  - Subscription status persisted in the app
- **Duration:** 1m 11s

### Task_5_PolishVerification: Finalize the UI with a vibrant Material 3 theme (Light/Dark), implement Full Edge-to-Edge display, create an adaptive app icon, and perform final verification for stability.
- **Status:** COMPLETED
- **Updates:** Refined the app based on critic feedback. 1. Added mock data fallback to MovieRepository for a functional catalog without API. 2. Fixed adaptive navigation: tablets/foldables now use the side-by-side detail pane instead of full-screen navigation. 3. Verified search and playback are functional. 4. Final verification by critic agent confirmed stability and adaptive excellence.
- **Acceptance Criteria:**
  - Material 3 vibrant theme and Material You colors applied
  - Full Edge-to-Edge display enabled
  - Adaptive app icon matching app function created
  - App runs without crashes and meets all user requirements
  - Existing tests pass
- **Duration:** 8m 15s

### Task_6_InternetArchiveData: Integrate the Internet Archive API into the data layer. Implement Retrofit services for Search (collection: feature_films) and Metadata. Refactor the MovieRepository to fetch live public-domain movie metadata and identifiers, maintaining a robust fallback mechanism.
- **Status:** COMPLETED
- **Updates:** Successfully integrated the Internet Archive API. Implemented ArchiveApiService for Search and Metadata. Refactored MovieRepositoryImpl to fetch live data with a two-step process (Search -> Metadata) using coroutines for concurrency. Maintained mock data fallback. Build passes.
- **Acceptance Criteria:**
  - Internet Archive Search API integrated
  - Metadata fetch logic implemented
  - MovieRepository provides real data
  - Robust fallback mechanism maintained
- **Duration:** 45s

### Task_7_LiveStreamVerify: Update the UI to display live movie data and configure ExoPlayer to stream real .mp4 video files from the Internet Archive. Perform final app-wide verification for stability and alignment with requirements.
- **Status:** COMPLETED
- **Updates:** Final verification confirmed the app is stable, live data from Internet Archive is correctly integrated, and ExoPlayer streams real video. Adaptive layout works perfectly on tablets. Search and Subscription are functional. Build passes. App is ready.
- **Acceptance Criteria:**
  - Discovery UI shows live movie posters and titles
  - ExoPlayer streams real video from Archive.org
  - App does not crash
  - Existing tests pass
  - Build pass
- **Duration:** 4m 24s

### Task_8_FullscreenVideo: Implement fullscreen mode for the video player. Add a toggle button to the player controls, handle system UI visibility (hiding status/navigation bars), and manage orientation changes (forcing landscape) using Compose state.
- **Status:** IN_PROGRESS
- **Acceptance Criteria:**
  - Fullscreen toggle added to video player
  - System UI hidden in fullscreen mode
  - Landscape orientation applied in fullscreen
  - Back navigation exits fullscreen mode correctly
- **StartTime:** 2026-07-25 21:04:50 CDT

### Task_9_RunAndVerify: Verify application stability, confirm fullscreen alignment with user requirements, and report critical UI issues. Instruct critic_agent to verify fullscreen behavior and adaptive UI on various screen sizes.
- **Status:** PENDING
- **Acceptance Criteria:**
  - Fullscreen works without crashes
  - App does not crash
  - Build pass
  - Make sure all existing tests pass

