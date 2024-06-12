package com.example.kanakubook.pre.activity

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.commit
import com.example.data.util.PreferenceHelper
import com.example.domain.usecase.response.PresentationLayerResponse
import com.example.kanakubook.R
import com.example.kanakubook.databinding.AddGroupScreenActivityBinding
import com.example.kanakubook.pre.KanakuBookApplication
import com.example.kanakubook.pre.fragment.MultiUserPickListFragment
import com.example.kanakubook.pre.viewmodel.FriendsViewModel
import com.example.kanakubook.pre.viewmodel.GroupViewModel

class AddGroupActivity : AppCompatActivity() {

    private lateinit var binding: AddGroupScreenActivityBinding
    private val viewModel: GroupViewModel by viewModels { GroupViewModel.FACTORY }
    private val friendsViewModel: FriendsViewModel by viewModels { FriendsViewModel.FACTORY }
    private var isBottomSheetOpen = false
    private var profileUri: Uri? = null
    private val PROFILE_URI_KEY = "group profile"
    private lateinit var preferenceHelper: PreferenceHelper
    private val fragment: MultiUserPickListFragment by lazy { MultiUserPickListFragment() }
    private var isFirstTimeValidation = true

    private val startActivityResultForProfilePhoto =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                val selectedImageUri = data?.data
                if (selectedImageUri != null) {
                    profileUri = selectedImageUri
                    binding.imageview.setImageURI(selectedImageUri)
                }
            }
            isBottomSheetOpen = false
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initialSetUp()
        setListener()
        setObserver()

        supportFragmentManager.commit {
            fragment.apply {
                arguments = Bundle().apply {
                    putLong("userId", loggedInUserId())
                }
            }
            val frag = supportFragmentManager.findFragmentByTag("fragAdd")
            if (frag == null) {
                replace(R.id.fragment_container_view, fragment, "fragAdd")
            }
            setReorderingAllowed(true)
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
            binding.imageview.setImageURI(this.profileUri)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.add_group_toolbar_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.done -> {
                if (validation()) {
                    showLoading()
                    val logUserId = loggedInUserId()
                    val groupName = binding.groupname.text.toString()
                    val membersId = friendsViewModel.selectedList.map {
                        it.userId
                    }.toMutableList()
                    membersId.add(logUserId)
                    viewModel.createGroup(this, logUserId, groupName, profileUri, membersId)
                }
            }
        }

        return super.onOptionsItemSelected(item)

    }

    private fun validation(): Boolean {
        isFirstTimeValidation = false
        fieldValidation(binding.groupname.text)
        val result = binding.layoutGroupName.error == null
        return result
    }

    private fun setListener() {
        binding.imageview.setOnClickListener {
            if (!isBottomSheetOpen) {
                val intent =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityResultForProfilePhoto.launch(intent)
                isBottomSheetOpen = true
            }
        }

        binding.close.setOnClickListener {
            finish()
        }

        binding.groupname.addTextChangedListener {
            if (!isFirstTimeValidation) {
                fieldValidation(it)
            }
        }
    }

    private fun setObserver() {
        viewModel.groupCreateResponse.observe(this) {
            when (it) {
                is PresentationLayerResponse.Success -> {
                    Toast.makeText(this, "success", Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK)
                    hideLoading()
                    finish()
                }

                is PresentationLayerResponse.Error -> {
                    Toast.makeText(this, "fail", Toast.LENGTH_SHORT).show()
                    setResult(RESULT_CANCELED)
                    hideLoading()
                    finish()
                }
            }
        }
    }

    private fun loggedInUserId(): Long {
        return if (preferenceHelper.readBooleanFromPreference(KanakuBookApplication.PREF_IS_USER_LOGIN)) {
            preferenceHelper.readLongFromPreference(KanakuBookApplication.PREF_USER_ID)
        } else {
            val intent = Intent(this, AppEntryPoint::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
            finish()
            -1
        }
    }

    private fun initialSetUp() {
        binding = AddGroupScreenActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = ""
        preferenceHelper = PreferenceHelper(this)
    }

    private fun showLoading() {
        binding.loadingScreen.loadingScreen.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        binding.loadingScreen.loadingScreen.visibility = View.GONE
    }

    private fun fieldValidation(value: Editable?) {
        val size = value?.length
        if (size == null || size < 3) {
            binding.layoutGroupName.error = "group name contain min 3 letters"
        } else {
            binding.layoutGroupName.error = null
        }
    }
}