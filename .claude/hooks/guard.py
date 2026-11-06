"""Claude Code 훅. CLAUDE.md, AGENTS.md는 '부탁'이라 어길 수 있어, 되돌리기 힘든 규칙 두 개만 훅으로 막는다.
종료 코드 2만 도구 실행을 막는다(1은 알림만). 막은 이유는 stderr로 Claude에게 간다.

- protect: .env 류(비밀키) 수정 금지. 템플릿 .env.example은 허용
- commit: git commit 전에 바뀐 영역의 빠른 검사만 돌린다. 느린 검사(전체 테스트, build)는 CI 필수 체크에 맡긴다

새 프로젝트에서는 아래 CHECKS만 고친다: (바뀐 파일 조건, 명령, 실행 폴더)
"""
import json
import re
import subprocess
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]

# (조건 함수, 명령, 실행 폴더). 조건은 바뀐 파일 목록을 받아 True면 실행한다.
CHECKS = [
    (lambda files: any(f.startswith("frontend/") and f.endswith((".ts", ".tsx")) for f in files),
     ["pnpm", "-s", "test:unit"], "frontend"),
    (lambda files: any(f.endswith(".py") for f in files),
     ["ruff", "check", "."], "."),
]


def protect(tool_input: dict) -> str | None:
    name = Path(tool_input.get("file_path", "")).name
    if re.fullmatch(r"\.env(\..+)?", name) and name != ".env.example":
        return f"{name}는 비밀키 파일이라 Claude가 고치지 않아요. 바꿀 값은 사람이 직접 넣어 주세요."
    return None


def commit(tool_input: dict) -> str | None:
    if not re.search(r"\bgit\b[^|;&]*\bcommit\b", tool_input.get("command", "")):
        return None
    # shortcut: 커밋에 안 들어갈 변경도 검사한다. `git add -A && git commit`은 훅 시점에 스테이징 전이라 작업 트리 전체를 본다
    status = subprocess.run(["git", "status", "--porcelain", "--no-renames", "-uall", "-z"], cwd=ROOT,
                            capture_output=True, text=True).stdout
    changed = [entry[3:] for entry in status.split("\0") if entry]
    for condition, command, folder in CHECKS:
        cwd = ROOT / folder
        if not condition(changed) or not cwd.exists():
            continue
        result = subprocess.run(command, cwd=cwd, capture_output=True, text=True)
        if result.returncode:
            tail = (result.stdout + result.stderr).strip().splitlines()[-15:]
            return f"커밋 전 검사 실패({' '.join(command[:3])}). 고친 뒤 다시 커밋하세요.\n" + "\n".join(tail)
    return None


if __name__ == "__main__":
    payload = json.load(sys.stdin)
    reason = {"protect": protect, "commit": commit}[sys.argv[1]](payload.get("tool_input", {}))
    if reason:
        print(reason, file=sys.stderr)
        sys.exit(2)
