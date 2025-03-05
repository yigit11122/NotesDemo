package com.yigit.notesdemo.model

import android.webkit.WebSettings.RenderPriority
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Note(
    @ColumnInfo(name = "Title")
    var title: String,

    @ColumnInfo(name = "Text")
    var text: String,

    @ColumnInfo(name="Priority")
    var priority : Int = 0

) {
    @PrimaryKey(autoGenerate = true)
    var id = 0
}