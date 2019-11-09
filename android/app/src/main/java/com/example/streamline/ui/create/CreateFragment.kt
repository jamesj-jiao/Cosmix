package com.example.streamline.ui.create

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.streamline.*

class CreateFragment : Fragment() {

    private lateinit var createViewModel: CreateViewModel

    private lateinit var partyText: EditText

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        createViewModel =
            ViewModelProviders.of(this).get(CreateViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_create, container, false)
        val textView: TextView = root.findViewById(R.id.text_create)
        createViewModel.text.observe(this, Observer {
            textView.text = it
        })
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        partyText = view.findViewById(R.id.partyText)

        with(view.findViewById<Button>(R.id.partyButton)) {
            text = "CREATE PARTY"

            setOnClickListener {

//                getFacts("AUFF01900128").addOnCompleteListener {
//                    Log.wtf("FACTS", it.result.toString())
//                }
//
//                with(partyText.text.toString()) {
//                    if (isBlank()) {
//                        partyText.error = "Must enter a party ID!"
//                    } else if (contains(" ")) {
//                        partyText.error = "Party ID cannot contain whitespace"
//                    } else if (!matches(Regex("^[a-zA-Z0-9]*$"))) {
//                        partyText.error = "Party ID must be alphanumeric"
//                    } else {
//                        checkIfMixExists(this).addOnCompleteListener {
//                            Log.wtf("RESULT", it.result.toString())
//                            if (getDict(it.result as String)["result"] as Boolean) {
//                                partyText.error = "Party already exists!"
//                            } else {
//                                createMix(this).addOnCompleteListener {
//                                    val partyIntent = Intent(context, PartyActivity::class.java)
//                                    partyIntent.putExtra(PARTY_ID, this)
//                                    startActivity(partyIntent)
//                                }
//                            }
//                        }
//                    }
//                }


                val partyID: String = partyText.text.toString()
                if (partyID.isBlank()) {
                    partyText.error = "Must enter a party ID!"
                } else if (partyID.contains(" ")) {
                    partyText.error = "Party ID cannot contain whitespace"
                } else if (!partyID.matches(Regex("^[a-zA-Z0-9]*$"))) {
                    partyText.error = "Party ID must be alphanumeric"
                } else if (AsyncUtils.checkParty(partyID)) {
                    partyText.error = "Party already exists!"
                } else {
                    AsyncUtils.newParty(partyID)
                    val partyIntent = Intent(context, PartyActivity::class.java)
                    partyIntent.putExtra(PARTY_ID, partyID)
                    startActivity(partyIntent)
                }
            }
        }
    }
}