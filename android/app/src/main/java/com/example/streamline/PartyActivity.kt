package com.example.streamline

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.spotify.sdk.android.authentication.AuthenticationClient
import com.spotify.sdk.android.authentication.AuthenticationRequest
import com.spotify.sdk.android.authentication.AuthenticationResponse
import android.view.ViewGroup
import android.view.LayoutInflater
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.view.View


class PartyActivity : AppCompatActivity() {

    // recycler stuff
    lateinit var recycler: RecyclerView
    lateinit var adapter: Adapter
    lateinit var linearManager: RecyclerView.LayoutManager

    lateinit var partyId: String
    lateinit var authToken: String

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

            var viewInflated = LayoutInflater.from(this).inflate(R.layout.text_dialog, findViewById(android.R.id.content), false);
            val input: EditText = viewInflated.findViewById(R.id.input)
            builder.setView(viewInflated)

            builder.setPositiveButton("OK", DialogInterface.OnClickListener { dialog, which ->
                Log.d("LOG", input.getText().toString())
            })
            builder.setNegativeButton("Cancel", DialogInterface.OnClickListener() { dialog, which ->
                dialog.cancel()
            })

            builder.show();
        }
        initRecycler()
    }

    fun initRecycler() {
        val recycler = findViewById<RecyclerView>(R.id.recycler)
        recycler.layoutManager = LinearLayoutManager(this)
        adapter = Adapter(this)
        recycler.adapter = adapter
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data);

        // Check if result comes from the correct activity
        if (requestCode == REQUEST_CODE) {
            val response = AuthenticationClient.getResponse(resultCode, data)

            when (response.type) {
                AuthenticationResponse.Type.ERROR -> Log.println(Log.ERROR, "auth", "ERROR LOGGING IN!")
                AuthenticationResponse.Type.TOKEN ->  authToken = response.accessToken
            }
        }
    }
}