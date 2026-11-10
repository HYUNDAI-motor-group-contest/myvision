package app.myvision.data

import app.myvision.alert.CameraInfo
import app.myvision.alert.Sensitivity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DemoCameraScriptTest {
    private fun replay(s: Sensitivity, times: Int = 1): Pair<List<String>, CameraInfo?> {
        val spoken = mutableListOf<String>()
        var shown: CameraInfo? = null
        val source = FakeCameraAlertSource()
        val relay = CameraAlertRelay({ s }, spoken::add)
        source.start { shown = relay.onAlerts(it) }
        repeat(times) { DemoCameraScript.steps.forEach { source.emit(*it.toTypedArray()) } }
        return spoken to shown
    }

    @Test fun `기본 - 500m 한 번, 구간 시작, 평균속도 초과를 말하고 끝나면 화면을 내린다`() {
        val (spoken, shown) = replay(Sensitivity.NORMAL)
        assertEquals(
            listOf(
                "500m 앞 단속 카메라, 제한 80.",
                "500m 앞 구간단속 시작, 제한 100.",
                "구간 평균 속도가 제한을 넘었어요. 평균 104, 제한 100.",
            ),
            spoken,
        )
        assertNull(shown)
    }

    @Test fun `자주 놓침 - 지점 카메라를 1000, 500, 200m에서 세 번 말한다`() {
        val (spoken, _) = replay(Sensitivity.OFTEN_MISSES)
        assertEquals(
            listOf("1000m 앞 단속 카메라, 제한 80.", "500m 앞 단속 카메라, 제한 80.", "200m 앞 단속 카메라, 제한 80."),
            spoken.take(3),
        )
        assertEquals(5, spoken.size)
    }

    @Test fun `다시 재생해도 처음과 같은 안내를 한다`() {
        val once = replay(Sensitivity.NORMAL).first
        assertEquals(once + once, replay(Sensitivity.NORMAL, times = 2).first)
    }
}
