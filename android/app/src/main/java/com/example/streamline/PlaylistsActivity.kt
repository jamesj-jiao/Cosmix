package com.example.streamline

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.streamline.PlaylistsActivity.Companion.partyID
import com.example.streamline.PlaylistsActivity.Companion.token

class PlaylistsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_playlists)

        token = intent.getStringExtra("token")
        partyID = intent.getStringExtra("partyID")

        val playlists: List<Playlist> = AsyncUtils.getPlaylists("spotify", token)

        with(findViewById<RecyclerView>(R.id.playlistRecycler)) {
            layoutManager = LinearLayoutManager(this@PlaylistsActivity)
            setHasFixedSize(true)
            adapter = PlaylistAdapter(this@PlaylistsActivity, playlists)
        }


    }

    companion object {
        lateinit var token: String
        lateinit var partyID: String
    }
}

class PlaylistAdapter(var context: Context, val data: List<Playlist>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder , position: Int) {
        val curr = data[position]
        with(holder as ViewHolder) {
            name.text = curr.name

            itemView.setOnClickListener {
                if (AsyncUtils.add(partyID, curr.id, token)) {
                    val intent = Intent(it.context, PartyActivity::class.java)
                    intent.putExtra("token", token)
                    intent.putExtra(PARTY_ID, partyID)
                    it.context.startActivity(intent)
                } else {
                    Toast.makeText(it.context, "Playlist is empty. Cannot add.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.song_row, parent, false))
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.songName)
    }
}