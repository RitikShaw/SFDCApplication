package com.example.majorprojectui

import android.annotation.SuppressLint
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.view.Window
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.net.toUri
import androidx.recyclerview.widget.GridLayoutManager
import com.example.majorprojectui.AuthActivities.LoginActivity
import com.example.majorprojectui.adapter.ContributionAdapter
import com.example.majorprojectui.databinding.ActivityContributionBinding
import com.example.majorprojectui.utills.LoginUtills
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.io.ByteArrayOutputStream
import java.io.File

class ContributionActivity : AppCompatActivity() {
    lateinit var binding: ActivityContributionBinding
    private lateinit var progressBar : ProgressDialog
    private lateinit var imageAdapter : ContributionAdapter
    private lateinit var drawerToggle: ActionBarDrawerToggle
    private val auth = Firebase.auth

    private var imageuriList = mutableListOf<Uri?>()
    private var filenameList = mutableListOf<String?>()
    private var currentFile : Uri? = null
    var storageRef = Firebase.storage.reference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContributionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        binding.toolbar.title = "Contribution"

        drawerToggle = ActionBarDrawerToggle(this@ContributionActivity,binding.drawerLayout,binding.toolbar,R.string.nav_open,R.string.nav_close)
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
            setMessage("Uploading...")
            setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
        }

        imageAdapter = ContributionAdapter(imageuriList)

        val diseaseArray = arrayOf("--Choose Disease--","Anthracnose", "Downey Mildew", "Cercospora", "Cladosporium","Stemphylium leaf spot")
        var diseaseAdapter = ArrayAdapter<String>(
            this,
            androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,
            diseaseArray
        )

        binding.spDisease.adapter = diseaseAdapter

        binding.btnTakePic.setOnClickListener {
            galleryCameraDialog()
        }

        binding.apply {
            btnUpload.setOnClickListener {
                if (imageuriList.isEmpty()){
                    Toast.makeText(
                        this@ContributionActivity,
                        "Please select an image",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }
                if (spDisease.selectedItemPosition == 0) {
                    Toast.makeText(
                        this@ContributionActivity,
                        "Please choose a disease",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }

                try {
                    progressBar.show()
                    var count = 0
                    for (position in 0 until imageuriList.size){
                        count++
                        currentFile = imageuriList[position]
                        uploadImageToCloud(filenameList[position])
                    }
                    if (count==imageuriList.size){
                        imageuriList.clear()
                        filenameList.clear()
                        progressBar.dismiss()
                        setUpRecyclerView()
                    }
                }
                catch (e:Exception) {
                    e.printStackTrace()
                }
            }
        }

    }

    @SuppressLint("MissingInflatedId")
    private fun galleryCameraDialog() {
        val alertDialogBuilder = Dialog(this)
        alertDialogBuilder.requestWindowFeature(Window.FEATURE_NO_TITLE)
        alertDialogBuilder.setContentView(R.layout.camera_gallery_popup)

        val gallery = alertDialogBuilder.findViewById<ImageView>(R.id.gallery)
        val camera = alertDialogBuilder.findViewById<ImageView>(R.id.camera)

        gallery.setOnClickListener {
            Intent(Intent.ACTION_GET_CONTENT).also {
                it.type = "image/*"
                it.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true)
                imageLauncher.launch(Intent.createChooser(it,"Choose Photos"))
            }
            alertDialogBuilder.dismiss()
        }

        camera.setOnClickListener {
            Intent(MediaStore.ACTION_IMAGE_CAPTURE).also {
                imageCapture.launch(Intent.createChooser(it,"Click Photo"))
            }
            alertDialogBuilder.dismiss()
        }
        alertDialogBuilder.show()
        val lp = alertDialogBuilder.window?.attributes
        if (lp != null) {
            lp.dimAmount = 0.9f
            lp.buttonBrightness = 1.0f
            lp.screenBrightness = 1.0f
        }

        alertDialogBuilder.window?.attributes = lp
        alertDialogBuilder.window?.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
    }

    private val imageCapture = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        result ->
        if (result.resultCode == RESULT_OK){
            val imageUri = result?.data?.extras?.get("data") as Bitmap
            val file = File(this@ContributionActivity.cacheDir,"CUSTOM NAME") //Get Access to a local file.
            file.delete() // Delete the File, just in Case, that there was still another File
            file.createNewFile()
            val fileOutputStream = file.outputStream()
            val byteArrayOutputStream = ByteArrayOutputStream()
            imageUri.compress(Bitmap.CompressFormat.PNG,100,byteArrayOutputStream)
            val bytearray = byteArrayOutputStream.toByteArray()
            fileOutputStream.write(bytearray)
            fileOutputStream.flush()
            fileOutputStream.close()
            byteArrayOutputStream.close()

            val uri = file.toUri()
            addItems(uri,getfileName(this,uri))
            setUpRecyclerView()
        }
    }

    private val imageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        result ->
        if (result.resultCode == RESULT_OK){
            result?.data?.clipData?.let {
                val count = it.itemCount
                for (clipPosition in 0 until count){
                    addItems(it.getItemAt(clipPosition).uri,getfileName(this,it.getItemAt(clipPosition).uri))
                }
                setUpRecyclerView()
            }?: kotlin.run {
                result.data?.data?.let {
                    addItems(it,getfileName(this,it))
                    setUpRecyclerView()
                }
            }
        }
        else{
            Toast.makeText(this,"Operation Failed",Toast.LENGTH_SHORT).show()
        }
    }

    private fun setUpRecyclerView(){
        if (imageuriList.isNotEmpty()){
            binding.rvBackground.setImageDrawable(null)
        }
        else{
            binding.rvBackground.setImageResource(R.drawable.sprout)
        }
        binding.rvImages.apply {
            layoutManager = GridLayoutManager(this@ContributionActivity,3)
            adapter = imageAdapter
        }
    }

    private fun getfileName(context: Context,uri: Uri): String? {
        var fileName: String?
        val cursor = context.contentResolver.query(uri,null,null,null,null)
        cursor?.moveToFirst()
        fileName = cursor?.getString(
            cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME)
        )
        cursor?.close()
        return fileName
    }

    private fun addItems(imageUri : Uri?, fileName : String?){
        imageuriList.add(imageUri)
        filenameList.add(fileName)
    }

    private fun uploadImageToCloud(filename: String?){

        currentFile?.let {
            storageRef.child("images/${LoginUtills.userId}/${binding.spDisease.selectedItem}/$filename").putFile(it).addOnSuccessListener{
                Toast.makeText(this@ContributionActivity,"Image Uploaded",Toast.LENGTH_SHORT).show()
            }.addOnFailureListener{
                Toast.makeText(this@ContributionActivity,"Upload Failed",Toast.LENGTH_SHORT).show()
            }
        }
    }


}