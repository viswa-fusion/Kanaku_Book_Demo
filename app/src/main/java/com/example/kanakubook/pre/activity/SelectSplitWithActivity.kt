package com.example.kanakubook.pre.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.commit
import com.example.kanakubook.R
import com.example.kanakubook.databinding.SelectSplitWithBinding
import com.example.kanakubook.pre.fragment.ViewPagerFragment
import com.example.kanakubook.pre.viewmodel.CommonViewModel

class SelectSplitWithActivity : AppCompatActivity() {

    private lateinit var binding: SelectSplitWithBinding
    private val commonViewModel: CommonViewModel by viewModels ()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initialSetUp()
        setListener()
        setObserver()
        supportFragmentManager.commit {
            replace(R.id.fragment_container_view_1,ViewPagerFragment())
            setReorderingAllowed(true)
        }
    }

    private fun setListener() {
        binding.searchView.addTextChangedListener {
            filterViewPagerFragments(it.toString())
        }
    }

    private fun setObserver() {
        commonViewModel.selectSplitWithListener.observe(this){
            val customIntent:Intent = if (it.expenseType){
                val gIntent = Intent()
                val bundle = Bundle()
                val list: List<Long> = it.members
                bundle.putLongArray("members", list.toLongArray())
                gIntent.putExtra("bundleFromDetailPage", bundle)
                gIntent.putExtra("connectionId",it.id)
                gIntent.putExtra("ExpenseType",true)
                gIntent
            }else{
                val fIntent = Intent()
                val bundle = Bundle()
                bundle.putLongArray("members", it.members.toLongArray())
                fIntent.putExtra("bundleFromDetailPage", bundle)
                fIntent.putExtra("groupId", it.id)
                fIntent.putExtra("ExpenseType", false)
                fIntent
            }

            setResult(Activity.RESULT_OK,customIntent)
            finish()
        }
    }

    private fun initialSetUp() {
        binding = SelectSplitWithBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(!isTaskRoot)
        binding.toolbar.setNavigationIcon(R.drawable.close_24px)

    }

    private fun filterViewPagerFragments(query: String) {
        val fragment = supportFragmentManager.findFragmentById(R.id.fragment_container_view_1)
        if (fragment is ViewPagerFragment) {
            fragment.filterData(query)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home -> {
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}