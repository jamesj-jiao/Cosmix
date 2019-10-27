package com.example.streamline

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
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
    val CLIENT_ID = "2fd46a7902e043e4bcb8ccda3d1381b2"
    val REDIRECT_URI = "http://com.example.streamline/callback"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_party)

        partyId = intent.getStringExtra(PARTY_ID)

        FirebaseFirestore.getInstance().collection(PARTIES).document(partyId)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot?.data != null) {
                    var data = snapshot.data as Map<String, List<String>>
                    initRecycler()
                    adapter.updateData(AsyncUtils.getSongs(data[FILTERED_TRACKS]))
                }
            }

        findViewById<Button>(R.id.spotifylogin).setOnClickListener {
                val builder = AuthenticationRequest.Builder(CLIENT_ID, AuthenticationResponse.Type.TOKEN, REDIRECT_URI)

                builder.setScopes(arrayOf("playlist-read-private", "playlist-read-collaborative", "playlist-modify-private"))
                val request = builder.build()

                AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request)
        }


        findViewById<Button>(R.id.spotifyadd).setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Playlist name")

            var viewInflated = LayoutInflater.from(this).inflate(R.layout.text_dialog, findViewById(android.R.id.content), false)
            val input: EditText = viewInflated.findViewById(R.id.input)
            builder.setView(viewInflated)

            builder.setPositiveButton("OK", DialogInterface.OnClickListener { _, _ ->
                if (authToken != null) {
                    AsyncUtils.save(partyId, input.text.toString(), authToken)
                    Toast.makeText(this@PartyActivity, "Uploaded to Spotify", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@PartyActivity, "Must log in to spotify first!", Toast.LENGTH_SHORT).show()
                }
            })
            builder.setNegativeButton("Cancel", DialogInterface.OnClickListener() { dialog, which ->
                dialog.cancel()
            })

            builder.show();
        }
        initRecycler()
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
        }
    }

}