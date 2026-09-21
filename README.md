# HabitTracker

HabitTracker is a comprehensive Android application designed to help users build and maintain healthy habits. It offers habit tracking, progress visualization, journaling, and secure authentication.

## Table of Contents
1. [Project Overview](#project-overview)
2. [Features](#features)
3. [Architecture](#architecture)
4. [Directory Structure](#directory-structure)
5. [How It Works](#how-it-works)
    - [Authentication](#authentication)
    - [Habit Management](#habit-management)
    - [Progress Tracking](#progress-tracking)
    - [Journaling](#journaling)
6. [Technologies Used](#technologies-used)
7. [Getting Started](#getting-started)

## Project Overview
HabitTracker allows users to define their goals, track daily activities, and see their progress over time. It integrates with a backend API for data persistence and offers a smooth user experience using modern Android development practices.

## Features
- **Secure Authentication**: Traditional Login/Register and Google Single Sign-In (SSO).
- **Customizable Habits**: Pre-set categories (Walk, Run, Cycle, Read, etc.) or fully custom habits.
- **Goal Setting**: Define targets, units, and frequencies (Daily, Weekly, etc.).
- **Progress Visualization**: Detailed charts and statistics to monitor streaks and completion rates.
- **Journaling**: Keep a local diary of your thoughts and progress.
- **Daily Dashboard**: A clear view of today's tasks and the ability to log completions.
- **Profile Management**: Update user information and settings.

## Architecture
The project follows the **MVVM (Model-View-ViewModel)** architectural pattern:
- **Model**: Data classes representing API entities and local objects.
- **View**: Activities and Fragments handling the UI and user interactions.
- **ViewModel**: Manages UI-related data and communicates with the repository/API layer.
- **Repository**: Handles data operations, abstracting the source (API/Local).

## Directory Structure
```text
app/src/main/java/com/example/habittracker/
├── activity/       # Entry points and UI Controllers (Activities)
├── api/            # Networking (Retrofit, API Service, Token Management)
├── models/         # Data Transfer Objects (DTOs) and Models
├── repository/     # Data source management
├── ui/             # Fragments and custom UI components
├── util/           # Helper classes and utilities
└── viewModels/     # Business logic and UI state management
```

## How It Works

### 1. Authentication
Upon launching, the app checks for a valid JWT (JSON Web Token) using `TokenManager`. If missing, users are directed to `LoginActivity`.
- **SSO**: Supports Google Sign-In for a seamless experience.
- **Token Handling**: Uses an `AuthInterceptor` to automatically attach the JWT to all secured API requests.

### 2. Habit Management
Users can create habits from `CreateHabitActivity`. 
- **Selection**: Choose from popular pre-defined templates.
- **Configuration**: Set custom titles, goals, units, and frequency.
- **Logging**: From the `HomeFragment`, users can mark habits as complete for the current day, which synchronizes with the backend.

### 3. Progress Tracking
The `StatsFragment` and `HabitProgressActivity` fetch detailed metrics from the API.
- **MPAndroidChart**: Used to render line charts and bar graphs showing habit trends.
- **Detailed Stats**: Provides specific insights into completion history, streaks, and overall statistics.

### 4. Journaling
The `JournalFragment` allows users to record daily notes about their journey. 
- **Local Storage**: `LocalJournalManager` handles saving journal entries locally on the device for quick access and privacy.

## Technologies Used
- **Kotlin**: Primary programming language.
- **Android Jetpack**:
  - ViewModel & LiveData
  - View Binding
  - Fragments
  - Security-Crypto (for secure token storage)
- **Networking**: Retrofit 2 & OkHttp 3.
- **UI Components**: Material Design 3, MPAndroidChart.
- **Concurrency**: Kotlin Coroutines.

## Getting Started
1. Clone the repository.
2. Open the project in **Android Studio**.
3. Ensure the backend API URL is correctly configured in `RetrofitClient`.
4. Build and Run the `:app` module on an emulator or physical device.
