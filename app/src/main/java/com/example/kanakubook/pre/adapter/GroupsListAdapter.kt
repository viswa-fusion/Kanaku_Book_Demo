package com.example.kanakubook.pre.adapter

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.domain.model.Group
import com.example.kanakubook.R
import com.example.kanakubook.databinding.GroupViewHolderCardBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.DecimalFormat

class GroupsListAdapter(
    private val callback :suspend (Long)-> Bitmap?
) : RecyclerView.Adapter<GroupsListAdapter.GroupListViewHolder>() {


    private val diffUtil = object :
        DiffUtil.ItemCallback<Group>() {

        override fun areItemsTheSame(
            oldItem: Group,
            newItem: Group
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: Group,
            newItem: Group
        ): Boolean {
            return oldItem == newItem
        }
    }

    private val asyncListDiffer = AsyncListDiffer(this, diffUtil)

    fun updateData(dataResponse: List<Group>) {
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

        private fun resetViewHolder(){
            bindImageReferenceCheck = -1
            groupName.text = ""
            amount.text = ""
            profile.setImageResource(R.drawable.default_group_profile)
        }

        fun bind(group: Group){
            resetViewHolder()
            bindImageReferenceCheck = group.id
            groupName.text = group.name
            val decimalFormat = DecimalFormat("#,###")
            val formattedAmount = "₹${decimalFormat.format(group.id)}"
            amount.text = formattedAmount

            if(group.bitmap != null){
                profile.setImageBitmap(group.bitmap)
            }else {
                CoroutineScope(Dispatchers.IO).launch {
                    val bitmap = callback(group.id)
                    group.bitmap = bitmap
                    withContext(Dispatchers.Main){
                        if(bindImageReferenceCheck  == group.id && group.bitmap != null) {
                            profile.setImageBitmap(group.bitmap)
                        }
                    }
                }
            }
        }
    }
}