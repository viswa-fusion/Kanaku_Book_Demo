package com.example.kanakubook.pre.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.view.inputmethod.InputMethodManager.HIDE_NOT_ALWAYS
import android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import androidx.fragment.app.commitNow
import com.example.data.entity.ExpenseType
import com.example.data.util.PreferenceHelper
import com.example.domain.model.UserProfileSummary
import com.example.domain.usecase.response.PresentationLayerResponse
import com.example.kanakubook.R
import com.example.kanakubook.databinding.AddExpenseScreenActivityBinding
import com.example.kanakubook.pre.KanakuBookApplication
import com.example.kanakubook.pre.adapter.SplitListAdapter
import com.example.kanakubook.pre.fragment.SplitUserListFragment
import com.example.kanakubook.pre.viewmodel.FriendsViewModel
import com.example.kanakubook.pre.viewmodel.GroupViewModel
import com.example.kanakubook.pre.viewmodel.UserViewModel
import com.example.kanakubook.util.NumberTextWatcher

class AddExpenseActivity : AppCompatActivity() {

    private lateinit var binding: AddExpenseScreenActivityBinding
    private lateinit var inputMethodManager: InputMethodManager
    private val fragment: SplitUserListFragment by lazy { SplitUserListFragment() }
    private lateinit var membersId: List<Long>
    private val userViewModel: UserViewModel by viewModels { UserViewModel.FACTORY }
    private val groupViewModel: GroupViewModel by viewModels { GroupViewModel.FACTORY }
    private val friendsViewModel: FriendsViewModel by viewModels { FriendsViewModel.FACTORY }
    private var members: List<UserProfileSummary>? = null
    private var isFragmentAdded = false
    private var id: Long = 0
    private lateinit var preferenceHelper: PreferenceHelper
    private lateinit var expenseType: ExpenseType
    private var isNextButtonClicked = false
    private var NEXT_BUTTON_CLICKED_TAG = "is_next_clicked"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initialSetUp()
        setListener()
        setObserve()
        getMembersDetail()
    }

    private fun initialSetUp(){
        binding = AddExpenseScreenActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(!isTaskRoot)
            title = ""
        }
        preferenceHelper = PreferenceHelper(this)
        binding.mainButton.isEnabled = false

        getIntentExtras()
    }
    private fun getIntentExtras(){
        val bundle = intent.getBundleExtra("bundleFromDetailPage")
        bundle?.let {
            it.getLongArray("members")?.toList()?.let { data ->
                membersId = data
            }
        }
        id = intent.getLongExtra("groupId", -1L).let {
            if (it == -1L) {
                intent.getLongExtra("connectionId",-1)
            }else it
        }
        expenseType = if (intent.getBooleanExtra(
                "ExpenseType",
                false
            )
        ) ExpenseType.FriendsExpense else ExpenseType.GroupExpense
    }

    private fun setListener(){
        binding.amount.addTextChangedListener(NumberTextWatcher(binding.amount) {
            binding.mainButton.isEnabled = it
        })

        binding.amount.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                isNextButtonClicked = false
                if (isFragmentAdded && supportFragmentManager.fragments.contains(fragment)) {
                    supportFragmentManager.commit {
                        remove(fragment)
                    }
                    binding.note.visibility = View.INVISIBLE
                    isFragmentAdded = false
                    switchButton(false)
                }
                showKeyboard(binding.amount)
            } else {
                hideKeyboard(binding.amount)
            }
        }

        binding.mainButton.setOnClickListener {
            isNextButtonClicked = true
            supportFragmentManager.commitNow {
                replace(R.id.fragment_container_view, fragment)
            }
            val totalAmount = binding.amount.text.toString().replace(",", "").toDouble()
            val calculateAmount = totalAmount / membersId.size

            binding.submitButton.isEnabled = calculateAmount >= 1

            val value = String.format("%.2f", calculateAmount)
            if (members != null) {
                fragment.setList(totalAmount, members!!.map { userData ->
                    SplitListAdapter.SplitList(
                        userData.userId,
                        userData.name,
                        value
                    )
                })
                binding.note.visibility = View.VISIBLE
                switchButton(true)
                binding.amount.clearFocus()
                isFragmentAdded = true
            }
        }

        binding.submitButton.setOnClickListener {
            showLoading()
            submitData()
        }
    }


    private fun submitData(){
        val finalList = fragment.getList()
        val totalAmount = binding.amount.text.toString().replace(",", "").toDouble()
        when(expenseType){
            ExpenseType.GroupExpense -> {
                groupViewModel.createExpense(
                    id,
                    getLoggedUserId(),
                    totalAmount,
                    binding.note.text.toString(),
                    finalList.map {
                        Pair(it.userId, it.amount.toDouble())
                    }
                )
            }

            ExpenseType.FriendsExpense -> {
                friendsViewModel.createExpense(
                    id,
                    getLoggedUserId(),
                    totalAmount,
                    binding.note.text.toString(),
                    finalList.map {
                        Pair(it.userId, it.amount.toDouble())
                    }
                )
            }
        }
    }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(NEXT_BUTTON_CLICKED_TAG,isNextButtonClicked)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
            isNextButtonClicked = savedInstanceState.getBoolean(NEXT_BUTTON_CLICKED_TAG)
    }
    private fun getMembersDetail() {
        userViewModel.getUser(membersId)
    }

    private fun setObserve() {
        userViewModel.userData.observe(this) {
            members = it
        }

        fragment.submitButtonState.observe(this) {
            binding.submitButton.isEnabled = it
        }

        when(expenseType ){
            ExpenseType.GroupExpense -> {
                groupViewModel.groupExpenseCreateResponse.observe(this) {
                    when (it) {
                        is PresentationLayerResponse.Success -> {
                            Toast.makeText(this, "split added", Toast.LENGTH_SHORT).show()
                            hideLoading()
                            setResult(Activity.RESULT_OK)
                        }

                        is PresentationLayerResponse.Error -> {
                            Toast.makeText(this, "something went wrong", Toast.LENGTH_SHORT).show()

                        }
                    }
                    finish()
                }
            }

            ExpenseType.FriendsExpense ->{
                friendsViewModel.friendsExpenseCreateResponse.observe(this){
                    when (it) {
                        is PresentationLayerResponse.Success -> {
                            Toast.makeText(this, "split added", Toast.LENGTH_SHORT).show()
                            hideLoading()
                            setResult(Activity.RESULT_OK)
                        }

                        is PresentationLayerResponse.Error -> {
                            Toast.makeText(this, "something went wrong", Toast.LENGTH_SHORT).show()

                        }
                    }
                    finish()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if(isNextButtonClicked){
            binding.mainButton.callOnClick()
        }
        binding.amount.requestFocus()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showKeyboard(view: View) {
        inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(view, SHOW_IMPLICIT)
    }

    private fun hideKeyboard(view: View) {
        inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, HIDE_NOT_ALWAYS)
    }


    private fun switchButton(direction: Boolean) {
        if (direction) {
            binding.mainButton.visibility = View.INVISIBLE
            binding.submitButton.visibility = View.VISIBLE
        } else {
            binding.mainButton.visibility = View.VISIBLE
            binding.submitButton.visibility = View.INVISIBLE
        }
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

    private fun showLoading() {
        binding.loadingScreen.loadingScreen.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        binding.loadingScreen.loadingScreen.visibility = View.GONE
    }

}
