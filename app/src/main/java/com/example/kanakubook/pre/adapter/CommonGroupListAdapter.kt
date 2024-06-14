package com.example.kanakubook.pre.adapter

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.domain.model.CommonGroupWIthAmountData
import com.example.kanakubook.R
import com.example.kanakubook.databinding.GroupViewHolderCardTestBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Math.abs

class CommonGroupListAdapter(
    private val context: Context,
    private val callback: CallBack
) : RecyclerView.Adapter<CommonGroupListAdapter.GroupListViewHolder>() {

    private val rupeeSymbol = "\u20B9"
    interface CallBack{
        suspend fun getImage(groupId: Long): Bitmap?
        fun onClickItemListener(groupData: CommonGroupWIthAmountData)
    }

    private val diffUtil = object :
        DiffUtil.ItemCallback<CommonGroupWIthAmountData>() {

        override fun areItemsTheSame(
            oldItem: CommonGroupWIthAmountData,
            newItem: CommonGroupWIthAmountData
        ): Boolean {
            return oldItem.group.id == newItem.group.id
        }

        override fun areContentsTheSame(
            oldItem: CommonGroupWIthAmountData,
            newItem: CommonGroupWIthAmountData
        ): Boolean {
            return oldItem == newItem
        }
    }

    private val asyncListDiffer = AsyncListDiffer(this, diffUtil)

    fun updateData(dataResponse: List<CommonGroupWIthAmountData>) {
        asyncListDiffer.submitList(dataResponse)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupListViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.group_view_holder_card_test, parent, false)
        val binding = GroupViewHolderCardTestBinding.bind(view)
        return GroupListViewHolder(binding)
    }

    override fun getItemCount(): Int = asyncListDiffer.currentList.size


    override fun onBindViewHolder(holder: GroupListViewHolder, position: Int) {
        val group = asyncListDiffer.currentList[position]
        holder.bind(group)
    }

    inner class GroupListViewHolder(binding: GroupViewHolderCardTestBinding) : RecyclerView.ViewHolder(binding.root){
        private var bindImageReferenceCheck : Long = -1
        private val profile = binding.shapeableImageView2
        private val groupName = binding.textViewName
        private val amount = binding.textViewAmount

        init {
            binding.root.setOnClickListener {
                callback.onClickItemListener(asyncListDiffer.currentList[bindingAdapterPosition])
            }
        }

        private fun resetViewHolder(){
            bindImageReferenceCheck = -1
            groupName.text = ""
            amount.text = ""
            profile.setImageResource(R.drawable.default_group_profile12)
        }

        fun bind(groupData: CommonGroupWIthAmountData){
            resetViewHolder()
            bindImageReferenceCheck = groupData.group.id
            groupName.text = groupData.group.name

            val cAmount = groupData.get - groupData.pay
            val formatted = String.format("% .2f",abs(cAmount))
            when{
                cAmount > 0 -> {
                    amount.text = "$rupeeSymbol$formatted"
                    amount.setTextColor(context.getColor(R.color.amount_Green_text))
                }
                cAmount < 0 -> {

                    amount.text = "$rupeeSymbol$formatted"
                    amount.setTextColor(context.getColor(R.color.amount_Red_text))
                }
                else ->{
                    amount.text ="no pending"
                    amount.setTextColor(Color.GRAY)
                }
            }


            if(groupData.profile != null){
                profile.setImageBitmap(groupData.profile)
            }else {
                CoroutineScope(Dispatchers.IO).launch {
                    val bitmap = callback.getImage(groupData.group.id)
                    groupData.profile = bitmap
                    withContext(Dispatchers.Main){
                        if(bindImageReferenceCheck  == groupData.group.id && groupData.profile != null) {
                            profile.setImageBitmap(groupData.profile)
                        }
                    }
                }
            }
        }
    }
}