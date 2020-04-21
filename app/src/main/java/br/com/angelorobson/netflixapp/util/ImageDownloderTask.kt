package br.com.angelorobson.netflixapp.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.LayerDrawable
import android.os.AsyncTask
import android.widget.ImageView
import androidx.core.content.ContextCompat
import br.com.angelorobson.netflixapp.R
import java.lang.Exception
import java.lang.ref.WeakReference
import java.net.HttpURLConnection
import java.net.URL

class ImageDownloderTask(private val weakReference: WeakReference<ImageView>) :
    AsyncTask<String, Void, Bitmap>() {


    private var shadowEnabled = false

    fun setShadowEnable(shadowEnabled: Boolean) {
        this.shadowEnabled = shadowEnabled
    }

    override fun doInBackground(vararg params: String?): Bitmap {
        val urlImg = params[0]

        var urlConnection: HttpURLConnection? = null
        try {
            val url = URL(urlImg)

            urlConnection = url.openConnection() as HttpURLConnection

            val statusCode = urlConnection.responseCode
            if (statusCode != 200) {
                throw  Exception()
            }

            val inputStream = urlConnection.inputStream

            if (inputStream != null) {
                return BitmapFactory.decodeStream(inputStream)
            }


        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            urlConnection?.apply {
                this.disconnect()
            }
        }

        return BitmapFactory.decodeFile("")
    }

    override fun onPostExecute(result: Bitmap?) {
        var bitmap = result
        super.onPostExecute(bitmap)
        if (isCancelled) {
            return
        }

        val imageView = weakReference.get()
        if (imageView != null && bitmap != null) {

            if (shadowEnabled) {
                val drawable = ContextCompat.getDrawable(
                    imageView.context,
                    R.drawable.shadows
                ) as LayerDrawable

                val bitmapDrawable = BitmapDrawable(bitmap)
                drawable.setDrawableByLayerId(R.id.cover_drawble, bitmapDrawable)
                imageView.setImageDrawable(drawable)
            } else {
                if (bitmap.width < imageView.width || bitmap.height < imageView.height) {
                    val matrix = Matrix()
                    val scaleWidth = imageView.width.toFloat() / bitmap.width.toFloat()
                    val scaleHeigth = imageView.height.toFloat() / bitmap.height.toFloat()
                    matrix.postScale(scaleWidth, scaleHeigth)

                    bitmap =
                        Bitmap.createBitmap(
                            bitmap,
                            0,
                            0,
                            bitmap.width,
                            bitmap.height,
                            matrix,
                            false
                        )
                }
            }



            imageView.setImageBitmap(bitmap)
        }
    }
}