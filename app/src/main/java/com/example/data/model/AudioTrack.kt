package com.example.data.model

data class AudioTrack(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String = "Unknown Album",
    val uriString: String,
    val durationMs: Long,
    val mimeType: String = "audio/*",
    val fileSize: Long = 0L,
    val codec: String = "AUDIO",
    val sampleRate: String = "44.1 kHz",
    val bitDepth: String = "16-bit",
    val isFavorite: Boolean = false,
    val isSample: Boolean = false
) {
    val formattedDuration: String
        get() = formatDuration(durationMs)

    companion object {
        fun formatDuration(ms: Long): String {
            if (ms <= 0) return "00:00"
            val totalSec = ms / 1000
            val minutes = totalSec / 60
            val seconds = totalSec % 60
            return "%02d:%02d".format(minutes, seconds)
        }
    }
}
