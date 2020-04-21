package br.com.angelorobson.netflixapp.model

data class Category(
    val name: String,
    var movies: List<Movie>
) {
    constructor(name: String) : this(name, listOf())
}