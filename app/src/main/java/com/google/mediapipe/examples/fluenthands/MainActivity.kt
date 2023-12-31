/*
 * Copyright 2022 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.mediapipe.examples.fluenthands

import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.mediapipe.examples.fluenthands.databinding.ActivityMainBinding
import com.google.mediapipe.examples.fluenthands.db.Result
import com.google.mediapipe.examples.fluenthands.db.ResultDatabase
import com.google.mediapipe.examples.fluenthands.db.ResultDatabaseDao
import com.google.mediapipe.examples.fluenthands.db.ResultRepository
import com.google.mediapipe.examples.fluenthands.db.ResultViewModel
import com.google.mediapipe.examples.fluenthands.db.ResultViewModelFactory
import com.google.mediapipe.examples.fluenthands.logic.ContextHolder
import java.util.Calendar

class MainActivity : AppCompatActivity() {
    private lateinit var activityMainBinding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private lateinit var backButton: Button
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var database: ResultDatabase
    private lateinit var databaseDao: ResultDatabaseDao
    private lateinit var repository: ResultRepository
    private lateinit var viewModelFactory: ResultViewModelFactory
    private lateinit var resultViewModel: ResultViewModel
    private var totalWords = 10 // Total number of words to generate
    private var currentWordCount = 0 // Current word count
    private var userScore = 0 // User's score

    private val displayedWords = mutableSetOf<String>()

    // Array containing alphabets from a to z
    private val easy = ('A'..'Z').toList().toCharArray()

    // Array containing a list of 3-letter words
    private val medium = arrayOf("CAT", "DOG", "BAT", "HAT", "RUN", "FUN", "SUN", "CAR", "BAR", "JAR",
        "GRAY", "FALL", "WARM", "BLUE", "FERN", "PEAK", "FADE", "TWIG", "SEED", "PEAK",
        "ACE", "TOP", "ZIP", "LAP", "MIX", "ZAP", "LIP", "JOY", "WET", "SKY",
        "STAR", "MOON", "SONG", "BOOK", "TREE", "FIRE", "LAKE", "ROSE", "BIRD", "WIND")

    // Array containing a list of 5-letter words
    private val difficult = arrayOf("HOUSE", "MOUSE", "APPLE", "HAPPY", "CLOUD", "TABLE", "CHAIR", "BEACH", "EARTH", "RIVER",
        "MOUNT", "SMILE", "GRAPE", "MELON", "BANANA", "ORANGE", "ROCKET", "GUITAR", "CANDLE", "PLANET",
        "CHAMPION", "SATURDAY", "AWESOME", "DIAMOND", "VICTORY", "MORNING", "FREEDOM")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding.root)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragment_container) as NavHostFragment
        val navController = navHostFragment.navController
        activityMainBinding.navigation.setupWithNavController(navController)
        activityMainBinding.navigation.setOnNavigationItemReselectedListener {

        }

        database = ResultDatabase.getInstance(this)
        databaseDao = database.resultDatabaseDao
        repository = ResultRepository(databaseDao)
        viewModelFactory = ResultViewModelFactory(repository)
        resultViewModel = ViewModelProvider(this, viewModelFactory).get(ResultViewModel::class.java)
        resultViewModel.allResultData.observe(this){
            println("size : ${it.size}")
        }

        val difficulty = getSavedDifficulty()
        displayRandomWord(difficulty)

        val submitButton = findViewById<ImageButton>(R.id.submitButton)
        submitButton.setOnClickListener {
            val newResult = Result(
                score = userScore,
                dateTime = Calendar.getInstance(),
                difficulty = difficulty.toString()
            )

            resultViewModel.insertResult(newResult)
            finish()
        }


        val backButton = findViewById<ImageButton>(R.id.back_button)
        backButton.setOnClickListener {
            finish()
        }

        val yesButton = findViewById<ImageButton>(R.id.yesButton)


        if (currentWordCount < totalWords - 1) {
            // Show "Yes" button if the word count is less than 10
            yesButton.visibility = View.VISIBLE
            submitButton.visibility = View.GONE
        } else {
            // Show "Submit" button if the word count is 10
            yesButton.visibility = View.GONE
            submitButton.visibility = View.VISIBLE
        }

        findViewById<ImageButton>(R.id.yesButton).setOnClickListener {
            yesButtonClick()
        }

        findViewById<ImageButton>(R.id.noButton).setOnClickListener {
            noButtonClick()
        }
    }

    enum class Difficulty {
        EASY, MEDIUM, HARD
    }

    companion object {
        private const val SHARED_PREFS_FILE = "shared_preferences"
        private const val DIFFICULTY_KEY = "difficulty"
    }

    private fun getSavedDifficulty(): Difficulty {
        val sharedPreferences = getSharedPreferences(SHARED_PREFS_FILE, MODE_PRIVATE)
        val savedDifficulty = sharedPreferences.getInt(DIFFICULTY_KEY, 0)

        Log.d("MainActivity", "Saved Difficulty level: $savedDifficulty")
        return getDifficulty(savedDifficulty)
    }

    private fun getDifficulty(progress: Int): Difficulty {
        return when (progress) {
            0 -> Difficulty.EASY
            1 -> Difficulty.MEDIUM
            2 -> Difficulty.HARD
            else -> Difficulty.EASY
        }
    }

    override fun onBackPressed() {
        finish()
    }

    private fun getRandomWord(difficulty: Difficulty): String {
        return when (difficulty) {
            Difficulty.EASY -> {
                // Return a random letter from the alphabets array
                easy.random().toString()
            }
            Difficulty.MEDIUM -> {
                // Return a random 3-letter word from the array
                medium.random()
            }
            Difficulty.HARD -> {
                // Return a random 5-letter word from the array
                difficult.random()
            }
        }
    }

    fun deletebuttonClick(view: View?) {
        val textLabel = findViewById<TextView>(R.id.textLabel)
        val currentText = textLabel.text.toString()

        if (currentText.isNotEmpty()) {
            val updatedText = currentText.substring(0, currentText.length - 1)
            textLabel.text = updatedText
            ContextHolder.currentWord = updatedText
        }
    }

    fun addSpacebuttonClick(view: View?) {
        val textLabel = findViewById<TextView>(R.id.textLabel)
        textLabel.text = textLabel.text.toString() + " "
        ContextHolder.currentWord = textLabel.text.toString() + " "
    }

    fun yesButtonClick(){
        // Get the current word displayed
        val currentWord = findViewById<TextView>(R.id.wordTextView).text.toString()

        // Check if the entered word matches the current random word
        if (currentWord.equals(ContextHolder.currentWord, ignoreCase = true)) {
            // Award points
            awardPoints(10)

            currentWordCount++

            if (currentWordCount<totalWords){
                // Display a new random word
                val difficulty = getSavedDifficulty()
                displayRandomWord(difficulty)
            } else {
                Toast.makeText(this, "You've completed $totalWords words!", Toast.LENGTH_SHORT).show()
            }
        } else {
            // Here, we're just displaying a toast message
            playSoundEffect()
            Toast.makeText(this,"Incorrect Answer! Try again.", Toast.LENGTH_SHORT).show()
        }
    }
    private fun playSoundEffect() {
        mediaPlayer?.release()  // Release any previous MediaPlayer
        mediaPlayer = MediaPlayer.create(this, R.raw.negative) // Replace 'sound_effect' with your actual sound file's name
        mediaPlayer?.start()  // Play sound

        // Optionally set OnCompletionListener if you want to do something after the sound finishes playing
        mediaPlayer?.setOnCompletionListener {
            // Release the MediaPlayer once the sound has finished playing
            it.release()
        }
    }

    fun noButtonClick(){
        val difficulty = getSavedDifficulty()

        currentWordCount++

        if(currentWordCount < totalWords){
            displayRandomWord(difficulty)
        } else {
            Toast.makeText(this, "You've completed $totalWords words! Please submit", Toast.LENGTH_SHORT).show()
        }
    }

    private fun awardPoints(points: Int) {
        //add point for every correct answer
        userScore += points
        Toast.makeText(this,"You earned $points points!", Toast.LENGTH_SHORT).show()
    }

    private fun displayRandomWord(difficulty: Difficulty) {
        var randomWord: String
        do {
            randomWord = getRandomWord(difficulty)
        } while (displayedWords.contains(randomWord)) //Display a random word, don't repeat words

        displayedWords.add(randomWord)

        findViewById<TextView>(R.id.wordTextView).text = randomWord

        val yesButton = findViewById<ImageButton>(R.id.yesButton)
        val submitButton = findViewById<ImageButton>(R.id.submitButton)

        if (currentWordCount < totalWords - 1) {
            // Show "Yes" button if the word count is less than 10
            yesButton.visibility = View.VISIBLE
            submitButton.visibility = View.GONE
        } else {
            // Show "Submit" button if the word count is 10
            yesButton.visibility = View.GONE
            submitButton.visibility = View.VISIBLE
        }
    }
}
