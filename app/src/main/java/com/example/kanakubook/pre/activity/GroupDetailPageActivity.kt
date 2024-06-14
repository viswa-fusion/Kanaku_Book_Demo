package com.example.kanakubook.pre.activity

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
import com.example.data.util.PreferenceHelper
import com.example.domain.model.ExpenseData
import com.example.domain.usecase.response.PresentationLayerResponse
import com.example.kanakubook.R
import com.example.kanakubook.databinding.DetailPageActivityBinding
import com.example.kanakubook.databinding.PayExpenseDialogBinding
import com.example.kanakubook.pre.KanakuBookApplication
import com.example.kanakubook.pre.adapter.ExpenseDetailScreenAdapter
import com.example.kanakubook.pre.viewmodel.FriendsViewModel
import com.example.kanakubook.pre.viewmodel.GroupViewModel
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


    private val addExpenseActivityResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                groupViewModel.getAllExpenseByGroupId(groupId)
            }
        }

    private val profileResultActivity = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        if(it.resultCode == Activity.RESULT_OK){
            members = it.data?.getLongArrayExtra("membersId")?.toList() ?: emptyList()
            binding.number.text = "${members.size} members"
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initialSetUp()
        setListener()
        setObserver()
    }

    override fun onResume() {
        super.onResume()
        groupViewModel.getAllExpenseByGroupId(groupId)
    }

    private fun setObserver() {
        groupViewModel.payResponse.observe(this) {
            if (alertDialog.isShowing) {
                when (it) {
                    is PresentationLayerResponse.Success -> {
                        groupViewModel.getAllExpenseByGroupId(groupId)
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
                    if (it.data.isEmpty()) {
                        binding.emptyTemplate.emptyTemplate.visibility = View.VISIBLE
                    } else {
                        binding.emptyTemplate.emptyTemplate.visibility = View.INVISIBLE
                        adapter.updateData(it.data)
                    }

                    it.data.forEach {
                        Log.i("dataTest", "test : $it")
                    }

                }

                is PresentationLayerResponse.Error -> {
                    Toast.makeText(this, "fail", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setListener() {
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
            val intent = Intent(this, GroupProfilePageActivity::class.java)
            val bundle = Bundle()
            bundle.putLongArray("members", members.toLongArray())
            intent.putExtra("bundleFromDetailPage", bundle)
            intent.putExtra("groupId", groupId)
            intent.putExtra("name", groupName)
            profileResultActivity.launch(intent)
        }
    }

    private fun initialSetUp() {
        binding = DetailPageActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(!isTaskRoot)
        supportActionBar?.title = ""
        binding.settleUpIcon.visibility = View.VISIBLE

        getGroupDataFromIntent()
        setGroupData()
        getGroupProfile()
        setAdapter()
    }

    private fun setAdapter() {
        adapter = ExpenseDetailScreenAdapter(this, object : ExpenseDetailScreenAdapter.Callback {
            override suspend fun getProfile(userId: Long): Bitmap? {
                return friendsViewModel.getProfile(userId)
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

            override fun pay(expenseId: Long) {
                showPaymentConfirmationDialog(expenseId)
            }
        })
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true)
        binding.recyclerview.layoutManager = layoutManager
        binding.recyclerview.adapter = adapter
    }

    private fun showPaymentConfirmationDialog(expenseId: Long) {
      if (::alertDialog.isInitialized){
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
                groupViewModel.pay(expenseId, getLoggedUserId())
            }

        if(!alertDialog.isShowing) {
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        super.onOptionsItemSelected(item)
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
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