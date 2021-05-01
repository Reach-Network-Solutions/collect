package app.nexusforms.audiorecorder.recording.internal

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.nexusforms.async.Scheduler
import app.nexusforms.audiorecorder.recording.internal.AudioRecorderService
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import app.nexusforms.audiorecorder.AudioRecorderDependencyModule
import app.nexusforms.audiorecorder.RobolectricApplication
import app.nexusforms.audiorecorder.recorder.Output
import app.nexusforms.audiorecorder.recorder.Recorder
import app.nexusforms.audiorecorder.recording.AudioRecorder
import app.nexusforms.audiorecorder.recording.AudioRecorderFactory
import app.nexusforms.audiorecorder.recording.AudioRecorderTest
import app.nexusforms.audiorecorder.recording.MicInUseException
import app.nexusforms.audiorecorder.setupDependencies
import app.nexusforms.audiorecorder.support.FakeRecorder
import app.nexusforms.shared.data.Consumable
import app.nexusforms.testshared.FakeScheduler
import org.robolectric.Robolectric
import org.robolectric.Shadows.shadowOf
import java.io.File

@RunWith(AndroidJUnit4::class)
class ForegroundServiceAudioRecorderTest : AudioRecorderTest() {

    @get:Rule
    val instantTaskExecutor = InstantTaskExecutorRule()
    private val application by lazy { getApplicationContext<RobolectricApplication>() }

    private val fakeRecorder = FakeRecorder()
    private val scheduler = FakeScheduler()

    override val viewModel: AudioRecorder by lazy {
        AudioRecorderFactory(application).create()
    }

    override fun runBackground() {
        while (shadowOf(application).peekNextStartedService() != null) {
            val serviceIntent = shadowOf(application).nextStartedService
            assertThat(serviceIntent.component?.className, equalTo(AudioRecorderService::class.qualifiedName))
            Robolectric.buildService(AudioRecorderService::class.java, serviceIntent)
                .create()
                .startCommand(0, 0)
        }
    }

    override fun getLastRecordedFile(): File? {
        return fakeRecorder.file
    }

    @Before
    fun setup() {
        application.setupDependencies(
            object : AudioRecorderDependencyModule() {
                override fun providesRecorder(cacheDir: File): Recorder {
                    return fakeRecorder
                }

                override fun providesScheduler(application: Application): Scheduler {
                    return scheduler
                }
            }
        )
    }

    @Test
    fun start_passesOutputToRecorder() {
        Output.values().forEach {
            viewModel.start("blah", it)
            viewModel.stop()
            runBackground()
            assertThat(fakeRecorder.output, equalTo(it))
        }
    }

    @Test
    fun start_incrementsDurationEverySecond() {
        viewModel.start("blah", Output.AAC)
        runBackground()

        val currentSession = viewModel.getCurrentSession()
        scheduler.runForeground(0)
        assertThat(currentSession.value?.duration, equalTo(0))

        scheduler.runForeground(500)
        assertThat(currentSession.value?.duration, equalTo(0))

        scheduler.runForeground(1000)
        assertThat(currentSession.value?.duration, equalTo(1000))
    }

    @Test
    fun start_updatesAmplitude() {
        viewModel.start("blah", Output.AAC)
        runBackground()

        val currentSession = viewModel.getCurrentSession()

        fakeRecorder.amplitude = 12
        scheduler.runForeground()
        assertThat(currentSession.value?.amplitude, equalTo(12))

        fakeRecorder.amplitude = 45
        scheduler.runForeground()
        assertThat(currentSession.value?.amplitude, equalTo(45))
    }

    @Test
    fun start_whenRecorderStartThrowsException_setsSessionToNull() {
        val exception = MicInUseException()
        fakeRecorder.failOnStart(exception)

        viewModel.start("blah", Output.AAC)
        runBackground()
        assertThat(viewModel.getCurrentSession().value, equalTo(null))
    }

    @Test
    fun start_whenRecorderStartThrowsException_setsFailedToStart() {
        val exception = MicInUseException()
        fakeRecorder.failOnStart(exception)

        viewModel.start("blah", Output.AAC)
        runBackground()
        assertThat(viewModel.failedToStart().value, equalTo(Consumable(exception)))
    }

    @Test
    fun start_whenRecorderStartThrowsException_thenSucceeds_setsFailedToStartToNull() {
        val exception = MicInUseException()
        fakeRecorder.failOnStart(exception)
        viewModel.start("blah", Output.AAC)
        runBackground()

        fakeRecorder.failOnStart(null)
        viewModel.start("blah", Output.AAC)
        runBackground()

        assertThat(viewModel.failedToStart().value, equalTo(Consumable(null)))
    }
}
