package br.com.angelorobson.netflixapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import br.com.angelorobson.netflixapp.model.Category
import br.com.angelorobson.netflixapp.model.Movie
import br.com.angelorobson.netflixapp.util.CategoryTask
import br.com.angelorobson.netflixapp.util.ImageDownloderTask
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.ref.WeakReference

class MainActivity : AppCompatActivity(), CategoryTask.CategoryLoader {


    private val mainAdapter = MainAdapter(mutableListOf())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val recyclerview = recycler_view_main
        with(recyclerview) {
            layoutManager =
                LinearLayoutManager(this@MainActivity, LinearLayoutManager.VERTICAL, false)
            adapter = mainAdapter
        }


        val categoryTask = CategoryTask(WeakReference(this))
        categoryTask.setCategoryLoader(this)
        categoryTask.execute("https://tiagoaguiar.co/api/netflix/home")
    }

    inner class CategoryHolder(view: View) : RecyclerView.ViewHolder(view) {

        val textViewTitle: TextView = view.findViewById(R.id.text_view_title)
        val recyclerView: RecyclerView = view.findViewById(R.id.recycler_view_movie)

    }

    inner class MainAdapter(private var categories: MutableList<Category>) :
        RecyclerView.Adapter<CategoryHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryHolder {
            return CategoryHolder(
                layoutInflater.inflate(R.layout.category_item, parent, false)
            )
        }

        override fun getItemCount(): Int = categories.size

        override fun onBindViewHolder(holder: CategoryHolder, position: Int) {
            val category = categories[position]
            holder.textViewTitle.text = category.name
            holder.recyclerView.adapter = MovieAdapter(category.movies)
            holder.recyclerView.layoutManager =
                LinearLayoutManager(baseContext, LinearLayoutManager.HORIZONTAL, false)
        }

        fun setCategories(categories: List<Category>) {
            this.categories.clear()
            this.categories.addAll(categories)
        }

    }


    inner class MovieAdapter(val movies: List<Movie>) : OnItemClickListener,
        RecyclerView.Adapter<MoviewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoviewHolder {
            return MoviewHolder(
                layoutInflater.inflate(R.layout.movie_item, parent, false),
                this
            )
        }

        override fun getItemCount(): Int = movies.size

        override fun onBindViewHolder(holder: MoviewHolder, position: Int) {
            val movie = movies[position]
            val imageDownloderTask = ImageDownloderTask(WeakReference(holder.imageView))
            val bitmapCache = Application.getBitmapFromMemCache(movie.coverUrl)

            if (bitmapCache != null && movie.coverUrl == bitmapCache.url) {
                holder.imageView.setImageBitmap(bitmapCache.bitmap)
                return
            }

            imageDownloderTask.execute(movie)
        }

        override fun onclick(position: Int) {
            val movie = movies[position]

            if (movie.id <= 3) {
                Intent(this@MainActivity, MovieActivity::class.java).apply {
                    putExtra("id", movie.id)
                    startActivity(this)
                }
            }
        }

    }

    inner class MoviewHolder(view: View, private val onItemClickListener: OnItemClickListener) :
        RecyclerView.ViewHolder(view) {

        val imageView: ImageView = view.findViewById(R.id.image_view)

        init {
            imageView.setOnClickListener {
                onItemClickListener.onclick(adapterPosition)
            }
        }
    }

    override fun onResult(categories: List<Category>) {
        mainAdapter.setCategories(categories)
        mainAdapter.notifyDataSetChanged()
    }

    interface OnItemClickListener {
        fun onclick(position: Int)
    }
}
