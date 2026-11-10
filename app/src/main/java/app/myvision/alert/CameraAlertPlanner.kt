package app.myvision.alert

// 장면 ② 단속 카메라 사전 안내 판단. Android, Pleos SDK에 의존하지 않는 결정론 규칙(같은 입력 순서 → 같은 출력, LLM, 난수, 시계 없음).
// 입력 필드는 NaviHelper 3.0.5 onCameraAlert의 CameraAlert(navi-data 3.0.4)에 맞춘다.

/**
 * 운전자별 안내 민감도. 달라지는 것은 안내 시점(거리)과 횟수뿐이다.
 * 거리, 횟수는 설계값이다(실측, 사용자 시험으로 정한 값 아님).
 */
enum class Sensitivity(val label: String, val distances: List<Int>, val maxOverSpeedAlerts: Int) {
    NORMAL("기본", listOf(500), 1),
    OFTEN_MISSES("자주 놓침", listOf(1000, 500, 200), 2);

    companion object {
        /** 저장된 이름을 읽는다. 없거나 모르는 값(옛 버전 등)은 기본. */
        fun fromStored(name: String?): Sensitivity = entries.firstOrNull { it.name == name } ?: NORMAL
    }
}

/** SPOT: 지점 단속, SECTION_START: 구간단속 시작점(지점처럼 거리로 안내), SECTION: 구간 안(평균속도로 판단). */
enum class CameraKind { SPOT, SECTION_START, SECTION }

data class CameraInfo(
    val id: String,
    val kind: CameraKind,
    val speedLimit: Int,          // km/h. 0 이하는 값 없음
    val remainDistance: Int,      // m
    val sectionAvgSpeed: Int,     // km/h
    val isOverSpeed: Boolean,
    val isOverSectionAvgSpeed: Boolean,
)

/** atMeters: 안내를 낸 기준 거리(거리 안내면 임계값, 과속 안내면 그때 남은 거리). isRepeat: 이 카메라에 대한 두 번째 이후 안내. */
data class Announcement(val text: String, val atMeters: Int, val isRepeat: Boolean)

class CameraAlertPlanner {
    private class Seen(var nearest: Int = Int.MAX_VALUE, var wasOver: Boolean = false, var overAlerts: Int = 0, var spoken: Boolean = false)

    private val seen = HashMap<String, Seen>()

    /** 지금 화면에 보여 줄 카메라인가(남은 거리가 있고, 첫 안내 거리 안이거나 구간 안). */
    fun shows(c: CameraInfo, s: Sensitivity): Boolean =
        c.remainDistance > 0 && (c.kind == CameraKind.SECTION || c.remainDistance <= s.distances.max())

    /** 이번 목록에 없는(지나간) 카메라 기록을 지운다. 같은 카메라가 다시 오면 처음부터 안내한다. */
    fun forgetExcept(current: List<CameraInfo>) {
        seen.keys.retainAll(current.map(::key).toSet())
    }

    /** 이번 이벤트에 말할 안내. 없으면 null. */
    fun plan(c: CameraInfo, s: Sensitivity): Announcement? {
        val st = seen.getOrPut(key(c)) { Seen() }
        val section = c.kind == CameraKind.SECTION
        val over = if (section) c.isOverSectionAvgSpeed else c.isOverSpeed
        val becameOver = over && !st.wasOver // 넘는 순간에만(계속 과속이어도 반복하지 않는다)
        st.wasOver = over
        val repeat = st.spoken

        if (!section) {
            // 안쪽에 들어온 임계 중 아직 안내하지 않은 가장 가까운 것 하나. 여러 임계를 한꺼번에 넘어도 한 번만 말한다
            val crossed = s.distances.filter { c.remainDistance in 1..it && it < st.nearest }.minOrNull()
            if (crossed != null) {
                st.nearest = crossed
                st.spoken = true
                return Announcement(aheadText(c, over), crossed, repeat)
            }
            if (!st.spoken) return null // 첫 안내 거리 밖에서는 과속이어도 말하지 않는다
        }
        if (becameOver && st.overAlerts < s.maxOverSpeedAlerts) {
            st.overAlerts++
            st.spoken = true
            val text = if (section) "구간 평균 속도가 제한을 넘었어요. 평균 ${c.sectionAvgSpeed}${limit(c)}." else "제한 속도를 넘었어요. 속도를 줄여요."
            return Announcement(text, c.remainDistance, repeat)
        }
        return null
    }

    private fun aheadText(c: CameraInfo, over: Boolean): String {
        val meters = maxOf(50, (c.remainDistance + 25) / 50 * 50) // 듣기 쉽게 50m 단위
        val what = if (c.kind == CameraKind.SECTION_START) "구간단속 시작" else "단속 카메라"
        return "${meters}m 앞 $what${limit(c)}." + if (over) " 지금 제한 속도를 넘었어요." else ""
    }

    private fun key(c: CameraInfo) = "${c.id}/${c.kind}"

    private fun limit(c: CameraInfo) = if (c.speedLimit > 0) ", 제한 ${c.speedLimit}" else ""
}
