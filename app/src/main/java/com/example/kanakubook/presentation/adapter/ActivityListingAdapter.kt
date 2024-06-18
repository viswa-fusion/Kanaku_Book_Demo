package com.example.kanakubook.presentation.adapter

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.domain.Converters.ActivityType
import com.example.domain.model.ActivityModel
import com.example.kanakubook.R
import com.example.kanakubook.databinding.ActivityViewHolderCardBinding
import com.example.kanakubook.util.Constants
import com.example.kanakubook.util.DateConvertor.formatTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ActivityListingAdapter(
    private val callback: CallBack
) : RecyclerView.Adapter<ActivityListingAdapter.ActivityListViewHolder>() {

    interface CallBack{
        suspend fun getUserImage(userId: Long): Bitmap?
        suspend fun getGroupImage(groupId: Long): Bitmap?
        fun onClickItemListener(activity: ActivityModel)
    }

    private val diffUtil = object :
        DiffUtil.ItemCallback<ActivityModel>() {

        override fun areItemsTheSame(
            oldItem: ActivityModel,
            newItem: ActivityModel
        ): Boolean {
            return oldItem.activityId == newItem.activityId
        }

        override fun areContentsTheSame(
            oldItem: ActivityModel,
            newItem: ActivityModel
        ): Boolean {
            return oldItem == newItem
        }
    }

    private val asyncListDiffer = AsyncListDiffer(this, diffUtil)

    fun updateData(dataResponse: List<ActivityModel>) {
        asyncListDiffer.submitList(dataResponse)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActivityListViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_view_holder_card, parent, false)
        val binding = ActivityViewHolderCardBinding.bind(view)
        return ActivityListViewHolder(binding)
    }

    override fun getItemCount(): Int = asyncListDiffer.currentList.size


    override fun onBindViewHolder(holder: ActivityListViewHolder, position: Int) {
        val group = asyncListDiffer.currentList[position]
        holder.bind(group)
    }

    inner class ActivityListViewHolder(binding: ActivityViewHolderCardBinding) : RecyclerView.ViewHolder(binding.root){
        private var bindImageReferenceCheck : Long = -1
        private val mainImage = binding.mainImage
        private val subImage = binding.subImage
        private val content = binding.textViewName
        private val date = binding.date

        init {
            binding.root.setOnClickListener {
                callback.onClickItemListener(asyncListDiffer.currentList[absoluteAdapterPosition])
            }
        }

        private fun resetViewHolder(){
            bindImageReferenceCheck = -1
            content.text = ""
            date.text = ""
            mainImage.setImageResource(R.drawable.payment_vector)
            subImage.setImageResource(R.drawable.default_profile_image)
        }

        fun bind(item: ActivityModel){
            resetViewHolder()
            bindImageReferenceCheck = item.activityId

            fun getGroupProfileForMain(groupId: Long){
                if (item.mainImage != null) {
                    mainImage.setImageBitmap(item.mainImage)
                } else {
                    CoroutineScope(Dispatchers.IO).launch {
                        val bitmap = callback.getGroupImage(groupId)
                        item.mainImage = bitmap
                        withContext(Dispatchers.Main) {
                            if (bindImageReferenceCheck == item.activityId ) {
                                item.mainImage?.let{mainImage.setImageBitmap(item.mainImage)}?:mainImage.setImageResource(R.drawable.default_group_profile12)
                            }
                        }
                    }
                }
            }

            fun getGroupProfileFroSub(groupId: Long){
                if (item.subImage != null) {
                    subImage.setImageBitmap(item.subImage)
                } else {
                    CoroutineScope(Dispatchers.IO).launch {
                        val bitmap = callback.getGroupImage(groupId)
                        item.subImage = bitmap
                        withContext(Dispatchers.Main) {
                            if (bindImageReferenceCheck == item.activityId && item.subImage != null) {
                                subImage.setImageBitmap(item.subImage)
                            }
                        }
                    }
                }
            }
            fun getUserProfileForSub(userId: Long){
                if (item.subImage != null) {
                    subImage.setImageBitmap(item.subImage)
                } else {
                    CoroutineScope(Dispatchers.IO).launch {
                        val bitmap = callback.getUserImage(userId)
                        item.subImage = bitmap
                        withContext(Dispatchers.Main) {
                            if (bindImageReferenceCheck == item.activityId && item.subImage != null) {
                                subImage.setImageBitmap(item.subImage)
                            }
                        }
                    }
                }
            }
            fun getUserProfileForMain(userId: Long){
                if (item.mainImage != null) {
                    mainImage.setImageBitmap(item.mainImage)
                } else {
                    CoroutineScope(Dispatchers.IO).launch {
                        val bitmap = callback.getUserImage(userId)
                        item.mainImage = bitmap
                        withContext(Dispatchers.Main) {
                            if (bindImageReferenceCheck == item.activityId && item.mainImage != null) {
                                mainImage.setImageBitmap(item.mainImage)
                            }
                        }
                    }
                }
            }

            val str = when(item.activityType){
                ActivityType.ADD_FRIEND ->{
                        getUserProfileForSub(item.user.userId)
                        item.friend?.let { getUserProfileForMain(item.friend!!.userId) }
                    "You added new connection '${item.friend?.name}'"
                }

                ActivityType.CREATE_GROUP -> {
                    getUserProfileForSub(item.user.userId)
                    item.group?.let { getGroupProfileForMain(item.group!!.id) }
                    "You created new group '${item.group?.name}'"
                }

                ActivityType.ADD_EXPENSE -> {
                    getUserProfileForSub(item.user.userId)
                    mainImage.setImageResource(R.drawable.expense_vector)
                    "You create new expense in '${item.group?.name?:item.friend?.name}'"
                }
                ActivityType.ADD_MEMBER_TO_GROUP -> {
                    getUserProfileForSub(item.user.userId)
                    item.group?.let { getUserProfileForMain(item.group!!.id) }
                    "You add new member in '${item.group?.name}'"
                }
                ActivityType.PAY_FOR_EXPENSE -> {
                    getUserProfileForSub(item.user.userId)
                    mainImage.setImageResource(R.drawable.payment_vector)
                    "You pay '${Constants.RUPEE_SYMBOL}${item.expense?.listOfSplits?.single { 
                        it.splitUserId == item.user.userId
                    }?.splitAmount}' for expense '${if(item.expense?.note.isNullOrEmpty()) "Split Request" else item.expense?.note}'"
                }
                ActivityType.SPLIT_MEMBER_PAY -> {
                    item.friend?.let { getUserProfileForSub(item.friend!!.userId) }
                    mainImage.setImageResource(R.drawable.payment_vector)
                    "${item.friend?.name} pay '${Constants.RUPEE_SYMBOL}${item.expense?.listOfSplits?.single {
                        it.splitUserId == item.friend?.userId
                    }?.splitAmount}' for expense '${if(item.expense?.note.isNullOrEmpty()) "Split Request" else item.expense?.note}'"
                }
            }
            content.text = str
            date.text = formatTime(item.timestamp)
        }
    }
}