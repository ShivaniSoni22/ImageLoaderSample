package com.sample.imageloadersample

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import okhttp3.*
import org.json.JSONArray
import org.json.JSONException
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerImages: RecyclerView
    private var itemsCount: Int = 20
    private var start: Int = 0
    private lateinit var adapter: RecyclerAdapter
    private var imageUrlList: ArrayList<String> = ArrayList()
    private var paginationList: ArrayList<String> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerImages = findViewById(R.id.recycler_images)

        val layoutManager = GridLayoutManager(this, 2)
        recyclerImages.layoutManager = layoutManager
        adapter = RecyclerAdapter(paginationList, this)
        recyclerImages.adapter = adapter

        recyclerImages.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(@NonNull recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                // number of visible items
                val visibleItemCount = layoutManager.childCount
                // number of items in layout
                val totalItemCount = layoutManager.itemCount
                // the position of first visible item
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                // flag if number of visible items is at the last
                val lastVisibleItemPosition = firstVisibleItemPosition + visibleItemCount
                // flag to know whether to load more
                if (lastVisibleItemPosition >= totalItemCount) {
                    start += 20
                    itemsCount += 20
                    loadMoreData()
                }
            }
        })

        if (isWriteStoragePermissionGranted() && isReadStoragePermissionGranted())
            makeNetworkRequest()
    }

    private fun makeNetworkRequest() {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://shibe.online/api/shibes?count=40")
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                call.cancel()
            }

            override fun onResponse(call: Call, response: Response) {
                val myResponse = response.body?.string()
                runOnUiThread {
                    try {
                        val json = JSONArray(myResponse)
                        imageUrlList.clear()
                        for (i in 0 until json.length()) {
                            imageUrlList.add(json.get(i) as String)
                        }
                        if (imageUrlList.size >= itemsCount) {
                            loadMoreData()
                        } else {
                            paginationList.clear()
                            recyclerImages.adapter = RecyclerAdapter(imageUrlList, this@MainActivity)
                            adapter.notifyDataSetChanged()
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }
            }
        })
    }

    private fun loadMoreData() {
        try {
            if (imageUrlList.size >= itemsCount) {
                for (i in start until itemsCount) {
                    paginationList.add(imageUrlList[i])
                }
                adapter.notifyDataSetChanged()
            }
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
    }

    private fun isWriteStoragePermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                true
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 4
                )
                false
            }
        } else {
            true
        }
    }

    private fun isReadStoragePermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                true
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    2
                )
                false
            }
        } else {
            true
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            2, 4 -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    makeNetworkRequest()
                }
            }
        }
    }

}