package com.example.lab7

import android.content.IntentFilter
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.receive_activity.*

class ActivityForReceive : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val path = intent.getStringExtra(keyPATH)


        val filter = IntentFilter()
        filter.addAction("IMAGE_DOWNLOADED")
        registerReceiver(Receiver(), filter)
        if (path != null) {
            receivers_url.text = "Our image's path is $path"
        }
        setContentView(R.layout.receive_activity)
    }
}