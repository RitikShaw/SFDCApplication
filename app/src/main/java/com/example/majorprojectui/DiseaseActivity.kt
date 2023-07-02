package com.example.majorprojectui

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log.e
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import com.example.majorprojectui.AuthActivities.LoginActivity
import com.example.majorprojectui.Models.DiseaseData
import com.example.majorprojectui.databinding.ActivityDiseaseBinding
import com.example.majorprojectui.utills.LoginUtills
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase

class DiseaseActivity : AppCompatActivity() {
    private lateinit var binding : ActivityDiseaseBinding
    private lateinit var database: DatabaseReference
    private lateinit var drawerToggle: ActionBarDrawerToggle
    private val auth = Firebase.auth

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

        binding.navigationView.setNavigationItemSelectedListener { menuItem ->
            when(menuItem.itemId){
                R.id.Profile ->{
                    binding.drawerLayout.closeDrawers()
                    startActivity(Intent(this,UserProfileActivity::class.java))
                    true
                }
                R.id.logout-> {
                    binding.drawerLayout.closeDrawers()
                    auth.signOut()
                    startActivity(Intent(this, LoginActivity::class.java))
                    true
                }
                else -> false
            }
        }

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
        var dataBase = FirebaseDatabase.getInstance()
        var datareference = dataBase.getReference("DiseaseData")
        datareference.child(disease).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                progressBar.dismiss()
                val diseaseData = snapshot.getValue(DiseaseData::class.java)
                if (diseaseData!=null){
                    binding.apply {
                        tvDiseaseName.text = disease
                        tvDetails.text = diseaseData.details
                        tvSymptoms.text = diseaseData.symptoms
                        tvCure.text = diseaseData.cure
                        val medicineList = diseaseData.medicine!!.split(",")
                        val medlinkList = diseaseData.medlinks!!.split(",")
                        val medicineArrayList = ArrayList<String>()
                        medicineList.forEach {
                            medicineArrayList.add(it)
                        }
                        tvMedicines.text = diseaseData.medicine.replace(",","\n")
                    }
                }
                else{
                    Toast.makeText(this@DiseaseActivity,"Empty data for $disease", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onCancelled(error: DatabaseError) {
                progressBar.dismiss()
                Toast.makeText(this@DiseaseActivity,"Cannot get find data for $disease", Toast.LENGTH_SHORT).show()
            }
        })
    }


}