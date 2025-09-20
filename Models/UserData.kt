package com.satori.japanese.learning.models

import java.util.Date

data class UserData(
    val userId: String,
    val isFirstTime: Boolean,
    val lastVisit: Date,
    val streak: Int,
    val level: Int,
    val learnedKanji: Set<String>,
    val learnedVocabulary: Set<String>,
    val grammarPointsCompleted: Set<String>
) {
    fun getDaysSinceLastVisit(): Long {
        val now = Date()
        val diff = now.time - lastVisit.time
        return diff / (24 * 60 * 60 * 1000)
    }
    
    fun getOverallProgress(): Int {
        val totalItems = 2136 + 5028 // كانجي + مفردات
        val learnedItems = learnedKanji.size + learnedVocabulary.size
        return (learnedItems * 100 / totalItems).coerceIn(0, 100)
    }
}
