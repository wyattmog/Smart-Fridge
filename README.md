# Smart Fridge

## Project Overview

Smart Fridge is an Android application that helps users find recipes based on the ingredients they have. The app uses a machine learning model to detect ingredients from a photo taken by the user. Once the ingredients are identified, the app suggests recipes that can be made with them.

## Features

*   **Ingredient Detection:** Take a photo of your ingredients, and the app will use a machine learning model to identify them.
*   **Recipe Suggestions:** Get recipe ideas based on the detected ingredients.
*   **Simple and Intuitive UI:** The app is built with Jetpack Compose for a modern and responsive user interface.

## Technology Stack

*   **UI:** [Jetpack Compose](https://developer.android.com/jetpack/compose)
*   **Asynchronous Programming:** [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html)
*   **Navigation:** [Jetpack Navigation Compose](https://developer.android.com/jetpack/compose/navigation)
*   **Networking:** [Retrofit](https://square.github.io/retrofit/) and [Moshi](https://github.com/square/moshi)
*   **Machine Learning:** [ONNX Runtime for Android](https://onnxruntime.ai/docs/tutorials/mobile/android.html)
*   **ViewModel:** [Jetpack ViewModel](https://developer.android.com/topic/libraries/architecture/viewmodel)

## Setup and Installation

1.  Clone the repository: `git clone https://github.com/your-username/smart-fridge.git`
2.  Open the project in Android Studio.
3.  Build and run the application on an Android device or emulator.

## Project Structure

The project is organized into the following main packages:

*   `ml`: Contains the `IngredientDetector.kt` class, which is responsible for running the ONNX model to detect ingredients from an image.
*   `network`: Handles all the network operations.
    *   `ApiService.kt`: Defines the API endpoints for recipe suggestions.
    *   `RetrofitInstance.kt`: Provides a singleton instance of Retrofit.
*   `data`: Includes data models for the application (e.g., `RecipeResponse`) and the `RecipeRepository` for fetching data.
*   `ui`: Contains the Jetpack Compose screens.
    *   `FridgeScreen.kt`: The main screen where users can take a photo of their ingredients.
    *   `RecipeScreen.kt`: Displays the suggested recipes.

## API

The application uses a custom backend API to fetch recipe suggestions. The API consists of the following endpoints:

*   `POST /api/sessions/start`: Starts a new session and returns a session ID and token.
*   `GET /api/recipes/generate`: Generates recipes based on a comma-separated list of ingredients. This endpoint requires the session ID and token to be passed in the headers.

**Note:** The base URL of the API is not included in the source code and needs to be configured separately.
