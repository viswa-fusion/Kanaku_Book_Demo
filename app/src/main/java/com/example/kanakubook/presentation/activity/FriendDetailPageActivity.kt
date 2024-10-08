package com.example.kanakubook.presentation.activity

import android.animation.ObjectAnimator
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
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
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.ArrayList
import androidx.core.util.Pair

class FriendDetailPageActivity : AppCompatActivity() {

    private lateinit var binding: DetailPageActivityBinding
    private val friendViewModel: FriendsViewModel by viewModels { FriendsViewModel.FACTORY }
    private var connectionId: Long? = null
    private var friendId: Long? = null
    private val preferenceHelper = PreferenceHelper(this)
    private lateinit var alertDialog: Dialog
    private lateinit var number: String
    private lateinit var friendName: String
    private var needToNotifyParent = false
    private var firstTimeAnimateComplete = false

    private lateinit var adapter: ExpenseDetailScreenAdapter

    private val addExpenseActivityResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                if (connectionId != null) {
                    friendViewModel.getAllExpenseByConnectionId(connectionId!!)
                    needToNotifyParent = true
                } else {
                    Toast.makeText(this, "No connection id", Toast.LENGTH_SHORT).show()
                }
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initialSetUp()
        setListener()
        setObserver()
        friendViewModel.getAllExpenseByConnectionId(connectionId!!)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        needToNotifyParent = savedInstanceState.getBoolean("needToNotify")
    }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("needToNotify", needToNotifyParent)
    }

    private fun initialSetUp() {
        binding = DetailPageActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(!isTaskRoot)
        supportActionBar?.title = ""
        friendName = intent.getStringExtra("name") ?: ""
        binding.name.text = friendName
        number = "+91 ${intent.getLongExtra("phone", 0)}"
        connectionId = intent.getLongExtra("connectionId", -1)
        friendId = intent.getLongExtra("userId", -1)


        if (friendId != -1L) {
            lifecycleScope.launch(Dispatchers.IO) {
                val result = friendViewModel.getProfile(friendId!!)
                withContext(Dispatchers.Main) {
                    result?.let {
                        binding.profile.setImageBitmap(it)
                    } ?: binding.profile.setImageResource(R.drawable.default_profile_image)
                }
            }
        }
        binding.number.text = number

        adapter = ExpenseDetailScreenAdapter(this, object : ExpenseDetailScreenAdapter.Callback {
            override suspend fun getProfile(userId: Long): Bitmap? {
                return friendViewModel.getProfile(userId)
            }

            override fun onclickCard(item: ExpenseData, view: View) {
                val check = item.listOfSplits.singleOrNull {
                    it.splitUserId == getLoggedUserId()
                }
                if (check != null && check.splitAmount != 0.0 || check?.splitUserId == item.spender.userId) {
                    val intent =
                        Intent(this@FriendDetailPageActivity, ExpenseDetailActivity::class.java)

                    intent.putExtra("userId", getLoggedUserId())
                    intent.putExtra("ownerId", item.spender.userId)
                    intent.putExtra("totalAmount", item.totalAmount)
                    intent.putExtra("ownerName", item.spender.name)
                    intent.putParcelableArrayListExtra("splitList", ArrayList(item.listOfSplits))

                    startActivity(intent)
                } else {
                    Toast.makeText(
                        this@FriendDetailPageActivity,
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
                if (newState != RecyclerView.SCREEN_STATE_ON) {
                    if (binding.createFab.visibility == View.INVISIBLE) {
                        showFab(binding.createFab)
                    }
                }else{
                    if (binding.createFab.visibility == View.VISIBLE) {
                        hideFab(binding.createFab)
                    }
                }
            }
        })
        binding.createFab.setOnClickListener {
            val intent = Intent(this, AddExpenseActivity::class.java)
            val bundle = Bundle()
            val list: List<Long> = listOf(getLoggedUserId(), friendId!!)
            bundle.putLongArray("members", list.toLongArray())
            intent.putExtra("bundleFromDetailPage", bundle)
            intent.putExtra("connectionId", connectionId!!)
            intent.putExtra("ExpenseType", true)

            addExpenseActivityResult.launch(intent)
        }

        binding.toolbar.setOnClickListener {
            val intent = Intent(this, FriendProfilePageActivity::class.java)

            intent.putExtra("userId", getLoggedUserId())
            intent.putExtra("friendId", friendId!!)
            intent.putExtra("friendName", friendName)
            intent.putExtra("friendNumber", number)
            val profilePair = Pair<View, String>(binding.profile, "imageT")
            val pairs = arrayOf(profilePair)
            val bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(this, *pairs)
            startActivity(intent,bundle.toBundle())
        }
    }

    private fun setObserver() {
        friendViewModel.payResponse.observe(this) {
            if (::alertDialog.isInitialized) {
                when (it) {
                    is PresentationLayerResponse.Success -> {
                        needToNotifyParent = true
                        friendViewModel.getAllExpenseByConnectionId(connectionId!!)
                        Toast.makeText(this, "payment Success", Toast.LENGTH_SHORT).show()
                    }

                    is PresentationLayerResponse.Error -> {
                        Toast.makeText(this, "payment Fail", Toast.LENGTH_SHORT).show()
                    }
                }
                alertDialog.dismiss()
            }
        }

        friendViewModel.getAllFriendsExpenseResponse.observe(this) {
            when (it) {
                is PresentationLayerResponse.Success -> {
                    if (it.data.isEmpty()) {
                        binding.emptyTemplate.emptyTemplate.visibility = View.VISIBLE
                    } else {
                        binding.emptyTemplate.emptyTemplate.visibility = View.INVISIBLE

                            adapter.updateData(it.data)

                    }
                }

                is PresentationLayerResponse.Error -> {
                    Toast.makeText(this, "get expense fail", Toast.LENGTH_SHORT).show()
                }
            }
        }
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

    private fun showPaymentConfirmationDialog(spenderId: Long, expenseId: Long) {
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
            friendViewModel.pay(spenderId, expenseId, getLoggedUserId())
        }
        alertDialog.show()
    }

    override fun finish() {
        if (needToNotifyParent) {
            setResult(Activity.RESULT_OK)
        }
        super.finish()
    }
}