#!/usr/bin/env bash
# CI 에뮬레이터 스모크(.github/workflows/emulator-smoke.yml에서만 쓴다):
# release APK 설치 → 실행 → 10초 뒤 프로세스 생존, logcat에 FATAL 없음. 표준 Android 이미지라 Pleos 서비스는 없다.
set -u
out=smoke
mkdir -p "$out"
adb install -r app/build/outputs/apk/release/app-release.apk || { echo "FAIL: 설치"; exit 1; }
adb logcat -c
adb shell am start -W -n app.myvision/.MainActivity | tee "$out/am-start.txt"
sleep 10
pid=$(adb shell pidof app.myvision | tr -d '\r')
adb logcat -d -v time > "$out/logcat.txt"
adb exec-out screencap -p > "$out/screen.png" || true

fail=0
if [ -n "$pid" ]; then echo "OK: 10초 뒤 프로세스 살아 있음(pid $pid)"; else echo "FAIL: 10초 뒤 프로세스 없음"; fail=1; fi
if grep -n 'FATAL EXCEPTION' "$out/logcat.txt"; then
  echo "FAIL: logcat FATAL"; grep -n -A25 'FATAL EXCEPTION' "$out/logcat.txt" | head -60; fail=1
else
  echo "OK: logcat FATAL 없음"
fi
echo "--- 앱 경고(Pleos 서비스 없음 확인용)"
grep -nE 'PleosVehicle|PleosNavi|Speaker|MyVision' "$out/logcat.txt" | head -30 || true
exit $fail
