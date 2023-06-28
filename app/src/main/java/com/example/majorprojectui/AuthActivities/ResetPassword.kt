package com.example.majorprojectui.AuthActivities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.widget.doAfterTextChanged
import com.example.majorprojectui.databinding.ActivityResetPasswordBinding

class ResetPassword : AppCompatActivity() {
    private lateinit var binding : ActivityResetPasswordBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResetPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            resetetOpassword.doAfterTextChanged {
                resettilOpassword.isErrorEnabled = false
            }
            resetetPassword.doAfterTextChanged {
                resettilPassword.isErrorEnabled = false
            }
            resetetCpassword.doAfterTextChanged {
                resettilCpassword.isErrorEnabled = false
            }
        }

        binding.updatebtn.setOnClickListener {
            binding.apply {
                if (resetetOpassword.text!!.trim().isEmpty()){
                    resettilOpassword.isErrorEnabled = true
                    resetetOpassword.error = "Please enter old password"
                }
                if (resetetPassword.text!!.trim().isEmpty()){
                    resettilOpassword.isErrorEnabled = true
                    resettilOpassword.error = "Please enter new password"
                }
                if (resetetCpassword.text!!.trim().isEmpty()){
                    resettilCpassword.isErrorEnabled = true
                    resettilCpassword.error = "Please enter old password"
                }
            }
        }
    }
}