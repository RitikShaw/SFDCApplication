package com.example.majorprojectui

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.appcompat.app.ActionBarDrawerToggle
import com.example.majorprojectui.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var drawerToggle: ActionBarDrawerToggle
    private val requestCode = 101
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        binding.btnTakePic.setOnClickListener {
            getPermission()
        }


        binding.cardDeseaseRemedy.setOnClickListener {
            startActivity(Intent(this@MainActivity,DiseaseActivity::class.java))
        }

        drawerToggle = ActionBarDrawerToggle(this@MainActivity,binding.drawerLayout,toolbar,R.string.nav_open,R.string.nav_close)
        binding.drawerLayout.addDrawerListener(drawerToggle!!)
        drawerToggle.syncState()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 101 && resultCode == RESULT_OK){
            val imageBitmap = data?.extras?.get("data") as Bitmap
            binding.imgCapturedPic.setImageBitmap(imageBitmap)
        }
    }

    private fun getPermission() {
        if( checkSelfPermission(android.Manifest.permission.CAMERA)==PackageManager.PERMISSION_GRANTED && checkSelfPermission(
                android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
        {
            val cameraService = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(cameraService,requestCode)
        }
        else {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CAMERA),requestCode)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == this.requestCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this@MainActivity, "Camera Permission Granted", Toast.LENGTH_SHORT).show()
                val cameraService = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                startActivityForResult(cameraService,requestCode)
            } else {
                Toast.makeText(this@MainActivity, "Camera Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
}