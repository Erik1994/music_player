package com.example.myspotify.ui.fragment

import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import android.view.View
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.RequestManager
import com.example.myspotify.R
import com.example.myspotify.data.entity.Song
import com.example.myspotify.exoplayer.isPlaying
import com.example.myspotify.exoplayer.toSong
import com.example.myspotify.ui.viewmodel.MainViewModel
import com.example.myspotify.ui.viewmodel.SongViewModel
import com.example.myspotify.util.Status
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_song.*
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject


@AndroidEntryPoint
class SongFragment: Fragment(R.layout.fragment_song) {

    @Inject
    lateinit var glide: RequestManager

    private lateinit var mainViewModel: MainViewModel

    private val songViewModel: SongViewModel by viewModels()

    private var playbackState: PlaybackStateCompat? = null

    private var currentlyPlayingSong: Song? = null

    private var shouldUpdateSeekbar = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainViewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
        subscribeToObservers()
        setClickListeners()
    }

    private fun setClickListeners() {
        ivPlayPauseDetail.setOnClickListener {
            currentlyPlayingSong?.let {
                mainViewModel.playOrToggleSong(it, true)
            }
        }

        ivSkipPrevious.setOnClickListener {
            mainViewModel.skipToPreviousSong()
        }

        ivSkip.setOnClickListener {
            mainViewModel.skipToNextSong()
        }


        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if(fromUser) {
                    setCurrentPlayerTimeToTextView(progress.toLong())
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                shouldUpdateSeekbar = false
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                seekBar?.let {
                    mainViewModel.seekTo(it.progress.toLong())
                    shouldUpdateSeekbar = true
                }
            }
        })
    }

    private fun updateTitleAndSongImage(song: Song) {
        val title = "${song.title} - ${song.subtitle}"
        tvSongName.text = title
        glide.load(song.imageUrl).into(ivSongImage)
    }

    private fun subscribeToObservers() {
        mainViewModel.mediaItems.observe(viewLifecycleOwner) {
            it?.let {result ->
                when(result.status) {
                    Status.SUCCESS -> {
                        result.data?.let {songs ->
                            if(currentlyPlayingSong != null && songs.isNotEmpty()) {
                                currentlyPlayingSong = songs[0]
                            }
                        }
                    }
                    else -> Unit
                }
            }
        }

        mainViewModel.currentlyPlayingSong.observe(viewLifecycleOwner) {
            if(it == null) return@observe
            currentlyPlayingSong = it.toSong()
            updateTitleAndSongImage(currentlyPlayingSong!!)
        }

        mainViewModel.playbackState.observe(viewLifecycleOwner) {
            playbackState = it
            ivPlayPauseDetail.setImageResource(
                if(playbackState?.isPlaying == true) R.drawable.ic_pause else R.drawable.ic_play
            )
            seekBar.progress = it?.position?.toInt() ?: 0
        }

        songViewModel.currentPlayerPosition.observe(viewLifecycleOwner) {
            if(shouldUpdateSeekbar) {
                seekBar.progress = it.toInt()
                setCurrentPlayerTimeToTextView(it)
            }
        }

        songViewModel.curSongDuration.observe(viewLifecycleOwner) {
            seekBar.max = it.toInt()
            val dateFormat = SimpleDateFormat("mm:ss", Locale.getDefault())
            tvSongDuration.text = dateFormat.format(it)
        }
    }

    private fun setCurrentPlayerTimeToTextView(milis: Long) {
        val dateFormat = SimpleDateFormat("mm:ss", Locale.getDefault())
        tvCurTime.text = dateFormat.format(milis)
    }




}