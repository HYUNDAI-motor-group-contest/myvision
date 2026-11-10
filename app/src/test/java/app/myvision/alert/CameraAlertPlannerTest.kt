package app.myvision.alert

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

// 카메라 값은 모두 가짜 데이터다(실측 아님).
class CameraAlertPlannerTest {
    private val planner = CameraAlertPlanner()

    private fun cam(
        remain: Int,
        over: Boolean = false,
        id: String = "c1",
        kind: CameraKind = CameraKind.SPOT,
        limit: Int = 80,
        avg: Int = 0,
        overAvg: Boolean = false,
    ) = CameraInfo(id, kind, limit, remain, avg, over, overAvg)

    @Test fun `거리 임계 - 기본은 501m에서 조용하고 500m에서 한 번 말한다`() {
        assertNull(planner.plan(cam(501), Sensitivity.NORMAL))
        assertEquals(Announcement("500m 앞 단속 카메라, 제한 80.", 500, false), planner.plan(cam(500), Sensitivity.NORMAL))
    }

    @Test fun `중복 방지 - 같은 카메라는 임계 안에서 다시 말하지 않는다`() {
        planner.plan(cam(500), Sensitivity.NORMAL)
        assertNull(planner.plan(cam(499), Sensitivity.NORMAL))
        assertNull(planner.plan(cam(500), Sensitivity.NORMAL))
        assertNull(planner.plan(cam(120), Sensitivity.NORMAL))
    }

    @Test fun `민감도 - 자주 놓침은 1000, 500, 200m 세 번, 기본은 500m 한 번`() {
        val route = listOf(1200, 1001, 1000, 700, 500, 350, 201, 200, 80)
        val often = route.mapNotNull { planner.plan(cam(it, id = "a"), Sensitivity.OFTEN_MISSES) }
        val normal = route.mapNotNull { planner.plan(cam(it, id = "b"), Sensitivity.NORMAL) }

        assertEquals(listOf(1000, 500, 200), often.map { it.atMeters })
        assertEquals(listOf(false, true, true), often.map { it.isRepeat })
        assertEquals(listOf(500), normal.map { it.atMeters })
    }

    @Test fun `임계 여러 개를 한꺼번에 넘어 들어오면 한 번만 말한다`() {
        val first = planner.plan(cam(150), Sensitivity.OFTEN_MISSES)
        assertEquals(Announcement("150m 앞 단속 카메라, 제한 80.", 200, false), first)
        assertNull(planner.plan(cam(100), Sensitivity.OFTEN_MISSES))
    }

    @Test fun `거리는 50m 단위로 말한다`() {
        assertEquals("500m 앞 단속 카메라, 제한 80.", planner.plan(cam(487), Sensitivity.NORMAL)?.text)
        assertEquals("50m 앞 단속 카메라, 제한 80.", planner.plan(cam(10, id = "c2"), Sensitivity.NORMAL)?.text)
    }

    @Test fun `과속 - 첫 안내 거리 밖에서는 말하지 않고, 안내 시점에 과속이면 문장에 넣는다`() {
        assertNull(planner.plan(cam(800, over = true), Sensitivity.NORMAL))
        assertEquals("500m 앞 단속 카메라, 제한 80. 지금 제한 속도를 넘었어요.", planner.plan(cam(500, over = true), Sensitivity.NORMAL)?.text)
        // 계속 과속이어도 반복하지 않는다
        assertNull(planner.plan(cam(400, over = true), Sensitivity.NORMAL))
    }

    @Test fun `과속 재안내 - 안내 뒤 제한을 넘는 순간 다시 말하고, 횟수는 민감도만큼`() {
        fun overAlerts(s: Sensitivity, id: String): List<Announcement> {
            planner.plan(cam(190, id = id), s)
            // 넘었다 줄였다를 세 번 반복
            return listOf(180 to true, 170 to false, 160 to true, 150 to false, 140 to true)
                .mapNotNull { (m, over) -> planner.plan(cam(m, over, id), s) }
        }
        val normal = overAlerts(Sensitivity.NORMAL, "n")
        val often = overAlerts(Sensitivity.OFTEN_MISSES, "o")

        assertEquals(listOf(Announcement("제한 속도를 넘었어요. 속도를 줄여요.", 180, true)), normal)
        assertEquals(listOf(180, 160), often.map { it.atMeters })
    }

