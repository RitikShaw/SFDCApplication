package com.example.majorprojectui.AuthActivities

import android.app.ProgressDialog
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.widget.doAfterTextChanged
import com.example.majorprojectui.Models.AppUser
import com.example.majorprojectui.R
import com.example.majorprojectui.databinding.ActivityWelcomeBinding
import com.google.firebase.FirebaseApp
import com.google.firebase.database.FirebaseDatabase
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class RegisterActivity : AppCompatActivity() {
    private lateinit  var binding : ActivityWelcomeBinding
    lateinit var progressBar : ProgressDialog

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding =ActivityWelcomeBinding.inflate(layoutInflater)
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

        val dataBase = FirebaseDatabase.getInstance()
        val datareference = dataBase.getReference("User")

        binding.apply {
            registerEtUserName.doAfterTextChanged {
                registerEtlUserName.isErrorEnabled = false
            }
            registerEtPhone.doAfterTextChanged {
                registerEtlPhone.isErrorEnabled = false
            }
            registerEtEmailId.doAfterTextChanged {
                registerEtlEmailId.isErrorEnabled = false
            }
            registerEtPassword.doAfterTextChanged {
                registerEtlPassword.isErrorEnabled = false
            }
        }

        binding.registerBtn.setOnClickListener {

            // Validations for input fields
            binding.apply {
                if (registerEtUserName.text.toString().trim().isEmpty()){
                    registerEtlUserName.isErrorEnabled = true
                    registerEtlUserName.error = "Please give your name"
                    return@setOnClickListener
                }
                if (registerEtPhone.text.toString().trim().isEmpty()){
                    registerEtlPhone.isErrorEnabled = true
                    registerEtlPhone.error = "Please give your phone number"
                    return@setOnClickListener
                }
                if (registerEtEmailId.text.toString().trim().isEmpty()){
                    registerEtlEmailId.isErrorEnabled = true
                    registerEtlEmailId.error = "Please give your email Id"
                    return@setOnClickListener
                }
                if (registerEtPassword.text.toString().trim().isEmpty()){
                    registerEtlPassword.isErrorEnabled = true
                    registerEtlPassword.error = "Please set a new password"
                    return@setOnClickListener
                }

            }
            if(userExist(binding.registerEtEmailId.text.toString().trim())){
                Toast.makeText(this@RegisterActivity,"User already exists",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            progressBar.show()
            //Get current Date
            val current = LocalDateTime.now()
            val formatter = DateTimeFormatter.BASIC_ISO_DATE
            val formatted = current.format(formatter)

            val userId = "$formatted${System.currentTimeMillis()}"
            val userName = binding.registerEtUserName.text.toString()
            val userPhone = binding.registerEtPhone.text.toString()
            val userEmail = binding.registerEtEmailId.text.toString()
            val userPassword = binding.registerEtPassword.text.toString()

            val appUser = AppUser(userId,userName,userPhone,userEmail,userPassword)
            datareference.child(userEmail).setValue(appUser).addOnSuccessListener {
                progressBar.dismiss()
                Toast.makeText(this@RegisterActivity,"Successfully Registered",Toast.LENGTH_SHORT).show()
                refresh()
                startActivity(Intent(this@RegisterActivity,LoginActivity::class.java))
                finish()
            }.addOnFailureListener {
                progressBar.dismiss()
                Toast.makeText(this@RegisterActivity,"Registration Failed",Toast.LENGTH_SHORT).show()
            }
        }

        binding.tvLogin.setOnClickListener {
            refresh()
            startActivity(Intent(this,LoginActivity::class.java))
        }
    }

    private fun refresh() {
        binding.apply {
            registerEtlUserName.isErrorEnabled = false
            registerEtUserName.setText("")
            registerEtlPhone.isErrorEnabled = false
            registerEtPhone.setText("")
            registerEtlEmailId.isErrorEnabled = false
            registerEtEmailId.setText("")
            registerEtlPassword.isErrorEnabled = false
            registerEtPassword.setText("")
        }
    }

    private fun userExist(userDetails: String): Boolean {
        var existence = false
        progressBar.show()

        val dataBase = FirebaseDatabase.getInstance()
        val datareference = dataBase.getReference("User")
        datareference.child(userDetails).get().addOnSuccessListener {
            progressBar.dismiss()
            existence = true
            if (it.exists()){
                existence = true
            }
        }.addOnFailureListener {
            progressBar.dismiss()
            Toast.makeText(this,it.toString(),Toast.LENGTH_SHORT).show()
        }

        return existence
    }
}