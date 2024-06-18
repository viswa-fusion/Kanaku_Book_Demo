package com.example.kanakubook.presentation.activity

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.data.util.PreferenceHelper
import com.example.domain.model.UserProfileSummary
import com.example.domain.usecase.response.PresentationLayerResponse
import com.example.kanakubook.R
import com.example.kanakubook.databinding.MultiUserPickListFragmentBinding
import com.example.kanakubook.databinding.SearchUserListLayout1Binding
import com.example.kanakubook.presentation.KanakuBookApplication
import com.example.kanakubook.presentation.adapter.UserListingAdapter
import com.example.kanakubook.presentation.viewmodel.FriendsViewModel
import com.example.kanakubook.presentation.viewmodel.UserViewModel
import java.util.Locale

class AddFriendActivity : AppCompatActivity() {

    private lateinit var binding: MultiUserPickListFragmentBinding
    private lateinit var preferenceHelper: PreferenceHelper
    private val userViewModel: UserViewModel by viewModels { UserViewModel.FACTORY }
    private val viewModel: FriendsViewModel by viewModels { FriendsViewModel.FACTORY }
    private lateinit var adapter: UserListingAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = MultiUserPickListFragmentBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.horizontalRecyclerView.visibility = View.GONE

        binding.toolbar.visibility = View.VISIBLE
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(!isTaskRoot)
        supportActionBar?.title = "Add Friend"
        setObserver()
        setListener()
        preferenceHelper = PreferenceHelper(this)
        adapter = UserListingAdapter(this,object : UserListingAdapter.Callback {
            override suspend fun getImage(userId: Long): Bitmap? {
                return viewModel.getProfile(userId)
            }


            override fun clickListener(user: UserProfileSummary) {
                val view = LayoutInflater.from(this@AddFriendActivity).inflate(R.layout.search_user_list_layout_1, null)
                val binding = SearchUserListLayout1Binding.bind(view)
                binding.textview.text = user.name
                user.profile?.let { binding.imageProfile.setImageBitmap(user.profile)}
                AlertDialog.Builder(this@AddFriendActivity).apply {
                    setView(view)
                    setMessage("Confirm to add this friend")
                    setPositiveButton("Ok") { _, _ ->
                        showLoading()
                        viewModel.addFriend(getLoggedUserId(), user.phone)
                    }
                    setNegativeButton("Cancel"){_,_->}
                    show()
                }

            }

        })
        binding.verticalRecyclerView.layoutManager = LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false)
        binding.verticalRecyclerView.adapter = adapter


        binding.searchView.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.searchText = s.toString().lowercase(Locale.ROOT).trim()
                val filteredList = viewModel.listOfMySelectableUserProfileSummary.filter {
                    it.name.lowercase(Locale.ROOT).contains(viewModel.searchText)
                }
                if (filteredList.isEmpty()){
                    binding.searchNotFound.emptyTemplate.visibility = View.VISIBLE
                }else{
                    binding.searchNotFound.emptyTemplate.visibility = View.GONE
                    adapter.highlightText(viewModel.searchText)
                    adapter.updateData(filteredList)
                }
            }
        })
    }

    private fun setListener() {
        binding.verticalRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    hideKeyboard()
                }
            }
        })
    }

    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home -> {
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }
    override fun onResume() {
        super.onResume()
        userViewModel.getAllKanakuBookUsers(getLoggedUserId())
    }

    private fun setObserver() {

        userViewModel.allUserData.observe(this) {
            if(viewModel.listOfMySelectableUserProfileSummary.isEmpty()){
                when (it) {
                    is PresentationLayerResponse.Success -> {
                        if (it.data.isEmpty()) {
                            binding.searchNotFound.emptyTemplate.visibility = View.VISIBLE
                        } else {
                            binding.searchNotFound.emptyTemplate.visibility = View.GONE
                            viewModel.listOfMySelectableUserProfileSummary = it.data

                            adapter.updateData(it.data)
                        }
                    }

                    is PresentationLayerResponse.Error -> {
                        Toast.makeText(this, "can't get friends data", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            viewModel.addFriend.observe(this) {
                when (it) {
                    is PresentationLayerResponse.Success -> {
                        if (it.data) {
                            hideLoading()
                            Toast.makeText(this, "add new friend", Toast.LENGTH_SHORT).show()
                            setResult(Activity.RESULT_OK)
                            finish()
                        }
                    }

                    is PresentationLayerResponse.Error -> {
                        Toast.makeText(this, "can't add now", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun showLoading() {
        binding.loadingScreen.loadingScreen.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        binding.loadingScreen.loadingScreen.visibility = View.GONE
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
}

