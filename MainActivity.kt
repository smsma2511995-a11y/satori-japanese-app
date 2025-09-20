package com.satori.japanese.learning

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.satori.japanese.learning.adapters.MainPagerAdapter
import com.satori.japanese.learning.databinding.ActivityMainBinding
import java.util.*

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var tts: TextToSpeech
    private lateinit var pagerAdapter: MainPagerAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // تهيئة محرك النطق
        tts = TextToSpeech(this, this)
        
        // إعداد أدابتر ViewPager2
        pagerAdapter = MainPagerAdapter(this)
        pagerAdapter.addFragment(KanjiLearningFragment(), "كانجي")
        pagerAdapter.addFragment(VocabularyFragment(), "مفردات")
        pagerAdapter.addFragment(GrammarFragment(), "قواعد")
        pagerAdapter.addFragment(SentenceLearningFragment(), "جمل")
        pagerAdapter.addFragment(TranslationFragment(), "ترجمة")
        pagerAdapter.addFragment(ReviewFragment(), "مراجعة")
        
        binding.viewPager.adapter = pagerAdapter
        binding.viewPager.offscreenPageLimit = 3
        
        // ربط TabLayout مع ViewPager2
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = pagerAdapter.getTitle(position)
            tab.icon = ContextCompat.getDrawable(this, pagerAdapter.getIconId(position))
        }.attach()
        
        // تحميل تقدم المستخدم
        loadUserProgress()
    }
    
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale.JAPANESE
            
            // إعداد معالج تقدم الكلام
            tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    // يمكن استخدام هذا لتحديث الواجهة عند بدء الكلام
                }
                
                override fun onDone(utteranceId: String?) {
                    // يمكن استخدام هذا لتحديث الواجهة عند انتهاء الكلام
                }
                
                override fun onError(utteranceId: String?) {
                    // معالجة الأخطاء
                }
            })
        }
    }
    
    private fun loadUserProgress() {
        val userData = UserDataManager.loadUserData(this)
        binding.progressBar.progress = userData.getOverallProgress()
        binding.levelText.text = "المستوى ${userData.level}"
    }
    
    fun speakJapanese(text: String, speed: Float = 1.0f) {
        val params = Bundle().apply {
            putFloat(TextToSpeech.Engine.KEY_PARAM_RATE, speed)
        }
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, params, "tts_utterance")
    }
    
    fun speakJapaneseSlowly(text: String) {
        speakJapanese(text, 0.7f) // سرعة بطيئة
    }
    
    fun speakJapaneseNormally(text: String) {
        speakJapanese(text, 1.0f) // سرعة عادية
    }
    
    fun speakJapaneseQuickly(text: String) {
        speakJapanese(text, 1.3f) // سرعة سريعة
    }
    
    fun speakJapaneseWordByWord(text: String, speed: Float = 0.8f) {
        val words = text.split(" ", "　") // تقسيم النص إلى كلمات
        var delay: Long = 0
        
        words.forEach { word ->
            Handler(Looper.getMainLooper()).postDelayed({
                if (word.isNotBlank()) {
                    speakJapanese(word, speed)
                }
            }, delay)
            delay += 1000 // تأخير ثانية بين كل كلمة
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        tts.shutdown()
    }
}
