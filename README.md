# MyVision

탑승부터 하차까지 지금 운전하는 사람을 알고, 놓치기 쉬운 정보(단속 카메라, 분기, 연료)를 소리 먼저, 화면은 차분하게 챙겨 주는 Pleos Connect 차량용 앱.

> 현대자동차그룹 차량용 앱 개발 공모전 출품작. 개발 중(뼈대 단계).

## 실행 방법

1. Windows에 Android Studio와 Pleos Connect Emulator 2.1.0(API 34 이미지)을 설치하고 실행한다.
2. APK 설치: `adb install -r apk/최중현_MyVision_release.apk`(제출본) 또는 직접 빌드한 debug APK.
3. 앱 목록에서 MyVision을 연다.

직접 빌드:

```bash
./gradlew assembleDebug   # → app/build/outputs/apk/debug/app-debug.apk
./gradlew testDebugUnitTest
```

## 테스트 환경과 설정

| 항목 | 값 |
|---|---|
| 에뮬레이터 | Pleos Connect Emulator 2.1.0, API 34 이미지 |
| 빌드 | JDK 17, Gradle 8.10.2(래퍼), Android Gradle Plugin 8.8.2, compileSdk 35, minSdk 28 |
| SDK | Pleos Vehicle 2.3.2, NaviHelper 3.0.5, TextToSpeech 3.3.1(https://nexus-playground.pleos.ai/repository/maven-releases/) |
| 퍼미션 | AndroidManifest 선언만으로 에뮬레이터에서 동작(개발 단계) |
| 차량 상태 조작 | `adb forward tcp:8088 tcp:8088` 후 http://127.0.0.1:8088/ (Virtual Console) |
| 시연 장면 설정 | 미정(장면 구현 후 적는다) |

## APK 위치

`apk/최중현_MyVision_release.apk` — 제출본(v1.1.0) release APK. 아직 없음.

## 개발

규칙은 [AGENTS.md](AGENTS.md), 화면 규칙은 [DESIGN.md](DESIGN.md), 공모전 대응은 [docs/contest/](docs/contest/).
