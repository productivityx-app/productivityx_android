package com.oussama_chatri.productivityx.core.ui.voice

import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import com.oussama_chatri.productivityx.MainActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class VoiceCommandActivity : ComponentActivity() {

    private val speechLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        val matches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
        val command = matches?.firstOrNull() ?: ""

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("voice_command", command)
        }
        startActivity(intent)
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startVoiceRecognition()
    }

    private fun startVoiceRecognition() {
        val intent = RecognizerIntent.getVoiceDetailsIntent(this) ?: Intent(
            RecognizerIntent.ACTION_RECOGNIZE_SPEECH,
        ).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "What would you like to do?")
        }
        speechLauncher.launch(intent)
    }
}
