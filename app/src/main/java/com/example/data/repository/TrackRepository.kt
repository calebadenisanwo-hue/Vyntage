package com.example.data.repository

import android.content.Context
import android.net.Uri
import com.example.audio.LocalMediaScanner
import com.example.audio.VintageAudioSampler
import com.example.data.local.PhonoDatabase
import com.example.data.local.TrackEntity
import com.example.data.model.AudioTrack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class TrackRepository(private val database: PhonoDatabase) {
    private val trackDao = database.trackDao()

    val allTracks: Flow<List<AudioTrack>> = trackDao.getAllTracks().map { list ->
        list.map { it.toAudioTrack() }
    }

    val favoriteTracks: Flow<List<AudioTrack>> = trackDao.getFavoriteTracks().map { list ->
        list.map { it.toAudioTrack() }
    }

    suspend fun scanAndSyncMediaStore(context: Context): Int = withContext(Dispatchers.IO) {
        val scanned = LocalMediaScanner.scanDeviceAudio(context)
        if (scanned.isNotEmpty()) {
            val entities = scanned.map { TrackEntity.fromAudioTrack(it) }
            trackDao.insertTracks(entities)
        }
        scanned.size
    }

    suspend fun loadVintageSampler(context: Context): List<AudioTrack> = withContext(Dispatchers.IO) {
        val sampleTracks = VintageAudioSampler.getOrGenerateSampleTracks(context)
        val entities = sampleTracks.map { TrackEntity.fromAudioTrack(it) }
        trackDao.insertTracks(entities)
        sampleTracks
    }

    suspend fun importAudioUris(context: Context, uris: List<Uri>): List<AudioTrack> = withContext(Dispatchers.IO) {
        val imported = mutableListOf<AudioTrack>()
        for (uri in uris) {
            val track = LocalMediaScanner.parseImportedUri(context, uri)
            if (track != null) {
                trackDao.insertTrack(TrackEntity.fromAudioTrack(track))
                imported.add(track)
            }
        }
        imported
    }

    suspend fun toggleFavorite(trackId: Long, currentFavorite: Boolean) = withContext(Dispatchers.IO) {
        trackDao.setFavorite(trackId, !currentFavorite)
    }

    suspend fun recordPlay(trackId: Long) = withContext(Dispatchers.IO) {
        trackDao.recordPlay(trackId, System.currentTimeMillis())
    }

    suspend fun removeTrack(trackId: Long) = withContext(Dispatchers.IO) {
        trackDao.deleteTrackById(trackId)
    }
}
