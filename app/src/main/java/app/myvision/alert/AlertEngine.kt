package app.myvision.alert

// 주행 중 경고 판단. Android, Pleos SDK에 의존하지 않는 결정론 규칙(같은 입력 → 같은 출력, LLM 없음).
// 입력 필드 이름은 NaviHelper onCameraAlert 콜백(speedLimit, remainDistance, isOverSpeed)에 맞춘다.

data class CameraAlert(
    val speedLimit: Int,      // km/h
    val remainDistance: Int,  // m
    val isOverSpeed: Boolean,
)

enum class AlertStage { NONE, AHEAD, NEAR, OVER_SPEED }

fun interface AlertEngine {
    fun stageFor(alert: CameraAlert): AlertStage
}

/** 거리 기준값은 예시다. 실제 안내 거리는 시연 장면 구현 때 근거와 함께 정한다. */
class CameraAlertRules(
    private val aheadMeters: Int = 500, // 예시
    private val nearMeters: Int = 200,  // 예시
) : AlertEngine {
    override fun stageFor(alert: CameraAlert): AlertStage = when {
        alert.remainDistance > aheadMeters -> AlertStage.NONE
        alert.isOverSpeed -> AlertStage.OVER_SPEED
        alert.remainDistance > nearMeters -> AlertStage.AHEAD
        else -> AlertStage.NEAR
    }
}
