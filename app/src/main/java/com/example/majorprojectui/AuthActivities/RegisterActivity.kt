package com.example.majorprojectui.AuthActivities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.majorprojectui.Models.AppUser
import com.example.majorprojectui.databinding.ActivityWelcomeBinding
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {
    private lateinit  var binding : ActivityWelcomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding =ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var dataBase = FirebaseDatabase.getInstance()
        var datareference = dataBase.getReference("User")

        binding.registerBtn.setOnClickListener {
            var userName = binding.registerEtUserId.text.toString()
            var userPhone = binding.registerEtUserId.text.toString()
            var userEmail = binding.registerEtUserId.text.toString()
            var userPassword = binding.registerEtUserId.text.toString()

            val appUser = AppUser(userName,userPhone,userEmail,userPassword)
            datareference.child(userName).setValue(appUser).addOnSuccessListener {
                Toast.makeText(this@RegisterActivity,"Successfully Registered",Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                Toast.makeText(this@RegisterActivity,"Registration Failed",Toast.LENGTH_SHORT).show()
            }
        }
    }
}