package com.example.streamline

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.spotify.sdk.android.authentication.AuthenticationClient
import com.spotify.sdk.android.authentication.AuthenticationRequest
import com.spotify.sdk.android.authentication.AuthenticationResponse

class LoginActivity : AppCompatActivity() {

    val REQUEST_CODE = 1337

    val CLIENT_ID = "2fd46a7902e043e4bcb8ccda3d1381b2"
    val REDIRECT_URI = "http://com.example.streamline/callback"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
    }

    private fun spotifyLogin() {
        val builder = AuthenticationRequest.Builder(CLIENT_ID, AuthenticationResponse.Type.TOKEN, REDIRECT_URI)

        builder.setScopes(arrayOf("playlist-read-private", "playlist-modify-private"))
        val request = builder.build()

        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Check if result comes from the correct activity
        if (requestCode == REQUEST_CODE) {
            val response = AuthenticationClient.getResponse(resultCode, data)

            when (response.type) {
                // Response was successful and contains auth token
                AuthenticationResponse.Type.TOKEN -> {
                    Log.println(Log.INFO, "auth","LOGGED IN!")
                    Log.println(Log.INFO,"auth", response.accessToken)
                }

                // Auth flow returned an error
                AuthenticationResponse.Type.ERROR -> Log.println(Log.ERROR, "auth", "ERROR LOGGING IN!")
                // Handle error response

                // Most likely auth flow was cancelled
            }
        }
    }
}
