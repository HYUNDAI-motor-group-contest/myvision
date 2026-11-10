package app.myvision.domain

import kotlin.math.roundToInt

/** 탑승 브리핑 문장. 값이 없으면(null) 그 부분은 말하지 않는다. */
object BriefingComposer {
    fun compose(applied: List<Change>, rangeKm: Float?, warningLights: Int?): String {
        val seat = applied.any { it.isSeat }
        val climate = applied.any { !it.isSeat }
        val head = when {
            seat && climate -> "시트와 공조를 맞췄어요."
            seat -> "시트를 맞췄어요."
            climate -> "공조를 맞췄어요."
            else -> "설정이 이미 맞춰져 있어요."
        }
        return listOfNotNull(
            head,
            rangeKm?.let { "주행 가능 거리 ${it.roundToInt()}km." },
            warningLights?.takeIf { it > 0 }?.let { "경고등 ${it}개가 켜져 있어요." },
        ).joinToString(" ")
    }
}
