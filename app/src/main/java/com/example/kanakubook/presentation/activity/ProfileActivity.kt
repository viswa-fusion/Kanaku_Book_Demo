package com.example.kanakubook.presentation.activity


import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.MenuItem
import android.view.MotionEvent
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.data.util.PreferenceHelper
import com.example.kanakubook.R
import com.example.kanakubook.databinding.LogoutConfirmationDialogBinding
import com.example.kanakubook.databinding.ProfilePageActivityBinding
import com.example.kanakubook.presentation.KanakuBookApplication
import com.example.kanakubook.presentation.viewmodel.FriendsViewModel
import com.example.kanakubook.util.ImageConversionHelper
import com.google.android.material.imageview.ShapeableImageView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ProfilePageActivityBinding
    private lateinit var preferenceHelper: PreferenceHelper
    private val viewModel: FriendsViewModel by viewModels { FriendsViewModel.FACTORY }
    private lateinit var name: String
    private var phone: Long = -1
    private var isBottomSheetOpen = false
    private var profileUri: Uri? = null
    private val PROFILE_URI_KEY = "person profile"
    private var startX: Float = 0f
    private var originalX: Float = 0f

    private val startActivityResultForProfilePhoto =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                val selectedImageUri = data?.data
                if (selectedImageUri != null) {
                    profileUri = selectedImageUri
                    binding.profile.setImageURI(selectedImageUri)
                    lifecycleScope.launch(Dispatchers.IO) {
                        val bitmap = ImageConversionHelper.loadBitmapFromUri(
                            this@ProfileActivity,
                            profileUri!!
                        )
                        bitmap?.let { viewModel.addProfile(getLoggedUserId(), bitmap) }
                    }
                }
            }
            isBottomSheetOpen = false
        }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initialSetUp()
        setListener()
        name = intent.getStringExtra("name").toString()
        phone = intent.getLongExtra("phone", -1)

        binding.name.text = name
        val phoneFormat = if (phone != -1L) "$phone" else "- empty -"
        binding.phone.text = phoneFormat

        if (profileUri != null) {
            binding.profile.setImageURI(profileUri)
        } else if (viewModel.profileImage != null) {
            binding.profile.setImageBitmap(viewModel.profileImage)
        } else {
            lifecycleScope.launch(Dispatchers.IO) {
                val profile = viewModel.getProfile(getLoggedUserId())
                withContext(Dispatchers.Main) {
                    viewModel.profileImage = profile
                    viewModel.profileImage?.let { binding.profile.setImageBitmap(viewModel.profileImage) }
                }
            }
        }

        binding.editIcon.setOnClickListener {
            if (!isBottomSheetOpen) {
                val intent =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityResultForProfilePhoto.launch(intent)
                isBottomSheetOpen = true
            }
        }
    }

    private fun initialSetUp() {
        binding = ProfilePageActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(!isTaskRoot)
        preferenceHelper = PreferenceHelper(this)
        if (viewModel.profileImage != null) {
            binding.profile.setImageBitmap(viewModel.profileImage)
        } else {
            val userId = intent.getLongExtra("userId", -1)
            lifecycleScope.launch(Dispatchers.IO) {
                val profile = viewModel.getProfile(userId)
                withContext(Dispatchers.Main) {
                    viewModel.profileImage = profile
                    viewModel.profileImage?.let { binding.profile.setImageBitmap(viewModel.profileImage) }
                }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setListener() {

        binding.logoutButton.setOnClickListener {
            Toast.makeText(this, "Swipe to logout", Toast.LENGTH_SHORT).show()
        }

        val logoutIcon = binding.iconCard
        logoutIcon.post {
            originalX = logoutIcon.x
        }

        logoutIcon.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    startX = event.rawX - view.translationX
                    true
                }

                MotionEvent.ACTION_MOVE -> {
                    val endBoundary = binding.linearLayout.right.toFloat() - 16

                    val thresholds = endBoundary - (logoutIcon.width + 16)
                    val newX = event.rawX - startX

                    if (newX >= thresholds || newX < 0) return@setOnTouchListener false
                    view.translationX = newX
                    val alpha = 1 - (newX / thresholds) + 0.2f
                    binding.linearLayout.alpha = alpha.coerceIn(0f, 1f)
                    true
                }

                MotionEvent.ACTION_UP -> {
                    val endBoundary = binding.linearLayout.right.toFloat() - 16
                    val threshold = endBoundary - logoutIcon.width + 16 / 2

                    if (event.rawX - startX >= threshold) {
                        showLogoutDialog()
                        view.animate().translationX(endBoundary - view.width)
                    } else {
                        view.animate().translationX(0f)
                        binding.linearLayout.alpha = 1f
                    }
                    true
                }

                else -> false
            }
        }


        binding.editButton.setOnClickListener {
            val intent = Intent(this, EditProfileActivity::class.java)
            intent.putExtra("userId", getLoggedUserId())
            intent.putExtra("name", name)
            intent.putExtra("phone", phone)
            startActivity(intent)
        }

        binding.profile.setOnClickListener {
            showEnlargedImage(binding.profile.drawable)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.dont_slide, R.anim.slide_out_right)
    }


    private fun showEnlargedImage(imageDrawable: Drawable?) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_enlarged_image)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCanceledOnTouchOutside(true)
        val enlargedImage = dialog.findViewById<ImageView>(R.id.enlarged_image)
        val close = dialog.findViewById<ShapeableImageView>(R.id.close)
        close.setOnClickListener {
            dialog.dismiss()
        }
        imageDrawable?.let {
            enlargedImage.setImageDrawable(it)
        }
        dialog.show()
    }

    private fun showLogoutDialog(): AlertDialog {
        val dialogView = layoutInflater.inflate(R.layout.logout_confirmation_dialog, null)
        val binding = LogoutConfirmationDialogBinding.bind(dialogView)
        val builder = AlertDialog.Builder(this)
            .setView(dialogView)
        val alertDialog = builder.create()
        alertDialog.setCanceledOnTouchOutside(false)
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        binding.cancelButton.setOnClickListener {
            alertDialog.dismiss()
            this.binding.iconCard.animate()?.translationX(0f)
            this.binding.linearLayout.alpha = 1f
        }
        binding.yesButton.setOnClickListener {
            if (preferenceHelper.readBooleanFromPreference(KanakuBookApplication.PREF_IS_USER_LOGIN)) {
                preferenceHelper.writeLongToPreference(KanakuBookApplication.PREF_USER_ID, -1)
                preferenceHelper.writeBooleanToPreference(
                    KanakuBookApplication.PREF_IS_USER_LOGIN,
                    false
                )
                val intent = Intent(this, AppEntryPoint::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
            alertDialog.dismiss()
        }
        alertDialog.show()
        return alertDialog
    }

    private fun getLoggedUserId(): Long {
        if (preferenceHelper.readBooleanFromPreference(KanakuBookApplication.PREF_IS_USER_LOGIN)) {
            val userId = preferenceHelper.readLongFromPreference(KanakuBookApplication.PREF_USER_ID)
            return userId
        } else {
            val intent = Intent(this, AppEntryPoint::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
            finish()
            return -1
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (profileUri != null) {
            outState.putString(PROFILE_URI_KEY, profileUri.toString())
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val profileUri = savedInstanceState.getString(PROFILE_URI_KEY)
        if (profileUri != null) {
            val parseUri = Uri.parse(profileUri)
            this.profileUri = parseUri
            binding.profile.setImageURI(this.profileUri)
        }
    }
}