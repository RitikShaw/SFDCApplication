package com.example.majorprojectui

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.media.ThumbnailUtils
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log.e
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import com.example.majorprojectui.AuthActivities.LoginActivity
import com.example.majorprojectui.databinding.ActivityMainBinding
import com.example.majorprojectui.ml.LiteLeafmodelBest
import com.example.majorprojectui.ml.LiteSfdnetModel
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.nio.ByteBuffer
import java.nio.ByteOrder

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var drawerToggle: ActionBarDrawerToggle
    private var requestCode = 0
    private val imageSize  = 256
    private var disease = ""
    private var ishealthy = false
    public lateinit var selectedImage: Bitmap
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        binding.btnTakePic.setOnClickListener {
            requestCode = 1

            getPermission(requestCode)
        }


        binding.cardDeseaseRemedy.setOnClickListener {
            if (disease.isEmpty()){
                Toast.makeText(this,"Please scan your plant for data",Toast.LENGTH_SHORT).show()
//                return@setOnClickListener
            }
            if (ishealthy){
                Toast.makeText(this,"No disease found for your plant",Toast.LENGTH_SHORT).show()
//                return@setOnClickListener
            }
            val intent = Intent(this@MainActivity,DiseaseActivity::class.java)
            intent.putExtra("disease",disease)
            intent.putExtra("image",selectedImage)
            startActivity(intent)
        }

        drawerToggle = ActionBarDrawerToggle(this@MainActivity,binding.drawerLayout,toolbar,R.string.nav_open,R.string.nav_close)
        binding.drawerLayout.addDrawerListener(drawerToggle!!)
        drawerToggle.syncState()
        val headerView = binding.navigationView.getHeaderView(0)
        val userName = headerView.findViewById<TextView>(R.id.tv_userName)
        val userMail = headerView.findViewById<TextView>(R.id.tv_userMail)

        val userDetails = getSharedPreferences("userDetails", Context.MODE_PRIVATE)
        userName.text=userDetails.getString("userName","defaultName")
        userMail.text=userDetails.getString("userMail","defaultName")

        binding.navigationView.setNavigationItemSelectedListener { menuItem ->
            when(menuItem.itemId){
                R.id.logout-> {
                    binding.drawerLayout.closeDrawers()
                    startActivity(Intent(this,LoginActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }
    }

    private fun getPermission(event : Int) {
        if( checkSelfPermission(android.Manifest.permission.CAMERA)==PackageManager.PERMISSION_GRANTED && checkSelfPermission(
                android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
        {
            if (event == 0) {
                val galleryService =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(galleryService, event)
            }
            else{
                val cameraService =
                    Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                startActivityForResult(cameraService, event)
            }
        }
        else {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CAMERA,android.Manifest.permission.READ_EXTERNAL_STORAGE),requestCode)
        }
    }
    @RequiresApi(Build.VERSION_CODES.P)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK){
            if (requestCode == 0){
                val imagedata = data!!.data
                var imageBitmap: Bitmap? = null
                var source =ImageDecoder.createSource(this.contentResolver, imagedata!!)
                imageBitmap = ImageDecoder.decodeBitmap(source)
                binding.imgCapturedPic.setImageBitmap(imageBitmap)
                selectedImage = imageBitmap

                imageBitmap =Bitmap.createScaledBitmap(imageBitmap,256,256,false)
                classify(imageBitmap)
            }
            else{
                var imageBitmap = data?.extras?.get("data") as Bitmap
                val dimensions = Math.min(imageBitmap.width,imageBitmap.height)
                imageBitmap = ThumbnailUtils.extractThumbnail(imageBitmap,dimensions,dimensions)
                binding.imgCapturedPic.setImageBitmap(imageBitmap)
                selectedImage = imageBitmap

                imageBitmap = Bitmap.createScaledBitmap(imageBitmap,256,256,false)
                classify(imageBitmap)
            }
        }
    }

    private fun classify(imageBitmap: Bitmap?) {
        val model = LiteLeafmodelBest.newInstance(this)

// Creates inputs for reference.
        val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, imageSize, imageSize, 3), DataType.FLOAT32)

        var byteBuffer = ByteBuffer.allocateDirect(4*imageSize*imageSize*3)
        byteBuffer.order(ByteOrder.nativeOrder())

        val intValue = IntArray(imageSize * imageSize)
        try {
            imageBitmap!!.getPixels(intValue,0, imageBitmap.width,0,0, imageBitmap.width, imageBitmap.height)
        }
        catch (e : Exception){
            e.printStackTrace()
        }

        var pixels= 0
        for (i in 0 until imageSize){
            for (j in 0 until imageSize){
                val values = intValue[pixels++] //RGB
                byteBuffer.putFloat((values shr 16 and 0xFF) * (1f / 255))
                byteBuffer.putFloat((values shr 8 and 0xFF) * (1f / 255))
                byteBuffer.putFloat((values and 0xFF) * (1f / 255))
            }
        }

        inputFeature0.loadBuffer(byteBuffer)

