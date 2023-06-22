package com.example.majorprojectui

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log.e
import androidx.appcompat.app.AppCompatActivity
import com.example.majorprojectui.databinding.ActivityDiseaseBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class DiseaseActivity : AppCompatActivity() {
    private lateinit var binding : ActivityDiseaseBinding
    private lateinit var database: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDiseaseBinding.inflate(layoutInflater)
        setContentView(binding.root)
        var disease = "Downey Mildew"
        try {
            val intent = intent
//            disease = intent.getStringExtra("disease").toString()
            val bitmap = intent.getParcelableExtra("BitmapImage") as Bitmap?
            binding.imvDesease.setImageBitmap(bitmap)
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
            e("firebase", "Got value ${it.value}")
            binding.tvSymptoms.text = it.value.toString()
        }.addOnFailureListener{
            e("firebase", "Error getting data", it)
        }
    }


}