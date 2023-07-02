package com.example.majorprojectui

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import com.example.majorprojectui.AuthActivities.LoginActivity
import com.example.majorprojectui.Models.AppUser
import com.example.majorprojectui.databinding.ActivityUserProfileBinding
import com.example.majorprojectui.utills.LoginUtills
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import java.util.regex.Matcher
import java.util.regex.Pattern


class UserProfileActivity : AppCompatActivity() {
    private lateinit var progressBar : ProgressDialog
    private lateinit var binding : ActivityUserProfileBinding
    private lateinit var drawerToggle: ActionBarDrawerToggle
    private val auth = Firebase.auth
    private var password = ""

    val dataBase = FirebaseDatabase.getInstance()
    val datareference = dataBase.getReference("User")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressBar = ProgressDialog(this)

        progressBar.apply {
            val view = layoutInflater.inflate(R.layout.custom_progressbar,null)
            this.setContentView(view)
            val title = view.findViewById<TextView>(R.id.tv_text)
            title.text = "Please wait ..."
            setMessage("Authenticating...")
        }

        setUserData()

        drawerToggle = ActionBarDrawerToggle(this@UserProfileActivity,binding.drawerLayout,binding.toolbar,R.string.nav_open,R.string.nav_close)
        binding.drawerLayout.addDrawerListener(drawerToggle!!)
        drawerToggle.syncState()
        val headerView = binding.navigationView.getHeaderView(0)
        val userName = headerView.findViewById<TextView>(R.id.tv_userName)
        val userMail = headerView.findViewById<TextView>(R.id.tv_userMail)
        userName.text=LoginUtills.username
        userMail.text=LoginUtills.usermail

        binding.apply {
            etuserProfileUsername.doAfterTextChanged {
                tiluserProfileUsername.isErrorEnabled = false
            }
            etuserProfilePhone.doAfterTextChanged {
                tiluserProfilePhone.isErrorEnabled = false
            }
            etuserProfileEmail.doAfterTextChanged {
                tiluserProfileEmail.isErrorEnabled = false
            }
            etuserProfilePassword.doAfterTextChanged {
                tiluserProfilePassword.isErrorEnabled = false
            }
        }

        binding.navigationView.setNavigationItemSelectedListener { menuItem ->
            when(menuItem.itemId){
                R.id.Profile ->{
                    binding.drawerLayout.closeDrawers()
                    startActivity(Intent(this,UserProfileActivity::class.java))
                    finish()
                    true
                }
                R.id.logout-> {
                    binding.drawerLayout.closeDrawers()
                    auth.signOut()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }


        binding.updatebtn.setOnClickListener {
            binding.apply {
                if (etuserProfileUsername.text.toString().trim().isEmpty()){
                    tiluserProfileUsername.isErrorEnabled=true
                    tiluserProfileUsername.error = "Please provide your name"
                    return@setOnClickListener
                }
                else if (etuserProfilePhone.text.toString().trim().isEmpty()){
                    tiluserProfilePhone.isErrorEnabled = true
                    tiluserProfilePhone.error = "Please provide your phone number"
                    return@setOnClickListener
                }
                else if (etuserProfileEmail.text.toString().trim().isEmpty()){
                    tiluserProfileEmail.isErrorEnabled = true
                    tiluserProfileEmail.error = "Please provide your email Id"
                    return@setOnClickListener
                }
                else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(
                        etuserProfileEmail.text.toString().trim()
                    ).matches()
                ) {
                    tiluserProfileEmail.isErrorEnabled = true
                    tiluserProfileEmail.error = "Please provide valid email Id"
                    return@setOnClickListener
                }
                else if (etuserProfilePassword.text.toString().trim().isEmpty()){
                    tiluserProfilePassword.isErrorEnabled = true
                    tiluserProfilePassword.error = "Please provide a password"
                    return@setOnClickListener
                }
                else if (!isValidPassword(etuserProfilePassword.text.toString().trim()) || etuserProfilePassword.text.toString().trim().length<8){
                    tiluserProfilePassword.isErrorEnabled = true
                    tiluserProfilePassword.error = "Password should be minimum of 8 characters and must contain at least one Upper case and special characters"
                    return@setOnClickListener
                }
            }
            val userId = auth.currentUser!!.uid
            val name = binding.etuserProfileUsername.text.toString()
            val phone = binding.etuserProfilePhone.text.toString()
            val email = binding.etuserProfileEmail.text.toString()
            val newpassword = binding.etuserProfilePassword.text.toString()
            val userData = AppUser(userId,name,phone,email,newpassword)

            if (password != newpassword){
                updatePassword(userData)
            }
            else{
                updateProfile(userData)
            }
        }
    }

    private fun setUserData() {
        val userId = auth.currentUser!!.uid
        datareference.child(userId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userData = snapshot.getValue(AppUser::class.java)
                if (userData!=null){
                    binding.etuserProfileUsername.setText(userData.name)
                    binding.etuserProfilePhone.setText(userData.phone)
                    binding.etuserProfileEmail.setText(userData.emailId)
                    password = userData.password.toString()
                    binding.etuserProfilePassword.setText(userData.password.toString())

                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@UserProfileActivity,"Cannot find user data for $userId $error",Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateProfile(
        userData: AppUser
    ) {
        progressBar.show()
        datareference.child(userData.userId.toString()).setValue(userData).addOnSuccessListener {
            Toast.makeText(this@UserProfileActivity,"Updated",Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(this@UserProfileActivity,"Cannot update data $it",Toast.LENGTH_SHORT).show()
        }
    }

    private fun updatePassword(userData: AppUser){
        val user = auth.currentUser
        val credential = EmailAuthProvider
            .getCredential(userData.emailId.toString(), password)
        user!!.reauthenticate(credential).addOnCompleteListener {
            if (it.isSuccessful){
                user.updatePassword(binding.etuserProfilePassword.text.toString()).addOnCompleteListener {
                    updateProfile(userData)
                }.addOnFailureListener {
                    Toast.makeText(this@UserProfileActivity,"Cannot update password $it",Toast.LENGTH_SHORT).show()
                }
            }

        }
            .addOnFailureListener {
                Toast.makeText(this@UserProfileActivity,"Update password failed $it",Toast.LENGTH_SHORT).show()
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
        super.onBackPressed()
        startActivity(Intent(this@UserProfileActivity,MainActivity::class.java))
    }
}