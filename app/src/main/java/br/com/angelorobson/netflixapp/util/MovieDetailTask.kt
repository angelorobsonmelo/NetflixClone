package br.com.angelorobson.netflixapp.util

import android.app.ProgressDialog
import android.content.Context
import android.os.AsyncTask
import br.com.angelorobson.netflixapp.model.Movie
import br.com.angelorobson.netflixapp.model.MovieDetail
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.lang.ref.WeakReference
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class MovieDetailTask(private val context: WeakReference<Context>) :
    AsyncTask<String, Void, MovieDetail>() {

    private lateinit var dialog: ProgressDialog
    private var movieDetailLoader: MovieDetailLoader? = null

    fun setMovieDetailLoader(movieDetailLoader: MovieDetailLoader) {
        this.movieDetailLoader = movieDetailLoader
    }

    override fun onPreExecute() {
        super.onPreExecute()
        context.get()?.apply {
            dialog = ProgressDialog.show(this, "Carregando", "", true)
        }
    }

    override fun doInBackground(vararg params: String?): MovieDetail {
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

            val movieDetail = getMovieDetail(JSONObject(jsonAsStrring))
            inStream.close()
            return movieDetail

        } catch (e: Exception) {
            e.printStackTrace()
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        return MovieDetail()
    }

    private fun getMovieDetail(json: JSONObject): MovieDetail {
        val id = json.getInt("id")
        val title = json.getString("title")
        val desc = json.getString("desc")
        val cast = json.getString("cast")
        val converUrl = json.getString("cover_url")

        val movies = arrayListOf<Movie>()

        val moviesArray = json.getJSONArray("movie")

        for (i in 0 until moviesArray.length()) {
            val movie = moviesArray.getJSONObject(i)
            val coverUrlSimilar = movie.getString("cover_url")
            val idSimilar = movie.getInt("id")

            val similar = Movie(idSimilar, coverUrlSimilar)
            movies.add(similar)
        }

        val movie = Movie(
            id,
            converUrl,
            title,
            desc,
            cast
        )

        return MovieDetail(movie, movies)
    }

    override fun onPostExecute(movieDetail: MovieDetail?) {
        super.onPostExecute(movieDetail)
        dialog.dismiss()
        movieDetailLoader?.apply {
            this.onResult(movieDetail)
        }

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

    interface MovieDetailLoader {
        fun onResult(movieDetail: MovieDetail?)
    }
}