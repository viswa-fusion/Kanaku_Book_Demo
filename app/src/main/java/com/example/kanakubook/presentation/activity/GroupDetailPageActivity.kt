package com.example.kanakubook.presentation.activity

import android.animation.ObjectAnimator
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.data.util.PreferenceHelper
import com.example.domain.model.ExpenseData
import com.example.domain.usecase.response.PresentationLayerResponse
import com.example.kanakubook.R
import com.example.kanakubook.databinding.DetailPageActivityBinding
import com.example.kanakubook.databinding.PayExpenseDialogBinding
import com.example.kanakubook.presentation.KanakuBookApplication
import com.example.kanakubook.presentation.adapter.ExpenseDetailScreenAdapter
import com.example.kanakubook.presentation.viewmodel.FriendsViewModel
import com.example.kanakubook.presentation.viewmodel.GroupViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.ArrayList

class GroupDetailPageActivity : AppCompatActivity() {

    private lateinit var binding: DetailPageActivityBinding
    private val groupViewModel: GroupViewModel by viewModels { GroupViewModel.FACTORY }
    private val friendsViewModel: FriendsViewModel by viewModels { FriendsViewModel.FACTORY }
    private lateinit var members: List<Long>
    private var groupId: Long = 0
    private lateinit var groupName: String
    private val preferenceHelper = PreferenceHelper(this)
    private var createBy: Long = 0
    private lateinit var adapter: ExpenseDetailScreenAdapter
    private lateinit var alertDialog: Dialog
    private var needToNotifyParent = false


