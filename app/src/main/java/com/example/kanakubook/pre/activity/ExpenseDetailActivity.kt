package com.example.kanakubook.pre.activity

import android.graphics.Bitmap
import android.os.Bundle
import android.transition.TransitionInflater
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.domain.Converters.PaidStatus
import com.example.domain.model.SplitEntry
import com.example.domain.model.UserProfileSummary
import com.example.domain.usecase.response.PresentationLayerResponse
import com.example.kanakubook.R
import com.example.kanakubook.databinding.ExpenseDetailPageBinding
import com.example.kanakubook.pre.adapter.ExpenseDetailProfileAdapter
import com.example.kanakubook.pre.adapter.ExpenseDetailScreenAdapter
import com.example.kanakubook.pre.viewmodel.FriendsViewModel
import com.example.kanakubook.pre.viewmodel.UserViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ExpenseDetailActivity: AppCompatActivity() {

    private lateinit var binding: ExpenseDetailPageBinding
    private val friendsViewModel: FriendsViewModel by viewModels { FriendsViewModel.FACTORY }
    private val userViewModel: UserViewModel by viewModels { UserViewModel.FACTORY }
    private var userId:Long? = null
    private var ownerId:Long? = null
    private lateinit var ownerName:String
    private var totalAmount:Double? = null
    private var listOfSplit :List<SplitEntry>? = null
    private val rupeeSymbol = "\u20B9"
    private lateinit var adapter: ExpenseDetailProfileAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initialSetUp()
        setObserver()
        setListener()
    }

    private fun setObserver() {
        userViewModel.userData.observe(this){userList ->
          if(userList != null){
              val expenseDetailList: List<ExpenseDetailProfileAdapter.ExpenseDetailObject>? =
                  listOfSplit?.mapNotNull { splitEntry ->
                      val userProfile = userList.find { it.userId == splitEntry.splitUserId && splitEntry.splitAmount != 0.0 }
                      userProfile?.let { splitEntry.toExpenseDetailObject(it) }
                  }

              if (expenseDetailList != null){
                  adapter.updateList(expenseDetailList)
              }else{
                  Toast.makeText(this@ExpenseDetailActivity,"user data not found", Toast.LENGTH_SHORT).show()
              }
          }else{
              Toast.makeText(this@ExpenseDetailActivity,"user data not found", Toast.LENGTH_SHORT).show()
          }
        }
    }

    private fun setListener() {
        binding.close.setOnClickListener {
            finish()
        }
    }

    private fun setUpAllData() {
        getProfile()
        val mySplit = listOfSplit?.single{
            it.splitUserId == userId
        }
        val includeCount = listOfSplit?.count { it.splitAmount != 0.0 }?:0
        val paidCount = listOfSplit?.count{ it.paidStatus == PaidStatus.Paid  && it.splitAmount != 0.0}?:0
        if (userId == ownerId){
            binding.titleText.text = "Total: $rupeeSymbol$totalAmount"
            var paidAmount = 0.0
            var leftAmount = 0.0
            listOfSplit?.forEach {
                if (it.splitAmount != 0.0){
                    if (it.paidStatus == PaidStatus.UnPaid) leftAmount += it.splitAmount
                    if (it.paidStatus == PaidStatus.Paid) paidAmount += it.splitAmount
                }
            }
            binding.progressBar.progress = (paidCount*100)/includeCount
            binding.textLeft.text = "$rupeeSymbol${String.format("%.2f",paidAmount)} paid"
            binding.textRight.text = "$rupeeSymbol${String.format("%.2f",leftAmount)} left"
        }else{
            val text =  "$ownerName Requested '$rupeeSymbol${mySplit?.splitAmount?:0.00}'"
            binding.titleText.text = text
            binding.content.visibility = View.GONE
            binding.progressBar.visibility = View.GONE
            if(mySplit?.paidStatus == PaidStatus.Paid){
                binding.paid.visibility = View.VISIBLE 
            }
        }
        
        binding.paidStatusText.text = "$paidCount of $includeCount paid"

    }

    private fun extractIntentData() {
        userId = intent.getLongExtra("userId",-1)
        ownerId = intent.getLongExtra("ownerId",-1)
        listOfSplit = intent.getParcelableArrayListExtra("splitList")
        totalAmount = intent.getDoubleExtra("totalAmount",0.0)
        ownerName = intent.getStringExtra("ownerName")?:""
    }

    private fun initialSetUp() {
        binding = ExpenseDetailPageBinding.inflate(layoutInflater)
        setContentView(binding.root)
//        window.sharedElementEnterTransition = TransitionInflater.from(this)
//            .inflateTransition(R.transition.shared_element_transition)

        extractIntentData()
        setUpAllData()
        setAdapter()
    }

    override fun onResume() {
        super.onResume()
        getMembers()
    }
    private fun setAdapter() {

        adapter = ExpenseDetailProfileAdapter(ownerId == userId,object :ExpenseDetailProfileAdapter.CallBack{
            override suspend fun getImage(userId: Long): Bitmap? {
                return friendsViewModel.getProfile(userId)
            }
        })
        binding.recyclerview.layoutManager = LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false)
        binding.recyclerview.adapter = adapter

    }

    private fun getMembers(){
        val list = listOfSplit?.map {
            it.splitUserId
        }
        if(list != null){
            userViewModel.getUser(list)
        }
    }

    private fun getProfile() {
        if (ownerId != null) {
            lifecycleScope.launch(Dispatchers.IO) {
                val bitmap = friendsViewModel.getProfile(ownerId!!)
                withContext(Dispatchers.Main) {
                    if (bitmap != null) {
                        binding.profile.setImageBitmap(bitmap)
                    } else {
                        binding.profile.setImageResource(R.drawable.default_profile_image)
                    }
                }
            }
        }
    }

    private fun SplitEntry.toExpenseDetailObject(userProfile: UserProfileSummary): ExpenseDetailProfileAdapter.ExpenseDetailObject {
        return ExpenseDetailProfileAdapter.ExpenseDetailObject(userProfile, paidStatus, splitAmount)
    }
}