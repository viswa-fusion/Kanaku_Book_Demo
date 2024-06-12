package com.example.kanakubook.pre.adapter


import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.hardware.camera2.params.ColorSpaceTransform
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.domain.model.UserProfileSummary
import com.example.kanakubook.R
import com.example.kanakubook.databinding.FriendsViewHolderCardBinding
import com.example.kanakubook.databinding.FriendsViewHolderCardTestBinding
import com.google.android.material.imageview.ShapeableImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.DecimalFormat
import kotlin.math.abs
import kotlin.random.Random

class FriendsProfileListAdapter(
    private val context: Context,
    private val callback :Callbacks
) :
    RecyclerView.Adapter<FriendsProfileListAdapter.ProfileViewHolder>() {


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
                callback.onClickItemListener(asyncListDiffer.currentList[bindingAdapterPosition])
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
            textViewName.text = profile.name
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
    }
}
