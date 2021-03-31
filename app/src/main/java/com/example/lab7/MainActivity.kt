package com.example.lab7

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

const val keyURL = "URL"

class MainActivity : AppCompatActivity() {
    private val url = "https://singulartm.com/wp-content/uploads/2020/10/Registro-marca-de-la-UE.jpg"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var messenger = Messenger(HandlerActivity())
        bindService(Intent(this, Messenger3::class.java), connection, Context.BIND_AUTO_CREATE)

        button_start.setOnClickListener {
            startService(Intent(this, Service1::class.java).putExtra(keyURL, url))
        }
        button_bind.setOnClickListener {
            Message.obtain().apply {
                obj = url
                replyTo = messenger
                what = 1
                connection.serviceMessenger?.send(this)
            }
        }
    }

    @SuppressLint("HandlerLeak")
    inner class HandlerActivity : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                1 -> message.text = msg.obj.toString()
                else -> super.handleMessage(msg)
            }

        }
    }

    private val connection = object : ServiceConnection {
        var serviceMessenger : Messenger? = null

        override fun onServiceConnected(className: ComponentName?, service: IBinder?) {
            serviceMessenger = Messenger(service)
        }

        override fun onServiceDisconnected(className: ComponentName?) {
            serviceMessenger = null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(connection)
    }
}