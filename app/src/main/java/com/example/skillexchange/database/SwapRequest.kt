package com.example.skillexchange.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "swap_requests")
data class SwapRequest(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val requesterId: Int,
    val receiverId: Int,
    val postId: Int,
    val requesterConfirmed: Boolean = false,
    val receiverConfirmed: Boolean = false,
    val isCompleted: Boolean = false
)