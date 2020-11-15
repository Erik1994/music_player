package com.example.myspotify.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.RequestManager
import com.example.myspotify.R
import com.example.myspotify.data.entity.Song
import com.example.myspotify.exoplayer.toSong
import com.example.myspotify.ui.viewmodel.MainViewModel
import com.example.myspotify.ui.viewmodel.SongViewModel
import com.example.myspotify.util.Status
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_song.*
import javax.inject.Inject


@AndroidEntryPoint
class SongFragment: Fragment(R.layout.fragment_song) {

    @Inject
    lateinit var glide: RequestManager

    private lateinit var mainViewModel: MainViewModel

    private val songViewModel: SongViewModel by viewModels()

    private var currentlyPlayingSong: Song? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainViewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
        subscribeToObservers()
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
    }




}