package com.yigit.notesdemo.view

import java.io.Serializable


data class NoteArguments(
    val edit: Int? = null,
    val id: Int? = null,
    val title: String? = null,
    val text: String? = null,
    val priority: Int? = null
) : Serializable