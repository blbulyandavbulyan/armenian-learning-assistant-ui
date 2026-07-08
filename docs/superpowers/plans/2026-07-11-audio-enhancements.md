# Project Implementation Plan

**Spec Reference:** `docs/superpowers/specs/2026-07-10-audio-enhancements-design.md`

## Overview
This plan implements the audio handling enhancements across the dialogue screens, introduces native Ktor HTTP caching to replace the custom manual cache, extracts a reusable `ListenButton` UI component, and fulfills the missing UI tests requested by the TODOs.

---

### Task 1: Core Networking & Models Update
**Description:** Update `PhraseResponse` to encapsulate the audio asset extraction logic, and switch the custom manual cache in `NetworkAssetRepository` to Ktor's native `HttpCache`.

1. **Step 1:** Modify `DialogueModels.kt`
   - Add the `audioAssetUrl` computed property to `PhraseResponse`.
2. **Step 2:** Refactor `AssetRepositoryTest.kt`
   - Update the test to verify that calling `getAssetBytes` twice with a `MockEngine` responding with `Cache-Control: max-age=60` results in only one network request (TDD). 
   - **Command:** `./gradlew :shared:cleanTest :shared:jsBrowserTest` (Verify failure).
3. **Step 3:** Implement Ktor `HttpCache`
   - In `AppModule.kt`, add `install(io.ktor.client.plugins.cache.HttpCache)` to the `HttpClient`.
   - In `NetworkAssetRepository.kt`, remove `audioCache` map and `audioCacheMutex`, simply returning `apiClient.getAssetBytes(url)`.
4. **Step 4:** Verify tests pass
   - **Command:** `./gradlew :shared:cleanTest :shared:jsBrowserTest` (Verify success).
5. **Step 5:** Review
   - Present the changes to the user and wait for them to review and commit.

---

### Task 2: UI Component (`ListenButton`) & Refactoring Screens
**Description:** Create the `ListenButton` component to ensure a consistent visual language, and integrate it into the search and detail screens, addressing the related TODOs.

1. **Step 1:** Create `ListenButton.kt`
   - Create `shared/src/commonMain/kotlin/com/blbulyandavbulyan/larm/kmp/ui/ListenButton.kt`.
   - Implement the composable accepting `onClick` and `isOutlined`, internally using the `listen_phrase_button` string resource.
2. **Step 2:** Refactor `DialogueSearchScreen.kt`
   - Replace the `OutlinedButton` logic in the results list to check `dialogue.title.audioAssetUrl` and use `ListenButton(isOutlined = true)`.
3. **Step 3:** Refactor `DialogueDetailScreen.kt`
   - Add a `ListenButton` for the dialogue title below translations.
   - Add a `ListenButton` inline with the speaker name.
   - Refactor the phrase listen button to use `audioAssetUrl` and `ListenButton`.
4. **Step 4:** Review
   - Present the changes to the user and wait for them to review and commit.

---

### Task 3: Automated UI Tests (Audio Features)
**Description:** Implement the missing test cases defined in the TODOs of `DialogueSearchScreenTest.kt` to guarantee the audio buttons function and cache correctly.

1. **Step 1:** Update Mock Data
   - In `DialogueSearchScreenTest.kt`, replace the `assets = emptyList()` and `translations = emptyList()` mock data with a valid `AssetResponse("audio/mpeg", "http://test.audio/1")` and `ChatTranslationResponse`.
2. **Step 2:** Write Failing UI Tests
   - Add Test Case 2: Verify pressing listen buttons in the details screen invokes the repository with the right URL.
   - Add Test Case 3: Verify pressing a listen button multiple times hits the backend only once (cache test).
   - Add Test Case 4: Verify pressing listen on the search screen invokes the right endpoint.
   - **Command:** `./gradlew :shared:cleanTest :shared:jsBrowserTest` (Verify they execute and fail/pass accordingly).
3. **Step 3:** Fix Issues (If any)
   - Ensure the UI test assertions pass against the real UI logic developed in Task 2.
   - **Command:** `./gradlew :shared:cleanTest :shared:jsBrowserTest` (Verify success).
4. **Step 4:** Review
   - Present the changes to the user and wait for them to review and commit.
