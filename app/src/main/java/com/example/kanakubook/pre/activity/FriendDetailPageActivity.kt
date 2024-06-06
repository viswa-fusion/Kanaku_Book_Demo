package com.example.kanakubook.pre.activity

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.data.util.PreferenceHelper
import com.example.domain.usecase.response.PresentationLayerResponse
import com.example.kanakubook.R
import com.example.kanakubook.databinding.DetailPageActivityBinding
import com.example.kanakubook.pre.KanakuBookApplication
import com.example.kanakubook.pre.adapter.ExpenseDetailScreenAdapter
import com.example.kanakubook.pre.viewmodel.FriendsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FriendDetailPageActivity : AppCompatActivity() {

    private lateinit var binding: DetailPageActivityBinding
    private val viewModel: FriendsViewModel by viewModels { FriendsViewModel.FACTORY }
    private var connectionId:Long? = null
    private var friendId: Long? = null
    private val preferenceHelper = PreferenceHelper(this)

    private lateinit var adapter: ExpenseDetailScreenAdapter

    private val addExpenseActivityResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        if(it.resultCode == Activity.RESULT_OK){
            if(connectionId != null) {
                viewModel.getAllExpenseByConnectionId(connectionId!!)
            }else{
                Toast.makeText(this, "No connection id", Toast.LENGTH_SHORT).show()
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initialSetUp()
        setListener()
    }

    private fun initialSetUp() {
        binding = DetailPageActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(!isTaskRoot)
        supportActionBar?.title = ""

        binding.name.text = intent.getStringExtra("name")
        val number = "+91 ${intent.getLongExtra("phone", 0)}"
        connectionId = intent.getLongExtra("connectionId",-1)
        friendId = intent.getLongExtra("userId",-1)

        setObserver()
        if(friendId != -1L ){
            lifecycleScope.launch(Dispatchers.IO) {
                viewModel.getProfile(friendId!!)?.let {
                    binding.profile.setImageBitmap(it)
                }?:binding.profile.setImageResource(R.drawable.default_profile_image)
            }
        }
        binding.number.text = number

        adapter = ExpenseDetailScreenAdapter(this){
            viewModel.getProfile(it)
        }
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true)
        binding.recyclerview.layoutManager = layoutManager
        binding.recyclerview.adapter = adapter

        viewModel.getAllExpenseByConnectionId(connectionId!!)
    }

    private fun setListener() {
        binding.createFab.setOnClickListener {
            val intent = Intent(this,AddExpenseActivity::class.java)
            val bundle = Bundle()
            val list: List<Long> = listOf(getLoggedUserId(), friendId!!)
            bundle.putLongArray("members", list.toLongArray())
            intent.putExtra("bundleFromDetailPage", bundle)
            intent.putExtra("ExpenseType",true)
            intent.putExtra("connectionId",connectionId!!)

            addExpenseActivityResult.launch(intent)
        }
    }

    private fun setObserver(){
        viewModel.getAllFriendsExpenseResponse.observe(this) {
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

    private fun getLoggedUserId() :Long{
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