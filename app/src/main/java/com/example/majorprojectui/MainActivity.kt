package com.example.majorprojectui

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.media.ThumbnailUtils
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log.e
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
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
import com.example.majorprojectui.utills.LoginUtills
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.math.BigDecimal
import java.nio.ByteBuffer
import java.nio.ByteOrder

class MainActivity : AppCompatActivity() {
    private var doubleBackToExitPressedOnce = false
    private lateinit var binding: ActivityMainBinding
    private lateinit var drawerToggle: ActionBarDrawerToggle
    private var requestCode = 0
    private val imageSize  = 256
    private var disease = ""
    private var ishealthy = false
    private var isleaf = false
    var selectedImage: Bitmap? = null
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        auth = Firebase.auth

        binding.btnTakePic.setOnClickListener {
            galleryCameraDialog()

        }


        binding.cardDeseaseRemedy.setOnClickListener {
            if (disease.isEmpty()){
                Toast.makeText(this,"Please scan your plant for data",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!isleaf){
                Toast.makeText(this,"Image is not leaf",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (ishealthy){
                Toast.makeText(this,"No disease found for your plant",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val intent = Intent(this@MainActivity,DiseaseActivity::class.java)
            intent.putExtra("disease",disease)
            LoginUtills.imageBitmap = selectedImage
            startActivity(intent)
        }

        drawerToggle = ActionBarDrawerToggle(this@MainActivity,binding.drawerLayout,toolbar,R.string.nav_open,R.string.nav_close)
        binding.drawerLayout.addDrawerListener(drawerToggle!!)
        drawerToggle.syncState()
        val headerView = binding.navigationView.getHeaderView(0)
        val userName = headerView.findViewById<TextView>(R.id.tv_userName)
        val userMail = headerView.findViewById<TextView>(R.id.tv_userMail)

        val userDetails = getSharedPreferences("userDetails", Context.MODE_PRIVATE)

        LoginUtills.userId = userDetails.getString("userId","defaultName").toString()
        LoginUtills.username = userDetails.getString("userName","defaultName").toString()
        LoginUtills.usermail = userDetails.getString("userMail","defaultName").toString()
        LoginUtills.userphone = userDetails.getString("userPhone","defaultName").toString()

        userName.text=LoginUtills.username
        userMail.text=LoginUtills.usermail

        binding.llContribute.setOnClickListener {
            startActivity(Intent(this,ContributionActivity::class.java))
        }

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
                    startActivity(Intent(this,LoginActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    private fun galleryCameraDialog() {
        val alertDialogBuilder = Dialog(this)
        alertDialogBuilder.requestWindowFeature(Window.FEATURE_NO_TITLE)
        alertDialogBuilder.setContentView(R.layout.camera_gallery_popup)

        val gallery = alertDialogBuilder.findViewById<ImageView>(R.id.gallery)
        val camera = alertDialogBuilder.findViewById<ImageView>(R.id.camera)

        gallery.setOnClickListener {
            requestCode = 0
            getPermission(requestCode)
            alertDialogBuilder.dismiss()
        }

        camera.setOnClickListener {
            requestCode = 1
            getPermission(requestCode)
            alertDialogBuilder.dismiss()
        }
        alertDialogBuilder.show()
        val lp = alertDialogBuilder.window?.attributes
        if (lp != null) {
            lp.dimAmount = 0.9f
            lp.buttonBrightness = 1.0f
            lp.screenBrightness = android.provider.Settings.System.getInt(
                contentResolver, android.provider.Settings.System.SCREEN_BRIGHTNESS).toFloat()
        }
        alertDialogBuilder.window?.attributes = lp
        alertDialogBuilder.window?.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
    }

    private fun getPermission(event : Int) {
        if( checkSelfPermission(android.Manifest.permission.CAMERA)==PackageManager.PERMISSION_GRANTED && checkSelfPermission(
                android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
        {
            if (event == 0) {
                val galleryService =
                    Intent(Intent.ACTION_GET_CONTENT).also {
                        it.type = "image/*"
                        it.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,false)
                    }
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
                imageBitmap =MediaStore.Images.Media.getBitmap(this.contentResolver, imagedata!!)
                val dimensions = Math.min(imageBitmap.width,imageBitmap.height)
                imageBitmap = ThumbnailUtils.extractThumbnail(imageBitmap,dimensions,dimensions)
                binding.imgCapturedPic.setImageBitmap(imageBitmap)
                selectedImage = imageBitmap
                e("selectedImage",selectedImage.toString())

                imageBitmap =Bitmap.createScaledBitmap(imageBitmap,256,256,false)
                classify(imageBitmap)
            }
            else{
                var imageBitmap = data?.extras?.get("data") as Bitmap
                val dimensions = Math.min(imageBitmap.width,imageBitmap.height)
                imageBitmap = ThumbnailUtils.extractThumbnail(imageBitmap,dimensions,dimensions)
                binding.imgCapturedPic.setImageBitmap(imageBitmap)
                selectedImage = imageBitmap
                e("selectedImage",selectedImage.toString())

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
            isleaf = false
            binding.tvResult.text="Not Leaf"
            disease = "Not Leaf"
            binding.tvResult.setTextColor(Color.RED)
            binding.tvConfidence.visibility = View.INVISIBLE
            Toast.makeText(this,"Not Leaf", Toast.LENGTH_SHORT).show()
        }
        else{
            isleaf = true
            binding.tvResult.setTextColor(resources.getColor(R.color.textcolor1))
            binding.tvConfidence.visibility = View.VISIBLE
            classifyDisease(imageBitmap)
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
            e("confidence",BigDecimal((confidence[i]).toString()).toString())
            if (confidence[max]<confidence[i]){
                max = i
            }
        }

        when (max) {

            0-> {
                binding.tvResult.text = "Anthracnose"
                disease = "Anthracnose"
                ishealthy = false
                binding.tvConfidence.text =
                    "${String.format("%.2f", BigDecimal((confidence[max] * 100).toString()))}%"
            }
            1-> {
                binding.tvResult.text = "Cercospora"
                disease = "Cercospora"
                ishealthy = false
                binding.tvConfidence.text =
                    "${String.format("%.2f", BigDecimal((confidence[max] * 100).toString()))}%"
            }
            2-> {
                binding.tvResult.text = "Cladosporium"
                disease = "Cladosporium"
                ishealthy = false
                binding.tvConfidence.text =
                    "${String.format("%.2f", BigDecimal((confidence[max] * 100).toString()))}%"
            }
            3-> {
                binding.tvResult.text = "Downy Mildew"
                disease = "Downy Mildew"
                ishealthy = false
                binding.tvConfidence.text =
                    "${String.format("%.2f", BigDecimal((confidence[max] * 100).toString()))}%"
            }
            4-> {
                binding.tvResult.text = "Healthy"
                binding.tvResult.setTextColor(Color.GREEN)
                ishealthy = true
                disease = "Healthy"
                binding.tvConfidence.text =
                    "${String.format("%.2f", BigDecimal((confidence[max] * 100).toString()))}%"
            }
            5-> {
                binding.tvResult.text = "Stemphylium leaf spot"
                disease = "Stemphylium leaf spot"
                ishealthy = false
                binding.tvConfidence.text =
                    "${String.format("%.2f", BigDecimal((confidence[max] * 100).toString()))}%"
            }
            else -> binding.tvResult.text = "Healthy"
        }
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