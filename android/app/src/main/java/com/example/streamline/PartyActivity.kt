package com.example.streamline

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

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

        FirebaseFirestore.getInstance().collection(PARTIES).document(partyId)
            .addSnapshotListener { snapshot, e ->
                if (snapshot?.data != null) {
                    var data = snapshot.data as Map<String, List<String>>
                    adapter.updateData(AsyncUtils.getSongs(data.get(ALL_TRACKS)))
                }
            }

        initRecycler()
    }

    fun initRecycler() {
        val recycler = findViewById<RecyclerView>(R.id.recycler)
        recycler.layoutManager = LinearLayoutManager(this)
        adapter = Adapter(this)
        recycler.adapter = adapter
    }
}