    @Test fun `구간단속 - 현재 속도가 아니라 구간 평균속도로 판단한다`() {
        val s = Sensitivity.NORMAL
        // 현재 속도는 넘었지만 평균은 안 넘음 → 조용
        assertNull(planner.plan(cam(3000, over = true, kind = CameraKind.SECTION, limit = 100, avg = 98), s))
        assertEquals(
            Announcement("구간 평균 속도가 제한을 넘었어요. 평균 103, 제한 100.", 2500, false),
            planner.plan(cam(2500, kind = CameraKind.SECTION, limit = 100, avg = 103, overAvg = true), s),
        )
        // 평균이 계속 넘는 동안은 반복하지 않는다
        assertNull(planner.plan(cam(2400, kind = CameraKind.SECTION, limit = 100, avg = 104, overAvg = true), s))
    }

    @Test fun `구간단속 - 평균 과속 안내 횟수도 민감도만큼`() {
        fun count(s: Sensitivity, id: String) = (1..6).count {
            planner.plan(cam(2000, id = id, kind = CameraKind.SECTION, limit = 100, avg = 101, overAvg = it % 2 == 1), s) != null
        }
        assertEquals(1, count(Sensitivity.NORMAL, "n"))
        assertEquals(2, count(Sensitivity.OFTEN_MISSES, "o"))
    }

    @Test fun `구간단속 시작점은 거리로 안내한다`() {
        assertEquals(
            "500m 앞 구간단속 시작, 제한 100.",
            planner.plan(cam(500, kind = CameraKind.SECTION_START, limit = 100), Sensitivity.NORMAL)?.text,
        )
    }

    @Test fun `카메라마다 따로 센다`() {
        planner.plan(cam(500, id = "a"), Sensitivity.NORMAL)
        assertEquals(false, planner.plan(cam(500, id = "b"), Sensitivity.NORMAL)?.isRepeat)
    }

    @Test fun `값이 없으면 지어내지 않는다 - 제한 0은 문장에서 빼고, 남은 거리 0 이하는 안내하지 않는다`() {
        assertEquals("500m 앞 단속 카메라.", planner.plan(cam(500, limit = 0), Sensitivity.NORMAL)?.text)
        assertNull(planner.plan(cam(0, id = "z"), Sensitivity.NORMAL))
        assertNull(planner.plan(cam(-5, id = "z"), Sensitivity.NORMAL))
    }

    @Test fun `화면 표시 - 첫 안내 거리 안이거나 구간 안일 때만`() {
        assertFalse(planner.shows(cam(501), Sensitivity.NORMAL))
        assertTrue(planner.shows(cam(500), Sensitivity.NORMAL))
        assertTrue(planner.shows(cam(501), Sensitivity.OFTEN_MISSES))
        assertFalse(planner.shows(cam(0), Sensitivity.NORMAL))
        assertTrue(planner.shows(cam(9000, kind = CameraKind.SECTION), Sensitivity.NORMAL))
        assertFalse(planner.shows(cam(0, kind = CameraKind.SECTION), Sensitivity.NORMAL))
    }

    @Test fun `목록에서 사라진 카메라는 기록을 지우고, 남은 카메라 기록은 유지한다`() {
        val a = cam(500, id = "a")
        val b = cam(500, id = "b")
        planner.plan(a, Sensitivity.NORMAL)
        planner.plan(b, Sensitivity.NORMAL)

        planner.forgetExcept(listOf(b))

        assertNull(planner.plan(cam(480, id = "b"), Sensitivity.NORMAL))
        assertEquals(Announcement("500m 앞 단속 카메라, 제한 80.", 500, false), planner.plan(cam(480, id = "a"), Sensitivity.NORMAL))
    }
}
