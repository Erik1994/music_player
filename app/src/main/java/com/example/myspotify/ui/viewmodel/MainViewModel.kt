package com.example.myspotify.ui.viewmodel

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_MEDIA_ID
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.myspotify.data.entity.Song
import com.example.myspotify.exoplayer.MusicServiceConnection
import com.example.myspotify.exoplayer.isPlayEnabled
import com.example.myspotify.exoplayer.isPlaying
import com.example.myspotify.exoplayer.isPrepared
import com.example.myspotify.util.Constants.MEDIA_ROOT_ID
import com.example.myspotify.util.Resource

class MainViewModel @ViewModelInject constructor(
    private val musicServiceConnection: MusicServiceConnection
): ViewModel() {

    private val _mediaItems = MutableLiveData<Resource<List<Song>>>()
    val mediaItems: LiveData<Resource<List<Song>>> = _mediaItems

    val isConected = musicServiceConnection.isConnected
    val networkError = musicServiceConnection.networkError
    val currentlyPlayingSong = musicServiceConnection.currentlyPlayingSong
    val playbackState = musicServiceConnection.playBackState

    init {
        _mediaItems.postValue(Resource.loading(null))
        musicServiceConnection.subscribe(MEDIA_ROOT_ID, object: MediaBrowserCompat.SubscriptionCallback() {
            override fun onChildrenLoaded(
                parentId: String,
                children: MutableList<MediaBrowserCompat.MediaItem>
            ) {
                super.onChildrenLoaded(parentId, children)
                val items = children.map {
                    Song(
                        it.description.iconUri.toString(),
                        it.mediaId!!,
                        it.description.mediaUri.toString(),
                        it.description.subtitle.toString(),
                        it.description.title.toString()
                    )
                }
                _mediaItems.postValue(Resource.success(items))
            }
        })
    }

    fun skipToNextSong() {
        musicServiceConnection.transportControl.skipToNext()
    }

    fun skipToPreviousSong() {
        musicServiceConnection.transportControl.skipToPrevious()
    }

    fun seekTo(pos: Long) {
        musicServiceConnection.transportControl.seekTo(pos)
    }

    fun playOrToggleSong(mediaItem: Song, toggle: Boolean = false) {
        val isPrepared = playbackState.value?.isPrepared ?: false
        if(isPrepared && mediaItem.mediaId == currentlyPlayingSong.value?.getString(METADATA_KEY_MEDIA_ID)) {
            playbackState.value?.let {playbackState ->
                when {
                    playbackState.isPlaying -> if(toggle) musicServiceConnection.transportControl.pause()
                    playbackState.isPlayEnabled -> musicServiceConnection.transportControl.play()
                    else -> Unit
                }
            }
        } else {
            musicServiceConnection.transportControl.playFromMediaId(mediaItem.mediaId, null)
        }

    }

    override fun onCleared() {
        super.onCleared()
        musicServiceConnection.unsubscribe(MEDIA_ROOT_ID, object : MediaBrowserCompat.SubscriptionCallback() {})
    }
}