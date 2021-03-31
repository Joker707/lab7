package com.example.lab7

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class Receiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {

        val path = intent?.getStringExtra(keyPATH)

        context?.startActivity(Intent(context, ActivityForReceive::class.java).putExtra(keyPATH, path))
    }
}