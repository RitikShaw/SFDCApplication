package com.example.majorprojectui.AuthActivities

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import com.example.majorprojectui.R
import com.example.majorprojectui.databinding.ActivityResetPasswordBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class ResetPassword : AppCompatActivity() {
    private lateinit var binding : ActivityResetPasswordBinding
    lateinit var progressBar : ProgressDialog
    private val auth = Firebase.auth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResetPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressBar = ProgressDialog(this)

        progressBar.apply {
            val view = layoutInflater.inflate(R.layout.custom_progressbar,null)
            this.setContentView(view)
            val title = view.findViewById<TextView>(R.id.tv_text)
            title.text = "Please wait ..."
            setMessage("Authenticating...")
        }

        binding.apply {
            resetetEmail.doAfterTextChanged {
                resettilEmail.isErrorEnabled = false
            }
            /*resetetOpassword.doAfterTextChanged {
                resettilOpassword.isErrorEnabled = false
            }
            resetetPassword.doAfterTextChanged {
                resettilPassword.isErrorEnabled = false
            }
            resetetCpassword.doAfterTextChanged {
                resettilCpassword.isErrorEnabled = false
            }*/
        }

        binding.updatebtn.setOnClickListener {
            binding.apply {
                if (resetetEmail.text!!.trim().isEmpty()){
                    resettilEmail.isErrorEnabled = true
                    resettilEmail.error = "Please enter email"
                }
                else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(
                        resetetEmail.text.toString().trim()
                    ).matches()
                ) {
                    resettilEmail.isErrorEnabled = true
                    resettilEmail.error = "Please provide valid email Id"
                    return@setOnClickListener
                }
                /*if (resetetOpassword.text!!.trim().isEmpty()){
                    resettilOpassword.isErrorEnabled = true
                    resettilOpassword.error = "Please enter old password"
                }
                if (resetetPassword.text!!.trim().isEmpty()){
                    resettilOpassword.isErrorEnabled = true
                    resettilOpassword.error = "Please enter new password"
                }
                if (resetetCpassword.text!!.trim().isEmpty()){
                    resettilCpassword.isErrorEnabled = true
                    resettilCpassword.error = "Please enter old password"
                }*/
                updatePassword()
            }
        }
    }

    private fun updatePassword() {
        /*FirebaseApp.initializeApp(this)
        var dataBase = FirebaseDatabase.getInstance()
        var datareference = dataBase.getReference("User")
        datareference.child(binding.resetetEmail.text.toString()).get().addOnSuccessListener {

            if (it.exists()){
                var password = it.child("password").value.toString()

                if (binding.resetetOpassword.text.toString() == password){
                    datareference.child(binding.resetetEmail.text.toString()).child("password").setValue(binding.resetetPassword.text.toString()).addOnSuccessListener {
                        progressBar.dismiss()
                        Toast.makeText(this,"Password Changed Successfully", Toast.LENGTH_SHORT).show()
                    }.addOnFailureListener {
                        progressBar.dismiss()
                        Toast.makeText(this,it.toString(), Toast.LENGTH_SHORT).show()
                    }
                    progressBar.dismiss()

                    startActivity(Intent(this, MainActivity::class.java))
                }
                else{
                    progressBar.dismiss()
                    Toast.makeText(this,"invalid password", Toast.LENGTH_SHORT).show()
                }
            }
            else{
                progressBar.dismiss()
                Toast.makeText(this,"No user found", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            progressBar.dismiss()
            Toast.makeText(this,it.toString(), Toast.LENGTH_SHORT).show()
        }*/
        auth.sendPasswordResetEmail(binding.resetetEmail.text.toString()).addOnCompleteListener {
            Toast.makeText(this@ResetPassword,"Please check your email inbox to reset password",Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(this@ResetPassword,"Send email failed $it",Toast.LENGTH_SHORT).show()
        }


    }
}