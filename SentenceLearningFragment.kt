package com.satori.japanese.learning.fragments

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.SpannableString
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.satori.japanese.learning.R
import com.satori.japanese.learning.databinding.FragmentSentenceLearningBinding
import com.satori.japanese.learning.models.LearningSentence
import com.satori.japanese.learning.viewmodels.SentenceViewModel
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class SentenceLearningFragment : Fragment() {
    private lateinit var binding: FragmentSentenceLearningBinding
    private lateinit var viewModel: SentenceViewModel
    private var currentSpeed: Float = 1.0f
    private var currentIndex: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSentenceLearningBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this).get(SentenceViewModel::class.java)
        
        setupObservers()
        setupControls()
        setupSeekBar()
        
        viewModel.loadSentences()
    }

    private fun setupObservers() {
        viewModel.sentences.observe(viewLifecycleOwner) { sentences ->
            if (sentences.isNotEmpty()) {
                showSentence(sentences[currentIndex])
            }
        }
        
        viewModel.currentSentence.observe(viewLifecycleOwner) { sentence ->
            sentence?.let { showSentence(it) }
        }
    }

    private fun showSentence(sentence: LearningSentence) {
        binding.japaneseSentence.text = sentence.japanese
        binding.translationText.text = sentence.translation
        binding.breakdownText.text = formatBreakdown(sentence.breakdown)

        // تمييز كل كلمة بلون مختلف
        highlightWords(sentence.japanese, sentence.breakdown)
    }

    private fun formatBreakdown(breakdown: Map<String, String>): String {
        return breakdown.entries.joinToString("\n") { (word, meaning) ->
            "$word: $meaning"
        }
    }

    private fun highlightWords(sentence: String, breakdown: Map<String, String>) {
        val spannable = SpannableString(sentence)
        var startIndex = 0

        breakdown.keys.forEach { word ->
            val index = sentence.indexOf(word, startIndex)
            if (index >= 0) {
                val color = getRandomColorForWord(word)
                spannable.setSpan(
                    ForegroundColorSpan(color),
                    index,
                    index + word.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                startIndex = index + word.length
            }
        }

        binding.japaneseSentence.text = spannable
    }

    private fun getRandomColorForWord(word: String): Int {
        val colors = listOf(
            Color.RED, Color.BLUE, Color.GREEN, Color.MAGENTA,
            Color.CYAN, Color.YELLOW, Color.GRAY
        )
        return colors[word.hashCode() % colors.size]
    }

    private fun setupControls() {
        binding.slowButton.setOnClickListener {
            currentSpeed = 0.7f
            speakCurrentSentence()
        }

        binding.normalButton.setOnClickListener {
            currentSpeed = 1.0f
            speakCurrentSentence()
        }

        binding.fastButton.setOnClickListener {
            currentSpeed = 1.3f
            speakCurrentSentence()
        }

        binding.wordByWordButton.setOnClickListener {
            speakWordByWord()
        }

        binding.nextButton.setOnClickListener {
            viewModel.nextSentence()
        }

        binding.prevButton.setOnClickListener {
            viewModel.previousSentence()
        }

        binding.repeatButton.setOnClickListener {
            speakCurrentSentence()
        }
    }

    private fun setupSeekBar() {
        binding.speedSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                currentSpeed = 0.5f + (progress / 100f) * 1.5f // من 0.5 إلى 2.0
                binding.speedValue.text = "%.1fx".format(currentSpeed)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                speakCurrentSentence()
            }
        })
    }

    private fun speakCurrentSentence() {
        viewModel.currentSentence.value?.japanese?.let { sentence ->
            (activity as MainActivity).speakJapanese(sentence, currentSpeed)
        }
    }

    private fun speakWordByWord() {
        viewModel.currentSentence.value?.japanese?.let { sentence ->
            (activity as MainActivity).speakJapaneseWordByWord(sentence, 0.8f)
            
            // تمييز الكلمة التي يتم نطقها حالياً
            val words = sentence.split(" ", "　")
            var delay: Long = 0
            
            words.forEachIndexed { index, word ->
                Handler(Looper.getMainLooper()).postDelayed({
                    highlightCurrentWord(word, index, words)
                }, delay)
                delay += 1000
            }
        }
    }

    private fun highlightCurrentWord(currentWord: String, currentIndex: Int, allWords: List<String>) {
        val sentence = viewModel.currentSentence.value?.japanese ?: return
        val spannable = SpannableString(sentence)
        var startIndex = 0
        
        allWords.forEachIndexed { index, word ->
            val wordIndex = sentence.indexOf(word, startIndex)
            if (wordIndex >= 0) {
                val color = if (index == currentIndex) {
                    Color.YELLOW // تمييز الكلمة الحالية
                } else {
                    getRandomColorForWord(word)
                }
                
                spannable.setSpan(
                    ForegroundColorSpan(color),
                    wordIndex,
                    wordIndex + word.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                
                spannable.setSpan(
                    BackgroundColorSpan(if (index == currentIndex) Color.DKGRAY else Color.TRANSPARENT),
                    wordIndex,
                    wordIndex + word.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                
                startIndex = wordIndex + word.length
            }
        }
        
        binding.japaneseSentence.text = spannable
    }
}
