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
import app.myvision.alert.CameraInfo
import app.myvision.alert.Sensitivity
import app.myvision.data.CameraAlertRelay
import app.myvision.data.DemoCameraScript
import app.myvision.data.DriverSwitcher
import app.myvision.data.FakeCameraAlertSource
import app.myvision.data.PleosCameraAlertSource
import app.myvision.data.PleosVehicleGateway
import app.myvision.data.ProfileRepository
import app.myvision.data.Speaker
import app.myvision.data.SwitchResult
import app.myvision.domain.Gear
import app.myvision.ui.CameraPanel
import app.myvision.ui.ProfileScreen
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var vehicle: PleosVehicleGateway
    private lateinit var speaker: Speaker
    private lateinit var cameras: PleosCameraAlertSource

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
        var driving by mutableStateOf(false)
        var camera by mutableStateOf<CameraInfo?>(null)
        var demoCamera by mutableStateOf<CameraInfo?>(null)
        var demoRunning by mutableStateOf(false)

        // 장면 ②: 선택된 운전자의 민감도로 단속 카메라를 소리 먼저 안내한다(운전자 미선택이면 기본)
        val relay = CameraAlertRelay(
            sensitivity = { profiles.firstOrNull { it.id == selectedId }?.sensitivity ?: Sensitivity.NORMAL },
            speak = speaker::speak,
        )
        cameras = PleosCameraAlertSource(this)
        // 콜백 스레드를 모르므로(추정) 판단과 화면 갱신은 메인에서 순서대로
        var staleTimer: Job? = null
        cameras.start { alerts ->
            lifecycleScope.launch {
                camera = relay.onAlerts(alerts)
                // 빈 목록이 오지 않아도 갱신이 끊기면 패널을 내린다(시계는 화면 층에만, 판단 로직은 결정론 유지)
                staleTimer?.cancel()
                staleTimer = launch { delay(CAMERA_STALE_MS); camera = null }
            }
        }

        // 예시 입력(시연용): 실제 내비와 섞이지 않게 별도 소스, 별도 relay(판단 기록 분리). 말하기와 민감도는 같다
        val demoSource = FakeCameraAlertSource()
        val demoRelay = CameraAlertRelay(relay.sensitivity, speaker::speak)
        demoSource.start { demoCamera = demoRelay.onAlerts(it) }
        val playDemo = {
            demoRunning = true
            lifecycleScope.launch {
                try {
                    DemoCameraScript.steps.forEach { demoSource.emit(*it.toTypedArray()); delay(DemoCameraScript.STEP_MS) }
                } finally {
                    demoCamera = null
                    demoRunning = false
                }
            }
        }

        lifecycleScope.launch {
            val gear = vehicle.snapshot().gear
            parked = gear == Gear.PARK
            driving = gear == Gear.NOT_PARK
        }
        vehicle.observeGear { gear ->
            lifecycleScope.launch {
                parked = gear == Gear.PARK
                driving = gear == Gear.NOT_PARK
                switcher.onGearChanged(gear)?.let { status = it }
            }
        }

        setContent {
            // DESIGN.md: 다크 테마 기본
            MaterialTheme(colorScheme = darkColorScheme()) {
                Surface(Modifier.fillMaxSize()) {
                    // 안내 중에는 카메라 정보만 보인다(DESIGN.md: 주행 중 정보 2개 이하)
                    demoCamera?.let { CameraPanel(it, demo = true) }
                        ?: camera?.let { CameraPanel(it, demo = false) }
                        ?: ProfileScreen(
                        profiles = profiles,
                        selectedId = selectedId,
                        status = status,
                        parked = parked,
                        driving = driving,
                        onSelect = { p ->
                            selectedId = p.id
                            lifecycleScope.launch {
                                status = when (val r = switcher.select(p)) {
                                    is SwitchResult.Applied -> r.briefing
                                    SwitchResult.WaitingForPark -> "정차 후 적용할게요. 기어를 P로 바꾸면 맞춰요."
                                }
                            }
                        },
                        onSensitivity = { p, s ->
                            repo.saveSensitivity(p.id, s)
                            profiles = repo.load()
                        },
                        demoRunning = demoRunning,
                        onDemo = { playDemo() },
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

    private companion object {
        const val CAMERA_STALE_MS = 10_000L // 설계값. 내비 이벤트 주기는 문서에 없음(추정 1초 안팎)
    }

    override fun onDestroy() {
        cameras.release()
        vehicle.release()
        speaker.release()
        super.onDestroy()
    }
}
