package com.example.matarpontun.ui.menu

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModelProvider
import com.example.matarpontun.R
import androidx.compose.ui.platform.ComposeView
import com.example.matarpontun.ui.theme.MatarpontunTheme

class MenuActivity : AppCompatActivity() {

    private lateinit var viewModel: MenuViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        viewModel = ViewModelProvider(this)[MenuViewModel::class.java]

        val composeView = findViewById<ComposeView>(R.id.composeMenuView)
        composeView.setContent {
            MatarpontunTheme {
                val foodTypes by viewModel.foodTypes.collectAsState()
                val menuState by viewModel.menuState.collectAsState()

                MenuScreen(
                    foodTypes = foodTypes,
                    menuState = menuState,
                    onFoodTypeSelected = { id -> viewModel.loadMenu(id) }
                )
            }
        }

        findViewById<Button>(R.id.btnBackMenu).setOnClickListener {
            finish()
        }
    }
}
