# Audio MP3 Support Design

## Purpose
Enable the KMP `AudioPlayer` to support playing MP3s (and potentially other formats) sent by the backend, reducing the heavy payload sizes of uncompressed WAV files. The backend will return compressed audio and provide the MIME type in the `Content-Type` header.

## Architecture & Interface Changes
1. **New Data Class**: Create a new class `Audio` in the `com.blbulyandavbulyan.larm.kmp.audio` package:
   ```kotlin
   package com.blbulyandavbulyan.larm.kmp.audio

   data class Audio(
       val data: ByteArray,
       val mimeType: String
   )
   ```
2. **AudioPlayer Update**: Update the `AudioPlayer` interface (and all expected/actual platform declarations) to accept the new data class:
   ```kotlin
   suspend fun play(audio: Audio)
   ```

## Platform-Specific Implementations

### JS and WasmJS
- **Implementation**: The JS and WasmJS implementations of `AudioPlayer` currently create a `Blob` hardcoded to `"audio/wav"`. This will be updated to dynamically use `audio.mimeType`.
- **Browser Support**: Modern browsers natively support playing MP3 via `HTMLAudioElement`. No additional dependencies are required.

### JVM (Desktop)
- **Dependencies**: Java's standard `javax.sound.sampled.AudioSystem` does not natively support MP3s. We will add `com.googlecode.soundlibs:mp3spi:1.9.5.4` to the `jvmMain` dependencies in `build.gradle.kts`.
- **Implementation**: The `mp3spi` library seamlessly integrates into Java's `AudioSystem` via the Service Provider Interface. The existing implementation (`AudioSystem.getAudioInputStream(ByteArrayInputStream(audio.data))`) will remain exactly the same but will transparently gain the ability to parse MP3 streams.

## API Client Integration
- When the API client fetches the audio from the backend, it must read the `Content-Type` header from the Ktor `HttpResponse`.
- This header value, along with the byte array, will be used to instantiate the new `Audio` data class.

## Self-Review Checklist
- [x] No placeholders or TBDs
- [x] Scope is well-defined and concise
- [x] No contradicting sections
