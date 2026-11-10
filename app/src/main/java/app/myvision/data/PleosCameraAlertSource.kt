package app.myvision.data

import ai.pleos.playground.navi.constants.CameraType
import ai.pleos.playground.navi.data.CameraAlert
import ai.pleos.playground.navi.helper.NaviHelper
import ai.pleos.playground.navi.helper.listener.NaviHelperEventListener
import android.content.Context
import android.util.Log
import app.myvision.alert.CameraInfo
import app.myvision.alert.CameraKind

// 출처: ai.pleos.playground:NaviHelper 3.0.5, navi-data 3.0.4 AAR 공개 API(javap로 확인)와
// github.com/Pleos-Playground/pleos-sample-navi-app NaviHelperViewModel.kt(initialize → addListener, removeListener → release).
// onCameraAlert(List<CameraAlert>), CameraAlert(id: String, type: CameraType, status: Boolean, speedLimit, remainDistance,
// sectionAvrSpeed: Int, isOverSpeed, isOverSectionAvrSpeed: Boolean). 퍼미션 NAVI_ROUTE(공식 샘플 매니페스트).
// 추정(문서 미확인, 에뮬레이터에서 확인): 단위 km/h와 m, 콜백 스레드, 카메라를 지나면 빈 목록이 오는지, status의 뜻(쓰지 않는다).
class PleosCameraAlertSource(context: Context) : CameraAlertSource {
    private val navi = NaviHelper(context.applicationContext)
    private var listener: NaviHelperEventListener? = null

    override fun start(onAlerts: (List<CameraInfo>) -> Unit) {
        val l = object : NaviHelperEventListener {
            override fun onCameraAlert(cameraAlertList: List<CameraAlert>) = onAlerts(cameraAlertList.map { it.toInfo() })
        }
        // 내비 서비스가 없으면(에뮬레이터 밖) 이벤트가 오지 않을 뿐 앱은 계속 돈다
        runCatching { navi.initialize(); navi.addListener(l) }
            .onSuccess { listener = l }
            .onFailure { Log.w(TAG, "NaviHelper 초기화 실패", it) }
    }

    override fun release() {
        runCatching {
            listener?.let { navi.removeListener(it) }
            navi.release()
        }
    }

    private fun CameraAlert.toInfo() = CameraInfo(
        id = id,
        // 종류 구분은 enum 이름에서 추정: SECTION_SPEED_ALERT, SECTION_END는 구간 안(평균속도 기준)
        kind = when (type) {
            CameraType.SPEED_CAMERA_SECTION_START -> CameraKind.SECTION_START
            CameraType.SECTION_SPEED_ALERT, CameraType.SPEED_CAMERA_SECTION_END -> CameraKind.SECTION
            else -> CameraKind.SPOT
        },
        speedLimit = speedLimit,
        remainDistance = remainDistance,
        sectionAvgSpeed = sectionAvrSpeed,
        isOverSpeed = isOverSpeed,
        isOverSectionAvgSpeed = isOverSectionAvrSpeed,
    )

    private companion object { const val TAG = "PleosNavi" }
}
