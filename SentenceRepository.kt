package com.satori.japanese.learning.repositories

import android.content.Context
import com.google.gson.Gson
import com.satori.japanese.learning.models.LearningSentence
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SentenceRepository(private val context: Context) {
    suspend fun loadSentences(): List<LearningSentence> {
        return withContext(Dispatchers.IO) {
            try {
                val json = context.assets.open("sentences_data.json")
                    .bufferedReader()
                    .use { it.readText() }
                
                Gson().fromJson(json, Array<LearningSentence>::class.java).toList()
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
        }
    }
    
    suspend fun filterSentencesByCategory(category: String): List<LearningSentence> {
        val allSentences = loadSentences()
        return allSentences.filter { it.category == category }
    }
    
    suspend fun filterSentencesByLevel(level: String): List<LearningSentence> {
        val allSentences = loadSentences()
        return allSentences.filter { it.level == level }
    }
}
