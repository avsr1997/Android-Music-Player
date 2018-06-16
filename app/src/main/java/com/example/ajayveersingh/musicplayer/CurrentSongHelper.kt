package com.example.ajayveersingh.musicplayer

class CurrentSongHelper {
    var songArtist: String? = null
    var songTitle: String? = null
    var songPath: String? = null
    var songID: Long = 0
    var currentPosition: Int = 0
    var isplaying: Boolean = false
    var isloop: Boolean = false
    var isshuffle: Boolean = false
    var trackPosition: Int = 0
}