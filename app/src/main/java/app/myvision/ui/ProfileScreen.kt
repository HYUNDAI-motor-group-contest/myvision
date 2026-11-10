package app.myvision.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.myvision.alert.Sensitivity
import app.myvision.domain.Profile

/**
 * 운전자 선택 화면(DESIGN.md: 다크, 큰 글자, 깜빡임 없음, 색만으로 뜻 전달 금지 → 선택은 "선택됨" 글자로도).
 * 정차(P)가 아니면 저장 버튼을 숨겨 화면 정보를 줄인다. 카메라 안내 스위치는 주행 중(D, R)에만 숨긴다
 * (기어를 못 읽어도 바꿀 수 있게. 차량 제어가 아니라 앱 설정이라서).
 */
@Composable
fun ProfileScreen(
    profiles: List<Profile>,
    selectedId: String?,
    status: String,
    parked: Boolean,
    driving: Boolean,
    onSelect: (Profile) -> Unit,
    onSensitivity: (Profile, Sensitivity) -> Unit,
    demoRunning: Boolean,
    onDemo: () -> Unit,
    onSaveCurrent: () -> Unit,
) {
    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Text("누가 운전하나요?", style = MaterialTheme.typography.headlineLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            profiles.forEach { p ->
                val selected = p.id == selectedId
                OutlinedButton(
                    onClick = { onSelect(p) },
                    modifier = Modifier.weight(1f).heightIn(min = 120.dp),
                    border = BorderStroke(if (selected) 4.dp else 1.dp, MaterialTheme.colorScheme.primary),
                ) {
                    Column {
                        Text(p.name, style = MaterialTheme.typography.displaySmall)
                        Text("카메라 안내 ${p.sensitivity.label}", style = MaterialTheme.typography.titleMedium)
                        if (selected) Text("선택됨", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
        Text("프로필 이름은 예시예요", style = MaterialTheme.typography.bodyLarge)
        val selected = profiles.firstOrNull { it.id == selectedId }
        if (selected != null && !driving) {
            val often = selected.sensitivity == Sensitivity.OFTEN_MISSES
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Switch(
                    checked = often,
                    onCheckedChange = { onSensitivity(selected, if (it) Sensitivity.OFTEN_MISSES else Sensitivity.NORMAL) },
                )
                Text("${selected.name}: 단속 카메라를 자주 놓쳐요(더 일찍, 세 번 안내)", style = MaterialTheme.typography.titleLarge)
            }
        }
        if (status.isNotEmpty()) Text(status, style = MaterialTheme.typography.headlineSmall)
        // 내비 모의 주행 없이 장면 ②를 보이는 버튼. 가짜 데이터임을 버튼 글자로 늘 밝힌다
        OutlinedButton(onClick = onDemo, enabled = !demoRunning, modifier = Modifier.fillMaxWidth().heightIn(min = 72.dp)) {
            Text(
                if (demoRunning) "예시 입력(시연용) 재생 중" else "예시 카메라 입력(시연용)",
                style = MaterialTheme.typography.titleLarge,
            )
        }
        if (parked && selectedId != null) {
            Button(onClick = onSaveCurrent, modifier = Modifier.fillMaxWidth().heightIn(min = 72.dp)) {
                Text("지금 상태를 이 프로필로 저장", style = MaterialTheme.typography.titleLarge)
            }
        }
    }
}
