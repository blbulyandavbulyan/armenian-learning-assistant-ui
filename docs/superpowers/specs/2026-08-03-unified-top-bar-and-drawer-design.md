# Unified App Top Bar and Navigation Drawer Design Specification

## Overview

Refactor the application's top navigation layer by introducing a unified top bar and a slide-out navigation drawer (`ModalNavigationDrawer`). This replaces fragmented, per-screen headers and floating menus with a centralized, clean navigation architecture.

---

## 1. Architectural Goals & Clean Architecture Principles

- **Zero Top-Bar Duplication**: A single `AppTopBar` instance in `App.kt` handles top-level actions and search across screens.
- **Strict Encapsulation**: Screen content (e.g. `DialogueDetailScreen`) manages its own internal titles, audio controls, and phrase views without top-bar bleed.
- **Unidirectional Data Flow (UDF)**:
  - Route state, screen navigation, and drawer state live at the `App.kt` / `AppViewModel` orchestration layer.
  - Dumb UI components receive primitive state and invoke callback lambdas.
- **Extensible Navigation Drawer**: A clean Material 3 side drawer containing user profile information, sign out, and a dedicated section for future navigation routes.

---

## 2. Component Specifications

### 2.1 `AppDrawerContent` (`com.blbulyandavbulyan.larm.kmp.ui.common.AppDrawerContent.kt`)
A composable rendered inside `ModalDrawerSheet`:
- **Profile Header**:
  - `AvatarImage(avatarUrl, displayName, size = 48.dp)`: Displays avatar image via Coil with initials fallback.
  - User Name: Bold title text (`userProfile?.displayName ?: "Anonymous"`).
  - User Email: Caption/body text (`userProfile?.email ?: ""`).
- **Divider**: `HorizontalDivider`.
- **Navigation Section**:
  - `NavigationDrawerItem` for **Dialogue Generator** (`ScreenState.Generator`).
  - Extensible architecture to support upcoming features/routes.
  - Clicking an item navigates to the destination and closes the drawer.
- **Footer**:
  - `HorizontalDivider`.
  - **Sign Out Item**: `NavigationDrawerItem` with sign-out icon and `stringResource(Res.string.action_sign_out)`. Invokes `onSignOut()` and closes the drawer.

---

### 2.2 `AppTopBar` (`com.blbulyandavbulyan.larm.kmp.ui.common.AppTopBar.kt`)
A composable replacing individual screen top bars:
- **Left Navigation Slot**:
  - **Hamburger Icon Button** (Always visible): Calls `onOpenDrawer()`. Test tag: `hamburger_button`.
  - **Back Icon Button** (Visible only when `onBack != null`): Calls `onBack()`. Test tag: `top_bar_back_button`.
- **Center / Content Slot**:
  - Customizable slot `@Composable () -> Unit`.
  - On `ScreenState.Generator` and `ScreenState.Search`: Hosts `SearchField` directly without redundant app titles or subtitles. Test tag: `top_bar_search_field`.
  - On `ScreenState.Detail`: Empty slot (detail screen manages its title internally).
- **Actions Slot**:
  - Trailing action composable slot for screen-specific actions when needed.

---

### 2.3 Screen Refactoring

1. **`DialogueGeneratorScreen.kt`**:
   - Remove internal `Header` (which contained duplicate search field, title, and subtitle).
   - Conversation box expands to utilize the full screen height cleanly.
2. **`DialogueSearchScreen.kt`**:
   - Remove internal `SearchBarAndBackButton`.
   - Directly render search results list, loading indicator, and error/retry views.
3. **`DialogueDetailScreen.kt`**:
   - Remove internal empty `TopAppBar`.
   - Keep `DialogueTitle`, phrase list, and audio playback intact and fully encapsulated.

---

### 2.4 `App.kt` Orchestration

- Wrap authenticated content inside `ModalNavigationDrawer(drawerState = rememberDrawerState(DrawerValue.Closed), drawerContent = { AppDrawerContent(...) })`.
- Pass contextual `onBack` lambda to `AppTopBar`:
  - `ScreenState.Generator` -> `null` (no back button).
  - `ScreenState.Search` -> `appViewModel::navigateToGenerator`.
  - `ScreenState.Detail` -> `appViewModel::navigateToSearch`.
- Pass contextual search bar to `AppTopBar`:
  - When on `Generator` or `Search`, pass `SearchField` bound to `searchViewModel`.

---

## 3. String Resources

Ensure all strings are defined in `strings.xml`:
- `action_sign_out`
- `profile_anonymous_user`
- `profile_no_email`
- `search_dialogues_placeholder`
- `nav_dialogue_generator`

---

## 4. Testing & Verification

1. **Component Tests**:
   - `AppDrawerTest`: Verify profile header (avatar, name, email), navigation items, and sign-out button.
   - `AppTopBarTest`: Verify Hamburger button presence, back button conditional visibility, and search field integration.
2. **Integration Tests (`AppTest.kt`)**:
   - Verify opening the drawer and signing out.
   - Verify back button navigation from Detail -> Search -> Generator.
   - Verify search input in top bar initiates search navigation.
3. **Full Multiplatform Suite**:
   - Execute `./gradlew detekt` and `./gradlew check`.