// Runs model inference and gets result.
        val outputs = model.process(inputFeature0)
        val outputFeature0 = outputs.outputFeature0AsTensorBuffer

        var confidence = outputFeature0.floatArray

        e("leafconfidence",confidence[0].toString())


        if (confidence[0] < 0.5){
            binding.tvResult.text="Not Leaf"
            Toast.makeText(this,"Not Leaf", Toast.LENGTH_SHORT).show()
        }
        else{
            classifyDisease(imageBitmap)
            Toast.makeText(this,"Leaf", Toast.LENGTH_SHORT).show()
        }
// Releases model resources if no longer used.
        model.close()
    }

    private fun classifyDisease(imageBitmap: Bitmap?) {
        val model = LiteSfdnetModel.newInstance(this)

// Creates inputs for reference.
        val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, imageSize, imageSize, 3), DataType.FLOAT32)

        var byteBuffer = ByteBuffer.allocateDirect(4*imageSize*imageSize*3)
        byteBuffer.order(ByteOrder.nativeOrder())

        val intValue = IntArray(imageSize * imageSize)
        try {
            imageBitmap!!.getPixels(intValue,0, imageBitmap.width,0,0, imageBitmap.width, imageBitmap.height)
        }
        catch (e : Exception){
            e.printStackTrace()
        }

        var pixels= 0
        for (i in 0 until imageSize){
            for (j in 0 until imageSize){
                val values = intValue[pixels++] //RGB
                byteBuffer.putFloat((values shr 16 and 0xFF) * (1f / 255))
                byteBuffer.putFloat((values shr 8 and 0xFF) * (1f / 255))
                byteBuffer.putFloat((values and 0xFF) * (1f / 255))
            }
        }

        inputFeature0.loadBuffer(byteBuffer)

// Runs model inference and gets result.
        val outputs = model.process(inputFeature0)
        val outputFeature0 = outputs.outputFeature0AsTensorBuffer

        var confidence = outputFeature0.floatArray

        var max = 0

        e("size",confidence.size.toString())

        for (i in confidence.indices){
            e("confidence","${confidence[i]} $i")
            if (confidence[max]<confidence[i]){
                max = i
            }
        }

        when (max) {
            0-> {
                binding.tvResult.text = "anthracnose"
                disease = "Anthracnose"
            }
            1-> {
                binding.tvResult.text = "Downey Mildew"
                disease = "Cercospora"
                disease = "Downey Mildew"
            }
            2-> {
                binding.tvResult.text = "Cercospora"
                disease = "Cladosporium"
            }
            3-> {
                binding.tvResult.text = "Downy Mildew"
            }
            4-> {
                binding.tvResult.text = "Healthy"
                ishealthy = true
                disease = "Healthy"
            }

            else -> binding.tvResult.text = "Healthy"
        }
        e("maxconfidence",max.toString())


    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 0) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this@MainActivity, "Gallery Permission Granted", Toast.LENGTH_SHORT).show()
                val cameraService = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(cameraService,requestCode)
            } else {
                Toast.makeText(this@MainActivity, "Gallery Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
        else{
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