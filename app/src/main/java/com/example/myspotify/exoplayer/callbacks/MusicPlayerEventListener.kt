package com.example.myspotify.exoplayer.callbacks

import android.widget.Toast
import com.example.myspotify.exoplayer.service.MusicService
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.material.snackbar.Snackbar

class MusicPlayerEventListener (
        private val musicService: MusicService
) : Player.EventListener {
    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        super.onPlayerStateChanged(playWhenReady, playbackState)
        if(playbackState == Player.STATE_READY && !playWhenReady) {
            musicService.stopForeground(false)
        }
    }

    override fun onPlayerError(error: ExoPlaybackException) {
        super.onPlayerError(error)
//        Snackbar.make(
//                musicService.application.applicationContext.findViewById(android.R.id.content),
//                "An unknown error accured",
//                Snackbar.LENGTH_LONG
//        ).show()
        Toast.makeText(musicService, "An unknown error accured", Toast.LENGTH_LONG).show()
    }
}