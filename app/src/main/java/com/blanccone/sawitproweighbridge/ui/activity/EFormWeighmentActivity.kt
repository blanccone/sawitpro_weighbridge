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
import com.blanccone.core.ui.activity.CoreActivity
import com.blanccone.core.util.FileUtils
import com.blanccone.core.util.Utils
import com.blanccone.sawitproweighbridge.BuildConfig
import com.blanccone.sawitproweighbridge.databinding.ActivityEformWeighmentBinding
import java.io.File

class EFormWeighmentActivity : CoreActivity<ActivityEformWeighmentBinding>() {

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
    }

    private fun setView() {
        with(binding) {
            iuvImageBeratMuatan.apply {
                imageNotes = "*Foto akan menjadi bukti kebenaran input berat muatan"
                setFieldName(FIELD_NAME)
            }
        }
    }

    private fun setEvent() {
        with(binding) {
            iuvImageBeratMuatan.apply {
                setOnCameraListener {
                    openCamera()
                }
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

            val filePathData = FileUtils.getPathConverted(
                context = this,
                path = filePath!!,
                reqWidth = 800,
                reqHeight = 800
            )

            if (filePathData != null) {
                if (FileUtils.isFileOverSize(File(filePathData), 1)) {
                    toast("ukuran file maksimal 1 MB")
                    return
                } else {
                    binding.iuvImageBeratMuatan.setFilePath(filePathData)
                }
            } else {
                toast("File path tidak terbaca")
            }
        }
    }

    private fun getPermissionResult() {
        if (Utils.isAllowModifiedStorageAndCamera(this)) {
            openCamera()
        } else {
            Toast.makeText(
                this,
                "Mohon aktifkan izin Camera, Gallery dan File Manager",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val FIELD_NAME = "FOTO_BERAT_MUATAN"
        private var isSecondWeight = false

        fun newInstance(
            context: Context,
            isSecondWeight: Boolean = false
        ) {
            val intent = Intent(context, EFormWeighmentActivity::class.java)
            this.isSecondWeight = isSecondWeight
            context.startActivity(intent)
        }
    }
}