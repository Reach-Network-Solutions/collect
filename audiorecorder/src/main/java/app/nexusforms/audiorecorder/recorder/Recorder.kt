package app.nexusforms.audiorecorder.recorder

import app.nexusforms.audiorecorder.recording.MicInUseException
import app.nexusforms.audiorecorder.recording.SetupException
import java.io.File

internal interface Recorder {

    @Throws(SetupException::class, MicInUseException::class)
    fun start(output: Output)
    fun pause()
    fun resume()
    fun stop(): File
    fun cancel()

    val amplitude: Int
    fun isRecording(): Boolean
}

enum class Output {
    AMR,
    AAC,
    AAC_LOW
}
