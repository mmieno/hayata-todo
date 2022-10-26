package com.example.hayatatodolist.model

data class Todo(
    var listId: Int,
    var listTitle: String,
    var no: Int,
    var title: String,
    var yotei: Boolean,
    var jisseki: Boolean,
    var kakunin: Boolean,
    var defaultYotei: Boolean
)