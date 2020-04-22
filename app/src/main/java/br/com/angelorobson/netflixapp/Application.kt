package br.com.angelorobson.netflixapp

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.util.LruCache
import br.com.angelorobson.netflixapp.model.BitmapCache



class Application : Application() {


    companion object {
        private var memoryCache: LruCache<String, BitmapCache>? = null

        fun addBitmapToMemoryCache(key: String?, bitmap: BitmapCache?) {
            if (getBitmapFromMemCache(key) == null) {
                memoryCache?.put(key, bitmap)
            }
        }

        fun getBitmapFromMemCache(key: String?): BitmapCache? {
            return memoryCache?.get(key)
        }
    }


    override fun onCreate() {
        super.onCreate()

        val memClass = (this.getSystemService(
            Context.ACTIVITY_SERVICE
        ) as ActivityManager).memoryClass

        val maxMemory = 1024 * 1024 * memClass / 8

        // Use 1/8th of the available memory for this memory cache.
        val cacheSize = maxMemory / 8

        memoryCache = object : LruCache<String, BitmapCache>(cacheSize) {
            override fun sizeOf(key: String, bitmapCache: BitmapCache): Int {
                // The cache size will be measured in kilobytes rather than
                // number of items.
                return bitmapCache.bitmap.byteCount / 1024
            }
        }
    }





}