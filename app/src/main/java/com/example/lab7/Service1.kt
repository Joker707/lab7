package com.example.lab7

import android.app.Service
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.IBinder
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File
import java.io.InputStream
import java.net.URL
import kotlin.random.Random

const val keyPATH = "PATH"

class Service1 : Service() {

    private var mIcon11: Bitmap? = null
    private var job: Job? = null

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val url = intent?.getStringExtra(keyURL)

        if (url != null) {
            job = CoroutineScope(Dispatchers.IO).launch {
                Log.i("Testing", "Service is running on " +
                        Thread.currentThread().name + " and it's not UI")
                val path = getPathAfterDownload(url)
                Log.i("Testing","Image located in $path")

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
            mIcon11?.compress(Bitmap.CompressFormat.JPEG, 75, it)
        }
        return File(filesDir, name).absolutePath
    }

    override fun onCreate() {
        super.onCreate()
    }

    override fun onDestroy() {
        super.onDestroy()
        job?.cancel()
    }

}
