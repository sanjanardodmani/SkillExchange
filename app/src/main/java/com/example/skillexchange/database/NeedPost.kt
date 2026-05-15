package com.example.skillexchange.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "need_posts")
data class NeedPost(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: Int,
    val userName: String,
    val skillNeeded: String,
    val offerSkill: String,
    val description: String,
    val isOpen: Boolean = true
)