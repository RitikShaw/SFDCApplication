package com.example.majorprojectui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log.e
import com.example.majorprojectui.databinding.ActivityDiseaseBinding
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class DiseaseActivity : AppCompatActivity() {
    private lateinit var binding : ActivityDiseaseBinding
    private lateinit var database: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDiseaseBinding.inflate(layoutInflater)
        setContentView(binding.root)
        var disease = ""
        try {
            val intent = intent
            disease = intent.getStringExtra("disease").toString()
        }
        catch (e : Exception){
            e.printStackTrace()
        }


// ...
        database = FirebaseDatabase.getInstance().reference
        database.child("DiseaseData").child(disease).child("details").get().addOnSuccessListener {
            e("firebase", "Got value ${it.value}")
            binding.tvDetails.text = it.value.toString()
        }.addOnFailureListener{
            e("firebase", "Error getting data", it)
        }
    }
}