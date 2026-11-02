package ytstudio.pet.model

data class PetCharacter(
    val id: String,
    val name: String,
    val avatarResId: Int,
    val isCustom: Boolean = false,
    val dialogues: List<String>,
    val specialActionType: SpecialAction = SpecialAction.KISS_AUDIO
)

enum class SpecialAction {
    NONE,
    KISS_AUDIO
}

data class PetSettings(
    val scaleFactor: Float = 1.0f,
    val soundEnabled: Boolean = true,
    val soundVolume: Float = 1.0f,
    val bubbleDurationMs: Long = 3500L,
    val snapToEdge: Boolean = true
)
