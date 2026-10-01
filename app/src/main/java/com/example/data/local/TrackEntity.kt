package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.AudioTrack

@Entity(tableName = "tracks")
data class TrackEntity(
    @PrimaryKey val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val uriString: String,
    val durationMs: Long,
    val mimeType: String,
    val fileSize: Long,
    val codec: String,
    val sampleRate: String,
    val bitDepth: String,
    val isFavorite: Boolean = false,
    val isSample: Boolean = false,
    val lastPlayedTime: Long = 0L,
    val playCount: Int = 0
) {
    fun toAudioTrack(): AudioTrack = AudioTrack(
        id = id,
        title = title,
        artist = artist,
        album = album,
        uriString = uriString,
        durationMs = durationMs,
        mimeType = mimeType,
        fileSize = fileSize,
        codec = codec,
        sampleRate = sampleRate,
        bitDepth = bitDepth,
        isFavorite = isFavorite,
        isSample = isSample
    )

    companion object {
        fun fromAudioTrack(track: AudioTrack, isFavorite: Boolean = track.isFavorite): TrackEntity =
            TrackEntity(
                id = track.id,
                title = track.title,
                artist = track.artist,
                album = track.album,
                uriString = track.uriString,
                durationMs = track.durationMs,
                mimeType = track.mimeType,
                fileSize = track.fileSize,
                codec = track.codec,
                sampleRate = track.sampleRate,
                bitDepth = track.bitDepth,
                isFavorite = isFavorite,
                isSample = track.isSample
            )
    }
}
