package app.myvision.data

import app.myvision.alert.CameraInfo
import app.myvision.alert.CameraKind

/**
 * 예시 카메라 입력(시연용). 내비 모의 주행 없이 장면 ②를 보이기 위한 가짜 데이터다.
 * 실제 내비 흐름과 섞이지 않게 별도 FakeCameraAlertSource, 별도 CameraAlertRelay로 재생한다.
 * 순서: 지점 카메라 1000 → 500 → 200m → 지남 → 구간 시작 500m → 구간 진입 → 구간 평균속도 초과 → 구간 끝.
 */
object DemoCameraScript {
    const val STEP_MS = 3_000L // 화면 층에서만 쓰는 재생 간격(안내 한 문장을 말할 시간)

    private fun spot(remain: Int) = CameraInfo("demo-spot", CameraKind.SPOT, 80, remain, 0, false, false)
    private fun section(kind: CameraKind, remain: Int, avg: Int = 0, over: Boolean = false) =
        CameraInfo("demo-section", kind, 100, remain, avg, false, over)

    /** 첫 빈 목록은 앞선 재생 기록을 지운다(다시 눌러도 처음부터 안내). */
    val steps: List<List<CameraInfo>> = listOf(
        emptyList(),
        listOf(spot(1000)),
        listOf(spot(500)),
        listOf(spot(200)),
        emptyList(),
        listOf(section(CameraKind.SECTION_START, 500)),
        listOf(section(CameraKind.SECTION, 2000, avg = 96)),
        listOf(section(CameraKind.SECTION, 1500, avg = 104, over = true)),
        emptyList(),
    )
}
