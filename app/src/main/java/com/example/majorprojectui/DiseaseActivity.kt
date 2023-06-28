package com.example.majorprojectui

import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log.e
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import com.example.majorprojectui.databinding.ActivityDiseaseBinding
import com.example.majorprojectui.utills.LoginUtills
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class DiseaseActivity : AppCompatActivity() {
    private lateinit var binding : ActivityDiseaseBinding
    private lateinit var database: DatabaseReference
    private lateinit var drawerToggle: ActionBarDrawerToggle

    private lateinit var progressBar : ProgressDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDiseaseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        drawerToggle = ActionBarDrawerToggle(this@DiseaseActivity,binding.drawerLayout,binding.toolbar,R.string.nav_open,R.string.nav_close)
        binding.drawerLayout.addDrawerListener(drawerToggle!!)
        drawerToggle.syncState()
        val headerView = binding.navigationView.getHeaderView(0)
        val userName = headerView.findViewById<TextView>(R.id.tv_userName)
        val userMail = headerView.findViewById<TextView>(R.id.tv_userMail)
        userName.text=LoginUtills.username
        userMail.text=LoginUtills.usermail

        progressBar = ProgressDialog(this)
        progressBar.apply {
            val view = layoutInflater.inflate(R.layout.custom_progressbar,null)
            this.setContentView(view)
            val title = view.findViewById<TextView>(R.id.tv_text)
            title.text = "Please wait ..."
            setMessage("Authenticating...")
        }

        var disease = "Downey Mildew"
        try {
            val intent = intent
            disease = intent.getStringExtra("disease").toString()
            e("disease",disease)
            binding.tvDiseaseName.text = disease
            binding.imvDesease.setImageBitmap(LoginUtills.imageBitmap)
        }
        catch (e : Exception){
            e.printStackTrace()
        }

        getData(disease)
// ..
    }

    private fun getData(disease: String) {
        progressBar.show()
        database = FirebaseDatabase.getInstance().reference
        database.child("DiseaseData").child(disease).child("details").get().addOnSuccessListener {
            e("firebase", "Got value ${it.value}")
            binding.tvDetails.text = it.value.toString()
        }.addOnFailureListener{
            e("firebase", "Error getting data", it)
        }
        database.child("DiseaseData").child(disease).child("cure").get().addOnSuccessListener {
            e("firebase", "Got value ${it.value}")
            binding.tvCure.text = it.value.toString()
        }.addOnFailureListener{
            e("firebase", "Error getting data", it)
        }
        database.child("DiseaseData").child(disease).child("medicine").get().addOnSuccessListener {
            e("firebase", "Got value ${it.value}")
            binding.tvMedicines.text = it.value.toString()
        }.addOnFailureListener{
            e("firebase", "Error getting data", it)
        }
        database.child("DiseaseData").child(disease).child("symptoms").get().addOnSuccessListener {
            progressBar.dismiss()
            e("firebase", "Got value ${it.value}")
            binding.tvSymptoms.text = it.value.toString()
        }.addOnFailureListener{
            e("firebase", "Error getting data", it)
        }
    }


}