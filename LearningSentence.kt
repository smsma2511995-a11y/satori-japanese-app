package com.satori.japanese.learning.models

data class LearningSentence(
    val japanese: String,
    val translation: String,
    val breakdown: Map<String, String>, // كلمة -> معنى
    val category: String,
    val level: String
)
