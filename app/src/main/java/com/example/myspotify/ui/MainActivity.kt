package com.example.myspotify.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.RequestManager
import com.example.myspotify.R
import com.example.myspotify.adapters.SwipeSongAdapter
import com.example.myspotify.data.entity.Song
import com.example.myspotify.exoplayer.isPlaying
import com.example.myspotify.exoplayer.toSong
import com.example.myspotify.ui.viewmodel.MainViewModel
import com.example.myspotify.util.Status
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var navController: NavController

    private val mainViewModel: MainViewModel by viewModels()

    @Inject
    lateinit var glide: RequestManager

    @Inject
    lateinit var swipeSongAdapter: SwipeSongAdapter

    private var currentlyPlayingSong: Song? = null

    private var playbackState: PlaybackStateCompat? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if(!this::navController.isInitialized) {
            navController = this.findNavController(R.id.navHostFragment)
        }
        subscribeToObservers()
        vpSong.adapter = swipeSongAdapter
        setClickListeners()
    }

    private fun setClickListeners() {
        ivPlayPause.setOnClickListener {
            currentlyPlayingSong?.let {
                mainViewModel.playOrToggleSong(it, true)
            }
        }


        vpSong.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if(playbackState?.isPlaying == true) {
                    mainViewModel.playOrToggleSong(swipeSongAdapter.songs[position])
                } else {
                    currentlyPlayingSong = swipeSongAdapter.songs[position]
                }
            }
        })


        swipeSongAdapter.setItemClickListener {
            navController.navigate(R.id.globalActionToSOngFragment)
        }

        navController.addOnDestinationChangedListener {_, destination, _ ->
            when(destination.id) {
                R.id.songFragment -> hideBottomBar()
                R.id.homeFragment -> showBottomBar()
                else -> Unit
            }

        }
    }

    override fun onNavigateUp(): Boolean {
        return navController.navigateUp()
    }

    private fun hideBottomBar() {
        ivCurSongImage.isVisible = false
        vpSong.isVisible = false
        ivPlayPause.isVisible = false
    }

    private fun showBottomBar() {
        ivCurSongImage.isVisible = true
        vpSong.isVisible = true
        ivPlayPause.isVisible = true
    }

    private fun switchViewPagerToCurrentSong(song: Song) {
        val newItemIndex = swipeSongAdapter.songs.indexOf(song)
        if (newItemIndex >= 0) {
            vpSong.currentItem = newItemIndex
            currentlyPlayingSong = song
        }
    }

    //calls once when app opens
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
            glide.load(currentlyPlayingSong?.imageUrl)
                .into(ivCurSongImage)
            switchViewPagerToCurrentSong(currentlyPlayingSong ?: return@observe)
        }

        //calls every time when playback state chnages pause, play etc.
        mainViewModel.playbackState.observe(this) {
            playbackState = it
            ivPlayPause.setImageResource(
                if(playbackState?.isPlaying == true) R.drawable.ic_pause else R.drawable.ic_play
            )
        }


        //show error snackbar once. When rotate screen not show snackbar again.
        mainViewModel.isConected.observe(this) {
            it?.getContentIfNotHandled()?.let {result ->
                when(result.status) {
                    Status.ERROR -> Snackbar.make(rootLayout, result.message ?:
                    "An unknown error occured",
                    Snackbar.LENGTH_LONG).show()
                    else -> Unit
                }
            }
        }

        mainViewModel.networkError.observe(this) {
            it?.getContentIfNotHandled()?.let {result ->
                when(result.status) {
                    Status.ERROR -> Snackbar.make(rootLayout, result.message ?:
                    "An unknown error occured",
                        Snackbar.LENGTH_LONG).show()
                    else -> Unit
                }
            }
        }
    }
}