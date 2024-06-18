package com.example.kanakubook.presentation.fragment

import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.domain.usecase.response.PresentationLayerResponse
import com.example.kanakubook.R
import com.example.kanakubook.databinding.MultiUserPickListFragmentBinding
import com.example.kanakubook.databinding.SearchUserListLayout1Binding
import com.example.kanakubook.databinding.SearchUserListLayout2Binding
import com.example.kanakubook.presentation.viewmodel.FriendsViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class MultiUserPickListFragment :
    BottomSheetDialogFragment(R.layout.multi_user_pick_list_fragment) {

    private lateinit var searchView: EditText
    private lateinit var horizontalRecyclerView: RecyclerView
    private lateinit var verticalRecyclerView: RecyclerView
    private val viewModel: FriendsViewModel by activityViewModels { FriendsViewModel.FACTORY }
    var isForBottomSheet = false
    private lateinit var membersId: List<Long>


    private lateinit var binding: MultiUserPickListFragmentBinding
    private lateinit var verticalAdapter: VerticalAdapter
    private lateinit var horizontalAdapter: HorizontalAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.i("lifecycleTest", "onViewCreated")

        binding = MultiUserPickListFragmentBinding.bind(view)
        searchView = binding.searchView
        horizontalRecyclerView = binding.horizontalRecyclerView
        verticalRecyclerView = binding.verticalRecyclerView

        setObserver()
        setListener()

        horizontalAdapter = HorizontalAdapter()
        horizontalRecyclerView.layoutManager =
            WrapperLinearLayoutManager(requireActivity(), LinearLayoutManager.HORIZONTAL, false)
        horizontalRecyclerView.adapter = horizontalAdapter
        verticalAdapter = VerticalAdapter()
        verticalRecyclerView.layoutManager =
            LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false)
        verticalRecyclerView.adapter = verticalAdapter


        binding.shapeableImageView3.setOnClickListener {
            if (viewModel.selectedList.isNotEmpty()) {
                parentFragmentManager.setFragmentResult("addFriend", Bundle())
                dismiss()
            } else {
                Toast.makeText(requireActivity(), "Select min 1 person", Toast.LENGTH_SHORT).show()
            }
        }


        searchView.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val searchText = s.toString().lowercase(Locale.ROOT).trim()
                val filteredList = viewModel.listOfMySelectableUserData.filter {
                    it.name.lowercase(Locale.ROOT).contains(searchText)
                }
                if (filteredList.isEmpty()) {
                    binding.searchNotFound.emptyTemplate.visibility = View.VISIBLE
                } else {
                    binding.searchNotFound.emptyTemplate.visibility = View.INVISIBLE
                }
                verticalAdapter.highlightText(searchText)
                verticalAdapter.updateData(filteredList)
            }
        })

        val userId = arguments?.getLong("userId")
        arguments?.getLongArray("membersId")?.toList()?.let { data ->
            membersId = data
        }

        if (viewModel.listOfMySelectableUserData.isEmpty()) {
            viewModel.getMyFriends(userId!!)
        } else {
            horizontalAdapter.updateData(viewModel.selectedList)
            verticalAdapter.updateData(viewModel.listOfMySelectableUserData)
        }
    }


    private fun setListener() {
        binding.verticalRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    hideKeyboard()
                }
            }
        })


    }

    fun Fragment.hideKeyboard() {
        val imm =
            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(requireView().windowToken, 0)
    }

    override fun onResume() {
        super.onResume()
        if (isForBottomSheet) setForBottomSheet()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext(), theme)
        dialog.behavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
        return dialog
    }


    private fun setObserver() {
        viewModel.friendsList.observe(viewLifecycleOwner) {
            if (viewModel.listOfMySelectableUserData.isEmpty()) {
                when (it) {
                    is PresentationLayerResponse.Success -> {
                        var list = it.data.map { userData ->
                            MySelectableUserData(
                                userData.userId,
                                userData.name
                            )
                        }
                        if (::membersId.isInitialized) {
                            val filterList = list.filter { myData ->
                                !membersId.contains(myData.userId)
                            }
                            list = filterList
                        }
                        viewModel.listOfMySelectableUserData = list
                        if (list.isEmpty()) {
                            binding.searchNotFound.emptyTemplate.visibility = View.VISIBLE
                        } else {
                            binding.searchNotFound.emptyTemplate.visibility = View.GONE
                            verticalAdapter.updateData(list)
                        }
                    }

                    is PresentationLayerResponse.Error -> {

                    }
                }
            }
        }

    }


    private inner class HorizontalAdapter :
        RecyclerView.Adapter<HorizontalAdapter.ViewHolder>() {

        private val diffUtil = object :
            DiffUtil.ItemCallback<MySelectableUserData>() {

            override fun areItemsTheSame(
                oldItem: MySelectableUserData,
                newItem: MySelectableUserData
            ): Boolean {
                return oldItem.userId == newItem.userId
            }

            override fun areContentsTheSame(
                oldItem: MySelectableUserData,
                newItem: MySelectableUserData
            ): Boolean {
                return oldItem == newItem
            }
        }

        private val asyncListDiffer = AsyncListDiffer(this, diffUtil)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.search_user_list_layout_2, parent, false)

            return ViewHolder(view)
        }

        fun updateData(dataResponse: List<MySelectableUserData>) {
             binding.shapeableImageView3.isEnabled = dataResponse.isNotEmpty()
            if (dataResponse.isEmpty()) asyncListDiffer.submitList(emptyList())
            else {
                asyncListDiffer.submitList(dataResponse)
            }
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = asyncListDiffer.currentList[position]
            holder.bind(item)
        }

        override fun getItemCount(): Int = asyncListDiffer.currentList.size

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val binding = SearchUserListLayout2Binding.bind(itemView)
            private val imageView = binding.imageProfile
            private val textView = binding.textView


            private fun resetViewHolder() {
                textView.text = ""
                imageView.setImageResource(R.drawable.default_profile_image)
            }

            fun bind(item: MySelectableUserData) {
                resetViewHolder()
                itemView.setOnClickListener {
                    viewModel.selectedList = viewModel.selectedList.filter {
                        it.userId != item.userId
                    }.toMutableList()
                    updateData(viewModel.selectedList)
                    (verticalRecyclerView.adapter as VerticalAdapter).deSelectItem(item)
                }
                item.profileImage?.let {
                    imageView.setImageBitmap(it)
                }
                textView.text = item.name
            }
        }
    }


    inner class VerticalAdapter :
        RecyclerView.Adapter<VerticalAdapter.ViewHolder>() {

        private var searchText: String = ""
        private val diffUtil = object :
            DiffUtil.ItemCallback<MySelectableUserData>() {

            override fun areItemsTheSame(
                oldItem: MySelectableUserData,
                newItem: MySelectableUserData
            ): Boolean {
                return oldItem.userId == newItem.userId
            }

            override fun areContentsTheSame(
                oldItem: MySelectableUserData,
                newItem: MySelectableUserData
            ): Boolean {
                return oldItem == newItem
            }
        }


        fun highlightText(searchText: String) {
            this.searchText = searchText
            notifyDataSetChanged()
        }

        private val asyncListDiffer = AsyncListDiffer(this, diffUtil)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.search_user_list_layout_1, parent, false)
            return ViewHolder(SearchUserListLayout1Binding.bind(view))
        }


        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = asyncListDiffer.currentList[position]
            holder.bind(item)

            holder.itemView.setOnClickListener {
                val limit = viewModel.selectedList.size

                if (item.isSelected) {
                    item.isSelected = false
                    viewModel.selectedList = viewModel.selectedList.filter {
                        it.userId != item.userId
                    }.toMutableList()
                    holder.binding.main.backgroundTintList =
                        ContextCompat.getColorStateList(requireActivity(), R.color.transparent)
                    holder.binding.checkIcon.visibility = View.INVISIBLE
                } else {
                    if (limit > 20) {
                        Toast.makeText(
                            requireActivity(),
                            "max 20 member per group exceed",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        item.isSelected = true
                        item.verticalListPosition = position
                        viewModel.selectedList.add(item)
                        holder.binding.main.backgroundTintList = ContextCompat.getColorStateList(
                            requireActivity(),
                            R.color.pick_list_selection
                        )
                        holder.binding.checkIcon.visibility = View.VISIBLE
                    }
                }
                (horizontalRecyclerView.adapter as? HorizontalAdapter)?.apply {
                    this.updateData(viewModel.selectedList)
                    val newPosition = viewModel.selectedList.indexOf(item)
                    if (newPosition != -1) {
                        notifyItemInserted(newPosition)
                        (horizontalRecyclerView.layoutManager as LinearLayoutManager).scrollToPosition(
                            newPosition
                        )
                    }
                }

            }
        }

        fun deSelectItem(item: MySelectableUserData) {

            notifyItemChanged(asyncListDiffer.currentList.indexOf(item))

            viewModel.listOfMySelectableUserData.forEach {
                if (it.userId == item.userId) {
                    it.isSelected = false
                    binding.searchView.text = binding.searchView.text
                }
            }
        }


        override fun getItemCount(): Int = asyncListDiffer.currentList.size

        fun updateData(dataResponse: List<MySelectableUserData>) {
            asyncListDiffer.submitList(dataResponse)
        }

        inner class ViewHolder(val binding: SearchUserListLayout1Binding) :
            RecyclerView.ViewHolder(binding.root) {

            private val textView = binding.textview
            private val imageView = binding.imageProfile
            private var bindImageReferenceCheck: Long = -1
            private fun resetViewHolder() {

                bindImageReferenceCheck = -1
                textView.text = ""
                imageView.setImageResource(R.drawable.default_profile_image)
            }

            fun bind(item: MySelectableUserData) {
                resetViewHolder()
                bindImageReferenceCheck = item.userId

                val name = item.name.lowercase(Locale.ROOT)
                val spannable = SpannableString(name)
                val startIndex = name.indexOf(searchText, ignoreCase = true)

                if (startIndex != -1) {
                    spannable.setSpan(
                        StyleSpan(Typeface.BOLD),
                        startIndex,
                        startIndex + searchText.length,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )

                    val colorSpan =
                        ForegroundColorSpan(requireActivity().getColor(R.color.md_theme_primaryContainer_mediumContrast))
                    spannable.setSpan(
                        colorSpan,
                        startIndex,
                        startIndex + searchText.length,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
                textView.text = spannable
                if (item.isSelected) {
                    binding.main.backgroundTintList = ContextCompat.getColorStateList(
                        requireActivity(),
                        R.color.pick_list_selection
                    )
                    binding.checkIcon.visibility = View.VISIBLE
                } else {
                    binding.main.backgroundTintList =
                        ContextCompat.getColorStateList(requireActivity(), R.color.transparent)
                    binding.checkIcon.visibility = View.INVISIBLE
                }

                if (item.profileImage != null) {
                    imageView.setImageBitmap(item.profileImage)
                } else {
                    CoroutineScope(Dispatchers.IO).launch {
                        val bitmap = viewModel.getProfile(item.userId)
                        item.profileImage = bitmap
                        withContext(Dispatchers.Main) {
                            if (bindImageReferenceCheck == item.userId && item.profileImage != null) {
                                imageView.setImageBitmap(item.profileImage)
                            }
                        }
                    }
                }
            }
        }
    }


    data class MySelectableUserData(
        val userId: Long,
        val name: String,
        var verticalListPosition: Int = -1,
        var isSelected: Boolean = false
    ) {
        var profileImage: Bitmap? = null
    }

    class WrapperLinearLayoutManager(
        context: Context,
        @RecyclerView.Orientation orientation: Int,
        reverseLayout: Boolean
    ) : LinearLayoutManager(context, orientation, reverseLayout) {
        override fun supportsPredictiveItemAnimations(): Boolean {
            return false
        }
    }

    private fun setForBottomSheet() {
        binding.topCard.visibility = View.VISIBLE
        binding.shapeableImageView3.visibility = View.VISIBLE
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("isForBottomSheet", isForBottomSheet)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        savedInstanceState?.getBoolean("isForBottomSheet")?.let { isForBottomSheet = it }
    }
}

