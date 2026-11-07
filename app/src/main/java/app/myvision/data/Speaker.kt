package app.myvision.data

import ai.pleos.playground.tts.TextToSpeech
import ai.pleos.playground.tts.constant.ErrorType
import ai.pleos.playground.tts.constant.Mode
import ai.pleos.playground.tts.listener.OnInitializeListener
import android.content.Context
import android.util.Log

// 출처: ai.pleos.playground:TextToSpeech 3.3.1 AAR 공개 API(javap). 공식 TTS 샘플 저장소는 없음.
// ON_DEVICE 모드 선택, speak의 두 번째 인자 의미(true = 앞 발화를 끊고 말하기)는 추정. 에뮬레이터에서 확인한다.
class Speaker(context: Context) {
    private val tts = TextToSpeech(context.applicationContext, Mode.ON_DEVICE)
    @Volatile private var ready = false // 초기화 콜백 스레드에서 바뀐다

    init {
        runCatching {
            tts.initialize(object : OnInitializeListener {
                override fun onSuccess() { ready = true }
                override fun onFailure(type: ErrorType) { Log.w(TAG, "TTS 초기화 실패: $type") }
            })
        }.onFailure { Log.w(TAG, "TTS 초기화 예외", it) }
    }

    /** 준비 전이거나 실패하면 말하지 않는다(화면에는 같은 문장이 남는다). */
    fun speak(text: String) {
        if (ready) runCatching { tts.speak(text, true) }.onFailure { Log.w(TAG, "TTS speak 실패", it) }
    }

    fun release() = runCatching { tts.release() }

    private companion object { const val TAG = "Speaker" }
}
