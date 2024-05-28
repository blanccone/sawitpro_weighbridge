package com.blanccone.sawitproweighbridge.ui.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.InputFilter
import android.view.LayoutInflater
import android.view.MenuItem
import android.widget.EditText
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import com.blanccone.core.R
import com.blanccone.core.model.local.Ticket
import com.blanccone.core.model.local.WeightImage
import com.blanccone.core.ui.activity.CoreActivity
import com.blanccone.core.ui.widget.LoadingDialog
import com.blanccone.core.util.FileUtils
import com.blanccone.core.util.Utils
import com.blanccone.core.util.Utils.generateUniqueId
import com.blanccone.core.util.Utils.getCurrentDateTime
import com.blanccone.core.util.Utils.isNumber
import com.blanccone.core.util.Utils.reformatDate
import com.blanccone.core.util.Utils.toast
import com.blanccone.core.util.ViewUtils.backgroundTint
import com.blanccone.core.util.ViewUtils.hide
import com.blanccone.core.util.ViewUtils.removeError
import com.blanccone.core.util.ViewUtils.show
import com.blanccone.core.util.ViewUtils.stateError
import com.blanccone.sawitproweighbridge.BuildConfig
import com.blanccone.sawitproweighbridge.databinding.ActivityEformWeighmentBinding
import com.blanccone.sawitproweighbridge.ui.viewmodel.WeighmentViewModel
import com.blanccone.sawitproweighbridge.util.TestWatcher
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
    private var imageFieldName = ""

    private var fields = hashMapOf<TextInputLayout, TextInputEditText>()

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
        currentDateTime = if (ticketData?.secondWeight == NOT_EDITED ||
            (!isEditRequested && ticketStatus != DONE)
        ) {
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
            if (!it.isNullOrEmpty()) {
                storeImageToLocal(it)
            }
        }

        viewModel.updateTicketSuccessful.observe(this) {
            it?.let { isSuccessful ->
                if (isSuccessful) {
                    if (ticketData?.secondWeight == NOT_EDITED) {
                        storeImageToLocal("${validatedTicket.id}")
                    } else {
                        updateImageToLocal()
                    }
                }
            }
        }

        viewModel.insertImageSuccessful.observe(this) {
            if (it) {
                toast("Data berhasil tersimpan")
                setResult(Activity.RESULT_OK)
                finish()
            }
        }

        viewModel.updateImageSuccessful.observe(this) {
            it?.let { isSuccessful ->
                if (isSuccessful) {
                    toast("Data berhasil diperbarui")
                    setResult(Activity.RESULT_OK)
                    finish()
                }
            }
        }
    }

    private fun fetchData() {
        if ((ticketStatus in setOf(FIRST_WEIGHT, SECOND_WEIGHT) && isEditRequested) ||
            ticketStatus == DONE
        ) {
            ticketData?.let {
                setObservesFirebase()
                fetchFromLocal("${ticketData?.id}")
            }
        }
    }

    private fun setObservesFirebase() {
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
                tilBeratMasuk to etBeratMasuk
            )
            etNoPol.apply {
                filters = arrayOf(InputFilter.AllCaps())
                doAfterTextChanged {
                    tilNoPol.removeError()
                }
            }
            etNama.doAfterTextChanged {
                tilNama.removeError()
            }
            etBeratMasuk.doAfterTextChanged {
                tilBeratMasuk.removeError()
            }
            etBeratKeluar.doAfterTextChanged {
                tilBeratKeluar.removeError()
                val firstWeight = ticketData?.firstWeight.toString()
                val secondWeight = it.toString()
                if (tilBeratBersih.isVisible && it.toString().isNumber()) {
                    etBeratBersih.setText(getNettWeight(firstWeight, secondWeight))
                }
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
                            tilWaktuTimbangKedua.show()
                            etWaktuTimbangKedua.setText(secondWeighedOn)
                            tilBeratKeluar.show()
                            etBeratKeluar.setText(secondWeight)
                            iuvImageBeratKeluar.show()
                            tilBeratBersih.show()
                            etBeratBersih.setText(
                                getNettWeight(
                                    firstWeight.toString(),
                                    secondWeight.toString()
                                )
                            )
                        }
                    }
                }

                isEditRequested && ticketStatus == SECOND_WEIGHT -> {
                    setAutoFillDefault()
                    disableView()
                    if (ticketStatus in setOf(DONE, SECOND_WEIGHT)) {
                        fields[tilBeratKeluar] = etBeratKeluar
                    }
                    ticketData?.let {
                        with(it) {
                            tilWaktuTimbangKedua.show()
                            etWaktuTimbangKedua.setText(
                                if (secondWeight == NOT_EDITED) {
                                    currentDateTime.reformatDate(
                                        "ddMMyyyyHHmmssSS",
                                        "dd/MM/yyyy HH:mm:ss"
                                    )
                                } else secondWeighedOn
                            )
                            tilBeratKeluar.show()
                            etBeratKeluar.setText(secondWeight)
                            tilBeratBersih.isVisible = secondWeight != NOT_EDITED
                            etBeratBersih.setText(
                                if (secondWeight != NOT_EDITED) {
                                    getNettWeight(
                                        firstWeight.toString(),
                                        secondWeight.toString()
                                    )
                                } else {
                                    firstWeight
                                }
                            )
                        }
                    }
                    iuvImageBeratMasuk.isPreviewOnly = true
                    iuvImageBeratKeluar.show()
                }

                isEditRequested && ticketStatus == FIRST_WEIGHT -> {
                    setAutoFillDefault()
                }

                else -> etWaktuTimbangPertama.setText(
                    currentDateTime.reformatDate(
                        "ddMMyyyyHHmmssSS",
                        "dd/MM/yyyy HH:mm:ss"
                    )
                )
            }
            iuvImageBeratMasuk.apply {
                imageNotes = "*Foto akan menjadi bukti kebenaran input berat muatan"
                setImageName(
                    "${getCurrentDateTime("ddMMyyyyHH")}_${FIRST_WEIGHT.uppercase()}",
                    FIRST_PHOTO_LABEL
                )
            }
            iuvImageBeratKeluar.apply {
                imageNotes = "*Foto akan menjadi bukti kebenaran input berat muatan"
                setImageName(
                    "${getCurrentDateTime("ddMMyyyyHH")}_${SECOND_WEIGHT.uppercase()}",
                    SECOND_PHOTO_LABEL
                )
            }
            if (isPreviewRequested) {
                disableView()
                cslFooter.hide()
            }
        }
    }

    private fun getNettWeight(firstWeight: String, secondWeight: String): String {
        var nettWeight = 0
        if (isEditedOutbound(secondWeight) || ticketStatus == DONE) {
            nettWeight = firstWeight.toInt() - secondWeight.toInt()
        }
        return nettWeight.toString()
    }

    private fun isEditedOutbound(secondWeight: String): Boolean {
        return ticketStatus == SECOND_WEIGHT && secondWeight != NOT_EDITED
    }

    private fun setAutoFillDefault() {
        with(binding) {
            ticketData?.let {
                with(it) {
                    etNoPol.setText(licenseNumber)
                    etNama.setText(driverName)
                    etBeratMasuk.setText(firstWeight)
                    etWaktuTimbangPertama.setText(firstWeighedOn)
                }
            }
        }
    }

    private fun setImagesFromFirebase(images: List<StorageReference>) {
        when {
            ticketStatus == DONE && images.size > 1 -> {
                images[0].downloadUrl.addOnSuccessListener {
                    binding.iuvImageBeratMasuk.loadFromFirebase(it)
                }
                images[1].downloadUrl.addOnSuccessListener {
                    binding.iuvImageBeratKeluar.loadFromFirebase(it)
                }
            }

            isEditRequested && ticketStatus in setOf(FIRST_WEIGHT, SECOND_WEIGHT) -> {
                if (images.size > 1) {
                    images[0].downloadUrl.addOnSuccessListener {
                        binding.iuvImageBeratMasuk.loadFromFirebase(it)
                    }
                    images[1].downloadUrl.addOnSuccessListener {
                        binding.iuvImageBeratKeluar.loadFromFirebase(it)
                    }
                } else {
                    images[0].downloadUrl.addOnSuccessListener {
                        binding.iuvImageBeratMasuk.loadFromFirebase(it)
                    }
                }
            }

            ticketStatus == SECOND_WEIGHT -> images[1].downloadUrl.addOnSuccessListener {
                binding.iuvImageBeratKeluar.loadFromFirebase(it)
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
        binding.iuvImageBeratMasuk.setFilePath(firstImagePath)
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
        binding.iuvImageBeratKeluar.setFilePath(secondImagePath)
    }

    private fun setEvent() {
        with(binding) {
            etAngka1.addTextChangedListener(
                object : TestWatcher(etAngka1) {
                    override fun getCurrentTotal(): Int {
                        val total = etAngka2.text.toString().replace("%", "")
                        return if (total.isNumber()) total.toInt()
                        else 0
                    }

                    override fun doAfterTextChanged() {
                        updateTotal()
                    }
                }
            )
            etAngka2.addTextChangedListener(
                object : TestWatcher(etAngka2) {
                    override fun getCurrentTotal(): Int {
                        val total = etAngka1.text.toString().replace("%", "")
                        return if (total.isNumber()) total.toInt()
                        else 0
                    }

                    override fun doAfterTextChanged() {
                        updateTotal()
                    }
                }
            )

            iuvImageBeratMasuk.apply {
                setOnCameraListener {
                    imageFieldName = it
                    openCamera()
                }
                setOnDeleteListener {
                    filePath = null
                    fileUri = null
                }
            }
            iuvImageBeratKeluar.apply {
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

    private fun updateTotal() {
        with(binding) {
            val sAngka1 = etAngka1.text.toString().replace("%", "")
            val nAngka1 = if (sAngka1.isNumber()) sAngka1.toInt()
            else 0
            val sAngka2 = etAngka2.text.toString().replace("%", "")
            val nAngka2 = if (sAngka2.isNumber()) sAngka2.toInt()
            else 0

            val total = nAngka1 + nAngka2
            etAngka3.setText("$total%")
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
            with(binding) {
                if (ticketStatus == FIRST_WEIGHT) {
                    iuvImageBeratMasuk.setFilePath(filePath!!)
                } else {
                    iuvImageBeratKeluar.setFilePath(filePath!!)
                }
            }
        }
    }

    private fun setData() {
        with(binding) {
            val driverName = etNama.text.toString()
            val licenseNumber = etNoPol.text.toString()
            val firstWeight = etBeratMasuk.text.toString()
            val secondWeight = if (ticketStatus != FIRST_WEIGHT) {
                etBeratKeluar.text.toString()
            } else NOT_EDITED
            val firstWeightOn = etWaktuTimbangPertama.text.toString()
            val secondWeightOn = etWaktuTimbangKedua.text.toString()
            val status = if (ticketStatus != FIRST_WEIGHT) "Outbound" else "Inbound"
            val combString = "$driverName$licenseNumber$currentDateTime"
            val id = if (ticketData != null) ticketData?.id else generateUniqueId(combString)
            validatedTicket = Ticket(
                id = id,
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
            .setValue(ticket)
            .addOnSuccessListener {
                storeImageToFirebase("${ticket["id"]}")
            }.addOnFailureListener {
                showLoading(false)
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
                    if (!isEditRequested) {
                        storeDataToLocal()
                    } else {
                        updateDataToLocal()
                    }
                }
                .addOnFailureListener {
                    showLoading(false)
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

    private fun updateDataToLocal() {
        viewModel.updateTicket(validatedTicket)
    }

    private fun updateImageToLocal() {
        val image = WeightImage(
            id = "${validatedTicket.id}_$ticketStatus",
            ticketId = validatedTicket.id,
            image = FileUtils.fileToByteArray(fileImage!!),
            imageName = FileUtils.getFileName(filePath!!),
            imagePath = filePath
        )
        viewModel.updateImage(image)
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
            iuvImageBeratMasuk.isPreviewOnly = true
            iuvImageBeratKeluar.isPreviewOnly = true
        }
    }

    private fun isDataValid(): Boolean {
        var isValid = true
        var message = "Mohon lengkapi data"
        for ((til, et) in fields) {
            if (et.text.isNullOrBlank()) {
                til.stateError()
                isValid = false
            }
        }
        with(binding) {
            val secondWeight = etBeratKeluar.text.toString()
            if (isValid && ticketStatus == SECOND_WEIGHT && !secondWeight.isNumber()) {
                tilBeratKeluar.stateError("Berat muatan tidak valid")
                etBeratKeluar.requestFocus()
                message = "Data tidak valid"
                isValid = false
            }
            if (isValid && filePath.isNullOrBlank()) {
                message = "Foto tidak boleh kosong"
                isValid = false
            }
        }
        if (!isValid) {
            toast(message)
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
        private const val FIRST_PHOTO_LABEL = "Foto Berat Masuk"
        private const val SECOND_PHOTO_LABEL = "Foto Berat Keluar"
        const val NOT_EDITED = "Belum Terisi"
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
        ): Intent {
            ticketStatus = status
            ticketData = data
            isEditRequested = isRequested
            return Intent(context, EFormWeighmentActivity::class.java)
        }
    }
}