package com.github.drjacky.imagepicker.sample

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.github.drjacky.imagepicker.ImagePicker
import com.github.drjacky.imagepicker.constant.ImageProvider
import com.github.drjacky.imagepicker.sample.databinding.ActivityMainBinding
import com.github.drjacky.imagepicker.sample.util.IntentUtil
import com.github.drjacky.imagepicker.util.IntentUtils

class MainActivity : AppCompatActivity() {

    companion object {
        private const val GITHUB_REPOSITORY = "https://github.com/drjacky/ImagePicker"
    }

    private var mCameraUri: Uri? = null
    private var mGalleryUri: Uri? = null
    private var mProfileUri: Uri? = null

    private lateinit var binding: ActivityMainBinding

    private val profileLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                val uri = it.data?.data!!
                mProfileUri = uri
                binding.contentMain.contentProfile.imgProfile.setLocalImage(uri, true)
            } else {
                parseError(it)
            }
        }
    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                if (it.data?.hasExtra(ImagePicker.EXTRA_FILE_PATH)!!) {
                    val uri = it.data?.data!!
                    mGalleryUri = uri
                    binding.contentMain.contentGalleryOnly.imgGallery.setLocalImage(uri)
                } else if (it.data?.hasExtra(ImagePicker.MULTIPLE_FILES_PATH)!!) {
                    val files = ImagePicker.getAllFile(it.data) as ArrayList<Uri>
                    if (files.size > 0) {
                        val uri = files[0] // first image
                        mGalleryUri = uri
                        binding.contentMain.contentGalleryOnly.imgGallery.setLocalImage(uri)
                    }
                } else {
                    parseError(it)
                }
            } else {
                parseError(it)
            }
        }

    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            print("EFEEEEEE - cameraLauncher it.resultCode: ${it.resultCode}")
            if (it.resultCode == RESULT_OK) {
                val uri = it.data?.data!!
                mCameraUri = uri
                binding.contentMain.contentCameraOnly.imgCamera.setLocalImage(uri, false)
            } else {
                parseError(it)
            }
        }

    private fun parseError(activityResult: ActivityResult) {
        if (activityResult.resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(this, ImagePicker.getError(activityResult.data), Toast.LENGTH_SHORT)
                .show()
        } else {
            Toast.makeText(this, "Task Cancelled", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        binding.contentMain.contentProfile.imgProfile.setDrawableImage(R.drawable.ic_person, true)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_github -> {
                IntentUtil.openURL(this, GITHUB_REPOSITORY)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun pickProfileImage(view: View) {
        ImagePicker.with(this)
            .crop()
            .cropOval()
            .maxResultSize(512, 512, true)
            .provider(ImageProvider.BOTH) // Or bothCameraGallery()
            .setDismissListener {
                Log.d("ImagePicker", "onDismiss")
            }
            .createIntentFromDialog { profileLauncher.launch(it) }
    }

    fun pickGalleryImage(view: View) {
        galleryLauncher.launch(
            ImagePicker.with(this)
                .crop()
                .galleryOnly()
                .setMultipleAllowed(true)
//                .setOutputFormat(Bitmap.CompressFormat.WEBP)
                .cropFreeStyle()
                .galleryMimeTypes( // no gif images at all
                    mimeTypes = arrayOf(
                        "image/png",
                        "image/jpg",
                        "image/jpeg"
                    )
                )
                .createIntent()
        )
    }

    fun pickCameraImage(view: View) {
        cameraLauncher.launch(
            ImagePicker.with(this)
                .crop()
                .cameraOnly()
                .maxResultSize(1080, 1920, true)
                .createIntent()
        )
    }

    fun showImage(view: View) {
        getUri(view)?.let { uri ->
            startActivity(IntentUtils.getUriViewIntent(this@MainActivity, uri))
        }
    }

    fun showImageInfo(view: View) {
        getUri(view)?.let { uri ->
            AlertDialog.Builder(this)
                .setTitle("Image Info")
//                .setMessage(FileUtil.getFileInfo(this, uri))
                .setPositiveButton("Ok", null)
                .show()
        }
    }

    private fun getUri(view: View): Uri? = with(binding.contentMain) {
        when (view) {
            contentProfile.imgProfile -> mProfileUri
            contentCameraOnly.imgCamera -> mCameraUri
            contentGalleryOnly.imgGallery -> mGalleryUri
            else -> null
        }
    }
}
