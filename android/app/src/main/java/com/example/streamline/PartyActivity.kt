package com.example.streamline

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class PartyActivity : AppCompatActivity() {

    // recycler stuff
    lateinit var recycler: RecyclerView
    lateinit var adapter: Adapter
    lateinit var linearManager: RecyclerView.LayoutManager

    lateinit var partyId: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_party)

        partyId = intent.getStringExtra(PARTY_ID)

        //only watch all tracks
        FirebaseDatabase.getInstance().reference.child(PARTIES).child(partyId).child(ALL_TRACKS).child("SDKFYKS")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    adapter.updateData(
                        readSnapshot(dataSnapshot)
                    )
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })
        initRecycler()
    }

    fun initRecycler() {
        val recycler = findViewById<RecyclerView>(R.id.recycler)
        recycler.layoutManager = LinearLayoutManager(this)
        adapter = Adapter(this)
        recycler.adapter = adapter
    }
}