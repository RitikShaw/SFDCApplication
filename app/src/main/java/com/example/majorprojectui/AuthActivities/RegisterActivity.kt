package com.example.majorprojectui.AuthActivities

import android.app.ProgressDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import com.example.majorprojectui.Models.AppUser
import com.example.majorprojectui.R
import com.example.majorprojectui.databinding.ActivityWelcomeBinding
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import java.util.regex.Matcher
import java.util.regex.Pattern

class RegisterActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit  var binding : ActivityWelcomeBinding
    lateinit var progressBar : ProgressDialog
    private lateinit var userData : AppUser
    val dataBase = FirebaseDatabase.getInstance()
    val datareference = dataBase.getReference("User")

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding =ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        FirebaseApp.initializeApp(this)
        auth = Firebase.auth

        progressBar = ProgressDialog(this)
        progressBar.apply {
            val view = layoutInflater.inflate(R.layout.custom_progressbar,null)
            this.setContentView(view)
            val title = view.findViewById<TextView>(R.id.tv_text)
            title.text = "Please wait ..."
            setMessage("Authenticating...")
        }

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
                else if (registerEtPhone.text.toString().trim().isEmpty()){
                    registerEtlPhone.isErrorEnabled = true
                    registerEtlPhone.error = "Please give your phone number"
                    return@setOnClickListener
                }
                else if (registerEtEmailId.text.toString().trim().isEmpty()){
                    registerEtlEmailId.isErrorEnabled = true
                    registerEtlEmailId.error = "Please give your email Id"
                    return@setOnClickListener
                }
                else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(
                        registerEtEmailId.text.toString().trim()
                    ).matches()
                ) {
                    registerEtlEmailId.isErrorEnabled = true
                    registerEtlEmailId.error = "Please provide valid email Id"
                    return@setOnClickListener
                }
                else if (registerEtPassword.text.toString().trim().isEmpty()){
                    registerEtlPassword.isErrorEnabled = true
                    registerEtlPassword.error = "Please set a new password"
                    return@setOnClickListener
                }
                else if (!isValidPassword(registerEtPassword.text.toString().trim())){
                    registerEtlPassword.isErrorEnabled = true
                    registerEtlPassword.error = "Password should be minimum of 8 characters and must contain at least one Upper case and special characters"
                    return@setOnClickListener
                }

            }
            progressBar.show()

            auth.createUserWithEmailAndPassword(binding.registerEtEmailId.text.toString(),binding.registerEtPassword.text.toString())
                .addOnCompleteListener(this){
                    if (it.isSuccessful) {
                        binding.apply {
                            addUserToDatabase(
                                auth.currentUser!!.uid,
                                registerEtUserName.text.toString(),
                                registerEtPhone.text.toString(),
                                registerEtEmailId.text.toString(),
                                registerEtPassword.text.toString()
                            )
                        }
                    }
                    else{
                        progressBar.dismiss()
                        Toast.makeText(this@RegisterActivity,"User id could not be created",Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    progressBar.dismiss()
                    Toast.makeText(this@RegisterActivity,"User id creation failed $it",Toast.LENGTH_LONG).show()
                }


            //Get current Date
            /*val current = LocalDateTime.now()
            val formatter = DateTimeFormatter.BASIC_ISO_DATE
            val formatted = current.format(formatter)

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
            }*/
        }

        binding.tvLogin.setOnClickListener {
            refresh()
            startActivity(Intent(this,LoginActivity::class.java))
        }
    }

    private fun addUserToDatabase(
        uid: String,
        name: String,
        phone: String,
        emailId: String,
        password: String
    ) {
        userData = AppUser(uid, name, phone, emailId, password)

        datareference.child(uid).setValue(userData).addOnSuccessListener {
            progressBar.dismiss()
            Toast.makeText(this@RegisterActivity, "Account created successfully", Toast.LENGTH_LONG)
                .show()
            refresh()
            startActivity(Intent(this@RegisterActivity,LoginActivity::class.java))
            finish()
        }
            .addOnFailureListener {
                progressBar.dismiss()
                Toast.makeText(this@RegisterActivity, "Account creation failed $it", Toast.LENGTH_LONG)
                    .show()
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
    fun isValidPassword(password: String?): Boolean {
        val pattern: Pattern
        val PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{4,}$"
        pattern = Pattern.compile(PASSWORD_PATTERN)
        val matcher: Matcher? = password?.let { pattern.matcher(it) }
        return matcher!!.matches()
    }
}