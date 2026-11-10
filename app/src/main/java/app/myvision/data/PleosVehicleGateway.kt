package app.myvision.data

import ai.pleos.playground.vehicle.Vehicle
import ai.pleos.playground.vehicle.api.model.area.SeatArea
import ai.pleos.playground.vehicle.api.model.telltale.Telltales
import ai.pleos.playground.vehicle.constant.area.SeatValue
import ai.pleos.playground.vehicle.constant.control.VehicleGear
import ai.pleos.playground.vehicle.constant.telltale.TelltaleState
import ai.pleos.playground.vehicle.constant.unit.TemperatureUnit
import ai.pleos.playground.vehicle.listener.GearSelectionListener
import android.content.Context
import android.util.Log
import app.myvision.domain.CabinSettings
import app.myvision.domain.Change
import app.myvision.domain.Gear
import app.myvision.domain.VehicleSnapshot
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

// API 이름, 시그니처 출처: ai.pleos.playground:Vehicle 2.3.2 AAR의 공개 인터페이스(javap로 확인)와
// github.com/Pleos-Playground/pleos-sample-vehicle-app VehicleClient.kt(initialize, 콜백 onComplete/onFailed, release 패턴).
// 퍼미션: Gear, Odometer → CAR_DRIVE, Safety → CAR_SAFETY, HVAC → CAR_CLIMATE, Seat → CAR_SEATS(SDK 구현 클래스의 RequiresCustomPermission).
// 시트 위치, 등받이는 Seat.moveSeatForeAft(MoveControl)처럼 이동 명령만 있어 목표 값 복원에서 뺐다.
// 기어, 주행 가능 거리 API는 2.3.2에서 deprecated("Google GEAR_SELECTION, RANGE_REMAINING property를 쓰라")지만
// SDK 안에서 대체 API를 주지 않아 그대로 쓴다. shortcut: 에뮬레이터에서 값이 안 오면 android.car CarPropertyManager로 바꾼다.
@Suppress("DEPRECATION")
class PleosVehicleGateway(context: Context) : VehicleGateway {
    private val vehicle = Vehicle(context.applicationContext)
    private val driver = SeatArea(SeatValue.ROW_1_LEFT) // 운전석(좌핸들 가정)
    private var gearListener: GearSelectionListener? = null

    init {
        // 서비스가 없으면(실차, 에뮬레이터 밖) 모든 읽기가 null → 기어 UNKNOWN → 복원하지 않는다
        runCatching { vehicle.initialize() }.onFailure { Log.w(TAG, "Vehicle initialize 실패", it) }
    }

    override suspend fun snapshot(): VehicleSnapshot {
        val hvac = vehicle.getHVAC()
        val seat = vehicle.getSeat()
        return VehicleSnapshot(
            gear = read<VehicleGear> { ok, fail -> vehicle.getGear().getCurrentGearSelectionState(ok, fail) }.toGear(),
            settings = CabinSettings(
                climateOn = read { ok, fail -> hvac.getHvacPowerState(driver, ok, fail) },
                temperatureC = read { ok, fail -> hvac.getHvacTemperature(driver, TemperatureUnit.CELSIUS, ok, fail) },
                fanSpeed = read { ok, fail -> hvac.getHvacFanSpeed(driver, ok, fail) },
                seatHeat = read { ok, fail -> seat.getHvacSeatTemperature(driver, ok, fail) },
                seatVent = read { ok, fail -> seat.getHvacSeatVentilationState(driver, ok, fail) },
            ),
            // 단위 km는 추정(문서 미확인). 에뮬레이터에서 Virtual Console 값과 맞춰 확인한다
            rangeKm = read { ok, fail -> vehicle.getOdometer().getRangeRemainingDistance(ok, fail) },
            warningLights = read<Telltales> { ok, fail -> vehicle.getSafety().getTelltalesInfo(ok, fail) }
                ?.telltales?.count { it.state != TelltaleState.OFF },
        )
    }

    override suspend fun apply(change: Change): Boolean = read<Unit> { ok, fail ->
        val done = { _: Any?, _: Any? -> ok(Unit) } // 완료 콜백이 돌려준 값은 쓰지 않는다
        when (change) {
            is Change.ClimatePower -> vehicle.getHVAC().setHvacPower(driver, change.on, done, fail)
            is Change.Temperature -> vehicle.getHVAC().setHvacTemperature(driver, change.celsius, TemperatureUnit.CELSIUS, done, fail)
            is Change.FanSpeed -> vehicle.getHVAC().setHvacFanSpeed(driver, change.level, done, fail)
            is Change.SeatHeat -> vehicle.getSeat().setHvacSeatTemperature(driver, change.level, done, fail)
            is Change.SeatVent -> vehicle.getSeat().setHvacSeatVentilation(driver, change.level, done, fail)
        }
    } != null

    fun observeGear(onGear: (Gear) -> Unit) {
        val listener = object : GearSelectionListener {
            override fun onSelected(gear: VehicleGear?) = onGear(gear.toGear())
            override fun onFailed(e: Exception) = onGear(Gear.UNKNOWN)
        }
        runCatching { vehicle.getGear().registerGearSelection(listener) }
            .onSuccess { gearListener = listener }
            .onFailure { Log.w(TAG, "기어 구독 실패", it) }
    }

    fun release() {
        runCatching {
            gearListener?.let { vehicle.getGear().unregisterGearSelection(it) }
            vehicle.release()
        }
    }

    /** SDK 콜백 하나를 값으로. null 값, 실패, 예외, 시간 초과는 null(결측). */
    private suspend fun <T : Any> read(call: (ok: (T?) -> Unit, fail: (Exception) -> Unit) -> Unit): T? =
        withTimeoutOrNull(TIMEOUT_MS) {
            suspendCancellableCoroutine { cont ->
                val finish = { value: T? -> if (cont.isActive) cont.resume(value) }
                try {
                    call({ finish(it) }, { Log.w(TAG, "Vehicle 호출 실패", it); finish(null) })
                } catch (e: Exception) {
                    Log.w(TAG, "Vehicle 호출 예외", e)
                    finish(null)
                }
            }
        }

    private fun VehicleGear?.toGear() = when (this) {
        null, VehicleGear.GEAR_UNKNOWN -> Gear.UNKNOWN
        VehicleGear.GEAR_PARK -> Gear.PARK
        else -> Gear.NOT_PARK
    }

    private companion object {
        const val TAG = "PleosVehicle"
        const val TIMEOUT_MS = 2_000L
    }
}
