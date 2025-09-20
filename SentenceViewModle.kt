package com.satori.japanese.learning.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.satori.japanese.learning.models.LearningSentence
import com.satori.japanese.learning.repositories.SentenceRepository
import kotlinx.coroutines.launch

class SentenceViewModel(private val repository: SentenceRepository) : ViewModel() {
    private val _sentences = MutableLiveData<List<LearningSentence>>()
    val sentences: LiveData<List<LearningSentence>> = _sentences
    
    private val _currentSentence = MutableLiveData<LearningSentence?>()
    val currentSentence: LiveData<LearningSentence?> = _currentSentence
    
    private var currentIndex = 0
    
    init {
        loadSentences()
    }
    
    fun loadSentences() {
        viewModelScope.launch {
            val sentencesList = repository.loadSentences()
            _sentences.value = sentencesList
            
            if (sentencesList.isNotEmpty()) {
                _currentSentence.value = sentencesList[currentIndex]
            }
        }
    }
    
    fun nextSentence() {
        val currentList = _sentences.value ?: return
        if (currentList.isNotEmpty()) {
            currentIndex = (currentIndex + 1) % currentList.size
            _currentSentence.value = currentList[currentIndex]
        }
    }
    
    fun previousSentence() {
        val currentList = _sentences.value ?: return
        if (currentList.isNotEmpty()) {
            currentIndex = (currentIndex - 1).takeIf { it >= 0 } ?: (currentList.size - 1)
            _currentSentence.value = currentList[currentIndex]
        }
    }
    
    fun getSentencesByCategory(category: String): List<LearningSentence> {
        return _sentences.value?.filter { it.category == category } ?: emptyList()
    }
    
    fun getSentencesByLevel(level: String): List<LearningSentence> {
        return _sentences.value?.filter { it.level == level } ?: emptyList()
    }
}
