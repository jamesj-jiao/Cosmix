package com.example.streamline

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.spotify.sdk.android.authentication.AuthenticationClient
import com.spotify.sdk.android.authentication.AuthenticationRequest
import com.spotify.sdk.android.authentication.AuthenticationResponse


class PartyActivity : AppCompatActivity() {

    // recycler stuff
    lateinit var recycler: RecyclerView
    lateinit var adapter: Adapter

    lateinit var partyId: String
    var authToken: String? = null

    val CHOOSE_PLAYLIST_RESULT_CODE = 100
    val REQUEST_CODE = 1337
    val ADD_TO_SPOTIFY = 123
    val CLIENT_ID = "2fd46a7902e043e4bcb8ccda3d1381b2"
    val REDIRECT_URI = "http://com.example.streamline/callback"

    val db = FirebaseFirestore.getInstance()

    lateinit var currListener: ListenerRegistration

    lateinit var lastPlaylistName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_party)

        initRecycler()

        partyId = intent.getStringExtra(PARTY_ID)

        initRecycler()

//        db.collection(PARTIES).document(partyId).get().addOnCompleteListener {
//            if (it.isSuccessful) {
//                val data = it.result?.data as Map<String, List<String>>
//                adapter.updateData(AsyncUtils.getSongs(data[FILTERED_TRACKS]))
//            } else {
//                Log.wtf("FIRESTORE", "GET REQUEST FAILED")
//            }
//        }

        startRealTime()

        findViewById<Button>(R.id.addPlaylist).setOnClickListener {
            getSpotifyToken(REQUEST_CODE)
        }

        findViewById<Button>(R.id.spotifyadd).setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Playlist name")

            var viewInflater = LayoutInflater.from(this).inflate(R.layout.text_dialog, findViewById(android.R.id.content), false)

            val input: EditText = viewInflater.findViewById(R.id.input)

            builder.setView(viewInflater)

            builder.setPositiveButton("OK", DialogInterface.OnClickListener { _, _ ->
                if (authToken != null) {
                    savePlaylistToSpotify(input.text.toString())
                } else {
                    lastPlaylistName = input.text.toString()
                    getSpotifyToken(ADD_TO_SPOTIFY)
                }
            })
            builder.setNegativeButton("Cancel", DialogInterface.OnClickListener() { dialog, which ->
                dialog.cancel()
            })

            builder.show()

            startRealTime()
        }

        findViewById<Button>(R.id.spotifygenre).setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Genre name")

            var viewInflated = LayoutInflater.from(this).inflate(R.layout.text_dialog, findViewById(android.R.id.content), false)
            val input: EditText = viewInflated.findViewById(R.id.input)
            builder.setView(viewInflated)

            builder.setPositiveButton("OK", DialogInterface.OnClickListener { _, _ ->

                class GenTask(val filter: String, val partyId: String, val adapter: Adapter) : AsyncTask<Void, Void, List<String>>() {
                    override fun doInBackground(vararg params: Void?): List<String> {
                        val isrcs: List<String> = genFilter(filter, 5, partyId)

                        return isrcs
                    }

                    override fun onPostExecute(result: List<String>?) {
                        if (result != null) {
                            stopRealTime()
                            adapter.updateData(AsyncUtils.getSongs(result))
                        }
                    }
                }

                GenTask(input.text.toString(), partyId, adapter).execute()

                Toast.makeText(this@PartyActivity, "Generating filtered playlist", Toast.LENGTH_LONG).show()

            })
            builder.setNegativeButton("Cancel", DialogInterface.OnClickListener() { dialog, which ->
                dialog.cancel()
            })

            builder.show()
        }
    }

    fun startRealTime() {
        currListener = db.collection(PARTIES).document(partyId)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot?.data != null) {
                    var data = snapshot.data as Map<String, List<String>>
                    Log.d("BBBB", data.toString())
                    adapter.updateData(AsyncUtils.getSongs(data[FILTERED_TRACKS]))
                }
            }
    }

    fun stopRealTime() {
        currListener.remove()
    }

    fun savePlaylistToSpotify(text: String) {
        val toast = Toast.makeText(this@PartyActivity, "Uploadeding to Spotify", Toast.LENGTH_LONG * Toast.LENGTH_LONG * Toast.LENGTH_LONG)
        toast.show()
        AsyncUtils.save(partyId, text, authToken, toast)
    }

    fun getSpotifyToken(nextStep: Int) {
        val builder = AuthenticationRequest.Builder(CLIENT_ID, AuthenticationResponse.Type.TOKEN, REDIRECT_URI)

        builder.setScopes(arrayOf("playlist-read-private", "playlist-read-collaborative", "playlist-modify-private"))
        val request = builder.build()

        AuthenticationClient.openLoginActivity(this, nextStep, request)
    }

    fun initRecycler() {
        recycler = findViewById<RecyclerView>(R.id.recycler)
        recycler.layoutManager = LinearLayoutManager(this)
        adapter = Adapter(this)
        recycler.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        authToken = intent.getStringExtra("token")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CHOOSE_PLAYLIST_RESULT_CODE) {
            authToken = data!!.getStringExtra("token")
        } else if (requestCode == REQUEST_CODE) {
            val response = AuthenticationClient.getResponse(resultCode, data)

            when (response.type) {
                AuthenticationResponse.Type.ERROR -> Log.println(Log.ERROR, "auth", "ERROR LOGGING IN!")
                AuthenticationResponse.Type.TOKEN ->  startActivityForResult(Intent(this@PartyActivity, PlaylistsActivity::class.java)
                    .putExtra("token", response.accessToken)
                    .putExtra("partyID", partyId)
                    , CHOOSE_PLAYLIST_RESULT_CODE)
            }
        } else if (requestCode == ADD_TO_SPOTIFY) {
            savePlaylistToSpotify(lastPlaylistName)
        }
    }

}