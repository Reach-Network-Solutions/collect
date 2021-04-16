package app.nexusforms.audiorecorder.recording

import android.app.Application
import app.nexusforms.audiorecorder.getComponent
import app.nexusforms.audiorecorder.recording.internal.ForegroundServiceAudioRecorder


open class AudioRecorderFactory(private val application: Application) {

    open fun create(): AudioRecorder {
        return ForegroundServiceAudioRecorder(application, application.getComponent().recordingRepository())
    }
}
