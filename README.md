# Armenian Learning Assistant Frontend

This is a Kotlin Multiplatform project currently focused on the **Web** (WASM and JS) and **Desktop** (JVM) targets.

> **Note**: Android and iOS targets have been temporarily removed from the project structure as the current focus is on the Web and Desktop applications. They can be restored from the git history in the future when needed.

This is the frontend UI for the Armenian Language Learning app.
The backend is located here: [armenian-learning-assistant-be](https://github.com/blbulyandavbulyan/armenian-learning-assistant-be).

### API Documentation
The OpenAPI specifications for the backend can be found in the `backend-api-docs.json` file in the root directory.

* [/shared](./shared/src) is for code that will be shared across your Compose Multiplatform applications (currently Web-only).
  It contains several subfolders:
  - [commonMain](./shared/src/commonMain/kotlin) is for code that’s common for all targets.

### Environment & Build Configuration

The app uses **BuildKonfig** to generate compile-time constants for API and authentication endpoints:
- `apiUrl` (default: `http://localhost:8080`) — Backend API base URL.
- `supabaseUrl` — Your Supabase project URL (e.g., `https://xyzcompany.supabase.co`).
- `supabaseAnonKey` — Your Supabase project anonymous/public API key.

Because Gradle's `project.findProperty(...)` is used, you can supply these parameters in several standard ways:

#### 1. In IntelliJ IDEA Run / Debug Configurations (Gradle Goal)
If you run tasks like `:webApp:wasmJsBrowserDevelopmentRun` from IntelliJ:
1. Open **Run** -> **Edit Configurations...** from the top menu.
2. Select your Gradle task (e.g., `webApp [wasmJsBrowserDevelopmentRun]`).
3. In the **Arguments** field, pass the properties with `-P`:
   ```text
   -PsupabaseUrl="https://<project-id>.supabase.co" -PsupabaseAnonKey="<your-anon-key>" -PapiUrl="http://localhost:8080"
   ```
4. Click **Apply** and run the configuration.

#### 2. Via CLI / Terminal
Pass the parameters as Gradle `-P` flags:
```bash
./gradlew :webApp:wasmJsBrowserDevelopmentRun \
  -PsupabaseUrl="https://<project-id>.supabase.co" \
  -PsupabaseAnonKey="<your-anon-key>"
```

#### 3. Persistent User Settings (`~/.gradle/gradle.properties`)
To avoid passing arguments every time and keep keys out of the repository, add them to your global `~/.gradle/gradle.properties`:
```properties
supabaseUrl=https://<project-id>.supabase.co
supabaseAnonKey=<your-anon-key>
apiUrl=http://localhost:8080
```

#### 4. Environment Variables
Gradle automatically maps any environment variable prefixed with `ORG_GRADLE_PROJECT_` to project properties:
```bash
export ORG_GRADLE_PROJECT_supabaseUrl="https://<project-id>.supabase.co"
export ORG_GRADLE_PROJECT_supabaseAnonKey="<your-anon-key>"
```

### Running the apps

Use the run configurations provided by the run widget in your IDE's toolbar. You can also use these commands and options:

- Web app:
  - Wasm target (faster, modern browsers): `./gradlew :webApp:wasmJsBrowserDevelopmentRun`
  - JS target (slower, supports older browsers): `./gradlew :webApp:jsBrowserDevelopmentRun`
- Desktop app:
  - Standard run: `./gradlew :desktopApp:run`

### Running tests

Use the run button in your IDE's editor gutter, or run tests using Gradle tasks:

- Web tests:
  - Wasm target: `./gradlew :shared:wasmJsTest`
  - JS target: `./gradlew :shared:jsTest`
- Desktop tests & Coverage:
  - Run JVM tests: `./gradlew :shared:jvmTest`
  - Run JVM tests and generate coverage report (viewable at `shared/build/reports/kover/html/index.html`): `./gradlew :shared:jvmTest koverHtmlReport`

---

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html),
[Compose Multiplatform](https://github.com/JetBrains/compose-multiplatform/#compose-multiplatform),
[Kotlin/Wasm](https://kotl.in/wasm/)…

We would appreciate your feedback on Compose/Web and Kotlin/Wasm in the public Slack channel [#compose-web](https://slack-chats.kotlinlang.org/c/compose-web).
If you face any issues, please report them on [YouTrack](https://youtrack.jetbrains.com/newIssue?project=CMP).

### When new dependencies were added (or updated)
```shell
# update lock files
./gradlew :dependencies :shared:dependencies :webApp:dependencies desktopApp:dependencies --write-locks -Dorg.gradle.dependency.verification=off
# update dependency verification xml
./gradlew --write-verification-metadata sha256,pgp --export-keys --refresh-dependencies build :shared:compileTestKotlinJvm
```

### MCP servers
#### Sonar MCP Server
To make it work, you have to create `.env` file in the root of the project with the following content:
```
SONARQUBE_TOKEN=<YOUR_SONAR_TOKEN>
```

And put instead of `<YOUR_SONAR_TOKEN>` your real sonar token