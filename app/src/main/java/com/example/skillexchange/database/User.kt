package com.example.skillexchange.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val skill: String,
    val phone: String,
    val trustScore: Int = 0
)