package com.example.majorprojectui.AuthActivities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log.e
import com.example.majorprojectui.MainActivity
import com.example.majorprojectui.databinding.ActivityLoginBinding
import com.google.firebase.FirebaseApp
import com.google.firebase.database.FirebaseDatabase

class LoginActivity : AppCompatActivity() {
    private lateinit var binding : ActivityLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.loginbtn.setOnClickListener {
//            startActivity(Intent(this@LoginActivity,DiseaseInfoActivity::class.java))
            var userDetails = binding.loginetUserid.text.toString()
            getuserDetails(userDetails)
        }

        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this@LoginActivity, RegisterActivity::class.java))
        }


    }

    private fun getuserDetails(userDetails: String) {
        FirebaseApp.initializeApp(this)
        var dataBase = FirebaseDatabase.getInstance()
        var datareference = dataBase.getReference("User")
        datareference.child(userDetails).get().addOnSuccessListener {
            if (it.exists()){
                e("success",it.child("emailId").value.toString())
                var password = it.child("password").value.toString()
                if (binding.loginetPassword.text.toString().equals(password)){
                    e("authenticate","${it.child("emailId").value.toString()} success")

                    val userDetails = getSharedPreferences("userDetails",Context.MODE_PRIVATE)
                    var editor = userDetails.edit()
                    editor.putString("userName",it.child("name").value.toString())
                    editor.putString("userMail",it.child("emailId").value.toString())
                    editor.commit()
                    startActivity(Intent(this, MainActivity::class.java))
                }
                else{
                    e("authenticate","${it.child("emailId").value.toString()} failed")
                }
            }
            else{
                e("failed",it.children.toString())
            }
        }.addOnFailureListener {
            e("failed","failed")
            e("failed",it.toString())
        }
    }
}