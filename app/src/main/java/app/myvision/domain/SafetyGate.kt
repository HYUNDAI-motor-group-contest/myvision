package app.myvision.domain

/**
 * 복원(차량 제어)은 기어 P에서만. P가 아니면(모름 포함) 마지막 요청을 들고 기다렸다가 P가 되면 한 번 내준다.
 * 같은 입력 순서 → 같은 출력(결정론).
 */
class SafetyGate {
    var pending: Profile? = null
        private set

    /** 지금 적용할 프로필을 돌려준다. null이면 "정차 후 적용" 대기. */
    fun request(profile: Profile, gear: Gear): Profile? =
        if (gear == Gear.PARK) profile.also { pending = null } else null.also { pending = profile }

    fun onGearChanged(gear: Gear): Profile? =
        if (gear == Gear.PARK) pending.also { pending = null } else null
}
