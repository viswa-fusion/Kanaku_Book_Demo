package com.example.kanakubook.presentation.adapter

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.kanakubook.R
import com.example.kanakubook.databinding.SplitUserCardBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SplitListAdapter(
    private val context: Context,
    private val callback: Callback
): RecyclerView.Adapter<SplitListAdapter.SplitViewHolder>() {


    private val diffUtil = object :
        DiffUtil.ItemCallback<SplitList>() {

        override fun areItemsTheSame(
            oldItem: SplitList,
            newItem: SplitList
        ): Boolean {
            return oldItem.userId == newItem.userId
        }

        override fun areContentsTheSame(
            oldItem: SplitList,
            newItem: SplitList
        ): Boolean {
            return oldItem == newItem
        }
    }

    private val asyncListDiffer = AsyncListDiffer(this, diffUtil)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SplitViewHolder {
            val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.split_user_card, parent, false)
        return SplitViewHolder(SplitUserCardBinding.bind(view))
    }

    override fun getItemCount(): Int  = asyncListDiffer.currentList.size

    override fun onBindViewHolder(holder: SplitViewHolder, position: Int) {
        val item = asyncListDiffer.currentList[position]
        holder.bind(item)
    }

    inner class SplitViewHolder(val binding: SplitUserCardBinding): RecyclerView.ViewHolder(binding.root){
        private var bindImageReferenceCheck : Long = -1

        init {
            binding.main.setOnClickListener {
                val item = asyncListDiffer.currentList[absoluteAdapterPosition]
                if(item.isSelected) {
                    item.isSelected = false
                    binding.imageProfileOuterCircle.setCardBackgroundColor(Color.GRAY)
                    binding.checkIcon.visibility = View.INVISIBLE
                }else{
                    item.isSelected = true
                    binding.imageProfileOuterCircle.setCardBackgroundColor(Color.GREEN)
                    binding.checkIcon.visibility = View.VISIBLE
                }
                val cData = asyncListDiffer.currentList
                callback.reEvaluateAmount(cData)
            }
        }
        private fun reSetViewHolder(){
            bindImageReferenceCheck = -1
            binding.textviewName.text = ""
            binding.textviewAmount.text = ""
            binding.imageProfile.setImageResource(R.drawable.default_profile_image)
        }
        fun bind(item: SplitList){
            reSetViewHolder()
            bindImageReferenceCheck = item.userId
            binding.textviewName.text = item.name
            val amount = "₹${item.amount}"
            binding.textviewAmount.text = amount
            if(item.amount.toDouble() == 0.0) {
                binding.textviewAmount.setTextColor(Color.GRAY)
            }else{
                binding.textviewAmount.setTextColor(context.resources.getColor(R.color.black))
            }
            if(item.isSelected) {
                binding.imageProfileOuterCircle.setCardBackgroundColor(Color.GREEN)
                binding.checkIcon.visibility = View.VISIBLE

            }else{
                binding.imageProfileOuterCircle.setCardBackgroundColor(Color.GRAY)
                binding.checkIcon.visibility = View.INVISIBLE
            }

           if(item.profile != null) {
               binding.imageProfile.setImageBitmap(item.profile)
           }else{
               CoroutineScope(Dispatchers.IO).launch {
                   val image = callback.getProfile(item.userId)
                   item.profile = image
                   withContext(Dispatchers.Main){
                       if(bindImageReferenceCheck == item.userId && item.profile != null){
                           binding.imageProfile.setImageBitmap(image)
                       }
                   }
               }
           }
        }
    }

    fun updateList(data: List<SplitList>){
        asyncListDiffer.submitList(data)
    }

    fun getCurrentList(): List<SplitList>{
        return asyncListDiffer.currentList
    }
    data class SplitList(
        val userId: Long,
        val name : String,
        var amount: String,
        var profile: Bitmap? = null,
        var isSelected: Boolean = true
    )

    interface Callback{
        suspend fun getProfile(userId: Long): Bitmap?
        fun reEvaluateAmount(listCopy: List<SplitList>)
    }
}


