package app.myvision.data

import app.myvision.domain.CabinSettings
import app.myvision.domain.Change
import app.myvision.domain.Gear
import app.myvision.domain.Profile
import app.myvision.domain.VehicleSnapshot
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

/** 쓰기를 기록하고 현재 값에 반영하는 가짜 차량. */
class FakeVehicleGateway(var gear: Gear, var settings: CabinSettings = CabinSettings()) : VehicleGateway {
    val writes = mutableListOf<Change>()

    override suspend fun snapshot() = VehicleSnapshot(gear, settings, rangeKm = 380f, warningLights = null)

    override suspend fun apply(change: Change): Boolean {
        writes += change
        settings = when (change) {
            is Change.ClimatePower -> settings.copy(climateOn = change.on)
            is Change.Temperature -> settings.copy(temperatureC = change.celsius)
            is Change.FanSpeed -> settings.copy(fanSpeed = change.level)
            is Change.SeatHeat -> settings.copy(seatHeat = change.level)
            is Change.SeatVent -> settings.copy(seatVent = change.level)
        }
        return true
    }
}

class DriverSwitcherTest {
    private val a = Profile("dad", "아빠", CabinSettings(true, 22f, 3, 2, 0))
    private val b = Profile("mom", "엄마", CabinSettings(true, 19f, 5, 0, 1))

    @Test fun `프로필 A에서 B로 바꾸면 B 값으로 쓰기를 호출한다`() = runBlocking {
        val car = FakeVehicleGateway(Gear.PARK)
        val spoken = mutableListOf<String>()
        val switcher = DriverSwitcher(car, spoken::add)

        switcher.select(a)
        car.writes.clear()
        val result = switcher.select(b)

        assertEquals(listOf(Change.Temperature(19f), Change.FanSpeed(5), Change.SeatHeat(0), Change.SeatVent(1)), car.writes)
        assertEquals(b.settings, car.settings)
        assertEquals(SwitchResult.Applied("시트와 공조를 맞췄어요. 주행 가능 거리 380km."), result)
        assertEquals("시트와 공조를 맞췄어요. 주행 가능 거리 380km.", spoken.last())
    }

    @Test fun `주행 중 선택은 쓰지 않고 P가 되면 적용한다`() = runBlocking {
        val car = FakeVehicleGateway(Gear.NOT_PARK)
        val switcher = DriverSwitcher(car) {}

        assertEquals(SwitchResult.WaitingForPark, switcher.select(b))
        assertEquals(emptyList<Change>(), car.writes)

        car.gear = Gear.PARK
        switcher.onGearChanged(Gear.PARK)
        assertEquals(b.settings, car.settings)
    }
}
