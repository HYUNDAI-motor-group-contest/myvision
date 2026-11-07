# 로컬(WSL) 빌드 환경. 셸 기본값을 바꾸지 않도록 이 셸에서만: `. scripts/env.sh && ./gradlew ...`
# 도구는 ~/opt에만 둔다(JDK 17, Android SDK, Gradle 캐시, ~/.android 대신 쓸 폴더). CI는 이 파일을 쓰지 않는다.
OPT="${MYVISION_OPT:-$HOME/opt}"
export JAVA_HOME="$OPT/jdk-17"
export ANDROID_HOME="$OPT/android-sdk"
export GRADLE_USER_HOME="$OPT/gradle-home"
export ANDROID_USER_HOME="$OPT/android-user-home"
export PATH="$JAVA_HOME/bin:$PATH"
