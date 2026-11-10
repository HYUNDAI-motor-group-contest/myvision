package app.myvision.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.myvision.alert.CameraInfo
import app.myvision.alert.CameraKind

/**
 * 단속 카메라 확인 패널(DESIGN.md: 주행 중 정보 2개 이하, 크게, 깜빡임 없음, 소리가 먼저고 화면은 확인만).
 * 지점, 구간 시작: 제한속도 + 남은 거리. 구간 안: 제한속도 + 구간 평균속도.
 */
@Composable
fun CameraPanel(camera: CameraInfo) {
    val section = camera.kind == CameraKind.SECTION
    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(if (camera.kind == CameraKind.SPOT) "단속 카메라" else "구간단속", style = MaterialTheme.typography.headlineLarge)
        if (camera.speedLimit > 0) Text("제한 ${camera.speedLimit}km/h", style = MaterialTheme.typography.displayLarge)
        Text(
            if (section) "구간 평균 ${camera.sectionAvgSpeed}km/h" else "남은 거리 ${camera.remainDistance}m",
            style = MaterialTheme.typography.displayMedium,
        )
        val over = if (section) camera.isOverSectionAvgSpeed else camera.isOverSpeed
        if (over) Text("제한 속도를 넘었어요", style = MaterialTheme.typography.headlineSmall)
    }
}
