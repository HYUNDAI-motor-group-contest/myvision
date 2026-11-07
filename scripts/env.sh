# 로컬(WSL) 빌드 환경. 셸 기본값을 바꾸지 않도록 이 셸에서만: `. scripts/env.sh && ./gradlew ...`
# 도구는 저장소 옆 ../opt(지금은 ~/hmg-contest/opt)에만 둔다(JDK 17, Android SDK, Gradle 캐시, ~/.android 대신 쓸 폴더). CI는 이 파일을 쓰지 않는다.
# 이 파일 기준 상대경로라 폴더를 통째로 옮겨도 그대로 동작한다
OPT="${MYVISION_OPT:-$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)/opt}"
export JAVA_HOME="$OPT/jdk-17"
export ANDROID_HOME="$OPT/android-sdk"
export GRADLE_USER_HOME="$OPT/gradle-home"
export ANDROID_USER_HOME="$OPT/android-user-home"
export PATH="$JAVA_HOME/bin:$PATH"
