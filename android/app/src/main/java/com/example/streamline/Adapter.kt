package com.example.streamline

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class Adapter(var context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var songs : MutableList<Song> = mutableListOf<Song>()

    override fun getItemCount() = songs.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder , position: Int) {
        val curr = songs[position]
        with(holder as ViewHolder) {
            name.text = curr.name
            artist.text = curr.artist
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.song_row, parent, false))
    }

    fun updateData(newData: Set<Song>) {
        songs = newData.toMutableList()
        notifyDataSetChanged()
    }

    fun addSong(song: Song) {
        songs.add(song)
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var name = itemView.findViewById<TextView>(R.id.songName)
        var artist = itemView.findViewById<TextView>(R.id.songArtist)
    }
}
