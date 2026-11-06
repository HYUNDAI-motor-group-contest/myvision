# 검토한 디자인 스킬 (허용 목록)

`AGENTS.md`의 스킬 허용 목록 실물이다. 원본 레포의 `SKILL.md`를 **고치지 않고** 복사했고
(각 README가 허용하는 설치 방법), 라이선스(MIT)를 같은 폴더에 둔다. 미러, 포크본은 쓰지 않는다. 새 스킬이나 새 판은 `SKILL.md`를 읽고 PR에 요약과 SHA를 남긴 뒤 이 표에 추가한다.

| 스킬 | 원본 | 커밋 SHA | 쓰는 곳 |
|---|---|---|---|
| `redesign-existing-projects` | https://github.com/Leonxlnx/taste-skill `skills/redesign-skill` | `5217fb45be2c0b302f29c9cd31cbd3237501c684` | 기존 화면 진단 |
| `minimalist-ui` | https://github.com/Leonxlnx/taste-skill `skills/minimalist-skill` | `5217fb45be2c0b302f29c9cd31cbd3237501c684` | 절제된 표면, 타이포 원칙 |
| `baseline-ui` | https://github.com/ibelick/ui-skills `skills/baseline-ui` | `b1cc8e0073ac64b09b3d38cd604407aa20c2b7ad` | 간격, 위계, 빈 상태 점검 |
| `fixing-motion-performance` | https://github.com/ibelick/ui-skills `skills/fixing-motion-performance` | `b1cc8e0073ac64b09b3d38cd604407aa20c2b7ad` | 모션 점검 |
| `frontend-design` | https://github.com/anthropics/skills `skills/frontend-design` (Apache-2.0) | `33375500bcea98d610eb30ce10ac4e59b89c390d` | 새 화면 기획(브리프 → 토큰 계획 → 기본값 점검 → 구현 → 스크린샷 자기 비판) |

## 우선순위

**AGENTS.md > DESIGN.md > 스킬.** 스킬 지시가 우리 규칙(표현 규칙, 색의 의미, 화이트박스, 결정론)과
부딪히면 우리 규칙을 따른다. 알려진 충돌과 판정:

| 스킬 지시 | 판정 | 이유 |
|---|---|---|
| minimalist-ui: 스크롤 등장, 순차 등장 애니메이션, 배경 이미지, 그레인, 그라데이션 광원 | 기각 | DESIGN.md 모션 규칙 — 모션은 펼치기/닫기 피드백에만 |
| minimalist-ui: 세리프 제목, Inter 금지, Phosphor 아이콘 | 기각 | 본문 서체는 하나(한글은 Pretendard). 아이콘 라이브러리 추가 없음 |
| minimalist-ui: 파스텔 알약 배지 | 기각 | 알약 배지 남발 금지, 색은 의미 있는 값에만 |
| redesign: 폰트 교체(Geist 등), 노이즈, 스포트라이트 테두리, 3열 카드 금지 | 기각 | 한글 서체, 데이터를 읽는 화면의 맥락과 맞지 않음 |
| redesign: 글래스모피즘 | 부분 채택(2026-09-26) | 탐색 층(헤더, 하단 탭 막대, 팝오버)에만. 데이터 면은 불투명 유지(DESIGN.md "유리 층") |
| frontend-design: "제품을 드러내는 과감한 선택 하나", 브리프 대조, 스크린샷 자기 비판, 가짜 01/02/03 금지, 전부 대문자 라벨 금지 | 채택 | 과감함은 한 곳에만 쓰고 나머지는 조용히 |
| frontend-design: 메타 문자열을 가운뎃점으로 잇지 말 것, 제목 위 eyebrow 라벨 줄이기 | 채택(2026-10-18) / 부분 채택 | 처음엔 기각했지만 "AI 티가 난다"는 피드백으로 가운데 점을 금지했다. eyebrow는 섹션 머리에만 |
| frontend-design: 기본값이 아닌 서체 고르기 | 기각 | 본문 서체는 하나(한글은 Pretendard) |
| redesign: 가짜 숫자를 "자연스럽게" 흩트리기 | 기각 | 없는 데이터를 만들지 않는다. 예시는 예시로 표시 |
| baseline-ui: motion/react, cn, Radix/Base UI 도입 | 기각 | 새 의존성 추가 없이 기존 스택(CSS 프레임워크 + 네이티브 요소)으로 |
| baseline-ui: 자간 변경 금지 | 채택(2026-10-09) | 제목 자간 -0.02em 보정도 걷어 내고 기본값으로(DESIGN.md) |
| 공통: 강조색 하나, 그라데이션과 글로우 금지, 빈 상태에 다음 행동 하나, tabular-nums, 200ms 이하 ease-out, prefers-reduced-motion | 채택 | DESIGN.md에 반영 |

taste-skill의 다이얼(DESIGN_VARIANCE, MOTION_INTENSITY, VISUAL_DENSITY)은 본체 스킬(`design-taste-frontend`) 설정이라
설치하지 않았다. 판단 기준으로만 **DESIGN_VARIANCE=3, MOTION_INTENSITY=2, VISUAL_DENSITY=5**를 쓴다
(절제된 레이아웃, 호버와 펼치기 정도의 모션, 대시보드치고 여유 있는 밀도). 프로젝트에 맞게 바꾼다.
`high-end-visual-design`, `industrial-brutalist-ui`는 근거를 읽는 화면과 맞지 않아 쓰지 않는다.

## 그 밖의 도구 판정

설치하지 않은 도구와 이유는 [JHarvis의 tool-radar.md](https://github.com/Choi-Jung-Hyeon/JHarvis/blob/main/tools/tool-radar.md)에 모은다(이 파일은 새 프로젝트로 복사되므로 전체 주소로 적는다).
