package com.yigit.notesdemo.roomdb

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.yigit.notesdemo.model.Note

@Database(entities = [Note::class], version = 2)
abstract class NoteDB : RoomDatabase() {
    abstract fun NoteDAO(): NoteDAO

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE Note ADD COLUMN Priority INTEGER NOT NULL DEFAULT 0")
            }

        }
    }
}