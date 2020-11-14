package com.example.myspotify.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import com.bumptech.glide.RequestManager
import com.example.myspotify.R
import com.example.myspotify.adapters.SwipeSongAdapter
import com.example.myspotify.data.entity.Song
import com.example.myspotify.exoplayer.toSong
import com.example.myspotify.ui.viewmodel.MainViewModel
import com.example.myspotify.util.Status
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val mainViewModel: MainViewModel by viewModels()

    @Inject
    lateinit var glide: RequestManager

    @Inject
    lateinit var swipeSongAdapter: SwipeSongAdapter

    private var currentlyPlayingSong: Song? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        subscribeToObservers()
        vpSong.adapter = swipeSongAdapter
    }

    private fun switchViewPagerToCurrentSong(song: Song) {
        val newItemIndex = swipeSongAdapter.songs.indexOf(song)
        if (newItemIndex >= 0) {
            vpSong.currentItem = newItemIndex
            currentlyPlayingSong = song
        }
    }

    private fun subscribeToObservers() {
        mainViewModel.mediaItems.observe(this) {
            it?.let { result ->
                when (result.status) {
                    Status.SUCCESS -> {
                        result.data?.let { songs ->
                            swipeSongAdapter.songs = songs
                            if (songs.isNotEmpty()) {
                                glide.load(currentlyPlayingSong ?: songs[0].imageUrl)
                                    .into(ivCurSongImage)
                            }
                            switchViewPagerToCurrentSong(currentlyPlayingSong ?: return@observe)
                        }
                    }
                    Status.ERROR -> Unit
                    Status.LODAING -> Unit
                }
            }
        }

        mainViewModel.currentlyPlayingSong.observe(this) {
            if(it == null) return@observe
            currentlyPlayingSong = it.toSong()
            glide.load(currentlyPlayingSong.imageUrl)
                .into(ivCurSongImage)
            switchViewPagerToCurrentSong(currentlyPlayingSong ?: return@observe)

        }
    }
}