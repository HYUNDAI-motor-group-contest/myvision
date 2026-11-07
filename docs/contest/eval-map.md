# 평가표 대응표

평가 항목마다 "무엇을 보여 주면 점수가 나오나"와 그 증거. 배점과 세부 기준 원문은 공모 요강에서 옮긴다(미정: 아직 옮기지 않음). 증거 칸은 실제로 생긴 것만 채운다.

| 평가 항목 | 보여 줄 것 | 증거(파일, 영상 시각, 링크) | 상태 |
|---|---|---|---|
| 차량특화성 | 운전자 전환 → 시트, 공조 복원(Vehicle SDK), 카메라 선안내(NaviHelper onCameraAlert → TextToSpeech). 주행 중 화면 1~2개 정보 | 미정 | 계획만 |
| 창의성 | 탑승부터 하차까지 "지금 운전하는 사람" 기준으로 안내를 고른다. 소리 먼저, 화면은 차분하게 | 미정 | 계획만 |
| 기술 활용성 | Pleos SDK 3종(Vehicle 2.3.2, NaviHelper 3.0.5, TextToSpeech 3.3.1), 결정론 규칙 엔진(`alert/`)과 단위 테스트, CI | `app/src/main/java/app/myvision/alert/`, `.github/workflows/android-ci.yml` | 뼈대만 |
| 사업성 | 미정(차량 공유, 가족 차량 등 대상 사용자와 근거. 수치는 출처와 함께, 추정은 추정이라고) | 미정 | 계획만 |
| 가산점 | 시연 영상, 공개 GitHub(소스 + `apk/최중현_MyVision_release.apk`) | https://github.com/HYUNDAI-motor-group-contest/myvision | 저장소만 |
| 2차 PT | 에뮬레이터 실시연(`demo-scenario.md`), 구조와 로직을 본인이 설명(규칙 엔진은 표 하나로 설명 가능하게) | 미정 | 계획만 |
