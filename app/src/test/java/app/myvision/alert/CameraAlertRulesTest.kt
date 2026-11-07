package app.myvision.alert

import org.junit.Assert.assertEquals
import org.junit.Test

// 테스트 값은 모두 예시다(실측 아님).
class CameraAlertRulesTest {
    private val engine = CameraAlertRules()

    @Test
    fun `예시 - 카메라까지 거리와 과속 여부로 안내 단계가 정해진다`() {
        assertEquals(AlertStage.NONE, engine.stageFor(CameraAlert(speedLimit = 80, remainDistance = 800, isOverSpeed = true)))
        assertEquals(AlertStage.AHEAD, engine.stageFor(CameraAlert(speedLimit = 80, remainDistance = 400, isOverSpeed = false)))
        assertEquals(AlertStage.NEAR, engine.stageFor(CameraAlert(speedLimit = 80, remainDistance = 150, isOverSpeed = false)))
        assertEquals(AlertStage.OVER_SPEED, engine.stageFor(CameraAlert(speedLimit = 80, remainDistance = 150, isOverSpeed = true)))
    }
}
