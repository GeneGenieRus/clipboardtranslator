package com.clipboardtranslator

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        addButtonsListeners()
    }

    private fun addButtonsListeners() {
        val translateIntoNotificationsCb = findViewById<CheckBox>(R.id.translateIntoNotificationsCheckbox)
        val buttonStart = findViewById<Button>(R.id.start_service_btn)
        val buttonStop = findViewById<Button>(R.id.stop_service_btn)
        val intent = Intent(applicationContext, TranslationService::class.java)

        buttonStart.setOnClickListener { v: View? ->
            intent.putExtra(Constants.PARAM_TRANSLATE_INTO_NOTIFICATIONS, translateIntoNotificationsCb.isChecked)
            startService(intent)
            Toast.makeText(applicationContext, getString(R.string.toast_service_started), Toast.LENGTH_SHORT).show()
        }

        buttonStop.setOnClickListener { v: View? ->
            stopService(intent)
            Toast.makeText(applicationContext, getString(R.string.toast_service_stopped), Toast.LENGTH_SHORT).show()
        }
    }
}