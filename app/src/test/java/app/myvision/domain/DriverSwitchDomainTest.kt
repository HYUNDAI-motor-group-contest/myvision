package app.myvision.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RestorePlannerTest {
    private val a = CabinSettings(climateOn = true, temperatureC = 22f, fanSpeed = 3, seatHeat = 2, seatVent = 0)

    @Test fun `바뀐 항목만 계획한다`() {
        val b = a.copy(temperatureC = 19f, seatHeat = 0)
        assertEquals(listOf(Change.Temperature(19f), Change.SeatHeat(0)), RestorePlanner.plan(a, b))
    }

    @Test fun `같은 값이면 빈 목록`() {
        assertEquals(emptyList<Change>(), RestorePlanner.plan(a, a.copy()))
    }

    @Test fun `저장하지 않은 값은 건드리지 않고 모르는 현재 값은 쓴다`() {
        val current = CabinSettings(climateOn = null, fanSpeed = 5)
        val target = CabinSettings(climateOn = true)
        assertEquals(listOf(Change.ClimatePower(true)), RestorePlanner.plan(current, target))
    }
}

class SafetyGateTest {
    private val p = Profile("dad", "아빠")
    private val q = Profile("mom", "엄마")

    @Test fun `P면 바로 적용`() {
        val gate = SafetyGate()
        assertEquals(p, gate.request(p, Gear.PARK))
        assertNull(gate.pending)
    }

    @Test fun `P가 아니면 대기하다가 P가 되면 한 번 적용`() {
        val gate = SafetyGate()
        assertNull(gate.request(p, Gear.NOT_PARK))
        assertNull(gate.onGearChanged(Gear.NOT_PARK))
        assertEquals(p, gate.onGearChanged(Gear.PARK))
        assertNull(gate.onGearChanged(Gear.PARK)) // 두 번 적용하지 않는다
    }

    @Test fun `기어를 모르면 대기하고 마지막 선택만 적용`() {
        val gate = SafetyGate()
        assertNull(gate.request(p, Gear.UNKNOWN))
        assertNull(gate.request(q, Gear.NOT_PARK))
        assertEquals(q, gate.onGearChanged(Gear.PARK))
    }
}

class BriefingComposerTest {
    @Test fun `시트와 공조, 주행 가능 거리`() {
        val applied = listOf(Change.SeatHeat(2), Change.Temperature(22f))
        assertEquals("시트와 공조를 맞췄어요. 주행 가능 거리 380km.", BriefingComposer.compose(applied, 380.4f, 0))
    }

    @Test fun `값이 없으면 그 부분은 말하지 않는다`() {
        assertEquals("공조를 맞췄어요.", BriefingComposer.compose(listOf(Change.FanSpeed(3)), null, null))
    }

    @Test fun `바꾼 것이 없고 경고등이 켜져 있으면`() {
        assertEquals("설정이 이미 맞춰져 있어요. 주행 가능 거리 120km. 경고등 2개가 켜져 있어요.",
            BriefingComposer.compose(emptyList(), 120f, 2))
    }
}
