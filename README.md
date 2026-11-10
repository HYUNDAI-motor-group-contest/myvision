# MyVision

탑승부터 하차까지 지금 운전하는 사람을 알고, 놓치기 쉬운 정보(단속 카메라, 분기, 연료)를 소리 먼저, 화면은 차분하게 챙겨 주는 Pleos Connect 차량용 앱.

> 현대자동차그룹 차량용 앱 개발 공모전 출품작.

- 앱 이름: MyVision
- 한 줄 소개: 차를 함께 쓰는 운전자를 알아보고 시트, 공조를 복원하며, 단속 카메라를 운전자별 민감도에 맞춰 소리 먼저 알려 주는 차량용 앱.

| 장면 | 하는 일 | 상태 |
|---|---|---|
| ① 운전자 전환 | 프로필 선택 → 기어 P에서만 시트 열선, 통풍과 공조 복원 → 탑승 브리핑 음성 | 구현, 단위 테스트 검증. 에뮬레이터 실행 미검증 |
| ② 단속 카메라 사전 안내 | 내비 카메라 이벤트 → 거리, 과속, 구간 평균속도 규칙으로 판단 → 음성 안내 + 숫자 2개 화면 | 구현, 단위 테스트 검증. 에뮬레이터 실행 미검증 |

판단은 모두 정해진 규칙(결정론)이고 LLM을 쓰지 않는다. 카메라 안내 민감도는 시점과 횟수만 바꾼다(설계값, 실측 아님): 기본은 500m 앞 1회, 자주 놓침은 1000, 500, 200m 앞 3회. 민감도는 프로필 화면의 스위치("단속 카메라를 자주 놓쳐요")로 프로필마다 정하고 기기 안에 저장한다(기본값 '기본').

## 실행 방법

1. Windows에 Android Studio와 Pleos Connect Emulator 2.1.0(API 34 이미지)을 설치하고 Cold boot로 켠다.
2. 제출 APK 설치(Windows PowerShell, 저장소 폴더에서):

   ```powershell
   & "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" install -r "apk\최중현_MyVision_release.apk"
   ```

   WSL에서는 Windows adb.exe로(`<사용자>`는 Windows 사용자 이름, `cmd.exe /c echo %USERNAME%`로 확인):

   ```bash
   ADB=/mnt/c/Users/<사용자>/AppData/Local/Android/Sdk/platform-tools/adb.exe
   "$ADB" install -r "$(wslpath -w apk/최중현_MyVision_release.apk)"
   ```

3. 앱 목록에서 MyVision을 연다. 차량 상태는 Virtual Console로 바꾼다: `adb forward tcp:8088 tcp:8088` 후 http://127.0.0.1:8088/
4. 장면 ①: 기어 P에서 프로필을 고르면 공조, 시트 값이 바뀌고 브리핑이 나온다. 장면 ②: 내비 경로 안내 중 단속 카메라에 가까워지면 음성 안내와 카메라 패널이 나온다. 내비 없이 보려면 "예시 카메라 입력(시연용)" 버튼을 누른다(가짜 이벤트를 3초 간격으로 재생: 지점 카메라 1000 → 500 → 200m → 구간 시작 → 구간 진입 → 구간 평균속도 초과). 확인 순서는 [docs/contest/emulator-check.md](docs/contest/emulator-check.md).

소스에서 빌드(WSL 또는 Linux, JDK 17):

```bash
. scripts/env.sh                 # 로컬 도구 경로(JDK, Android SDK). CI는 쓰지 않음
./gradlew lint testDebugUnitTest assembleDebug
# → app/build/outputs/apk/debug/app-debug.apk
```

release 서명 값(`MYVISION_KEYSTORE`, `MYVISION_KEYSTORE_PASSWORD`, `MYVISION_KEY_ALIAS`)은 저장소 밖 Gradle 사용자 설정이나 `ORG_GRADLE_PROJECT_*` 환경변수에서 읽는다. 값이 없으면 `assembleRelease`는 서명 없는 APK를 만든다.

## 테스트 환경과 설정

| 항목 | 값 |
|---|---|
| 에뮬레이터 | Pleos Connect Emulator 2.1.0 / API 34 / Cold boot / RAM 4GB 권장 |
| 검증 상태 | 개발자 장비 사양 문제로 에뮬레이터 실행 검증은 미완, 판단 로직은 단위 테스트로 검증(34건, `./gradlew testDebugUnitTest`). lint, 단위 테스트, debug 빌드는 GitHub Actions(`android-ci`)에서 매 PR 실행. 표준 Android 34 에뮬레이터(Pleos 서비스 없음)에서 release APK가 설치, 실행되고 10초 뒤 살아 있는 것은 CI 스모크(`emulator-smoke`)로 확인 |
| 기능 ③ 확인 | 예시 입력 버튼으로 기능 ③(단속 카메라 사전 안내) 확인 가능: 프로필 화면 "예시 카메라 입력(시연용)". 화면에 "예시 입력(시연용)"이 함께 표시되고 실제 내비 흐름과 섞이지 않는다 |
| 빌드 | JDK 17, Gradle 8.10.2(래퍼), Android Gradle Plugin 8.8.2, compileSdk 35, minSdk 28 |
| SDK | Pleos Vehicle 2.3.2, NaviHelper 3.0.5, TextToSpeech 3.3.1(https://nexus-playground.pleos.ai/repository/maven-releases/) |
| 퍼미션 | AndroidManifest 선언만으로 에뮬레이터에서 동작(개발 단계, Pleos 문서) |
| 차량 상태 조작 | `adb forward tcp:8088 tcp:8088` 후 http://127.0.0.1:8088/ (Virtual Console) |
| 예시 데이터 | 프로필 3개(아빠, 엄마, 나)의 이름과 예시 카메라 입력 값은 예시(가짜 데이터). 시트, 공조 값은 앱에서 "지금 상태를 이 프로필로 저장"으로 넣는다 |
| 시연 장면 확인표 | [docs/contest/emulator-check.md](docs/contest/emulator-check.md) (전 항목 미검증) |

## APK 위치

`apk/최중현_MyVision_release.apk` — 서명된 release APK(약 39MB, minify 끔). 서명 확인: `apksigner verify apk/최중현_MyVision_release.apk`

## 개발

규칙은 [AGENTS.md](AGENTS.md), 화면 규칙은 [DESIGN.md](DESIGN.md), 공모전 대응은 [docs/contest/](docs/contest/).
