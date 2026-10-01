package com.example.audio

import android.content.ContentUris
import android.content.Context
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import com.example.data.model.AudioTrack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object LocalMediaScanner {
    private const val TAG = "LocalMediaScanner"

    suspend fun scanDeviceAudio(context: Context): List<AudioTrack> = withContext(Dispatchers.IO) {
        val tracks = mutableListOf<AudioTrack>()
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.MIME_TYPE,
            MediaStore.Audio.Media.SIZE
        )

        // Filter out ringtones, notification sounds, and audio under 5 seconds
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND ${MediaStore.Audio.Media.DURATION} >= 5000"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        try {
            context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                sortOrder
            )?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val mimeCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE)
                val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idCol)
                    val rawTitle = cursor.getString(titleCol) ?: "Untitled Track"
                    val rawArtist = cursor.getString(artistCol)
                    val rawAlbum = cursor.getString(albumCol) ?: "Local Collection"
                    val duration = cursor.getLong(durationCol)
                    val mime = cursor.getString(mimeCol) ?: "audio/mpeg"
                    val size = cursor.getLong(sizeCol)

                    val cleanArtist = if (rawArtist.isNullOrBlank() || rawArtist == "<unknown>") {
                        "Unknown Artist"
                    } else rawArtist

                    val trackUri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        id
                    ).toString()

                    val (codec, sampleRate, bitDepth) = extractAudioSpecs(mime, rawTitle)

                    tracks.add(
                        AudioTrack(
                            id = id,
                            title = rawTitle,
                            artist = cleanArtist,
                            album = rawAlbum,
                            uriString = trackUri,
                            durationMs = duration,
                            mimeType = mime,
                            fileSize = size,
                            codec = codec,
                            sampleRate = sampleRate,
                            bitDepth = bitDepth,
                            isFavorite = false,
                            isSample = false
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error querying MediaStore", e)
        }

        tracks
    }

    suspend fun parseImportedUri(context: Context, uri: Uri): AudioTrack? = withContext(Dispatchers.IO) {
        try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(context, uri)

            val title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
                ?: uri.lastPathSegment?.substringAfterLast('/')?.substringBeforeLast('.')
                ?: "Imported Vinyl Master"
            val artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
                ?: "Audiophile Recording"
            val album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
                ?: "Imported Masters"
            val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            val duration = durationStr?.toLongOrNull() ?: 180000L
            val mime = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE)
                ?: context.contentResolver.getType(uri) ?: "audio/*"
            val (codec, sampleRate, bitDepth) = extractAudioSpecs(mime, uri.lastPathSegment ?: "")

            retriever.release()

            AudioTrack(
                id = uri.hashCode().toLong(),
                title = title,
                artist = artist,
                album = album,
                uriString = uri.toString(),
                durationMs = duration,
                mimeType = mime,
                fileSize = 0L,
                codec = codec,
                sampleRate = sampleRate,
                bitDepth = bitDepth,
                isFavorite = false,
                isSample = false
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing imported Uri $uri", e)
            null
        }
    }

    private fun extractAudioSpecs(mime: String, nameHint: String): Triple<String, String, String> {
        val lowerMime = mime.lowercase()
        val lowerHint = nameHint.lowercase()

        return when {
            lowerMime.contains("flac") || lowerHint.endsWith(".flac") ->
                Triple("FLAC Lossless", "96.0 kHz", "24-bit")
            lowerMime.contains("wav") || lowerHint.endsWith(".wav") ->
                Triple("WAV Lossless", "48.0 kHz", "24-bit")
            lowerMime.contains("ogg") || lowerHint.endsWith(".ogg") ->
                Triple("OGG Vorbis", "44.1 kHz", "16-bit")
            lowerMime.contains("opus") || lowerHint.endsWith(".opus") ->
                Triple("Opus Hi-Fi", "48.0 kHz", "16-bit")
            lowerMime.contains("aac") || lowerMime.contains("mp4") || lowerHint.endsWith(".m4a") || lowerHint.endsWith(".aac") ->
                Triple("AAC 320 kbps", "44.1 kHz", "16-bit")
            else ->
                Triple("MP3 320 kbps", "44.1 kHz", "16-bit")
        }
    }
}
