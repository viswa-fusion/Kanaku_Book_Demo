package com.example.kanakubook.pre.adapter

import android.graphics.Bitmap
import android.icu.util.CurrencyAmount
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.domain.Converters.PaidStatus
import com.example.domain.model.UserProfileSummary
import com.example.kanakubook.R
import com.example.kanakubook.databinding.SplitUserCardBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ExpenseDetailProfileAdapter(
    private val isOwnerView :Boolean,
    private val callback: CallBack
): RecyclerView.Adapter<ExpenseDetailProfileAdapter.MyViewHolder>() {
    private val rupeeSymbol = "\u20B9"

    private val diffUtil = object :
        DiffUtil.ItemCallback<ExpenseDetailObject>() {

        override fun areItemsTheSame(
            oldItem: ExpenseDetailObject,
            newItem:  ExpenseDetailObject
        ): Boolean {
            return oldItem.user.userId == newItem.user.userId
        }

        override fun areContentsTheSame(
            oldItem:  ExpenseDetailObject,
            newItem:  ExpenseDetailObject
        ): Boolean {
            return oldItem == newItem
        }
    }

    private val asyncListDiffer = AsyncListDiffer(this, diffUtil)

    fun updateList(list: List<ExpenseDetailObject>){
        asyncListDiffer.submitList(list)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.split_user_card, parent, false)
        return MyViewHolder(SplitUserCardBinding.bind(view))
    }

    override fun getItemCount(): Int  = asyncListDiffer.currentList.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = asyncListDiffer.currentList[position]
        holder.bind(item)
    }

    inner class MyViewHolder(val binding: SplitUserCardBinding): RecyclerView.ViewHolder(binding.root){
        private var bindImageReferenceCheck : Long = -1

        init {
            binding.root.setOnClickListener {
                callback.onClick(asyncListDiffer.currentList[absoluteAdapterPosition].user)
            }
        }

        private fun reSetViewHolder(){
            bindImageReferenceCheck = -1
            binding.textviewName.text = ""
            binding.textviewAmount.text = ""
            binding.checkIcon.visibility = View.GONE
            binding.imageProfile.setImageResource(R.drawable.default_profile_image)
        }
        fun bind(item:  ExpenseDetailObject){
            bindImageReferenceCheck = item.user.userId
            reSetViewHolder()
            binding.textviewName.text = item.user.name
            val formatted = String.format("%.2f",item.splitAmount)
            binding.textviewAmount.text = "$rupeeSymbol$formatted"
            if (item.paidStatus == PaidStatus.Paid && isOwnerView){
                binding.checkIcon.visibility = View.VISIBLE
            }
            if (item.user.profile != null){
                binding.imageProfile.setImageBitmap(item.user.profile)
            }else{
                CoroutineScope(Dispatchers.IO).launch {
                    item.user.profile = callback.getImage(item.user.userId)
                    withContext(Dispatchers.Main){
                        item.user.profile?.let{binding.imageProfile.setImageBitmap(item.user.profile)}
                    }
                }
            }
        }
    }

   interface CallBack{
       suspend fun getImage(userId:Long):Bitmap?
       fun onClick(user: UserProfileSummary)
   }

    data class ExpenseDetailObject(
        val user: UserProfileSummary,
        val paidStatus: PaidStatus,
        val splitAmount: Double
    )
}


