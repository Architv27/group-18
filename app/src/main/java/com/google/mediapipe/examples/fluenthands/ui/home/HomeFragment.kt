package com.google.mediapipe.examples.fluenthands.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import com.google.mediapipe.examples.fluenthands.LearningPage
import com.google.mediapipe.examples.fluenthands.MainActivity
import com.google.mediapipe.examples.fluenthands.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var recognizerCard: FrameLayout
    private lateinit var learningCard: FrameLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        // Initialize and set click listeners for the quiz and learning cards
        recognizerCard = binding.card1
        recognizerCard.setOnClickListener {
            // Start MainActivity when the recognizer card is clicked
            val intent = Intent(requireActivity(), MainActivity::class.java)
            startActivity(intent)
        }
        learningCard = binding.card3
        learningCard.setOnClickListener {
            // Start LearningPage when the learning card is clicked
            val intent = Intent(requireActivity(), LearningPage::class.java)
            startActivity(intent)
        }

        return binding.root
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}