package com.example.interview

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.interview.compose.ComposeLabActivity
import com.example.interview.pulsenews.MainActivity
import com.example.interview.recyclerlab.RecyclerViewLabActivity
import com.google.android.material.button.MaterialButton

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<MaterialButton>(R.id.btn_open_lifecycle_lab).setOnClickListener {
            startActivity(Intent(this, LifecycleLabActivity::class.java))
        }
        findViewById<MaterialButton>(R.id.btn_open_recycler_lab).setOnClickListener {
            startActivity(Intent(this, RecyclerViewLabActivity::class.java))
        }
        findViewById<MaterialButton>(R.id.btn_open_compose_lab).setOnClickListener {
            startActivity(Intent(this, ComposeLabActivity::class.java))
        }
        findViewById<MaterialButton>(R.id.btn_open_pulse_news).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
        findViewById<MaterialButton>(R.id.btn_open_coin_watch).setOnClickListener {
            startActivity(Intent(this, com.example.interview.coinwatch.MainActivity::class.java))
        }
    }
}