package com.example.majorprojectui.AuthActivities

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import com.example.majorprojectui.MainActivity
import com.example.majorprojectui.R
import com.example.majorprojectui.databinding.ActivityLoginBinding
import com.google.firebase.FirebaseApp
import com.google.firebase.database.FirebaseDatabase


class LoginActivity : AppCompatActivity() {
    var doubleBackToExitPressedOnce = false
    private lateinit var binding : ActivityLoginBinding
    lateinit var progressBar : ProgressDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        FirebaseApp.initializeApp(this)

        progressBar = ProgressDialog(this)

        progressBar.apply {
            val view = layoutInflater.inflate(R.layout.custom_progressbar,null)
            this.setContentView(view)
            val title = view.findViewById<TextView>(R.id.tv_text)
            title.text = "Please wait ..."
            setMessage("Authenticating...")
        }

        binding.apply {
            loginetUserid.doAfterTextChanged {
                logintilUserid.isErrorEnabled = false
            }
            loginetPassword.doAfterTextChanged {
                logintilPassword.isErrorEnabled = false
            }
        }

        binding.loginbtn.setOnClickListener {

            binding.apply {
                if (loginetUserid.text.toString().trim().isEmpty()){
                    logintilUserid.isErrorEnabled = true
                    logintilUserid.error = "Please give your email Id"
                    return@setOnClickListener
                }
                if (loginetPassword.text.toString().trim().isEmpty()){
                    logintilPassword.isErrorEnabled = true
                    logintilPassword.error = "Please give your password"
                    return@setOnClickListener
                }
            }
            var userDetails = binding.loginetUserid.text.toString()
            getuserDetails(userDetails)
        }

        binding.tvRegister.setOnClickListener {
            refresh()
            startActivity(Intent(this@LoginActivity, RegisterActivity::class.java))
        }

        binding.tvFrgtPassword.setOnClickListener {
            startActivity(Intent(this@LoginActivity,ResetPassword::class.java))
            refresh()
        }


    }

    private fun refresh() {
        binding.apply {
            logintilUserid.isErrorEnabled = false
            loginetUserid.setText("")
            logintilPassword.isErrorEnabled = false
            loginetPassword.setText("")
        }
    }

    private fun getuserDetails(userDetails: String) {
        progressBar.show()
        
        var dataBase = FirebaseDatabase.getInstance()
        var datareference = dataBase.getReference("User")
        datareference.child(userDetails).get().addOnSuccessListener {

            if (it.exists()){
                var password = it.child("password").value.toString()

                if (binding.loginetPassword.text.toString().equals(password)){
                    progressBar.dismiss()

                    val userDetails = getSharedPreferences("userDetails",Context.MODE_PRIVATE)
                    var editor = userDetails.edit()
                    editor.putString("userId",it.child("userId").value.toString())
                    editor.putString("userName",it.child("name").value.toString())
                    editor.putString("userMail",it.child("emailId").value.toString())
                    editor.commit()
                    refresh()
                    startActivity(Intent(this, MainActivity::class.java))
                }
                else{
                    progressBar.dismiss()
                    Toast.makeText(this,"invalid password",Toast.LENGTH_SHORT).show()
                }
            }
            else{
                progressBar.dismiss()
                Toast.makeText(this,"No user found",Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            progressBar.dismiss()
            Toast.makeText(this,it.toString(),Toast.LENGTH_SHORT).show()
        }
    }

    override fun onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed()
            return
        }

        this.doubleBackToExitPressedOnce = true
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show()

        Handler(Looper.getMainLooper()).postDelayed(Runnable {
            doubleBackToExitPressedOnce = false
        }, 1000)
    }
}