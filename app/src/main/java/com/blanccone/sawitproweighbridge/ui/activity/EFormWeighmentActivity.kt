package com.blanccone.sawitproweighbridge.ui.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.blanccone.core.model.local.Ticket
import com.blanccone.core.ui.activity.CoreActivity
import com.blanccone.core.ui.widget.LoadingDialog
import com.blanccone.core.util.FileUtils
import com.blanccone.core.util.Utils
import com.blanccone.core.util.Utils.generateUniqueId
import com.blanccone.core.util.Utils.getCurrentDateTime
import com.blanccone.core.util.Utils.reformatDate
import com.blanccone.sawitproweighbridge.BuildConfig
import com.blanccone.sawitproweighbridge.databinding.ActivityEformWeighmentBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class EFormWeighmentActivity : CoreActivity<ActivityEformWeighmentBinding>() {

    @Inject
    internal lateinit var firebaseDb: DatabaseReference
    @Inject
    internal lateinit var storageDb: StorageReference

    private var isFirstEntry = false
    private val currentDateTime = getCurrentDateTime("ddMMyyyyHHmmssSS")

    private var filePath: String? = null
    private var fileUri: Uri? = null

    private val resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            getActivityResult(it)
        }

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            getPermissionResult()
        }

    override fun inflateLayout(inflater: LayoutInflater): ActivityEformWeighmentBinding {
        return ActivityEformWeighmentBinding.inflate(inflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setView()
        setEvent()
        fetchDataFromFirebase()
    }

    private fun fetchDataFromFirebase() {
        firebaseDb
            .child("tickets")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    isFirstEntry = snapshot.exists()
                }

                override fun onCancelled(error: DatabaseError) { }
            })
    }

    private fun setView() {
        with(binding) {
            etWaktuTimbang.setText(
                currentDateTime.reformatDate(
                "ddMMyyyyHHmmssSS",
                "dd/MM/yyyy HH:mm:ss"
                )
            )
            iuvImageBeratMuatanFirst.apply {
                imageNotes = "*Foto akan menjadi bukti kebenaran input berat muatan"
                setFieldName(FIELD_NAME)
            }
        }
    }

    private fun setEvent() {
        with(binding) {
            iuvImageBeratMuatanFirst.apply {
                setOnCameraListener {
                    openCamera()
                }
                setOnDeleteListener {
                    filePath = null
                    fileUri = null
                }
            }
            iuvImageBeratMuatanFirst.apply {
                setOnCameraListener {
                    openCamera()
                }
                setOnDeleteListener {
                    filePath = null
                    fileUri = null
                }
            }
            btnSimpan.setOnClickListener {
                setData()
            }
        }
    }

    private fun openCamera() {
        if (!Utils.isAllowModifiedStorageAndCamera(this)) {
            permissionLauncher.launch(Utils.getCameraPermissions())
            return
        }
        try {
            val fileName = FileUtils.getFileNameTemplate(FIELD_NAME, "jpg")
            val dir = FileUtils.getPictureDir(this)
            val file = File(dir, fileName)

            fileUri = FileProvider.getUriForFile(
                this,
                BuildConfig.APPLICATION_ID + ".provider",
                file
            )
            filePath = file.path

            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri)
            cameraIntent.putExtra("return-data", true)
            resultLauncher.launch(cameraIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getActivityResult(result: ActivityResult) {
        if (result.resultCode == Activity.RESULT_OK) {
            if (fileUri == null) {
                val resultData = result.data
                if (resultData?.data != null) {
                    fileUri = resultData.data
                } else {
                    toast("File path tidak terbaca")
                    return
                }
            }
            binding.iuvImageBeratMuatanFirst.setFilePath(filePath!!)
        }
    }

    private fun setData() {
        with(binding) {
            val driverName = etNama.text.toString()
            val licenseNumber = etNoPol.text.toString()
            val weight = etBeratMuatan.text.toString().toInt()
            val weightOn = etWaktuTimbang.text.toString()
            val status = "Inbound"
            val combString = "$driverName$licenseNumber$currentDateTime"
            val ticket = Ticket(
                id = generateUniqueId(combString),
                licenseNumber = licenseNumber,
                driverName = driverName,
                weight = weight,
                weighedOn = weightOn,
                status = status
            ).toMap()

            showLoading(true)
            saveData(ticket)
        }
    }

    private fun saveData(ticket: Map<String, Any?>) {
        firebaseDb
            .push()
            .setValue(ticket)
            .addOnSuccessListener {
                saveImage("${ticket["id"]}")
            }.addOnFailureListener {
                toast("Gagal menyimpan data")
            }
    }

    private fun saveImage(ticketId: String) {
        if(isImageReady(ticketId)) {
            storageDb
                .child(ticketId)
                .putFile(fileUri!!)
                .addOnSuccessListener {
                    toast("Data berhasil tersimpan")
                    finish()
                }
                .addOnFailureListener {
                    toast("Gagal menyimpan data")
                }
        }
    }

    private fun isImageReady(ticketId: String): Boolean {
        var isReady = true
        val filePathData = FileUtils.getPathConverted(
            context = this,
            path = filePath!!,
            fileName = ticketId
        )

        if (filePathData != null) {
            if (FileUtils.isFileOverSize(File(filePathData), 1)) {
                toast("ukuran file maksimal 1 MB")
                isReady = false
            } else {
                fileUri = File(filePathData).toUri()
            }
        } else {
            toast("File path tidak terbaca")
            isReady = false
        }
        return isReady
    }

    private fun getPermissionResult() {
        if (Utils.isAllowModifiedStorageAndCamera(this)) {
            openCamera()
            toast("Open Camera")
        } else {
            toast("Mohon aktifkan izin Camera")
        }
    }

    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            LoadingDialog.showDialog(supportFragmentManager)
        } else {
            LoadingDialog.dismissDialog(supportFragmentManager)
        }
    }

    companion object {
        private const val FIELD_NAME = "BERAT_MUATAN"
        private var ticketStatus = ""

        fun newInstance(
            context: Context,
            status: String
        ) {
            val intent = Intent(context, EFormWeighmentActivity::class.java)
            ticketStatus = status
            context.startActivity(intent)
        }
    }
}