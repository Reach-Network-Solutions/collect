package app.nexusforms.audiorecorder.recording.internal

import android.app.Application
import android.content.Intent
import androidx.lifecycle.LiveData
import app.nexusforms.audiorecorder.recorder.Output
import app.nexusforms.audiorecorder.recording.AudioRecorder
import app.nexusforms.audiorecorder.recording.RecordingSession
import app.nexusforms.audiorecorder.recording.internal.AudioRecorderService.Companion.ACTION_CLEAN_UP
import app.nexusforms.audiorecorder.recording.internal.AudioRecorderService.Companion.ACTION_PAUSE
import app.nexusforms.audiorecorder.recording.internal.AudioRecorderService.Companion.ACTION_RESUME
import app.nexusforms.audiorecorder.recording.internal.AudioRecorderService.Companion.ACTION_START
import app.nexusforms.audiorecorder.recording.internal.AudioRecorderService.Companion.ACTION_STOP
import app.nexusforms.audiorecorder.recording.internal.AudioRecorderService.Companion.EXTRA_OUTPUT
import app.nexusforms.audiorecorder.recording.internal.AudioRecorderService.Companion.EXTRA_SESSION_ID
import app.nexusforms.shared.data.Consumable
import java.io.Serializable

internal class ForegroundServiceAudioRecorder internal constructor(private val application: Application, private val recordingRepository: RecordingRepository) : AudioRecorder() {

    override fun isRecording(): Boolean {
        val currentSession = recordingRepository.currentSession.value
        return currentSession != null && currentSession.file == null
    }

    override fun getCurrentSession(): LiveData<RecordingSession?> {
        return recordingRepository.currentSession
    }

    override fun failedToStart(): LiveData<Consumable<Exception?>> {
        return recordingRepository.failedToStart
    }

    override fun start(sessionId: Serializable, output: Output) {
        application.startService(
            Intent(application, AudioRecorderService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_SESSION_ID, sessionId)
                putExtra(EXTRA_OUTPUT, output)
            }
        )
    }

    override fun pause() {
        application.startService(
            Intent(application, AudioRecorderService::class.java).apply { action = ACTION_PAUSE }
        )
    }

    override fun resume() {
        application.startService(
            Intent(application, AudioRecorderService::class.java).apply { action = ACTION_RESUME }
        )
    }

    override fun stop() {
        application.startService(
            Intent(application, AudioRecorderService::class.java).apply { action = ACTION_STOP }
        )
    }

    override fun cleanUp() {
        application.startService(
            Intent(application, AudioRecorderService::class.java).apply { action = ACTION_CLEAN_UP }
        )
    }
}
