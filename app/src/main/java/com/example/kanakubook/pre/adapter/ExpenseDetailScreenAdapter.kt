package com.example.kanakubook.pre.adapter

import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.data.util.PreferenceHelper
import com.example.domain.model.ExpenseData
import com.example.kanakubook.R
import com.example.kanakubook.databinding.ExpenseLeftCardBinding
import com.example.kanakubook.databinding.ExpenseRightCardBinding
import com.example.kanakubook.pre.KanakuBookApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ExpenseDetailScreenAdapter(val context: Context, val callback: Callback) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var loggedUserId: Long? = null
    private val preferenceHelper = PreferenceHelper(context)
    private val RIGHT_CARD = 1
    private val LEFT_CARD = 2
    private val rupeeSymbol = "\u20B9"

    init {
        if (loggedUserId == null && preferenceHelper.readBooleanFromPreference(KanakuBookApplication.PREF_IS_USER_LOGIN)) {
            loggedUserId =
                preferenceHelper.readLongFromPreference(KanakuBookApplication.PREF_USER_ID)
        }
    }

    private val diffUtil = object :
        DiffUtil.ItemCallback<ExpenseData>() {

        override fun areItemsTheSame(
            oldItem: ExpenseData,
            newItem: ExpenseData
        ): Boolean {
            return oldItem.expenseId == newItem.expenseId
        }

        override fun areContentsTheSame(
            oldItem: ExpenseData,
            newItem: ExpenseData
        ): Boolean {
            return oldItem == newItem
        }
    }

    private val asyncListDiffer = AsyncListDiffer(this, diffUtil)


    fun updateData(dataResponse: List<ExpenseData>) {
        asyncListDiffer.submitList(dataResponse)
        notifyItemInserted(dataResponse.size -1)
    }

    override fun getItemViewType(position: Int): Int {
        super.getItemViewType(position)
        val item = asyncListDiffer.currentList[position]
        return if (item.spender.userId == loggedUserId) RIGHT_CARD
        else LEFT_CARD
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            RIGHT_CARD -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.expense_right_card, parent, false)
                RightViewHolder(ExpenseRightCardBinding.bind(view))
            }

            LEFT_CARD -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.expense_left_card, parent, false)
                LeftViewHolder(ExpenseLeftCardBinding.bind(view))
            }

            else -> throw (Exception())
        }
    }

    override fun getItemCount(): Int = asyncListDiffer.currentList.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = asyncListDiffer.currentList[position]
        when (holder) {
            is RightViewHolder -> holder.bind(item)
            is LeftViewHolder -> holder.bind(item)
        }
    }


    inner class LeftViewHolder(val binding: ExpenseLeftCardBinding) :
        RecyclerView.ViewHolder(binding.root) {
        var bindImageReferenceCheck = -1L

        private fun resetViewHolder() {
            bindImageReferenceCheck = -1
            binding.note.text = ""
            binding.ownerName.text = ""
            binding.amount.text = ""
            binding.ownerProfile.setImageResource(R.drawable.default_profile_image)
        }

        fun bind(item: ExpenseData) {
            bindImageReferenceCheck = item.expenseId
            val amount =
                rupeeSymbol + item.listOfSplits.find { it.splitUserId == loggedUserId }?.splitAmount.toString()
            binding.amount.text = amount
            if (!item.note.isNullOrEmpty()) {
                binding.note.text = item.note
            }
            binding.ownerName.text = item.spender.name
            if (item.spender.profile != null) {
                binding.ownerProfile.setImageBitmap(item.spender.profile)
            } else {
                CoroutineScope(Dispatchers.IO).launch {
                    val bitmap = callback.getProfile(item.spender.userId)
                    item.spender.profile = bitmap
                    withContext(Dispatchers.Main) {
                        if (bindImageReferenceCheck == item.expenseId && item.spender.profile != null) {
                            binding.ownerProfile.setImageBitmap(item.spender.profile)
                        }
                    }
                }
            }
        }
    }

    inner class RightViewHolder(val binding: ExpenseRightCardBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ExpenseData) {
            if (!item.note.isNullOrEmpty()) {
                binding.note.text = item.note
            }
            val amount = rupeeSymbol + item.totalAmount
            binding.amount.text = amount

        }
    }

    fun interface Callback {
        suspend fun getProfile(userId: Long): Bitmap?
    }
}