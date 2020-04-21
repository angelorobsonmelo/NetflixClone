package br.com.angelorobson.netflixapp.model

data class Movie(
    val id: Int,
    val coverUrl: String,
    val title: String,
    val desc: String,
    val cast: String
) {
    constructor() : this(0, "", "", "", "")
    constructor(id: Int, coverUrl: String) : this(id, coverUrl, "", "", "")
}