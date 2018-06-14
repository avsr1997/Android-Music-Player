package com.example.ajayveersingh.musicplayer

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class favorite_database : SQLiteOpenHelper {
    val table_name = "favaorite_database"
    val column_id = "song_id"
    val column_artist = "song_artist"
    val column_title = "song_title"
    val column_data = "song_data"
    var songs_list: ArrayList<Songs>? = null

    object static {
        val database_name = "music_database"
    }

    constructor(context: Context?, name: String?, factory: SQLiteDatabase.CursorFactory?, version: Int) : super(context, name, factory, version)
    constructor(context: Context?) : super(context, static.database_name, null, 1)

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL("CREATE TABLE " + table_name + "( " + column_id + "INTEGER," + column_artist + "STRING,"
                + column_title + "STRING," + column_data + "STRING);")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {

    }

    fun storefavoritesong(id: Int?, artist: String?, songtitle: String?, songpath: String?) {
        val database = this.writableDatabase
        var contentvalues = ContentValues()
        contentvalues.put(column_id, id)
        contentvalues.put(column_artist, artist)
        contentvalues.put(column_title, songtitle)
        contentvalues.put(column_data, songpath)
        database.insert(table_name, null, contentvalues)
        database.close()
    }

    fun retrieve_database(): ArrayList<Songs>? {
        val database = this.readableDatabase
        var query_database = "SELECT * FROM " + table_name
        var cursor = database.rawQuery(query_database, null)
        do {
            var _id = cursor.getInt(cursor.getColumnIndex(column_id))
            var _title = cursor.getString(cursor.getColumnIndex(column_title))
            var _data = cursor.getString(cursor.getColumnIndex(column_data))
            var _artist = cursor.getString(cursor.getColumnIndex(column_artist))
            songs_list?.add(Songs(_id as Long, _title, _artist, _data, 0))
        } while (cursor.moveToNext())
        return songs_list
    }

    fun checkidexists(_id: Int): Boolean {
        var store_id = -1090
        var database = this.readableDatabase
        var query_database = "SELECT * FROM " + table_name + "WHERE SongId = '$_id'"
        var cursor = database.rawQuery(query_database, null)
        if (cursor.moveToFirst()) {
            do {
                store_id = cursor.getInt(cursor.getColumnIndex(column_id))
            } while (cursor.moveToNext())
        } else {
            return false
        }
        return store_id != -1090
    }

    fun deletesong_database(_id: Int) {
        val db = this.writableDatabase
        val delete = db.delete(table_name, column_id + "=" + _id, null)
        db.close()
    }

}