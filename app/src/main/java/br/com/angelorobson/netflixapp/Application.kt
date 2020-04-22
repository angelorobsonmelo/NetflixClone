package br.com.angelorobson.netflixapp

import android.app.Application
import android.graphics.Bitmap
import android.util.LruCache

private var memoryCache: LruCache<String, Bitmap>? = null

class Application : Application() {


    override fun onCreate() {
        super.onCreate()

        val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()

        // Use 1/8th of the available memory for this memory cache.
        val cacheSize = maxMemory / 8

        memoryCache = object : LruCache<String, Bitmap>(cacheSize) {

            override fun sizeOf(key: String, bitmap: Bitmap): Int {
                // The cache size will be measured in kilobytes rather than
                // number of items.
                return bitmap.byteCount / 1024
            }
        }
    }


    companion object {
        fun addBitmapToMemoryCache(key: String?, bitmap: Bitmap?) {
            if (getBitmapFromMemCache(key) == null) {
                memoryCache?.put(key, bitmap)
            }
        }

        fun getBitmapFromMemCache(key: String?): Bitmap? {
            return memoryCache?.get(key)
        }
    }


}