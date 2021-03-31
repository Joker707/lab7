package com.example.lab7

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.os.Messenger
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File
import java.io.InputStream
import java.net.URL
import kotlin.random.Random

class Messenger3 : Service() {

    private var mIcon11: Bitmap? = null
    private var job: Job? = null


    override fun onBind(p0: Intent?): IBinder? {
        return Messenger(HandlerMessenger()).binder
    }

    @SuppressLint("HandlerLeak")
    inner class HandlerMessenger : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                1 -> {
                    val replyTo = msg.replyTo
                    val url = msg.obj.toString()
                    job = CoroutineScope(Dispatchers.IO).launch {
                        val message = Message.obtain(null, 1)
                        message.obj = getPathAfterDownload(url)
                        replyTo.send(message)
                    }
                }
                else -> super.handleMessage(msg)
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val url = intent?.getStringExtra(keyURL)

        if (url != null) {
            job = CoroutineScope(Dispatchers.IO).launch {
                Log.i("Testing", "Service is running on " +
                        Thread.currentThread().name + " which is not UI")
                val path = getPathAfterDownload(url)

                sendBroadcast(Intent("IMAGE_DOWNLOADED").putExtra(keyPATH, path))
            }
        }
        stopSelf()

        return START_NOT_STICKY
    }

    private fun getPathAfterDownload(url : String): String? {
        val name = "file${Random.nextInt(100000)}"
        try {
            val input: InputStream = URL(url).openStream()
            mIcon11 = BitmapFactory.decodeStream(input)
        } catch (e: Exception) {
            Log.e("Error", e.message.toString())
            e.printStackTrace()
        }
        openFileOutput(name, MODE_PRIVATE).use {
            mIcon11?.compress(Bitmap.CompressFormat.JPEG, 100, it)
        }
        return File(filesDir, name).absolutePath
    }


    override fun onDestroy() {
        super.onDestroy()
        job?.cancel()
    }
}