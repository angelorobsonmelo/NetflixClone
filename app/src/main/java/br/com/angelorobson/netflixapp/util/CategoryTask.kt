package br.com.angelorobson.netflixapp.util

import android.app.ProgressDialog
import android.content.Context
import android.os.AsyncTask
import br.com.angelorobson.netflixapp.model.Category
import br.com.angelorobson.netflixapp.model.Movie
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.lang.ref.WeakReference
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class CategoryTask(private val context: WeakReference<Context>) :
    AsyncTask<String, Void, List<Category>>() {

    private lateinit var dialog: ProgressDialog
    private var categoryLoader: CategoryLoader? = null

    fun setCategoryLoader(categoryLoader: CategoryLoader) {
        this.categoryLoader = categoryLoader
    }

    // main-thread
    override fun onPreExecute() {
        super.onPreExecute()
        context.get()?.apply {
            dialog = ProgressDialog.show(this, "Carregando", "", true)
        }
    }

    // thread -- background
    override fun doInBackground(vararg params: String?): List<Category> {
        val url = params[0]

        try {
            val requestUrl = URL(url)
            val urlConnection = requestUrl.openConnection() as HttpsURLConnection
            urlConnection.readTimeout = 2000
            urlConnection.connectTimeout = 2000

            val responseCode = urlConnection.responseCode
            if (responseCode > 400) {
                throw IOException("Erro de comunicação do servidor")
            }

            val inputStream = urlConnection.inputStream

            val inStream = BufferedInputStream(inputStream)

            val jsonAsStrring = toString(inStream)

            val categories = getCategories(JSONObject(jsonAsStrring))
            inStream.close()
            return categories

        } catch (e: Exception) {
            e.printStackTrace()
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        return mutableListOf()
    }

    private fun toString(inp: BufferedInputStream): String {
        val bytes = ByteArray(1024) // init param issue
        val baos = ByteArrayOutputStream()

        var bytesRead: Int
        while (inp.read(bytes).also { bytesRead = it } >= 0) {
            baos.write(bytes, 0, bytesRead)
        }

        return String(baos.toByteArray())
    }

    private fun getCategories(json: JSONObject): List<Category> {
        val categories = arrayListOf<Category>()
        val categoryArray = json.getJSONArray("category")

        for (i in 0 until categoryArray.length()) {
            val category = categoryArray.getJSONObject(i)
            val title = category.getString("title")

            val movies = arrayListOf<Movie>()
            val movieArray = category.getJSONArray("movie")

            for (j in 0 until movieArray.length()) {
                val movie = movieArray.getJSONObject(j)
                val coverUrl = movie.getString("cover_url")
                val id = movie.getInt("id")
                val movieJ = Movie(id, coverUrl)
                movies.add(movieJ)
            }

            val categoryObj = Category(
                title,
                movies
            )

            categories.add(categoryObj)
        }

        return categories
    }

    // main-thread
    override fun onPostExecute(result: List<Category>) {
        super.onPostExecute(result)
        categoryLoader?.apply {
            this.onResult(result)
        }

        dialog.dismiss()
    }


    interface CategoryLoader {
        fun onResult(categories: List<Category>)
    }
}