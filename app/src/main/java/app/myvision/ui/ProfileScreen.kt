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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.myvision.domain.Profile

/**
 * 운전자 선택 화면(DESIGN.md: 다크, 큰 글자, 깜빡임 없음, 색만으로 뜻 전달 금지 → 선택은 "선택됨" 글자로도).
 * 정차(P)가 아니면 저장 버튼을 숨겨 화면 정보를 줄인다.
 */
@Composable
fun ProfileScreen(
    profiles: List<Profile>,
    selectedId: String?,
    status: String,
    parked: Boolean,
    onSelect: (Profile) -> Unit,
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
                        if (selected) Text("선택됨", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
        Text("프로필 이름은 예시예요", style = MaterialTheme.typography.bodyLarge)
        if (status.isNotEmpty()) Text(status, style = MaterialTheme.typography.headlineSmall)
        if (parked && selectedId != null) {
            Button(onClick = onSaveCurrent, modifier = Modifier.fillMaxWidth().heightIn(min = 72.dp)) {
                Text("지금 상태를 이 프로필로 저장", style = MaterialTheme.typography.titleLarge)
            }
        }
    }
}
