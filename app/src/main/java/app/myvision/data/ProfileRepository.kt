package app.myvision.data

import android.content.Context
import android.content.SharedPreferences
import app.myvision.alert.Sensitivity
import app.myvision.domain.CabinSettings
import app.myvision.domain.Profile

/** 프로필은 기기 안 SharedPreferences에만(네트워크 없음, AGENTS.md 데이터 규칙). 저장 안 한 값은 null. */
class ProfileRepository(context: Context) {
    private val prefs = context.getSharedPreferences("profiles", Context.MODE_PRIVATE)

    fun load(): List<Profile> = EXAMPLE_PROFILES.map { (id, name) ->
        Profile(
            id, name,
            CabinSettings(
                climateOn = get(id, "climateOn") { getBoolean(it, false) },
                temperatureC = get(id, "temperatureC") { getFloat(it, 0f) },
                fanSpeed = get(id, "fanSpeed") { getInt(it, 0) },
                seatHeat = get(id, "seatHeat") { getInt(it, 0) },
                seatVent = get(id, "seatVent") { getInt(it, 0) },
            ),
            Sensitivity.fromStored(prefs.getString("$id.sensitivity", null)),
        )
    }

    fun save(id: String, s: CabinSettings) {
        prefs.edit().apply {
            put(id, "climateOn", s.climateOn) { k, v -> putBoolean(k, v) }
            put(id, "temperatureC", s.temperatureC) { k, v -> putFloat(k, v) }
            put(id, "fanSpeed", s.fanSpeed) { k, v -> putInt(k, v) }
            put(id, "seatHeat", s.seatHeat) { k, v -> putInt(k, v) }
            put(id, "seatVent", s.seatVent) { k, v -> putInt(k, v) }
        }.apply()
    }

    fun saveSensitivity(id: String, s: Sensitivity) {
        prefs.edit().putString("$id.sensitivity", s.name).apply()
    }

    private fun <T> get(id: String, key: String, read: SharedPreferences.(String) -> T): T? =
        "$id.$key".let { if (prefs.contains(it)) prefs.read(it) else null }

    private fun <T> SharedPreferences.Editor.put(id: String, key: String, value: T?, write: SharedPreferences.Editor.(String, T) -> Unit) {
        if (value == null) remove("$id.$key") else write("$id.$key", value)
    }

    companion object {
        /** 이름은 예시 데이터(화면에 "예시"로 표시). 카메라 안내 민감도는 프로필마다 저장, 기본값은 '기본'. */
        val EXAMPLE_PROFILES = listOf("dad" to "아빠", "mom" to "엄마", "me" to "나")
    }
}