    private val addExpenseActivityResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                needToNotifyParent = true
                groupViewModel.getAllExpenseByGroupId(groupId)
            }
        }

    private val profileResultActivity =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                members = it.data?.getLongArrayExtra("membersId")?.toList() ?: emptyList()
                binding.number.text = "${members.size} members"
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initialSetUp()
        setListener()
        setObserver()
        groupViewModel.getAllExpenseByGroupId(groupId)
    }


    private fun setObserver() {
        groupViewModel.payResponse.observe(this) {
            if (alertDialog.isShowing) {
                when (it) {
                    is PresentationLayerResponse.Success -> {
                        groupViewModel.getAllExpenseByGroupId(groupId)
                        needToNotifyParent = true
                        Toast.makeText(this, "payment Success", Toast.LENGTH_SHORT).show()
                    }

                    is PresentationLayerResponse.Error -> {
                        Toast.makeText(this, "payment Fail", Toast.LENGTH_SHORT).show()
                    }
                }
                alertDialog.dismiss()
            }
        }

        groupViewModel.getAllGroupExpenseResponse.observe(this) {
            when (it) {
                is PresentationLayerResponse.Success -> {
                    adapter.updateData(it.data)
                    if (it.data.isEmpty()) {
                        binding.emptyTemplate.emptyTemplate.visibility = View.VISIBLE
                    } else {
                        binding.emptyTemplate.emptyTemplate.visibility = View.INVISIBLE
                    }
                }

                is PresentationLayerResponse.Error -> {
                    Toast.makeText(this, "fail", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun showFab(fab: FloatingActionButton) {
        fab.visibility = View.VISIBLE
        ObjectAnimator.ofFloat(fab, "scaleX", 1f).apply {
            duration = 200
            start()
        }

        ObjectAnimator.ofFloat(fab, "scaleY", 1f).apply {
            duration = 200
            start()
        }

    }

    fun hideFab(fab: FloatingActionButton) {
        ObjectAnimator.ofFloat(fab, "scaleX", 0f).apply {
            duration = 200
            start()
        }
        ObjectAnimator.ofFloat(fab, "scaleY", 0f).apply {
            duration = 200
            start()
        }
        fab.visibility = View.INVISIBLE
    }
    private fun setListener() {
        binding.recyclerview.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                   hideFab(binding.createFab)
                }else{
                   showFab(binding.createFab)
                }
            }
        })
        binding.createFab.setOnClickListener {
            val intent = Intent(this, AddExpenseActivity::class.java)
            val bundle = Bundle()
            bundle.putLongArray("members", members.toLongArray())
            intent.putExtra("bundleFromDetailPage", bundle)
            intent.putExtra("groupId", groupId)
            intent.putExtra("ExpenseType", false)

            addExpenseActivityResult.launch(intent)
        }

        binding.toolbar.setOnClickListener {
            val anim = ActivityOptionsCompat.makeSceneTransitionAnimation(this,binding.profile,"imageT")
            val intent = Intent(this, GroupProfilePageActivity::class.java)
            val bundle = Bundle()
            bundle.putLongArray("members", members.toLongArray())
            intent.putExtra("bundleFromDetailPage", bundle)
            intent.putExtra("groupId", groupId)
            intent.putExtra("name", groupName)
            intent.putExtra("createdBy", createBy)
            profileResultActivity.launch(intent,anim)
        }
    }

    private fun initialSetUp() {
        binding = DetailPageActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(!isTaskRoot)
        supportActionBar?.title = ""
        binding.settleUpIcon.visibility = View.GONE

        getGroupDataFromIntent()
        setGroupData()
        getGroupProfile()
        setAdapter()
    }

    private fun setAdapter() {
        adapter = ExpenseDetailScreenAdapter(this, object : ExpenseDetailScreenAdapter.Callback {
            override suspend fun getProfile(userId: Long): Bitmap? {
                val w = friendsViewModel.getProfile(userId)
                return w
            }

            override fun onclickCard(item: ExpenseData, view: View) {
                val check = item.listOfSplits.singleOrNull {
                    it.splitUserId == getLoggedUserId()
                }
                if (check != null && check.splitAmount != 0.0 || check?.splitUserId == item.spender.userId) {
                    val intent =
                        Intent(this@GroupDetailPageActivity, ExpenseDetailActivity::class.java)

                    intent.putExtra("userId", getLoggedUserId())
                    intent.putExtra("ownerId", item.spender.userId)
                    intent.putExtra("totalAmount", item.totalAmount)
                    intent.putExtra("ownerName", item.spender.name)
                    intent.putParcelableArrayListExtra("splitList", ArrayList(item.listOfSplits))
                    startActivity(intent)
                } else {
                    Toast.makeText(
                        this@GroupDetailPageActivity,
                        "not include in split",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun pay(expense: ExpenseData) {
                showPaymentConfirmationDialog(expense.spender.userId, expense.expenseId)
            }
        })
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true)
        binding.recyclerview.layoutManager = layoutManager
        binding.recyclerview.adapter = adapter
    }

    private fun showPaymentConfirmationDialog(spenderId: Long, expenseId: Long) {
        if (::alertDialog.isInitialized) {
            alertDialog.dismiss()
        }
        val dialogView = layoutInflater.inflate(R.layout.pay_expense_dialog, null)
        val binding = PayExpenseDialogBinding.bind(dialogView)
        val builder = AlertDialog.Builder(this)
            .setView(dialogView)
        alertDialog = builder.create()
        alertDialog.setCanceledOnTouchOutside(false)
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        binding.btnCancel.setOnClickListener {
            alertDialog.dismiss()
        }
        binding.btnProceed.setOnClickListener {
            binding.loadingScreen.loadingScreen.visibility = View.VISIBLE
            groupViewModel.pay(spenderId, expenseId, getLoggedUserId())
        }

        if (!alertDialog.isShowing) {
            alertDialog.show()
        }
    }

    private fun getGroupProfile() {
        if (groupId != -1L) {
            lifecycleScope.launch(Dispatchers.IO) {
                val bitmap = groupViewModel.getProfile(groupId)
                withContext(Dispatchers.Main) {
                    if (bitmap != null) {
                        binding.profile.setImageBitmap(bitmap)
                    } else {
                        binding.profile.setImageResource(R.drawable.default_group_profile12)
                    }
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("needToNotify",needToNotifyParent)
    }


    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        needToNotifyParent = savedInstanceState.getBoolean("needToNotify")
    }

    override fun finish() {
        if (needToNotifyParent) {
            setResult(Activity.RESULT_OK)
        }
        super.finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        super.onOptionsItemSelected(item)
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }

            else -> {
                false
            }
        }
    }

    private fun getGroupDataFromIntent() {
        groupId = intent.getLongExtra("groupId", -1)
        groupName = intent.getStringExtra("groupName") ?: "- empty -"
        createBy = intent.getLongExtra("createdBy", -1)

        val bundle = intent.getBundleExtra("bundle")
        members = bundle?.getLongArray("members")?.toList() ?: emptyList()
    }

    private fun setGroupData() {
        binding.name.text = groupName
        val totalMembers = "${members.size} members"
        binding.number.text = totalMembers

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