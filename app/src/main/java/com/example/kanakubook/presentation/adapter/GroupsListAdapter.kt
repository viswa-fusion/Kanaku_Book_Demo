package com.example.kanakubook.presentation.adapter

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.domain.model.GroupData
import com.example.kanakubook.R
import com.example.kanakubook.databinding.GroupViewHolderCardTestBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.DecimalFormat
import java.util.Locale
import kotlin.math.abs

class GroupsListAdapter(
    private val context: Context,
    private val callback: CallBack
) : RecyclerView.Adapter<GroupsListAdapter.GroupListViewHolder>() {
    private var searchText: String = ""

    interface CallBack {
        suspend fun getImage(groupId: Long): Bitmap?
        fun onClickItemListener(groupData: GroupData)

        fun clickImage(drawable: Drawable?)
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

    fun getData(): List<GroupData> {
        return asyncListDiffer.currentList
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

    inner class GroupListViewHolder(binding: GroupViewHolderCardTestBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private var bindImageReferenceCheck: Long = -1
        private val profile = binding.shapeableImageView2
        private val groupName = binding.textViewName
        private val amount = binding.textViewAmount

        init {
            binding.root.setOnClickListener {
                callback.onClickItemListener(asyncListDiffer.currentList[bindingAdapterPosition])
            }

            binding.shapeableImageView2.setOnClickListener {
                callback.clickImage(binding.shapeableImageView2.drawable)
            }
        }

        private fun resetViewHolder() {
            bindImageReferenceCheck = -1
            groupName.text = ""
            amount.text = ""
            profile.setImageResource(R.drawable.default_group_profile12)
        }

        fun bind(groupData: GroupData) {
            resetViewHolder()
            bindImageReferenceCheck = groupData.id

            val name = groupData.name
            val spannable = SpannableString(name)
            val searchText = searchText.lowercase(Locale.getDefault())

            val startIndex = name.lowercase(Locale.getDefault()).indexOf(searchText)
            if (startIndex != -1) {
                spannable.setSpan(
                    StyleSpan(Typeface.BOLD),
                    startIndex,
                    startIndex + searchText.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )

                val colorSpan =
                    ForegroundColorSpan(context.getColor(R.color.md_theme_primaryContainer_mediumContrast))
                spannable.setSpan(
                    colorSpan,
                    startIndex,
                    startIndex + searchText.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }

            groupName.text = spannable
            val decimalFormat = DecimalFormat("#,###.##")
            val calculateAmount = groupData.get - groupData.pay
            val absAmount = abs(calculateAmount)
            val formattedAmount = "₹${decimalFormat.format(absAmount)}"
            when {
                calculateAmount > 0 -> {
                    amount.setTextColor(context.getColor(R.color.amount_Green_text))
                    amount.text = formattedAmount
                }

                calculateAmount < 0 -> {
                    amount.setTextColor(context.getColor(R.color.amount_Red_text))
                    amount.text = formattedAmount
                }

                else -> {
                    amount.setTextColor(Color.GRAY)
                    amount.text = "no pending"
                }
            }


            if (groupData.profile != null) {
                profile.setImageBitmap(groupData.profile)
            } else {
                CoroutineScope(Dispatchers.IO).launch {
                    val bitmap = callback.getImage(groupData.id)
                    groupData.profile = bitmap
                    withContext(Dispatchers.Main) {
                        if (bindImageReferenceCheck == groupData.id && groupData.profile != null) {
                            profile.setImageBitmap(groupData.profile)
                        }
                    }
                }
            }
        }
    }

    fun highlightText(searchText: String) {
        this.searchText = searchText
        notifyDataSetChanged()
    }
}