package com.example.equalequations

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView

class SummaryActivity : AppCompatActivity() {

    private val correctTotConst = "correct_tot"
    private val incorrectTotConst = "incorrect_tot"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_summary)
        // Get data from intent and display on UI
        findViewById<TextView>(R.id.tot_correct).text = intent.extras?.getInt(correctTotConst).toString()
        findViewById<TextView>(R.id.tot_incorrect).text = intent.extras?.getInt(incorrectTotConst).toString()
    }

}
