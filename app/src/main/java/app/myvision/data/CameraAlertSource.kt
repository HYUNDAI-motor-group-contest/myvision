package app.myvision.data

import app.myvision.alert.CameraAlertPlanner
import app.myvision.alert.CameraInfo
import app.myvision.alert.Sensitivity

/** 내비 카메라 이벤트 경계. 실제는 PleosCameraAlertSource, 테스트는 FakeCameraAlertSource. */
interface CameraAlertSource {
    fun start(onAlerts: (List<CameraInfo>) -> Unit)
    fun release()
}

/** 카메라 이벤트 → 가장 가까운 하나만 판단 → 말한다. 순수 Kotlin이라 Fake로 시험한다. 한 스레드에서만 부른다. */
class CameraAlertRelay(private val sensitivity: () -> Sensitivity, private val speak: (String) -> Unit) {
    private val planner = CameraAlertPlanner()

    /** 화면에 보여 줄 카메라를 돌려준다(없으면 null). */
    fun onAlerts(cameras: List<CameraInfo>): CameraInfo? {
        val nearest = cameras.minByOrNull { it.remainDistance } ?: return null
        val s = sensitivity()
        planner.plan(nearest, s)?.let { speak(it.text) }
        return nearest.takeIf { planner.shows(it, s) }
    }
}
