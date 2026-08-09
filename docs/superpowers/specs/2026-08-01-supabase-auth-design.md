# Supabase Auth Integration Design

**Date**: 2026-08-01

## 1. Goal
Integrate Supabase into the Armenian Learning Assistant (KMP) project specifically for authentication and social login, while avoiding unnecessary features like database querying (since a Spring Boot backend is already present). Include proper configuration injection via BuildKonfig.

## 2. Architecture & Modules
We will rely on the official `supabase-kt` client.
- **Dependencies**: 
  - `io.github.jan-tennert.supabase:bom:3.6.0` (Latest stable BOM)
  - `io.github.jan-tennert.supabase:auth-kt` (Core Auth functionality)
- **Ktor Engines**: Supabase relies on Ktor. The project currently uses `ktor-client-core` (common) and `ktor-client-okhttp` (jvm). We will add `ktor-client-js` to the `jsMain` target to support browser environments.
- **Dependency Injection**: We will instantiate the `SupabaseClient` in the `AppModule.kt` file (the existing manual DI object) as a lazy singleton.

## 3. Configuration Management (BuildKonfig)
We will define two new String properties in the `buildkonfig` block of `shared/build.gradle.kts`:
- `SUPABASE_URL`
- `SUPABASE_ANON_KEY`

These will be injected using Gradle properties (`project.findProperty`), falling back to empty strings if not provided. This ensures secrets are not hardcoded into the source code and can be passed securely in CI/CD or local `gradle.properties`.

## 4. Sub-Projects & Scope
This is a single self-contained configuration change and thus does not need decomposition.

---
**Status**: Ready for implementation.
