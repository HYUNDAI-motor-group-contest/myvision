package app.myvision.data

import app.myvision.alert.CameraInfo

/** 내비 대신 카메라 이벤트를 손으로 넣는 가짜 소스. 값은 모두 가짜 데이터다. */
class FakeCameraAlertSource : CameraAlertSource {
    private var onAlerts: ((List<CameraInfo>) -> Unit)? = null
    override fun start(onAlerts: (List<CameraInfo>) -> Unit) { this.onAlerts = onAlerts }
    override fun release() { onAlerts = null }
    fun emit(vararg cameras: CameraInfo) { onAlerts?.invoke(cameras.toList()) }
}
