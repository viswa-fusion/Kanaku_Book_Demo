package com.example.kanakubook.pre.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.domain.usecase.response.PresentationLayerResponse
import com.example.kanakubook.R
import com.example.kanakubook.databinding.DetailPageActivityBinding
import com.example.kanakubook.pre.adapter.ExpenseDetailScreenAdapter
import com.example.kanakubook.pre.viewmodel.GroupViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GroupDetailPageActivity : AppCompatActivity() {

    private lateinit var binding: DetailPageActivityBinding
    private val viewModel: GroupViewModel by viewModels { GroupViewModel.FACTORY }
    private lateinit var members: List<Long>
    private var groupId : Long = 0
    private val groupViewModel: GroupViewModel by viewModels { GroupViewModel.FACTORY }
    private val addExpenseActivityResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        if(it.resultCode == Activity.RESULT_OK){
            groupViewModel.getAllExpenseByGroupId(groupId)
        }
    }
    private lateinit var adapter: ExpenseDetailScreenAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initialSetUp()
        setListener()
        setObserver()
        groupViewModel.getAllExpenseByGroupId(groupId)
    }

    private fun setObserver() {
        groupViewModel.getAllGroupExpenseResponse.observe(this){
            when(it){
                is PresentationLayerResponse.Success -> {
                    if (it.data.isEmpty()){
                        binding.emptyTemplate.emptyTemplate.visibility = View.VISIBLE
                    }else{
                        binding.emptyTemplate.emptyTemplate.visibility = View.INVISIBLE
                        adapter.updateData(it.data)
                    }

                    it.data.forEach {
                        Log.i("dataTest","test : $it")
                    }

                }
                is PresentationLayerResponse.Error -> {
                    Toast.makeText(this,"fail", Toast.LENGTH_SHORT).show()
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
            intent.putExtra("groupId",groupId)
            addExpenseActivityResult.launch(intent)
        }
    }

    private fun initialSetUp() {
        binding = DetailPageActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(!isTaskRoot)
        supportActionBar?.title = ""
        binding.settleUpIcon.visibility = View.VISIBLE
        binding.name.text = intent.getStringExtra("groupName")
        val createBy = "Created by: ${intent.getLongExtra("createdBy", -1)}"


        val bundle = intent.getBundleExtra("bundle")

        members = bundle?.getLongArray("members")?.toList() ?: emptyList()

        Log.i(
            "datatest123", "data new : ${
                members?.map {
                    "$it\n"
                }
            }"
        )
        val totalMembers = "${members.size} members"
        binding.number.text = totalMembers
        groupId = intent.getLongExtra("groupId", -1)

        if (groupId != -1L) {
            lifecycleScope.launch(Dispatchers.IO) {
                val bitmap = viewModel.getProfile(groupId)
                withContext(Dispatchers.Main) {
                    if (bitmap != null) {
                        binding.profile.setImageBitmap(bitmap)
                    } else {
                        binding.profile.setImageResource(R.drawable.default_group_profile)
                    }
                }
            }
        }

        adapter = ExpenseDetailScreenAdapter(this)
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true)
        binding.recyclerview.layoutManager = layoutManager
        binding.recyclerview.adapter = adapter
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

}