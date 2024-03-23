package com.blanccone.core.ui.widget

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.blanccone.core.R
import com.blanccone.core.databinding.ActivityImageViewerBinding
import com.blanccone.core.ui.activity.CoreActivity
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

class ImageViewerActivity: CoreActivity<ActivityImageViewerBinding>() {

    private var isFailed: Boolean = false

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            getPermissionResult()
        }

    companion object{
        var path: String? = null

        fun launch(context: Context, path: String){
            val intent = Intent(context, ImageViewerActivity::class.java)
            this.path = path
            context.startActivity(intent)
        }
    }

    override fun inflateLayout(inflater: LayoutInflater): ActivityImageViewerBinding {
        return  ActivityImageViewerBinding.inflate(inflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setFullScreenMode()
        checkPermissions()
    }

    @Suppress("DEPRECATION")
    fun setFullScreenMode(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
    }

    private fun checkPermissions() {
        try {
            if (!Utils.isAllowModifiedStorageAndCamera(this)) {
                permissionLauncher.launch(Utils.getCameraPermissions())
            } else {
                setImage()
            }
        } catch (e:Exception){
            e.printStackTrace()
        }
    }

    private fun setImage() {
        binding.apply {
            pbCircular.show()
            tvLoading.show()
            path?.let {
                Glide.with(this@ImageViewerActivity)
                    .load(it)
                    .apply(RequestOptions.skipMemoryCacheOf(true))
                    .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
                    .transition(GenericTransitionOptions.with(android.R.anim.slide_in_left))
                    .error(R.drawable.ic_no_image)
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(
                            p0: GlideException?,
                            p1: Any?,
                            p2: Target<Drawable>?,
                            p3: Boolean
                        ): Boolean {
                            isFailed = true
                            return false
                        }

                        override fun onResourceReady(
                            p0: Drawable?,
                            p1: Any?,
                            p2: Target<Drawable>?,
                            p3: DataSource?,
                            p4: Boolean
                        ): Boolean {
                            isFailed = false
                            return false
                        }
                    })
                    .into(binding.photoView)

                if(isFailed){
                    photoView.isZoomable = false
                }
                pbCircular.visibility = View.GONE
                tvLoading.hide()
                photoView.show()
                return
            }

            pbCircular.visibility = View.GONE
            tvLoading.hide()
            photoView.apply {
                isZoomable = false
                setImageResource(R.drawable.ic_no_image)
                show()
            }
        }
    }

    private fun getPermissionResult() {
        if (Utils.isAllowModifiedStorageAndCamera(this)) {
            setImage()
        } else {
            Toast.makeText(
                this,
                "Mohon aktifkan izin Camera, Gallery dan File Manager",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}