package com.example.mvi.presentation.main.intent

sealed class MainIntent {
    object FetchUser: MainIntent()
}