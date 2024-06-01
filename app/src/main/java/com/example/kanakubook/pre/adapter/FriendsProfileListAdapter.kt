package com.example.kanakubook.pre.adapter


import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.domain.model.UserProfileSummary
import com.example.kanakubook.R
import com.example.kanakubook.databinding.FriendsViewHolderCardBinding
import com.google.android.material.imageview.ShapeableImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.DecimalFormat

class FriendsProfileListAdapter(
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
            return oldItem == newItem
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
        private val binding = FriendsViewHolderCardBinding.bind(itemView)
        private var bindImageReferenceCheck : Long = -1
        private val textViewName: TextView = binding.textViewName
        private val textViewAmount: TextView = binding.textViewAmount
        private val image: ShapeableImageView = binding.imageViewProfile

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
            val decimalFormat = DecimalFormat("#,###")
            val formattedAmount = "₹${decimalFormat.format(profile.phone)}"
            textViewAmount.text = formattedAmount
            if(profile.bitmap != null){
                image.setImageBitmap(profile.bitmap)
            }else {
                CoroutineScope(Dispatchers.IO).launch {
                    val bitmap = callback.getImage(profile.userId)
                    profile.bitmap = bitmap
                    withContext(Dispatchers.Main){
                        if(bindImageReferenceCheck  == profile.userId && profile.bitmap != null) {
                            image.setImageBitmap(profile.bitmap)
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


//package com.example.kanakubook.pre.adapter
//
//import android.graphics.Bitmap
//import android.os.Handler
//import android.os.Looper
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.TextView
//import androidx.recyclerview.widget.DiffUtil
//import androidx.recyclerview.widget.RecyclerView
//import com.example.domain.model.UserProfileSummery
//import com.example.kanakubook.R
//import com.google.android.material.imageview.ShapeableImageView
//import kotlinx.coroutines.*
//import java.text.DecimalFormat
//
//class FriendsProfileListAdapter(
//    private var profileList: List<UserProfileSummery>,
//    private val callback: suspend (Long) -> Bitmap?
//) : RecyclerView.Adapter<FriendsProfileListAdapter.ProfileViewHolder>() {
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileViewHolder {
//        val view = LayoutInflater.from(parent.context)
//            .inflate(R.layout.friends_view_holder_card_test, parent, false)
//        return ProfileViewHolder(view)
//    }
//
//    override fun onBindViewHolder(holder: ProfileViewHolder, position: Int) {
//        holder.bind(profileList[position])
//    }
//
//    override fun getItemCount(): Int = profileList.size
//
//    fun updateList(newList: List<UserProfileSummery>) {
//        val diffCallback = ProfileDiffCallback(profileList, newList)
//        val diffResult = DiffUtil.calculateDiff(diffCallback)
//        profileList = newList
//        diffResult.dispatchUpdatesTo(this)
//    }
//
//    inner class ProfileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        private val textViewName: TextView = itemView.findViewById(R.id.textViewName)
//        private val textViewAmount: TextView = itemView.findViewById(R.id.textViewAmount)
//        private val image: ShapeableImageView = itemView.findViewById(R.id.imageViewProfile)
//        private var job: Job? = null
//
//        fun bind(profile: UserProfileSummery) {
//            textViewName.text = profile.name
//            val decimalFormat = DecimalFormat("#,###")
//            val formattedAmount = "₹${decimalFormat.format(profile.phone)}"
//            textViewAmount.text = formattedAmount
//
//            job?.cancel()
//            job = CoroutineScope(Dispatchers.Main).launch {
//                val bitmap = withContext(Dispatchers.IO) {
//                    callback(profile.userId)
//                }
//                image.setImageBitmap(bitmap)
//            }
//        }
//    }
//
//    private class ProfileDiffCallback(
//        private val oldList: List<UserProfileSummery>,
//        private val newList: List<UserProfileSummery>
//    ) : DiffUtil.Callback() {
//        override fun getOldListSize(): Int = oldList.size
//        override fun getNewListSize(): Int = newList.size
//
//        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
//            return oldList[oldItemPosition].userId == newList[newItemPosition].userId
//        }
//
//        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
//            return oldList[oldItemPosition] == newList[newItemPosition]
//        }
//    }
//}
