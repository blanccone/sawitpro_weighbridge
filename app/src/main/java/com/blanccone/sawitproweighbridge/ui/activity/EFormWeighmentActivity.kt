package com.blanccone.sawitproweighbridge.ui.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.widget.AppCompatAutoCompleteTextView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import com.blanccone.core.model.local.Ticket
import com.blanccone.core.ui.activity.CoreActivity
import com.blanccone.core.ui.widget.LoadingDialog
import com.blanccone.core.util.FileUtils
import com.blanccone.core.util.Utils
import com.blanccone.core.util.Utils.generateUniqueId
import com.blanccone.core.util.Utils.getCurrentDateTime
import com.blanccone.core.util.Utils.reformatDate
import com.blanccone.core.util.Utils.toast
import com.blanccone.core.R
import com.blanccone.core.util.ViewUtils.backgroundTint
import com.blanccone.core.util.ViewUtils.stateError
import com.blanccone.sawitproweighbridge.BuildConfig
import com.blanccone.sawitproweighbridge.databinding.ActivityEformWeighmentBinding
import com.blanccone.sawitproweighbridge.ui.viewmodel.EFormWeighmentViewModel
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.StorageReference
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class EFormWeighmentActivity : CoreActivity<ActivityEformWeighmentBinding>() {

    private val viewModel: EFormWeighmentViewModel by viewModels()

    @Inject
    internal lateinit var firebaseDb: DatabaseReference
    @Inject
    internal lateinit var storageDb: StorageReference

    private val currentDateTime = getCurrentDateTime("ddMMyyyyHHmmssSS")

    private var filePath: String? = null
    private var fileUri: Uri? = null

    private var fields = hashMapOf<TextInputLayout, View>()

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
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = when (ticketStatus) {
                FIRST_WEIGHT -> "First Weight"
                SECOND_WEIGHT -> "Second Weight"
                else -> "Result"
            }
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }
        setView()
        setEvent()
        fetchFromFirebase()
    }

    private fun fetchFromFirebase() {
        firebaseDb.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
            }

            override fun onCancelled(error: DatabaseError) {
                if (!Utils.isConnected(this@EFormWeighmentActivity)) {
                    fetchFromLocal()
                }
            }
        })
    }

    private fun fetchFromLocal() {
        viewModel.getimages()
    }

    private fun setView() {
        with(binding) {
            fields = hashMapOf(
                tilNoPol to etNoPol,
                tilNama to etNama,
                tilBeratMuatan to etBeratMuatan
            )
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
        if (isImageReady(ticketId)) {
            storageDb
                .child(ticketId)
                .putFile(fileUri!!)
                .addOnSuccessListener {
                    toast("Data berhasil tersimpan")
                    showLoading(false)
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

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            LoadingDialog.showDialog(supportFragmentManager)
        } else {
            LoadingDialog.dismissDialog(supportFragmentManager)
        }
    }

    private fun disableView() {
        val disabledColor = ContextCompat.getColor(this, R.color.grey5)
        for ((_, et) in fields) {
            et.isEnabled = false
            if (et is EditText) {
                ViewCompat.setBackgroundTintList(et, ColorStateList.valueOf(disabledColor))
            } else {
                et.backgroundTint(R.color.grey5)
            }
        }
    }

    private fun isDataValid(): Boolean {
        var isValid = true
        for ((til, et) in fields) {
            if (et is TextInputEditText && et.text.isNullOrBlank()) {
                til.stateError()
                isValid = false
            }
            if (et is AppCompatAutoCompleteTextView && et.text.isNullOrBlank()) {
                til.stateError()
                isValid = false
            }
        }
        if (!isValid) {
            toast("Mohon lengkapi data")
        }
        return isValid
    }

    companion object {
        private const val FIELD_NAME = "BERAT_MUATAN"
        const val FIRST_WEIGHT = "FIRST"
        const val SECOND_WEIGHT = "SECOND"
        private var ticketStatus = ""
        private var ticketData: Ticket? = null

        fun newInstance(
            context: Context,
            status: String,
            data: Ticket? = null,
        ) {
            val intent = Intent(context, EFormWeighmentActivity::class.java)
            ticketStatus = status
            ticketData = data
            context.startActivity(intent)
        }
    }
}