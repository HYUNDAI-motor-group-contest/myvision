package app.myvision

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.lifecycle.lifecycleScope
import app.myvision.data.DriverSwitcher
import app.myvision.data.PleosVehicleGateway
import app.myvision.data.ProfileRepository
import app.myvision.data.Speaker
import app.myvision.data.SwitchResult
import app.myvision.domain.Gear
import app.myvision.ui.ProfileScreen
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var vehicle: PleosVehicleGateway
    private lateinit var speaker: Speaker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vehicle = PleosVehicleGateway(this)
        speaker = Speaker(this)
        val repo = ProfileRepository(this)
        val switcher = DriverSwitcher(vehicle, speaker::speak)

        var profiles by mutableStateOf(repo.load())
        var selectedId by mutableStateOf<String?>(null)
        var status by mutableStateOf("")
        var parked by mutableStateOf(false)

        lifecycleScope.launch { parked = vehicle.snapshot().gear == Gear.PARK }
        vehicle.observeGear { gear ->
            lifecycleScope.launch {
                parked = gear == Gear.PARK
                switcher.onGearChanged(gear)?.let { status = it }
            }
        }

        setContent {
            // DESIGN.md: 다크 테마 기본
            MaterialTheme(colorScheme = darkColorScheme()) {
                Surface(Modifier.fillMaxSize()) {
                    ProfileScreen(
                        profiles = profiles,
                        selectedId = selectedId,
                        status = status,
                        parked = parked,
                        onSelect = { p ->
                            selectedId = p.id
                            lifecycleScope.launch {
                                status = when (val r = switcher.select(p)) {
                                    is SwitchResult.Applied -> r.briefing
                                    SwitchResult.WaitingForPark -> "정차 후 적용할게요. 기어를 P로 바꾸면 맞춰요."
                                }
                            }
                        },
                        onSaveCurrent = {
                            val id = selectedId ?: return@ProfileScreen
                            lifecycleScope.launch {
                                repo.save(id, vehicle.snapshot().settings)
                                profiles = repo.load()
                                status = "지금 시트와 공조를 저장했어요."
                            }
                        },
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        vehicle.release()
        speaker.release()
        super.onDestroy()
    }
}
