package com.example.mvi.data.api

import com.example.mvi.data.model.User

interface ApiHelper {
    suspend fun getUsers(): List<User>
}