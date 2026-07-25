# Domain Models Refactoring Design Spec

## Overview
The goal is to decouple the API response models (Data layer) from the rest of the application (Domain and Presentation layers) by introducing intermediate, pure Kotlin data classes. The repositories will be responsible for fetching the API responses and mapping them into these domain models before returning them to the ViewModels.

## Requirements & Constraints
1. **Separation of Concerns:** API models (`*Response`) must not leak out of the repository.
2. **Immutable Collections:** All domain models must use `ImmutableList` from `kotlinx.collections.immutable` instead of standard Kotlin `List` to ensure downstream Jetpack Compose stability.
3. **Distinct Lifecycles:** Do not unify the "Generated/Draft" models with the "Saved/Search" models. Drafts do not have database IDs or audio assets, while saved models do. They must remain strictly separated.
4. **Mappers:** Mapping functions must be written as `private` extension functions scoped within the respective Repository files (e.g., `DialogueChatRepository.kt`).

## Domain Models Definition

### 1. Saved / Search Dialogues
**Package:** `com.blbulyandavbulyan.larm.kmp.domain.model.dialogue.search`

```kotlin
import kotlinx.collections.immutable.ImmutableList

data class Asset(val contentType: String, val url: String)

data class PhraseTranslation(val id: String, val isoLanguageCode: String, val translationText: String)

data class Phrase(
    val id: String, 
    val text: String,
    val isoLanguageCode: String,
    val transcription: String,
    val translations: ImmutableList<PhraseTranslation>,
    val assets: ImmutableList<Asset>
) {
    val audioAssetUrl: String? get() = assets.firstOrNull { it.contentType.startsWith("audio/") }?.url
}

data class DialogueSummary(val id: String, val title: Phrase)
data class Speaker(val id: String, val name: Phrase)
data class DialoguePhrase(val speakerId: String, val phrase: Phrase)

data class Dialogue(
    val id: String,
    val title: Phrase,
    val speakers: ImmutableList<Speaker>,
    val phrases: ImmutableList<DialoguePhrase>
)
```

### 2. Generated / Chat Dialogues
**Package:** `com.blbulyandavbulyan.larm.kmp.domain.model.dialogue.chat`

```kotlin
import kotlinx.collections.immutable.ImmutableList

data class ChatTranslation(
    val translationText: String,
    val isoLanguageCode: String
)

data class DialogueTitle(
    val text: String,
    val transcription: String,
    val translations: ImmutableList<ChatTranslation>
)

data class DraftSpeaker(
    val id: String, 
    val name: String,
    val transcription: String,
    val translations: ImmutableList<ChatTranslation>
)

data class DraftPhrase(
    val text: String,
    val isoLanguageCode: String,
    val transcription: String,
    val translations: ImmutableList<ChatTranslation>
)

data class DraftDialoguePhrase(
    val speakerId: String,
    val phrase: DraftPhrase
)

data class GeneratedDialogue(
    val message: String,
    val info: DialogueTitle,
    val speakers: ImmutableList<DraftSpeaker>,
    val phrases: ImmutableList<DraftDialoguePhrase>
)
```

## Repository Mappers
Inside `DialogueRepository.kt`, we will add private mappers like:
- `private fun SearchDialoguesResponse.toDomain(): ImmutableList<DialogueSummary>`
- `private fun GetDialogueResponse.toDomain(): Dialogue`

Inside `DialogueChatRepository.kt`, we will add private mappers like:
- `private fun DialogueChatResponse.toDomain(): GeneratedDialogue`

Note: The `SaveDialogueRequest` mappings will also be updated to map *from* the `GeneratedDialogue` domain model back to the API request model.

## UI / ViewModel Updates
- `SearchState.Success` will hold `ImmutableList<DialogueSummary>` (it currently holds `DialogueSummaryResponse`).
- `DialogueChatViewModel` will hold `GeneratedDialogue` instead of `DialogueChatResponse` inside its `ConversationItem.AiResponse` wrapper.
- Update all relevant UI components in `shared/src/commonMain/kotlin/com/blbulyandavbulyan/larm/kmp/ui/...` to consume the new domain properties (e.g. `phrase.text` instead of `phrase.phrase`, etc.).
