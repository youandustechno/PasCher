# PasCher

PasCher is a modern Android movie streaming application built with the latest Jetpack libraries and architectural best practices. It offers a seamless discovery experience, immersive movie details, and a flexible subscription model.

## Features

- **Movie Discovery**: Explore a wide range of movies categorized for easy browsing.
- **Adaptive Layouts**: Optimized for different screen sizes and orientations using Jetpack Compose Adaptive Layouts.
- **Immersive Playback**: High-quality video playback powered by Media3 ExoPlayer.
- **Dark Theme Support**: Choose between Light, Dark, or System Default themes.
- **Secure Subscriptions**: Integrated PayPal SDK for managing premium subscriptions.
- **Offline Capabilities**: Local caching using Room database for a smooth experience.

## Tech Stack

- **UI**: [Jetpack Compose](https://developer.android.com/jetpack/compose) with Material 3.
- **Navigation**: [Navigation 3](https://developer.android.com/guide/navigation/navigation-3) for flexible, state-aware routing.
- **Concurrency**: Kotlin Coroutines and Flow.
- **Networking**: Retrofit with Moshi for API communication.
- **Database**: Room for local persistence.
- **Preferences**: DataStore for user settings.
- **Image Loading**: Coil for efficient image fetching and caching.
- **Video Playback**: Media3 ExoPlayer.
- **Payments**: PayPal Android SDK.

## Getting Started

### Prerequisites

- Android Studio Meerkat (or newer)
- JDK 17+
- Android SDK 28+

### Setup

1.  **Clone the repository**:
    ```bash
    git clone https://github.com/youandustechno/PasCher.git
    ```
2.  **Configure Secrets**:
    The project uses a third-party repository that requires credentials. These are stored locally to prevent accidental disclosure.
    
    Create a `local.properties` file in the root directory (if it doesn't exist) and add the following:
    ```properties
    cardinal.username=braintree_team_sdk
    cardinal.password=YOUR_CARDINAL_PASSWORD
    ```
3.  **Build and Run**:
    Open the project in Android Studio and run the `app` module on an emulator or physical device.

## Project Structure

- `ui/`: Compose-based screens, components, and themes.
- `data/`: Repositories, API services, and database models.
- `domain/`: Business logic and entity definitions.
- `di/`: Manual dependency injection using an `AppContainer`.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
