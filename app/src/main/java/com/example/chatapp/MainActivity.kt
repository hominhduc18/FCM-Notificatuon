package com.example.chatapp


import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText

import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.iid.FirebaseInstanceId



const val TOPIC = "/topics/myTopic2"

class MainActivity : AppCompatActivity() {

    val TAG = "MainActivity"

    lateinit var btnSend: Button
    lateinit var etTitle: EditText
    lateinit var etMessage: EditText
    lateinit var etToken: EditText



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnSend = findViewById(R.id.btnSend)
        etTitle = findViewById(R.id.etTitle)
        etMessage = findViewById(R.id.etMessage)
        etToken = findViewById(R.id.etToken)


        FirebaseService.sharedPref = getSharedPreferences("sharedPref", MODE_PRIVATE)
        FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener {
            FirebaseService.token = it.token
            etToken.setText(it.token)
        }
        FirebaseMessaging.getInstance().subscribeToTopic(TOPIC)

        btnSend.setOnClickListener {
            val title = etTitle.text.toString()
            val message = etMessage.text.toString()
            val recipientToken = etToken.text.toString()
            if(title.isNotEmpty() && message.isNotEmpty() && recipientToken.isNotEmpty()) {
                PushNotification(
                    NotificationData(title, message),
                    recipientToken
                ).also {
                    sendNotification(it)
                }
            }
        }
    }

    private fun sendNotification(notification: PushNotification) = CoroutineScope(Dispatchers.IO).launch {
        try {
            val response = RetrofitInstance.api.postNotification(notification)
            if(response.isSuccessful) {
                Log.d(TAG, "Response: ${Gson().toJson(response)}")
            } else {
                Log.e(TAG, response.errorBody().toString())
            }
        } catch(e: Exception) {
            Log.e(TAG, e.toString())
        }
    }
}