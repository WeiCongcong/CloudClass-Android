package io.agora.edu.extensions.extapp.vote

data class ReplyItem(
    val startTime: String,
    val replyTime: String,
    val answer: Array<String>
)

data class ResultItem (
    val choice: String,
    var count: Int,
    var proportion: Int
)
