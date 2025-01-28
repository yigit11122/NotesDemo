package com.yigit.notesdemo.roomdb

import androidx.room.Database
import androidx.room.RoomDatabase
import com.yigit.notesdemo.model.Note

@Database(entities = [Note::class], version = 1)
abstract class NoteDB : RoomDatabase() {
    abstract fun NoteDAO(): NoteDAO
}