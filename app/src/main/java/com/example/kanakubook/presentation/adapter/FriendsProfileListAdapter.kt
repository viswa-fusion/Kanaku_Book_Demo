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
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.domain.model.UserProfileSummary
import com.example.kanakubook.R
import com.example.kanakubook.databinding.FriendsViewHolderCardTestBinding
import com.google.android.material.imageview.ShapeableImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.DecimalFormat
import java.util.Locale
import kotlin.math.abs

class FriendsProfileListAdapter(
    private val context: Context,
    private val callback :Callbacks
) :
    RecyclerView.Adapter<FriendsProfileListAdapter.ProfileViewHolder>() {
    private var searchText:String = ""


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
            return oldItem == newItem && oldItem.get == newItem.get && oldItem.pay == newItem.pay
        }
    }

    private val asyncListDiffer = AsyncListDiffer(this, diffUtil)


    fun updateData(dataResponse: List<UserProfileSummary>) {
        asyncListDiffer.submitList(dataResponse)
    }

    fun getData():List<UserProfileSummary>{
        return asyncListDiffer.currentList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.friends_view_holder_card_test, parent, false)
        return ProfileViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProfileViewHolder, position: Int) {
        val profile = asyncListDiffer.currentList[position]
        holder.bind(profile)
    }

    override fun getItemCount(): Int {
        return asyncListDiffer.currentList.size
    }

    inner class ProfileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val binding = FriendsViewHolderCardTestBinding.bind(itemView)
        private var bindImageReferenceCheck : Long = -1
        private val textViewName: TextView = binding.textViewName
        private val textViewAmount: TextView = binding.textViewAmount
        private val image: ShapeableImageView = binding.imageViewProfile
        private val phone: TextView = binding.number

        init {
            binding.cardView.setOnClickListener {
                callback.onClickItemListener(asyncListDiffer.currentList[absoluteAdapterPosition])
            }

            binding.imageViewProfile.setOnClickListener {
                callback.clickImage(binding.imageViewProfile.drawable)
            }
        }
        private fun resetViewHolder(){
            bindImageReferenceCheck = -1
            textViewName.text = ""
            textViewAmount.text = ""
            image.setImageResource(R.drawable.default_profile_image)
        }

        fun bind(profile: UserProfileSummary){
            resetViewHolder()
            bindImageReferenceCheck = profile.userId

            val name = profile.name
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

                val colorSpan = ForegroundColorSpan(context.getColor(R.color.md_theme_primaryContainer_mediumContrast))
                spannable.setSpan(
                    colorSpan,
                    startIndex,
                    startIndex + searchText.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }

            textViewName.text = spannable
            textViewAmount.setTextColor(context.getColor(R.color.black))
            val decimalFormat = DecimalFormat("#,###.##")
            val displayAmount = profile.get - profile.pay
            val formattedAmount = "₹${decimalFormat.format(abs(displayAmount))}"

            when {
                displayAmount > 0 -> {
                    textViewAmount.text = formattedAmount
                    binding.statusBar.setCardBackgroundColor(context.getColor(R.color.amount_Green))
                }

                displayAmount < 0 -> {
                    textViewAmount.text = formattedAmount
                    binding.statusBar.setCardBackgroundColor(context.getColor(R.color.amount_Red))
                }

                else -> {
                    textViewAmount.text = "no pending"
                    textViewAmount.setTextColor(Color.GRAY)
                    binding.statusBar.setCardBackgroundColor(Color.GRAY)
                }
            }

            phone.text = profile.phone.toString()
            if(profile.profile != null){
                image.setImageBitmap(profile.profile)
            } else {
                CoroutineScope(Dispatchers.IO).launch {
                    val bitmap = callback.getImage(profile.userId)
                    profile.profile = bitmap
                    withContext(Dispatchers.Main){
                        if(bindImageReferenceCheck  == profile.userId && profile.profile != null) {
                            image.setImageBitmap(profile.profile)
                        }
                    }
                }
            }
        }

    }


    interface Callbacks{
        suspend fun getImage(userId: Long): Bitmap?
        fun onClickItemListener(userProfileSummary: UserProfileSummary)
        fun clickImage(drawable: Drawable?)
    }

    fun highlightText(searchText: String) {
        this.searchText = searchText
        notifyDataSetChanged()
    }
}
