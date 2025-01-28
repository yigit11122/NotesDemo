package com.yigit.notesdemo.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Note(
    @ColumnInfo(name = "Title")
    var title: String,

    @ColumnInfo(name = "Text")
    var text: String
) {
    @PrimaryKey(autoGenerate = true)
    var id = 0
}