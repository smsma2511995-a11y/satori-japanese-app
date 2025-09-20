package com.satori.japanese.learning.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vocabulary")
data class Vocabulary(
    @PrimaryKey val id: Int,
    val word: String,
    val reading: String,
    val meanings: List<String>,
    val pos: String,
    val jlptLevel: String,
    val category: String,
    val examples: List<Example>
) {
    data class Example(
        val japanese: String,
        val translation: String
    )
}
