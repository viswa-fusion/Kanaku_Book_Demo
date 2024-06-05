package com.example.kanakubook.pre.adapter

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.domain.model.GroupData
import com.example.kanakubook.R
import com.example.kanakubook.databinding.GroupViewHolderCardBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.DecimalFormat

class GroupsListAdapter(
    val callback: CallBack
) : RecyclerView.Adapter<GroupsListAdapter.GroupListViewHolder>() {

    interface CallBack{
        suspend fun getImage(groupId: Long): Bitmap?
        fun onClickItemListener(groupData: GroupData)
    }

    private val diffUtil = object :
        DiffUtil.ItemCallback<GroupData>() {

        override fun areItemsTheSame(
            oldItem: GroupData,
            newItem: GroupData
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: GroupData,
            newItem: GroupData
        ): Boolean {
            return oldItem == newItem
        }
    }

    private val asyncListDiffer = AsyncListDiffer(this, diffUtil)

    fun updateData(dataResponse: List<GroupData>) {
        asyncListDiffer.submitList(dataResponse)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupListViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.group_view_holder_card, parent, false)
        val binding = GroupViewHolderCardBinding.bind(view)
        return GroupListViewHolder(binding)
    }

    override fun getItemCount(): Int = asyncListDiffer.currentList.size


    override fun onBindViewHolder(holder: GroupListViewHolder, position: Int) {
        val group = asyncListDiffer.currentList[position]
        holder.bind(group)
    }

    inner class GroupListViewHolder(binding: GroupViewHolderCardBinding) : RecyclerView.ViewHolder(binding.root){
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
            profile.setImageResource(R.drawable.default_group_profile)
        }

        fun bind(groupData: GroupData){
            resetViewHolder()
            bindImageReferenceCheck = groupData.id
            groupName.text = groupData.name
            val decimalFormat = DecimalFormat("#,###")
            val formattedAmount = "₹${decimalFormat.format(groupData.id)}"
            amount.text = formattedAmount

            if(groupData.profile != null){
                profile.setImageBitmap(groupData.profile)
            }else {
                CoroutineScope(Dispatchers.IO).launch {
                    val bitmap = callback.getImage(groupData.id)
                    groupData.profile = bitmap
                    withContext(Dispatchers.Main){
                        if(bindImageReferenceCheck  == groupData.id && groupData.profile != null) {
                            profile.setImageBitmap(groupData.profile)
                        }
                    }
                }
            }
        }
    }
}