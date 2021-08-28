package com.sample.imagery

import android.content.Context
import android.os.Environment
import java.io.File

class FileCache(context: Context) {

    private var cacheDir: File? = null

    init {
        cacheDir = if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED)
            context.getExternalFilesDir("ImageLoaderSample")
        else
            context.cacheDir
        if (!cacheDir!!.exists())
            cacheDir!!.mkdirs()
    }

    fun getFile(url: String): File {
        val filename = url.hashCode().toString()
        return File(cacheDir, filename)
    }

    fun clear() {
        val files = cacheDir!!.listFiles() ?: return
        for (f in files)
            f.delete()
    }

}