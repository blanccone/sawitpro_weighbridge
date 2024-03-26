package com.blanccone.core.ui.widget

import android.content.Context
import android.content.ContextWrapper
import android.graphics.drawable.AnimationDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.AttributeSet
import android.util.Patterns
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.blanccone.core.databinding.LayoutImageUploaderBinding
import com.blanccone.core.R
import com.blanccone.core.util.Utils
import com.blanccone.core.util.ViewUtils.hide
import com.blanccone.core.util.ViewUtils.show
import com.bumptech.glide.GenericTransitionOptions
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import java.io.File

class ImageUploaderView(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    constructor(context: Context) : this(context, null)

    private var _binding: LayoutImageUploaderBinding? = null

    private val binding get() = requireNotNull(_binding)

    private var fieldTitle = ""
    private var fieldName = ""
    private var filePath = ""

    var isPreviewOnly = false
    var isFileDetected = false
    var uploadLabel = ""
    var imageNotes = ""

    private var isImageLoadSucceed = false

    init {
        initView()
        setContent()
        setEvent()
    }

    private fun initView() {
        _binding = LayoutImageUploaderBinding.inflate(LayoutInflater.from(context))
        addView(binding.root)
    }

    private fun setEvent() {
        binding.apply {
            cslUpload.setOnClickListener {
                onCameraClickListener?.let { isClicked ->
                    isClicked(fieldName)
                }
            }

            ivResult.setOnClickListener {
                if (!isFileDetected && !isImageLoadSucceed) {
                    Toast.makeText(context, "Tidak ada file", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                if (!isImageLoadSucceed) return@setOnClickListener
                if (isPreviewOnly) {
                    showPreviewFile(context)
                } else {
                    showFileOptionsBottomSheet()
                }
            }

            cvRefresh.setOnClickListener {
                if (!Utils.isConnected(context)) {
                    Toast.makeText(
                        context,
                        "Pastikan Koneksi Internet Anda Tersedia",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    setContent()
                }
            }
        }
    }

    fun setImageName(fieldName: String, fieldTitle: String) {
        this.fieldName = fieldName
        this.fieldTitle = fieldTitle
        setContent()
    }

    private fun showPreviewFile(context: Context) {
        ImageViewerActivity.launch(
            context,
            filePath
        )
        onOpenFileListener?.let { it() }
    }

    private fun showFileOptionsBottomSheet() {
        var contextWrapper: Context
        var bottomSheetDialog: ImageOptionsBottomSheet

        try {
            contextWrapper = (context as ContextWrapper).baseContext
            (contextWrapper as FragmentActivity).supportFragmentManager
            bottomSheetDialog = ImageOptionsBottomSheet.showDialog(contextWrapper)
        } catch (e: Exception) {
            contextWrapper = (context as FragmentActivity)
            bottomSheetDialog = ImageOptionsBottomSheet.showDialog(contextWrapper)
        }

        bottomSheetDialog.apply {
            setOnOpenFileListener {
                showPreviewFile(contextWrapper)
            }

            setOnRemoveListener {
                clearSelector()
                dismiss()
            }
        }
    }

    fun setFilePath(path: String?) {
        if (path != null) {
            filePath = path
            setContent()
        }
    }

    private fun setContent() {
        binding.apply {
            tvImageTitle.text = fieldTitle
            if (filePath.isEmpty()) {
                cslUpload.show()
                ivResult.hide()
                piLoading.hide()
                cvRefresh.hide()
                tvImageUpload.text = uploadLabel.ifEmpty { "Upload Foto" }
                tvImageNotes.text = imageNotes
            } else {
                if (!Patterns.WEB_URL.matcher(filePath).matches()) {
                    loadFileFromLocal()
                } else {
                    loadFileFromRemote()
                }
            }
        }
    }

    private fun loadFileFromLocal() {
        val file = File(filePath)
        with(binding) {
            if (file.exists()) {
                Glide.with(context)
                    .load(file)
                    .transition(GenericTransitionOptions.with(android.R.anim.fade_in))
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .into(ivResult)

                ivResult.show()
                cslUpload.hide()
                piLoading.hide()
                cvRefresh.hide()
                isImageLoadSucceed = true
            }
        }
    }

    fun loadFromFirebase(fileUri: Uri) {
        binding.apply {
            val errorNoImage = R.drawable.ic_no_image
            val loading = (piLoading.background as AnimationDrawable)
            cslUpload.hide()
            cvRefresh.hide()
            ivResult.show()
            piLoading.show()
            loading.start()
            Glide.with(context)
                .load(fileUri)
                .apply(
                    RequestOptions()
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                )
                .transition(GenericTransitionOptions.with(android.R.anim.fade_in))
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        loading.stop()
                        piLoading.hide()
                        ivResult.background =
                            ContextCompat.getDrawable(context, errorNoImage)
                        ivResult.show()
                        cvRefresh.show()
                        isFileDetected = true
                        isImageLoadSucceed = false
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        loading.stop()
                        piLoading.hide()
                        cvRefresh.hide()
                        ivResult.show()
                        isFileDetected = true
                        isImageLoadSucceed = true
                        return false
                    }
                })
                .into(ivResult)
        }
    }

    private fun loadFileFromRemote() {
        binding.apply {
            val errorNoImage = R.drawable.ic_no_image
            val loading = (piLoading.background as AnimationDrawable)
            cslUpload.hide()
            ivResult.hide()
            cvRefresh.hide()
            piLoading.show()
            loading.start()

            if (filePath.isEmpty()) {
                loading.stop()
                piLoading.hide()
                ivResult.background = ContextCompat.getDrawable(context, errorNoImage)
                ivResult.show()
                isImageLoadSucceed = false
                isFileDetected = false
            }
            else {
                if (filePath.contains("noimage")) {
                    loading.stop()
                    piLoading.hide()
                    ivResult.background = ContextCompat.getDrawable(context, errorNoImage)
                    ivResult.show()
                    cvRefresh.show()
                    isImageLoadSucceed = false
                    isFileDetected = false
                }
                else {
                    ivResult.show()
                    Glide.with(context)
                        .load(filePath)
                        .apply(
                            RequestOptions()
                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                .skipMemoryCache(true)
                        )
                        .transition(GenericTransitionOptions.with(android.R.anim.fade_in))
                        .listener(object : RequestListener<Drawable> {
                            override fun onLoadFailed(
                                e: GlideException?,
                                model: Any?,
                                target: Target<Drawable>?,
                                isFirstResource: Boolean
                            ): Boolean {
                                loading.stop()
                                piLoading.hide()
                                ivResult.background =
                                    ContextCompat.getDrawable(context, errorNoImage)
                                ivResult.show()
                                cvRefresh.show()
                                isFileDetected = true
                                isImageLoadSucceed = false
                                return false
                            }

                            override fun onResourceReady(
                                resource: Drawable?,
                                model: Any?,
                                target: Target<Drawable>?,
                                dataSource: DataSource?,
                                isFirstResource: Boolean
                            ): Boolean {
                                loading.stop()
                                piLoading.hide()
                                cvRefresh.hide()
                                ivResult.show()
                                isFileDetected = true
                                isImageLoadSucceed = true
                                return false
                            }
                        })
                        .into(ivResult)
                }
            }
        }
    }

    fun clearSelector() {
        onDeleteFileListener?.let { it(filePath) }
        isFileDetected = false
        filePath = ""
        setContent()
    }

    private var onCameraClickListener: ((String) -> Unit)? = null
    private var onMultipleFilesListener: ((String) -> Unit)? = null
    private var onOpenFileListener: (() -> Unit)? = null
    private var onDeleteFileListener: ((String) -> Unit)? = null

    fun setOnCameraListener(listener: ((String) -> Unit)?) {
        onCameraClickListener = listener
    }

    fun setOnMultipleFilesListener(listener: ((String) -> Unit)?) {
        onMultipleFilesListener = listener
    }

    fun setOnOpenFileListener(listener: (() -> Unit)?) {
        onOpenFileListener = listener
    }

    fun setOnDeleteListener(listener: ((String) -> Unit)) {
        this.onDeleteFileListener = listener
    }
}