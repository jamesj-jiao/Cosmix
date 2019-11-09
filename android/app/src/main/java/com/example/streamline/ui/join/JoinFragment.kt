package com.example.streamline.ui.join

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

class JoinFragment : Fragment() {

    private lateinit var joinViewModel: JoinViewModel
    private lateinit var partyText: EditText

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        joinViewModel =
            ViewModelProviders.of(this).get(JoinViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_join, container, false)
        val textView: TextView = root.findViewById(R.id.text_join)
        joinViewModel.text.observe(this, Observer {
            textView.text = it
        })
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        partyText = view.findViewById(R.id.partyText)

        with(view.findViewById<Button>(R.id.partyButton)) {
            text = "JOIN PARTY"

            setOnClickListener {
                with(partyText.text.toString()) {
                    if (isBlank()) {
                        partyText.error = "Must enter a party ID!"
                    } else if (contains(" ")) {
                        partyText.error = "Party ID cannot contain whitespace"
                    } else if (!matches(Regex("^[a-zA-Z0-9]*$"))) {
                        partyText.error = "Party ID must be alphanumeric"
                    }  else if (!AsyncUtils.checkParty(this)) {
                        partyText.error = "Party doesn't exist!"
                    } else {
                        val partyIntent = Intent(context, PartyActivity::class.java)
                        partyIntent.putExtra(PARTY_ID, this)
                        startActivity(partyIntent)
                    }
                }
            }
        }
    }
}