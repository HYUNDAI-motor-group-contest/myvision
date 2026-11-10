package app.myvision.data

import app.myvision.alert.CameraInfo
import app.myvision.alert.CameraKind
import app.myvision.alert.Sensitivity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CameraAlertRelayTest {
    private val spoken = mutableListOf<String>()
    private var sensitivity = Sensitivity.NORMAL
    private var shown: CameraInfo? = null
    private val source = FakeCameraAlertSource()

    init {
        val relay = CameraAlertRelay({ sensitivity }, spoken::add)
        source.start { shown = relay.onAlerts(it) }
    }

    private fun cam(id: String, remain: Int) = CameraInfo(id, CameraKind.SPOT, 80, remain, 0, false, false)

    @Test fun `이벤트가 오면 가장 가까운 카메라 하나만 말하고 화면에 보인다`() {
        source.emit(cam("far", 900), cam("near", 480))

        assertEquals(listOf("500m 앞 단속 카메라, 제한 80."), spoken)
        assertEquals("near", shown?.id)
    }

    @Test fun `같은 이벤트가 반복돼도 한 번만 말하고, 빈 목록이면 화면에서 내린다`() {
        repeat(3) { source.emit(cam("a", 480)) }
        assertEquals(1, spoken.size)

        source.emit()
        assertNull(shown)
    }

    @Test fun `운전자를 바꾸면 그 민감도로 판단한다`() {
        source.emit(cam("a", 900))
        assertEquals(emptyList<String>(), spoken)
        assertNull(shown)

        sensitivity = Sensitivity.OFTEN_MISSES
        source.emit(cam("a", 890))
        assertEquals(listOf("900m 앞 단속 카메라, 제한 80."), spoken)
        assertEquals("a", shown?.id)
    }

    @Test fun `지나간 카메라는 남은 거리 0이면 화면에서 내리고, 목록에서 빠지면 기록을 지운다`() {
        source.emit(cam("a", 480))
        source.emit(cam("a", 0))
        assertNull(shown)

        source.emit(cam("b", 900)) // a가 목록에서 빠짐
        source.emit(cam("a", 450)) // 같은 id가 다시 오면 새 카메라로 안내
        assertEquals(listOf("500m 앞 단속 카메라, 제한 80.", "450m 앞 단속 카메라, 제한 80."), spoken)
    }

    @Test fun `release 뒤에는 이벤트를 받지 않는다`() {
        source.release()
        source.emit(cam("a", 480))
        assertEquals(emptyList<String>(), spoken)
    }
}
