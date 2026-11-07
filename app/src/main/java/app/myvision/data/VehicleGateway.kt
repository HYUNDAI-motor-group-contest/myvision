package app.myvision.data

import app.myvision.domain.BriefingComposer
import app.myvision.domain.Change
import app.myvision.domain.Gear
import app.myvision.domain.Profile
import app.myvision.domain.RestorePlanner
import app.myvision.domain.SafetyGate
import app.myvision.domain.VehicleSnapshot

/** 차량 읽기, 쓰기 경계. 실제는 PleosVehicleGateway, 테스트는 FakeVehicleGateway. */
interface VehicleGateway {
    /** 읽지 못한 값은 null. */
    suspend fun snapshot(): VehicleSnapshot

    /** 성공하면 true. */
    suspend fun apply(change: Change): Boolean
}

sealed interface SwitchResult {
    data class Applied(val briefing: String) : SwitchResult
    data object WaitingForPark : SwitchResult
}

/** 프로필 선택 → (P일 때만) 바뀐 항목만 쓰기 → 브리핑 한 줄을 말한다. 순수 Kotlin이라 Fake로 시험한다. */
class DriverSwitcher(private val vehicle: VehicleGateway, private val speak: (String) -> Unit) {
    private val gate = SafetyGate()
    val pending: Profile? get() = gate.pending

    suspend fun select(profile: Profile): SwitchResult {
        val snap = vehicle.snapshot()
        val now = gate.request(profile, snap.gear) ?: return SwitchResult.WaitingForPark
        return SwitchResult.Applied(restore(now, snap))
    }

    /** 대기 중인 프로필이 있고 P가 되면 적용하고 브리핑을 돌려준다. */
    suspend fun onGearChanged(gear: Gear): String? =
        gate.onGearChanged(gear)?.let { restore(it, vehicle.snapshot()) }

    private suspend fun restore(profile: Profile, snap: VehicleSnapshot): String {
        val applied = RestorePlanner.plan(snap.settings, profile.settings).filter { vehicle.apply(it) }
        return BriefingComposer.compose(applied, snap.rangeKm, snap.warningLights).also(speak)
    }
}
