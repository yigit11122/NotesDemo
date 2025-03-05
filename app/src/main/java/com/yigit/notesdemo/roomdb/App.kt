package com.yigit.notesdemo.roomdb

import android.app.Application
import androidx.room.Room

class App : Application() {

    companion object {
        lateinit var noteDB: NoteDB
            private set
    }

    override fun onCreate() {
        super.onCreate()
        noteDB = Room.databaseBuilder(applicationContext, NoteDB::class.java, "Note")
            .addMigrations(NoteDB.MIGRATION_1_2).build()
    }
}