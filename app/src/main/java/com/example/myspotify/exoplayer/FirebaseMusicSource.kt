package com.example.myspotify.exoplayer

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import androidx.core.net.toUri
import com.example.myspotify.data.remote.MusicDatabase
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class FirebaseMusicSource @Inject constructor(private val musicDatabase: MusicDatabase) {
    // list containing meta information about songs
    var songs = emptyList<MediaMetadataCompat>()
    private val onReadyListeners = mutableListOf<(Boolean) -> Unit>()


    suspend fun fetchMediaData = withContext(Dispatchers.IO) {
        state = State.STATE_INITIALIZING
        val allSongs = musicDatabase.getAllSongs()
        songs = allSongs.map {
            MediaMetadataCompat.Builder()
                    .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, it.subtitle)
                    .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, it.mediaId)
                    .putString(MediaMetadataCompat.METADATA_KEY_TITLE, it.title)
                    .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, it.title)
                    .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI, it.imageUrl)
                    .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, it.songUrl)
                    .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, it.imageUrl)
                    .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, it.subtitle)
                    .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_DESCRIPTION, it.subtitle)
                    .build()
        }
        state = State.STATE_INITIALIZED
    }


    fun asMediaItems() = songs.map {song ->
        val desc = MediaDescriptionCompat.Builder()
                .setMediaUri(song.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI))
                .setTitle(song.description.title)
                .setSubtitle(song.description.subtitle)
                .setMediaId(song.description.mediaId)
                .setIconUri(song.description.iconUri)
                .build()
        MediaBrowserCompat.MediaItem(desc, FLAG_PLAYABLE)
    }

    //create plyer list of mediasources that contains information for exoplayer to listen first song then automaticaly second and so on
    fun asMediaSource(dataSourceFactory: DefaultDataSourceFactory): ConcatenatingMediaSource {
        val concatenatingMediaSource = ConcatenatingMediaSource()
        songs.forEach{song ->
            val  mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(song.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI).toUri())
            concatenatingMediaSource.addMediaSource(mediaSource)
        }
        return concatenatingMediaSource
    }



    private var state: State = State.STATE_CREATED
    set(value) {
        if(value == State.STATE_INITIALIZED || value == State.STATE_ERROR) {
            synchronized(onReadyListeners) {
                field = value
                onReadyListeners.forEach {
                    it(state == State.STATE_INITIALIZED)
                }
            }
        } else {
            field = value
        }
    }

    fun whenReady(action: (Boolean)  -> Unit): Boolean {
        if(state == State.STATE_CREATED || state == State.STATE_INITIALIZING) {
            onReadyListeners += action
            return false
        } else {
            action(state == State.STATE_INITIALIZED)
            return true
        }
    }
}

enum class State {
    STATE_CREATED,
    STATE_INITIALIZING,
    STATE_INITIALIZED,
    STATE_ERROR
}