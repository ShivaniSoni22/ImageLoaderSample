package com.sample.imagery

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Handler
import android.widget.ImageView
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ImageLoader(context:Context) {

    internal var memoryCache = MemoryCache()
    private var fileCache: FileCache = FileCache(context)
    private val imageViews = Collections.synchronizedMap(WeakHashMap<ImageView, String>())
    private var executorService: ExecutorService = Executors.newFixedThreadPool(5)
    private var handler = Handler()

    fun displayImage(url: String, imageView: ImageView, drawableId: Int) {
        imageViews[imageView] = url
        val bitmap = memoryCache[url]
        if (bitmap != null)
            imageView.setImageBitmap(bitmap)
        else {
            queuePhoto(url, imageView)
            imageView.setImageResource(drawableId)
        }
    }

    private fun queuePhoto(url: String, imageView: ImageView) {
        val p = PhotoToLoad(url, imageView)
        executorService.submit(PhotosLoader(p))
    }

    private fun getBitmap(url: String): Bitmap? {
        val f = fileCache.getFile(url)
        val b = decodeFile(f)
        if (b != null)
            return b
        try {
            val imageUrl = URL(url)
            val conn = imageUrl.openConnection() as HttpURLConnection
            conn.connectTimeout = 30000
            conn.readTimeout = 30000
            conn.instanceFollowRedirects = true
            val `is` = conn.inputStream
            val os = FileOutputStream(f)
            copyStream(`is`, os)
            os.close()
            conn.disconnect()
            return decodeFile(f)
        } catch (ex: Throwable) {
            ex.printStackTrace()
            if (ex is OutOfMemoryError)
                memoryCache.clear()
            return null
        }

    }

    private fun decodeFile(f: File): Bitmap? {
        if(f.exists()) {
            try {
                val o = BitmapFactory.Options()
                o.inJustDecodeBounds = true
                val stream1 = FileInputStream(f)
                BitmapFactory.decodeStream(stream1, null, o)
                stream1.close()

                val REQUIRED_SIZE = 140
                var width_tmp = o.outWidth
                var height_tmp = o.outHeight
                var scale = 1
                while (true) {
                    if (width_tmp / 2 < REQUIRED_SIZE || height_tmp / 2 < REQUIRED_SIZE)
                        break
                    width_tmp /= 2
                    height_tmp /= 2
                    scale *= 2
                }

                val o2 = BitmapFactory.Options()
                o2.inSampleSize = scale
                val stream2 = FileInputStream(f)
                val bitmap = BitmapFactory.decodeStream(stream2, null, o2)
                stream2.close()
                return bitmap
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return null
    }

    private inner class PhotoToLoad(var url: String, var imageView: ImageView)

    private inner class PhotosLoader(var photoToLoad: PhotoToLoad) : Runnable {

        override fun run() {
            try {
                if (imageViewReused(photoToLoad))
                    return
                val bmp = getBitmap(photoToLoad.url)
                memoryCache.put(photoToLoad.url, bmp!!)
                if (imageViewReused(photoToLoad))
                    return
                val bd = BitmapDisplayer(bmp, photoToLoad)
                handler.post(bd)
            } catch (th: Throwable) {
                th.printStackTrace()
            }
        }

    }

    private fun imageViewReused(photoToLoad: PhotoToLoad): Boolean {
        val tag = imageViews[photoToLoad.imageView]
        return tag == null || tag != photoToLoad.url
    }

    private inner class BitmapDisplayer(var bitmap: Bitmap?, var photoToLoad: PhotoToLoad) : Runnable {
        override fun run() {
            if (imageViewReused(photoToLoad))
                return
            if (bitmap != null)
                photoToLoad.imageView.setImageBitmap(bitmap)
            else
                photoToLoad.imageView.setBackgroundColor(Color.parseColor("#00000000"))
        }
    }

    fun clearCache() {
        memoryCache.clear()
        fileCache.clear()
    }

    companion object {
        fun copyStream(`is`: InputStream, os: OutputStream) {
            val buffer_size = 1024
            try {
                val bytes = ByteArray(buffer_size)
                while (true) {
                    val count = `is`.read(bytes, 0, buffer_size)
                    if (count == -1)
                        break
                    os.write(bytes, 0, count)
                }
            } catch (ex: Exception) {
            }

        }
    }

}