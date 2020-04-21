package br.com.angelorobson.netflixapp.model

data class MovieDetail(
    val movie: Movie,
    val moviesSimilar: List<Movie>
) {
    constructor(): this(Movie(), listOf())
}
