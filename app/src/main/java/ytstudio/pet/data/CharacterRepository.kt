package ytstudio.pet.data

import android.content.Context
import android.content.SharedPreferences
import ytstudio.pet.R
import ytstudio.pet.model.PetCharacter
import ytstudio.pet.model.PetSettings
import ytstudio.pet.model.SpecialAction

object CharacterRepository {

    private const val PREF_NAME = "pet_preferences"
    private const val KEY_ACTIVE_ID = "active_character_id"
    private const val KEY_SCALE = "pet_scale"
    private const val KEY_SOUND = "sound_enabled"
    private const val KEY_SNAP = "snap_to_edge"
    private const val KEY_BUBBLE_DURATION = "bubble_duration"

    val defaultCharacter = PetCharacter(
        id = "xiaoshazi",
        name = "小傻子",
        avatarResId = R.drawable.avatar_sleep,
        isCustom = false,
        dialogues = listOf(
            "我想酱酱了",
            "我想阳阳了",
            "我想圈圈了",
            "我是扬州小傻子",
            "我想去嫖娼"
        ),
        specialActionType = SpecialAction.KISS_AUDIO
    )

    private val charactersList = mutableListOf(
        defaultCharacter,
        PetCharacter(
            id = "yangzhou_mengxin",
            name = "萌萌哒",
            avatarResId = R.drawable.ic_launcher_foreground,
            isCustom = false,
            dialogues = listOf(
                "主人好呀！",
                "今天也要元气满满哦~",
                "摸摸头，给你小心心~"
            ),
            specialActionType = SpecialAction.NONE
        )
    )

    fun getCharacters(): List<PetCharacter> = charactersList.toList()

    fun getActiveCharacter(context: Context): PetCharacter {
        val sp = getPrefs(context)
        val activeId = sp.getString(KEY_ACTIVE_ID, defaultCharacter.id)
        return charactersList.find { it.id == activeId } ?: defaultCharacter
    }

    fun setActiveCharacter(context: Context, characterId: String) {
        getPrefs(context).edit().putString(KEY_ACTIVE_ID, characterId).apply()
    }

    fun addCustomCharacter(name: String, dialogues: List<String>): PetCharacter {
        val newChar = PetCharacter(
            id = "custom_" + System.currentTimeMillis(),
            name = name,
            avatarResId = R.drawable.avatar_sleep,
            isCustom = true,
            dialogues = dialogues.ifEmpty { listOf("你好呀！") },
            specialActionType = SpecialAction.KISS_AUDIO
        )
        charactersList.add(newChar)
        return newChar
    }

    fun getSettings(context: Context): PetSettings {
        val sp = getPrefs(context)
        return PetSettings(
            scaleFactor = sp.getFloat(KEY_SCALE, 1.0f),
            soundEnabled = sp.getBoolean(KEY_SOUND, true),
            snapToEdge = sp.getBoolean(KEY_SNAP, true),
            bubbleDurationMs = sp.getLong(KEY_BUBBLE_DURATION, 3500L)
        )
    }

    fun saveSettings(context: Context, settings: PetSettings) {
        getPrefs(context).edit()
            .putFloat(KEY_SCALE, settings.scaleFactor)
            .putBoolean(KEY_SOUND, settings.soundEnabled)
            .putBoolean(KEY_SNAP, settings.snapToEdge)
            .putLong(KEY_BUBBLE_DURATION, settings.bubbleDurationMs)
            .apply()
    }

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }
}
