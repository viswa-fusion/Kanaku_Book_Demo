package com.example.kanakubook.presentation.adapter

import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.data.util.PreferenceHelper
import com.example.domain.Converters.PaidStatus
import com.example.domain.model.ExpenseData
import com.example.domain.model.SplitEntry
import com.example.kanakubook.R
import com.example.kanakubook.databinding.ExpenseLeftCardBinding
import com.example.kanakubook.databinding.ExpenseRightCardBinding
import com.example.kanakubook.presentation.KanakuBookApplication
import com.example.kanakubook.util.Constants
import com.example.kanakubook.util.DateConvertor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ExpenseDetailScreenAdapter(val context: Context, val callback: Callback) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var loggedUserId: Long? = null
    private val preferenceHelper = PreferenceHelper(context)
    private val RIGHT_CARD = 1
    private val LEFT_CARD = 2

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

        init {
            binding.payButton.setOnClickListener {
                callback.pay(asyncListDiffer.currentList[absoluteAdapterPosition])
            }

            binding.cardView.setOnClickListener {
                callback.onclickCard(
                    asyncListDiffer.currentList[absoluteAdapterPosition],
                    binding.cardView
                )
            }
        }

        private fun resetViewHolder() {
            bindImageReferenceCheck = -1
            binding.note.text = ""
            binding.ownerName.text = ""
            binding.amount.text = ""
            binding.ownerProfile.setImageResource(R.drawable.default_profile_image)
            binding.amount.visibility = View.VISIBLE
            binding.progressBar.visibility = View.VISIBLE
            binding.payButton.visibility = View.VISIBLE
            binding.paidStatusText.text = ""
            binding.paidStatusIcon.setImageResource(R.drawable.pace_24px)
            binding.progressText.text = ""
        }

        fun bind(item: ExpenseData) {
            resetViewHolder()

                binding.progressBar.postDelayed({
                    val mySplit: SplitEntry? =
                        item.listOfSplits.singleOrNull { it.splitUserId == loggedUserId }
                    val totalIncludeCount = item.listOfSplits.count { it.splitAmount != 0.0 }
                    val amount = Constants.RUPEE_SYMBOL + mySplit?.splitAmount.toString()
                    val totalPaid: Int =
                        item.listOfSplits.count { it.paidStatus == PaidStatus.Paid }

                    if (mySplit != null) {
                        if (amount == "${Constants.RUPEE_SYMBOL}0.0") {
                            binding.amount.visibility = View.GONE
                            binding.progressBar.visibility = View.GONE
                            binding.paidStatusText.text = "No payment required"
                            binding.payButton.visibility = View.GONE
                            binding.paidStatusIcon.setImageResource(R.drawable.info_24px)
                        } else {

                            binding.amount.text = amount
                            val progressLevel: Int = (totalPaid * 100) / totalIncludeCount
                            binding.progressBar.progress = progressLevel

                            binding.progressText.text = "$totalPaid/${totalIncludeCount} paid"
                            val time = DateConvertor.millisToDateTime(item.date)
                            if (mySplit.paidStatus == PaidStatus.Paid) {
                                binding.paidStatusText.text = "Paid • $time"
                                binding.paidStatusIcon.setImageResource(R.drawable.check_circle_24px)
                                binding.payButton.visibility = View.GONE
                            } else {
                                binding.paidStatusText.text = "Unpaid • $time"
                                binding.paidStatusIcon.setImageResource(R.drawable.pace_24px)
                                binding.payButton.visibility = View.VISIBLE
                            }
                        }
                    }

                    if (!item.note.isNullOrEmpty()) {
                        binding.note.text = "Request for '${item.note}'"
                    } else {
                        binding.note.text = "Split request"
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
                },50)
        }
    }

    inner class RightViewHolder(val binding: ExpenseRightCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.cardView.setOnClickListener {
                callback.onclickCard(
                    asyncListDiffer.currentList[absoluteAdapterPosition],
                    binding.cardView
                )
            }
        }

        private fun resetViewHolder() {
            binding.note.text = ""
            binding.amount.text = ""
            binding.paidStatusIcon.setImageResource(R.drawable.pace_24px)
            binding.paidStatusText.text = ""
            binding.progressBar.progress = 0
            binding.progressBar.visibility = View.VISIBLE
            binding.progressText.visibility = View.VISIBLE
        }

        fun bind(item: ExpenseData) {

            binding.progressBar.postDelayed({
                val totalIncludeCount = item.listOfSplits.count { it.splitAmount != 0.0 }
                resetViewHolder()
                if (!item.note.isNullOrEmpty()) {
                    binding.note.text = "Request for '${item.note}'"
                } else {
                    binding.note.text = "Split request"
                }
                val amount = Constants.RUPEE_SYMBOL + item.totalAmount
                binding.amount.text = amount
                val totalPaid: Int =
                    item.listOfSplits.count { it.paidStatus == PaidStatus.Paid && it.splitAmount != 0.0 }

                var leftAmount = 0.0
                item.listOfSplits.forEach {
                    if (it.paidStatus == PaidStatus.UnPaid && it.splitAmount != 0.0) {
                        leftAmount += it.splitAmount
                    }
                }
                binding.progressText.text =
                    "${Constants.RUPEE_SYMBOL}${String.format("%.2f", leftAmount)} left"
                val time = DateConvertor.millisToDateTime(item.date)
                if (totalPaid == item.listOfSplits.size) {
                    binding.paidStatusIcon.setImageResource(R.drawable.check_circle_24px)
                    binding.paidStatusText.text = "All paid • $time"
                    binding.progressBar.visibility = View.GONE
                    binding.progressText.visibility = View.GONE
                } else {
                    binding.paidStatusIcon.setImageResource(R.drawable.pace_24px)
                    val str = "$totalPaid of $totalIncludeCount paid • $time"
                    binding.paidStatusText.text = str
                }
                val progressLevel: Int = (totalPaid * 100) / totalIncludeCount
                binding.progressBar.progress = progressLevel
            },50)
        }
    }

    interface Callback {
        suspend fun getProfile(userId: Long): Bitmap?

        fun onclickCard(item: ExpenseData, view: View)
        fun pay(expense: ExpenseData)
    }
}