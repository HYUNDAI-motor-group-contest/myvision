package app.myvision.domain

// 장면 ① 운전자 전환의 순수 Kotlin 모델. Android, Pleos SDK import 금지(단위 테스트로 검증).
// null = 읽지 못했거나 프로필에 저장하지 않은 값(결측). 값을 지어 채우지 않는다.

/** 운전석 기준 시트, 공조 값. 시트 위치, 등받이는 SDK가 절대값 쓰기를 주지 않아(이동 명령만) 뺐다. */
data class CabinSettings(
    val climateOn: Boolean? = null,
    val temperatureC: Float? = null,
    val fanSpeed: Int? = null,
    val seatHeat: Int? = null,
    val seatVent: Int? = null,
)

data class Profile(val id: String, val name: String, val settings: CabinSettings = CabinSettings())

enum class Gear { PARK, NOT_PARK, UNKNOWN }

data class VehicleSnapshot(
    val gear: Gear,
    val settings: CabinSettings,
    val rangeKm: Float?,
    val warningLights: Int?, // 켜져 있거나 깜빡이는 경고등 수
)

/** 차에 쓸 변경 하나. 공조 전원을 먼저 켜야 온도, 풍량이 먹으므로 순서가 의미 있다. */
sealed interface Change {
    data class ClimatePower(val on: Boolean) : Change
    data class Temperature(val celsius: Float) : Change
    data class FanSpeed(val level: Int) : Change
    data class SeatHeat(val level: Int) : Change
    data class SeatVent(val level: Int) : Change
}

val Change.isSeat get() = this is Change.SeatHeat || this is Change.SeatVent
