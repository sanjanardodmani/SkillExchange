package com.example.skillexchange.database

import androidx.room.*

@Dao
interface NeedPostDao {
    @Insert
    suspend fun insertNeedPost(post: NeedPost)

    @Query("SELECT * FROM need_posts WHERE isOpen = 1")
    suspend fun getAllOpenPosts(): List<NeedPost>

    @Query("SELECT * FROM need_posts WHERE skillNeeded LIKE '%' || :skill || '%' AND isOpen = 1")
    suspend fun filterBySkillNeeded(skill: String): List<NeedPost>

    @Query("UPDATE need_posts SET isOpen = 0 WHERE id = :postId")
    suspend fun closePost(postId: Int)
}