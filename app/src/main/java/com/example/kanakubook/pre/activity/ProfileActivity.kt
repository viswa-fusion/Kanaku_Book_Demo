package com.example.kanakubook.pre.activity


import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.MenuItem
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.data.util.PreferenceHelper
import com.example.domain.model.UserProfileData
import com.example.kanakubook.R
import com.example.kanakubook.databinding.LogoutConfirmationDialogBinding
import com.example.kanakubook.databinding.ProfilePageActivityBinding
import com.example.kanakubook.pre.KanakuBookApplication
import com.example.kanakubook.pre.viewmodel.FriendsViewModel
import com.example.kanakubook.pre.viewmodel.LoginViewModel
import com.example.kanakubook.pre.viewmodel.UserViewModel
import com.example.kanakubook.util.ImageConversionHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.properties.Delegates

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ProfilePageActivityBinding
    private lateinit var preferenceHelper: PreferenceHelper
    private val viewModel: FriendsViewModel by viewModels { FriendsViewModel.FACTORY }
    private lateinit var name: String
    private var phone : Long = -1
    private var isBottomSheetOpen = false
    private var profileUri : Uri? = null
    private val PROFILE_URI_KEY = "person profile"

    private val startActivityResultForProfilePhoto =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                val selectedImageUri = data?.data
                if (selectedImageUri != null) {
                    profileUri = selectedImageUri
                    binding.profile.setImageURI(selectedImageUri)
                    lifecycleScope.launch(Dispatchers.IO) {
                    val bitmap = ImageConversionHelper.loadBitmapFromUri(this@ProfileActivity,profileUri!!)
                    bitmap?.let { viewModel.addProfile(getLoggedUserId(),bitmap)}
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
        val phoneFormat = if (phone != -1L) "+91 $phone" else "- empty -"
        binding.phone.text = phoneFormat

        if(profileUri != null){
            binding.profile.setImageURI(profileUri)
        }
        else if(viewModel.profileImage != null){
            binding.profile.setImageBitmap(viewModel.profileImage)
        }else {
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

    @RequiresApi(Build.VERSION_CODES.S)
    private fun setListener() {
        binding.logoutButton.setOnClickListener {
            showLogoutDialog()
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

    @RequiresApi(Build.VERSION_CODES.S)
    private fun showEnlargedImage(imageDrawable: Drawable?) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_enlarged_image)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCanceledOnTouchOutside(true)
        val enlargedImage = dialog.findViewById<ImageView>(R.id.enlarged_image)
        imageDrawable?.let {
            enlargedImage.setImageDrawable(it)
        }
        dialog.show()
    }

    private fun showLogoutDialog() {
        val dialogView = layoutInflater.inflate(R.layout.logout_confirmation_dialog, null)
        val binding = LogoutConfirmationDialogBinding.bind(dialogView)
        val builder = AlertDialog.Builder(this)
            .setView(dialogView)
        val alertDialog = builder.create()
        alertDialog.setCanceledOnTouchOutside(true)
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        binding.cancelButton.setOnClickListener {
            alertDialog.dismiss()
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