package com.example.matarpontun.ui.ward

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.matarpontun.R

class WardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ward)

        val wardName = intent.getStringExtra("WARD_NAME")

        val tvWelcome = findViewById<TextView>(R.id.tvWelcome)
        tvWelcome.text = "Welcome $wardName"
    }
}
