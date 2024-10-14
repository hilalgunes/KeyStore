package com.example.keystore

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.ArrayAdapter
import android.widget.TextView
import android.content.SharedPreferences
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

data class User(val id: Int, val fullname: String, val password: String)

class MainActivity : AppCompatActivity() {

    private lateinit var cryptoManager: CryptoManager
    private lateinit var spinner: Spinner
    private lateinit var ids: MutableList<Int>
    private lateinit var adapter: ArrayAdapter<Int>
    private var currentId = 1
    private lateinit var sharedPreferences: SharedPreferences

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        cryptoManager = CryptoManager()
        sharedPreferences = getSharedPreferences("com.example.keystore", MODE_PRIVATE)

        val editTextName = findViewById<EditText>(R.id.editTextFullName)
        val editTextPassword = findViewById<EditText>(R.id.editTextPassword)
        val buttonEncrypt = findViewById<Button>(R.id.buttonEncrypt)
        val buttonDecrypt = findViewById<Button>(R.id.buttonDecrypt)
        val textViewOutput = findViewById<TextView>(R.id.textViewOutput)
        spinner = findViewById(R.id.spinner)

        ids = loadIds()
        adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, ids)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        val file = File(filesDir, "user_data.txt")
        if (!file.exists()) {
            file.createNewFile()
        }

        buttonEncrypt.setOnClickListener {
            val user = User(
            id = currentId,
            fullname = editTextName.text.toString(),
            password = editTextPassword.text.toString())

         val fos = FileOutputStream(file, true) // Append mode

         try {
            cryptoManager.encrypt(
            id = user.id,
            bytes = user.toString().encodeToByteArray(),
            outputStream = fos
           )
         ids.add(user.id)
         saveIds(ids)
         adapter.notifyDataSetChanged()
         textViewOutput.text = "User data encrypted and saved."
         currentId++
        } catch (e: Exception) {
        textViewOutput.text = "Error: ${e.message}"
          } finally {
        fos.close()
     }
  }

        buttonDecrypt.setOnClickListener {
            val selectedId = spinner.selectedItem as Int
            val fis = FileInputStream(file)
        try {
            val decryptedMessage = cryptoManager.decrypt(
            inputStream = fis,
            targetId = selectedId
          )
         if (decryptedMessage != null) {
            textViewOutput.text = decryptedMessage.decodeToString()
         } else {
            textViewOutput.text = "No data found for the selected ID."
         }
        } catch (e: Exception) {
        textViewOutput.text = "Error: ${e.message}"
         } finally {
        fis.close()
         }
       }
     }

    private fun saveIds(ids: List<Int>) {
        val editor = sharedPreferences.edit()
        editor.putStringSet("ids", ids.map { it.toString() }.toSet())
        editor.apply()
    }

    private fun loadIds(): MutableList<Int> {
        val idSet = sharedPreferences.getStringSet("ids", emptySet())
        return idSet?.map { it.toInt() }?.toMutableList() ?: mutableListOf()
    }
}