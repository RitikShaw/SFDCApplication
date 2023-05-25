package com.example.majorprojectui.AuthActivities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log.e
import com.example.majorprojectui.MainActivity
import com.example.majorprojectui.databinding.ActivityLoginBinding
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
        var dataBase = FirebaseDatabase.getInstance()
        var datareference = dataBase.getReference("User")
        datareference.child(userDetails).get().addOnSuccessListener {
            if (it.exists()){
                e("success",it.child("emailId").value.toString())
                var password = it.child("password").value.toString()
                if (binding.loginetPassword.text.toString().equals(password)){
                    e("authenticate","${it.child("emailId").value.toString()} success")
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
        }
    }
}