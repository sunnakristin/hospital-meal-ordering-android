# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**Matarpontun** — a hospital meal ordering Android app (Kotlin). Wards log in and place meal orders for patients.

## Build & Run Commands

```bash
./gradlew assembleDebug          # Build debug APK
./gradlew assembleRelease        # Build release APK
./gradlew build                  # Full build (all variants)
./gradlew clean                  # Clean build artifacts
./gradlew lint                   # Run lint checks
```

## Test Commands

```bash
./gradlew test                   # Run all unit tests
./gradlew testDebugUnitTest      # Run debug unit tests
./gradlew connectedAndroidTest   # Run instrumented tests (requires device/emulator)

# Run a single test class
./gradlew testDebugUnitTest --tests "com.example.matarpontun.ExampleUnitTest"
```

## SDK & Java Versions

- compileSdk / targetSdk: 36 (Android 15)
- minSdk: 24 (Android 7.0)
- Java: VERSION_11
- Kotlin: 2.0.21
- AGP: 9.0.1

## Architecture

Clean Architecture with MVVM. Three layers:

### Domain Layer (`domain/`)
- `model/` — core data classes: `Patient`, `Ward`, `Meal`, `Menu`, `DailyOrder`, `FoodType`
- `repository/` — repository interfaces (abstractions)
- `service/` — business logic services: `PatientService`, `WardService`, `DailyOrderService`

### Data Layer (`data/`)
- `network/RetrofitClient` — Retrofit singleton; base URL `http://10.0.2.2:8080/` (emulator localhost)
- `remote/RemoteApiService` — Retrofit interface defining all API endpoints
- `remote/` — data source implementations (`RemotePatientDataSourceImpl`, etc.)
- `remote/dto/` — request/response DTOs
- `repository/` — `Network*Repository` implementations; `Mock*Repository` variants also exist but are **commented out**

### UI Layer (`ui/`)
- `login/` — `LoginActivity` + `LoginViewModel` (app entry point, ward sign-in)
- `ward/` — `WardActivity` (ward dashboard after login)
- `patients/` — `PatientListActivity` + `PatientListViewModel` + `PatientListAdapter`
- `theme/` — Compose theming (Color, Theme, Type)

### Dependency Injection

`AppContainer` (object/singleton in root package) manually wires all dependencies. It also holds `currentLoginRequest` which stores the authenticated ward's credentials for subsequent API calls requiring a `LoginRequest` body.

## Key API Endpoints

- `POST /wards/signIn` — ward login
- `GET /wards/summary/{wardId}` — ward summary
- `POST /patients/all` — list ward's patients (body: `LoginRequest`)
- `POST /patients/{id}/order` — order meal for one patient
- `POST /wards/{id}/order` — order meal for entire ward

## UI Approach

The project mixes **traditional Views** (Activities + RecyclerView) with **Jetpack Compose** theming scaffolding. New UI work should follow the existing View-based pattern unless migrating to Compose.

**ViewBinding is not enabled.** Views are wired with `findViewById` directly — `buildFeatures { compose = true }` is set but `viewBinding = true` is not.

## State Management

ViewModels expose two channels that Activities collect:

- `uiState: StateFlow` — screen state (Idle / Loading / Success / Error sealed classes)
- `events: SharedFlow` — one-shot side effects (e.g. `ShowToast`)

Observe both using `lifecycleScope.launch { ... collect { ... } }` (note: `PatientListActivity` does not use `repeatOnLifecycle` — match the pattern of the file you are editing).

## Activity Navigation Flow

```
LoginActivity → WardActivity → PatientListActivity
```

- `LoginActivity` passes `WARD_NAME: String` and `WARD_ID: Long` as Intent extras to `WardActivity`
- `WardActivity` forwards `WARD_ID` to `PatientListActivity`

`LoginActivity` instantiates its ViewModel manually. `PatientListActivity` uses `ViewModelProvider` with `PatientListViewModelFactory`.

## Offline / Mock Development

To run without a backend, uncomment `Mock*Repository` classes in `data/repository/` and swap them into `AppContainer` in place of the `Network*Repository` instances.
