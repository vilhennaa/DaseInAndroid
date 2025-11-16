package com.cotovicz.daseinandroid.data.remote.models

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Creation(
    @get:Exclude var id: String = "",
    val userId: String = "",
    val authorName: String = "",
    val title: String = "",
    val textContent: String? = null,
    val imageUrl: String? = null,
    @ServerTimestamp
    val timestamp: Date? = null,
    val commentCount: Int = 0,
    val tags: List<String> = emptyList()
)

data class Comment(
    @get:Exclude var id: String = "",
    val creationId: String = "",
    val userId: String = "",
    val authorName: String = "",
    val commentText: String = "",
    val parentId: String? = null,
    @ServerTimestamp
    val timestamp: Date? = null
)

data class UserProfile(
    @get:Exclude var uid: String = "",
    val displayName: String = "",
    val bio: String? = null,
    val savedPostIds: List<String> = emptyList()
) {
    constructor() : this("", "", null, emptyList())
}