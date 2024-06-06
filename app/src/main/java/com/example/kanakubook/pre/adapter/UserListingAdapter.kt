package com.example.kanakubook.pre.adapter

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.domain.model.UserProfileSummary
import com.example.kanakubook.R
import com.example.kanakubook.databinding.SearchUserListLayout1Binding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UserListingAdapter(private val callback: Callback): RecyclerView.Adapter<UserListingAdapter.MyViewHolder>() {


    private val diffUtil = object :
        DiffUtil.ItemCallback<UserProfileSummary>() {

        override fun areItemsTheSame(
            oldItem: UserProfileSummary,
            newItem: UserProfileSummary
        ): Boolean {
            return oldItem.userId == newItem.userId
        }

        override fun areContentsTheSame(
            oldItem: UserProfileSummary,
            newItem: UserProfileSummary
        ): Boolean {
            return oldItem == newItem
        }
    }

    private val asyncListDiffer = AsyncListDiffer(this, diffUtil)


    fun updateData(dataResponse: List<UserProfileSummary>) {
        asyncListDiffer.submitList(dataResponse)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.search_user_list_layout_1, parent, false)
        return MyViewHolder(SearchUserListLayout1Binding.bind(view))
    }

    override fun getItemCount(): Int = asyncListDiffer.currentList.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = asyncListDiffer.currentList[position]
        holder.bind(item)
    }

    inner class MyViewHolder(val binding: SearchUserListLayout1Binding) : RecyclerView.ViewHolder(binding.root){
        private var bindImageReferenceCheck : Long = -1
        init {
            binding.checkIcon.visibility = View.GONE
            binding.main.setOnClickListener {
                callback.clickListener(asyncListDiffer.currentList[absoluteAdapterPosition])
            }
        }

        private fun resetViewHolder(){
            binding.textview.text = ""
            binding.imageProfile.setImageResource(R.drawable.default_profile_image)
        }
        fun bind(item: UserProfileSummary){
            resetViewHolder()
            bindImageReferenceCheck = item.userId
            binding.textview.text = item.name


            if(item.profile != null){
                binding.imageProfile.setImageBitmap(item.profile)
            }else {
                CoroutineScope(Dispatchers.IO).launch {
                    val bitmap = callback.getImage(item.userId)
                    item.profile = bitmap
                    withContext(Dispatchers.Main){
                        if(bindImageReferenceCheck  == item.userId && item.profile != null) {
                            binding.imageProfile.setImageBitmap(item.profile)
                        }
                    }
                }
            }
        }
    }
    interface Callback{
        suspend fun getImage(userId:Long):Bitmap?
        fun clickListener(user: UserProfileSummary)
    }
}