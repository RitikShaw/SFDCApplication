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
import com.example.majorprojectui.Models.AppUser
import com.example.majorprojectui.R
import com.example.majorprojectui.databinding.ActivityLoginBinding
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import java.util.regex.Matcher
import java.util.regex.Pattern


class LoginActivity : AppCompatActivity() {
    private var auth = Firebase.auth
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
                else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(loginetUserid.text.toString().trim()).matches()){
                    logintilUserid.isErrorEnabled = true
                    logintilUserid.error = "Please provide valid email Id"
                    return@setOnClickListener
                }
                else if (loginetPassword.text.toString().trim().isEmpty()){
                    logintilPassword.isErrorEnabled = true
                    logintilPassword.error = "Please give your password"
                    return@setOnClickListener
                }
                else if (!isValidPassword(loginetPassword.text.toString().trim()) || loginetPassword.text.toString().trim().length<8){
                    logintilPassword.isErrorEnabled = true
                    logintilPassword.error = "Password should be minimum of 8 characters and must contain at least one Upper case and special characters"
                    return@setOnClickListener
                }
            }
            progressBar.show()
            auth.signInWithEmailAndPassword(binding.loginetUserid.text.toString(),binding.loginetPassword.text.toString()).addOnSuccessListener {
                getuserDetails(auth.currentUser!!.uid)
            }.addOnFailureListener {
                progressBar.dismiss()
                Toast.makeText(this@LoginActivity,"Incorrect email or password",Toast.LENGTH_SHORT).show()
            }

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

    private fun getuserDetails(userid: String) {
        var dataBase = FirebaseDatabase.getInstance()
        var datareference = dataBase.getReference("User")
        datareference.child(userid).child("password").setValue(binding.loginetPassword.text.toString()).addOnSuccessListener {
            datareference.child(userid).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    progressBar.dismiss()
                    val userData = snapshot.getValue(AppUser::class.java)
                    if (userData!=null){
                        val userDetails = getSharedPreferences("userDetails",Context.MODE_PRIVATE)
                        var editor = userDetails.edit()
                        editor.putString("userId",userData.userId)
                        editor.putString("userName",userData.name)
                        editor.putString("userMail",userData.emailId)
                        editor.putString("userPhone",userData.phone)
                        editor.apply()
                        refresh()
                        startActivity(Intent(this@LoginActivity,MainActivity::class.java))
                    }
                    else{
                        Toast.makeText(this@LoginActivity,"Empty data for $userid",Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    progressBar.dismiss()
                    Toast.makeText(this@LoginActivity,"Cannot get user data for $userid",Toast.LENGTH_SHORT).show()
                }
            })
        }
            .addOnFailureListener {
                progressBar.dismiss()
                Toast.makeText(this@LoginActivity,"User database error",Toast.LENGTH_SHORT).show()
            }

    }

    fun isValidPassword(password: String?): Boolean {
        val pattern: Pattern
        val PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{4,}$"
        pattern = Pattern.compile(PASSWORD_PATTERN)
        val matcher: Matcher? = password?.let { pattern.matcher(it) }
        return matcher!!.matches()
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