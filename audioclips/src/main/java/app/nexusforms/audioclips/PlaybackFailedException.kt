package app.nexusforms.audioclips

data class PlaybackFailedException(val uRI: String, val exceptionMsg: Int) : Exception()
