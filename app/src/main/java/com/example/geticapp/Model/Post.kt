package com.example.geticapp.Model

data class Post(
    var postid: String = "",
    var postimage: String = "",
    var publisher: String = "",
    var deskripsi: String = ""
) {
constructor():this("","","","")
}
