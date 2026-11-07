package app.myvision.domain

/** 현재 값과 프로필 목표 값을 비교해 바꿀 항목만 고른다. 목표가 없으면(저장 안 함) 건드리지 않는다. */
object RestorePlanner {
    fun plan(current: CabinSettings, target: CabinSettings): List<Change> = listOfNotNull(
        target.climateOn?.takeIf { it != current.climateOn }?.let(Change::ClimatePower),
        target.temperatureC?.takeIf { it != current.temperatureC }?.let(Change::Temperature),
        target.fanSpeed?.takeIf { it != current.fanSpeed }?.let(Change::FanSpeed),
        target.seatHeat?.takeIf { it != current.seatHeat }?.let(Change::SeatHeat),
        target.seatVent?.takeIf { it != current.seatVent }?.let(Change::SeatVent),
    )
}
