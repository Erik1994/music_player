package com.example.myspotify.adapters

import androidx.recyclerview.widget.AsyncListDiffer
import com.example.myspotify.R
import kotlinx.android.synthetic.main.swipe_item.view.*
import javax.inject.Inject

class SwipeSongAdapter @Inject constructor(): BaseSongAdapter(R.layout.swipe_item) {

    override var differ = AsyncListDiffer(this, diffCallback)


    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = songs[position]
        holder.itemView.apply {
            val text = "${song.title} - ${song.subtitle}"
            tvPrimary.text = text

            setOnClickListener {
                onItemClickListener?.invoke(song)
            }
        }
    }


}