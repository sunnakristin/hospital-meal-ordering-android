package com.example.matarpontun.ui.ward

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.matarpontun.R
import com.example.matarpontun.ui.patients.PatientListActivity

class WardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ward)

        val wardName = intent.getStringExtra("WARD_NAME")

        val tvWelcome = findViewById<TextView>(R.id.tvWelcome)
        tvWelcome.text = "Welcome $wardName"

        val wardId = intent.getLongExtra("WARD_ID", -1)

        findViewById<Button>(R.id.btnPatients).setOnClickListener {
            val intent = Intent(this, PatientListActivity::class.java)
            intent.putExtra("WARD_ID", wardId)
            startActivity(intent)
        }

    }
}
