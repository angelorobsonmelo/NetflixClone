package br.com.angelorobson.netflixapp

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import br.com.angelorobson.netflixapp.model.Movie
import br.com.angelorobson.netflixapp.model.MovieDetail
import br.com.angelorobson.netflixapp.util.ImageDownloderTask
import br.com.angelorobson.netflixapp.util.MovieDetailTask
import kotlinx.android.synthetic.main.activity_movie.*
import kotlinx.android.synthetic.main.movie_item_similar.view.*
import java.lang.ref.WeakReference

class MovieActivity : AppCompatActivity(), MovieDetailTask.MovieDetailLoader {

    private lateinit var txtTitle: TextView
    private lateinit var txtDesc: TextView
    private lateinit var txtCast: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var imageCover: ImageView

    private val movieAdapter = MovieAdapter(mutableListOf())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_movie)

        imageCover = image_view_cover
        txtTitle = text_view_title
        txtDesc = text_view_desc
        txtCast = text_view_cast


        recyclerView = recycler_view_similar

        val toolbar = toolbar
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24dp)
        supportActionBar?.title = null

        with(recyclerView) {
            adapter = movieAdapter
            layoutManager = GridLayoutManager(this@MovieActivity, 3)
        }

        intent.extras?.apply {
            val id = get("id")
            val movideDetailTask = MovieDetailTask(WeakReference(this@MovieActivity))
            movideDetailTask.setMovieDetailLoader(this@MovieActivity)
            movideDetailTask.execute("https://tiagoaguiar.co/api/netflix/$id")
        }
    }


    inner class MovieAdapter(
        private val movies: MutableList<Movie>
    ) :
        RecyclerView.Adapter<MoviewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoviewHolder {
            return MoviewHolder(
                layoutInflater.inflate(R.layout.movie_item_similar, parent, false)
            )
        }

        override fun getItemCount(): Int = movies.size

        override fun onBindViewHolder(holder: MoviewHolder, position: Int) {
            val movie = movies[position]
            holder.bind(movie)
        }

        fun setMovies(moviesSimilar: List<Movie>) {
            movies.clear()
            movies.addAll(moviesSimilar)
        }

    }

    inner class MoviewHolder(view: View) :
        RecyclerView.ViewHolder(view) {

        fun bind(item: Movie) {
            with(itemView) {
                val imageDownloderTask = ImageDownloderTask(WeakReference(image_view))
                val bitmapCache = Application.getBitmapFromMemCache(item.coverUrl)

                if (bitmapCache != null && item.coverUrl == bitmapCache.url) {
                    image_view.setImageBitmap(bitmapCache.bitmap)
                    return
                }

                imageDownloderTask.execute(item)
            }
        }
    }

    override fun onResult(movieDetail: MovieDetail?) {
        movieDetail?.apply {
            txtTitle.text = this.movie.title
            txtDesc.text = this.movie.desc
            txtCast.text = this.movie.cast

            movieAdapter.setMovies(this.moviesSimilar)
            movieAdapter.notifyDataSetChanged()

            val il = ImageDownloderTask(WeakReference(imageCover))
            il.setShadowEnable(true)

            val bitmapCache = Application.getBitmapFromMemCache(this.movie.coverUrl)
            if (bitmapCache != null && this.movie.coverUrl == bitmapCache.url) {
                imageCover.setImageBitmap(bitmapCache.bitmap)
                return
            }

            il.execute(this.movie)
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }

        return super.onOptionsItemSelected(item)
    }


}

