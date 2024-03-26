package com.blanccone.sawitproweighbridge.ui.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.MenuItem
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
import androidx.core.view.isVisible
import com.blanccone.core.R
import com.blanccone.core.model.local.Ticket
import com.blanccone.core.model.local.WeightImage
import com.blanccone.core.ui.activity.CoreActivity
import com.blanccone.core.ui.widget.LoadingDialog
import com.blanccone.core.util.FileUtils
import com.blanccone.core.util.Utils
import com.blanccone.core.util.Utils.generateUniqueId
import com.blanccone.core.util.Utils.getCurrentDateTime
import com.blanccone.core.util.Utils.reformatDate
import com.blanccone.core.util.Utils.toast
import com.blanccone.core.util.ViewUtils.backgroundTint
import com.blanccone.core.util.ViewUtils.show
import com.blanccone.core.util.ViewUtils.stateError
import com.blanccone.sawitproweighbridge.BuildConfig
import com.blanccone.sawitproweighbridge.databinding.ActivityEformWeighmentBinding
import com.blanccone.sawitproweighbridge.ui.viewmodel.WeighmentViewModel
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.database.DatabaseReference
import com.google.firebase.storage.StorageReference
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class EFormWeighmentActivity : CoreActivity<ActivityEformWeighmentBinding>() {

    private val viewModel: WeighmentViewModel by viewModels()

    @Inject
    internal lateinit var firebaseDb: DatabaseReference

    @Inject
    internal lateinit var storageDb: StorageReference

    private lateinit var currentDateTime: String

    private var filePath: String? = null
    private var fileUri: Uri? = null
    private var fileImage: File? = null
    private var imageFieldName= ""

    private var fields = hashMapOf<TextInputLayout, View>()

    private var validatedTicket = Ticket()

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
        currentDateTime = if (!isEditRequested && ticketStatus != DONE) {
            getCurrentDateTime("ddMMyyyyHHmmssSS")
        } else ""

        setFields()
        setView()
        setEvent()
        setObserves()
        fetchData()
    }

    private fun setObserves() {
        viewModel.isLoading.observe(this) {
            it?.let { isLoading ->
                showLoading(isLoading)
            }
        }

        viewModel.error.observe(this) {
            toast(it.toString())
        }

        viewModel.images.observe(this) {
            setImagesFromLocal(it)
        }

        viewModel.insertTicketSuccessful.observe(this) {
            if (!it.isNullOrEmpty()) storeImageToLocal(it)
        }

        viewModel.insertImageSuccessful.observe(this) {
            if (it) {
                toast("Data berhasil tersimpan")
                setResult(RESULT_OK)
                finish()
            }
        }
    }

    private fun fetchData() {
        if ((ticketStatus in setOf(FIRST_WEIGHT, SECOND_WEIGHT) && isEditRequested) ||
            ticketStatus == DONE
        ) {
            ticketData?.let {
                if (!Utils.isConnected(this)) {
                    fetchFromLocal("${ticketData?.id}")
                } else {
                    fetchFromFirebase()
                }
            }
        }
    }

    private fun fetchFromFirebase() {
        storageDb.listAll().addOnSuccessListener { listResult ->
            val items = listResult.items
            if (items.isNotEmpty()) {
                setImagesFromFirebase(items)
            } else {
                fetchFromLocal("${ticketData?.id}")
            }
        }
    }

    private fun fetchFromLocal(ticketId: String) {
        viewModel.getimages(ticketId)
    }

    private fun setFields() {
        with(binding) {
            fields = hashMapOf(
                tilNoPol to etNoPol,
                tilNama to etNama,
                tilBeratMuatanPertama to etBeratMuatanPertama
            )
            if (ticketStatus in setOf(DONE, SECOND_WEIGHT)) {
                fields[tilBeratMuatanKedua] = etBeratMuatanKedua
            }
        }
    }

    private fun setView() {
        with(binding) {
            when {
                ticketStatus == DONE -> {
                    setAutoFillDefault()
                    ticketData?.let {
                        with(it) {
                            etBeratMuatanKedua.apply {
                                show()
                                setText(secondWeight.toString())
                            }
                            etWaktuTimbangKedua.apply {
                                show()
                                setText(secondWeighedOn)
                            }
                            iuvImageBeratMuatanSecond.show()
                        }
                    }
                }

                isEditRequested && ticketStatus in setOf(FIRST_WEIGHT, SECOND_WEIGHT) -> {
                    setAutoFillDefault()
                    ticketData?.let {
                        with(it) {
                            etBeratMuatanKedua.apply {
                                isVisible = ticketStatus == SECOND_WEIGHT
                                setText(secondWeight.toString())
                            }
                            etWaktuTimbangKedua.apply {
                                isVisible = ticketStatus == SECOND_WEIGHT
                                setText(secondWeighedOn)
                            }
                            iuvImageBeratMuatanSecond.isVisible = ticketStatus == SECOND_WEIGHT
                        }
                    }
                }

                ticketStatus == SECOND_WEIGHT -> {
                    setAutoFillDefault()
                    etWaktuTimbangKedua.apply {
                        show()
                        setText(
                            currentDateTime.reformatDate(
                                "ddMMyyyyHHmmssSS",
                                "dd/MM/yyyy HH:mm:ss"
                            )
                        )
                    }
                    iuvImageBeratMuatanFirst.isPreviewOnly = true
                    iuvImageBeratMuatanSecond.show()
                }

                else -> etWaktuTimbangPertama.setText(
                    currentDateTime.reformatDate(
                        "ddMMyyyyHHmmssSS",
                        "dd/MM/yyyy HH:mm:ss"
                    )
                )
            }
            iuvImageBeratMuatanFirst.apply {
                imageNotes = "*Foto akan menjadi bukti kebenaran input berat muatan"
                setFieldName("${getCurrentDateTime("ddMMyyyyHH")}_$FIRST_PHOTO")
            }
            iuvImageBeratMuatanSecond.apply {
                imageNotes = "*Foto akan menjadi bukti kebenaran input berat muatan"
                setFieldName("${getCurrentDateTime("ddMMyyyyHH")}_$SECOND_PHOTO")
            }
            if (isPreviewRequested) disableView()
        }
    }

    private fun setAutoFillDefault() {
        with(binding) {
            ticketData?.let {
                with(it) {
                    etNoPol.setText(licenseNumber)
                    etNama.setText(driverName)
                    etBeratMuatanPertama.setText(firstWeight.toString())
                    etWaktuTimbangPertama.setText(firstWeighedOn)
                }
            }
        }
    }

    private fun setImagesFromFirebase(images: List<StorageReference>) {
        when {
            ticketStatus == DONE && images.size > 1 -> {
                images[0].downloadUrl.addOnSuccessListener {
                    binding.iuvImageBeratMuatanFirst.loadFromFirebase(it)
                }
                images[1].downloadUrl.addOnSuccessListener {
                    binding.iuvImageBeratMuatanSecond.loadFromFirebase(it)
                }
            }

            isEditRequested && ticketStatus in setOf(FIRST_WEIGHT, SECOND_WEIGHT) -> {
                if (images.size > 1) {
                    images[0].downloadUrl.addOnSuccessListener {
                        binding.iuvImageBeratMuatanFirst.loadFromFirebase(it)
                    }
                    images[1].downloadUrl.addOnSuccessListener {
                        binding.iuvImageBeratMuatanSecond.loadFromFirebase(it)
                    }
                } else {
                    images[0].downloadUrl.addOnSuccessListener {
                        binding.iuvImageBeratMuatanFirst.loadFromFirebase(it)
                    }
                }
            }

            ticketStatus == SECOND_WEIGHT -> images[1].downloadUrl.addOnSuccessListener {
                binding.iuvImageBeratMuatanSecond.loadFromFirebase(it)
            }
        }
    }

    private fun setImagesFromLocal(images: List<WeightImage>) {
        when {
            ticketStatus == DONE && images.size > 1 -> {
                setFirstImage(images[0])
                setSecondImage(images[1])
            }

            isEditRequested && ticketStatus in setOf(FIRST_WEIGHT, SECOND_WEIGHT) -> {
                if (images.size > 1) {
                    setFirstImage(images[0])
                    setSecondImage(images[1])
                } else {
                    setFirstImage(images[0])
                }
            }

            ticketStatus == SECOND_WEIGHT -> setSecondImage(images[1])
        }
    }

    private fun setFirstImage(image: WeightImage) {
        var firstImagePath = image.imagePath
        if (firstImagePath.isNullOrEmpty()) {
            val file = FileUtils.byteArrayToFile(
                context = this@EFormWeighmentActivity,
                byteArray = image.image!!,
                fileName = "${ticketData?.id}"
            )
            firstImagePath = file.absolutePath
        }
        binding.iuvImageBeratMuatanFirst.setFilePath(firstImagePath)
    }

    private fun setSecondImage(image: WeightImage) {
        var secondImagePath = image.imagePath
        if (secondImagePath.isNullOrEmpty()) {
            val file = FileUtils.byteArrayToFile(
                context = this@EFormWeighmentActivity,
                byteArray = image.image!!,
                fileName = "${ticketData?.id}"
            )
            secondImagePath = file.absolutePath
        }
        binding.iuvImageBeratMuatanSecond.setFilePath(secondImagePath)
    }

    private fun setEvent() {
        with(binding) {
            iuvImageBeratMuatanFirst.apply {
                setOnCameraListener {
                    imageFieldName = it
                    openCamera()
                }
                setOnDeleteListener {
                    filePath = null
                    fileUri = null
                }
            }
            iuvImageBeratMuatanFirst.apply {
                setOnCameraListener {
                    imageFieldName = it
                    openCamera()
                }
                setOnDeleteListener {
                    filePath = null
                    fileUri = null
                }
            }
            btnSimpan.setOnClickListener {
                if (isDataValid()) setData()
            }
        }
    }

    private fun openCamera() {
        if (!Utils.isAllowModifiedStorageAndCamera(this)) {
            permissionLauncher.launch(Utils.getCameraPermissions())
            return
        }
        try {
            val fileName = FileUtils.getFileNameTemplate(imageFieldName, "jpg")
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
            val firstWeight = etBeratMuatanPertama.text.toString().toInt()
            val secondWeight = etBeratMuatanKedua.text.toString().toInt()
            val firstWeightOn = etWaktuTimbangPertama.text.toString()
            val secondWeightOn = etWaktuTimbangKedua.text.toString()
            val status = if (ticketStatus == FIRST_WEIGHT) "Inbound" else "Outbound"
            val combString = "$driverName$licenseNumber$currentDateTime"
            validatedTicket = Ticket(
                id = generateUniqueId(combString),
                licenseNumber = licenseNumber,
                driverName = driverName,
                firstWeight = firstWeight,
                firstWeighedOn = firstWeightOn,
                secondWeight = secondWeight,
                secondWeighedOn = secondWeightOn,
                status = status
            )
            storeDataToFirebase(validatedTicket.toMap())
        }
    }

    private fun storeDataToFirebase(ticket: Map<String, Any?>) {
        showLoading(true)
        firebaseDb
            .child("${validatedTicket.id}")
            .child("${validatedTicket.id}_$ticketStatus")
            .setValue(ticket)
            .addOnSuccessListener {
                storeImageToFirebase("${ticket["id"]}")
            }.addOnFailureListener {
                toast("Gagal menyimpan data")
            }
    }

    private fun storeImageToFirebase(ticketId: String) {
        val fileName = "${ticketId}_${ticketStatus}"
        if (isImageReady(fileName)) {
            storageDb
                .child(ticketId)
                .child(fileName)
                .putFile(fileUri!!)
                .addOnSuccessListener {
                    showLoading(false)
                    storeDataToLocal()
                }
                .addOnFailureListener {
                    toast("Gagal menyimpan data")
                }
        }
    }

    private fun storeDataToLocal() {
        viewModel.insertTicket(validatedTicket)
    }

    private fun storeImageToLocal(ticketId: String) {
        val image = WeightImage(
            id = "${validatedTicket.id}_$ticketStatus",
            ticketId = ticketId,
            image = FileUtils.fileToByteArray(fileImage!!),
            imageName = FileUtils.getFileName(filePath!!),
            imagePath = filePath
        )
        viewModel.insertImage(image)
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
                filePath = filePathData
                fileImage = File(filePathData)
                fileUri = fileImage!!.toUri()
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
        with(binding) {
            iuvImageBeratMuatanFirst.isPreviewOnly = true
            iuvImageBeratMuatanSecond.isPreviewOnly = true
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        private const val FIRST_PHOTO = "FIRST"
        private const val SECOND_PHOTO = "SECOND"
        const val FIRST_WEIGHT = "first"
        const val SECOND_WEIGHT = "second"
        const val DONE = "done"
        private var ticketStatus = ""
        private var ticketData: Ticket? = null
        private var isEditRequested = false
        private var isPreviewRequested = false

        fun newInstance(
            context: Context,
            status: String,
            data: Ticket? = null,
            isRequested: Boolean = false,
            isPreviewOnly: Boolean = false
        ) {
            val intent = Intent(context, EFormWeighmentActivity::class.java)
            ticketStatus = status
            ticketData = data
            isEditRequested = isRequested
            isPreviewRequested = isPreviewOnly
            context.startActivity(intent)
        }

        fun resultInstance(
            context: Context,
            status: String,
            data: Ticket? = null,
            isRequested: Boolean = false
        ) : Intent {
            ticketStatus = status
            ticketData = data
            isEditRequested = isRequested
            return Intent(context, EFormWeighmentActivity::class.java)
        }
    }
}