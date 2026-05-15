package com.example.skillexchange.database

import androidx.room.*

@Dao
interface SwapRequestDao {
    @Insert
    suspend fun insertSwapRequest(request: SwapRequest)

    @Update
    suspend fun updateSwapRequest(request: SwapRequest)

    @Query("SELECT * FROM swap_requests WHERE receiverId = :userId OR requesterId = :userId")
    suspend fun getSwapsByUser(userId: Int): List<SwapRequest>

    @Query("SELECT * FROM swap_requests WHERE id = :swapId")
    suspend fun getSwapById(swapId: Int): SwapRequest?
}