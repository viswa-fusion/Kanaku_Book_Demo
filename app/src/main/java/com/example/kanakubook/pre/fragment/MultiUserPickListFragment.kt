package com.example.kanakubook.pre.fragment

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
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
import com.example.kanakubook.pre.viewmodel.FriendsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class MultiUserPickListFragment : Fragment(R.layout.multi_user_pick_list_fragment) {

    private lateinit var searchView: EditText
    private lateinit var horizontalRecyclerView: RecyclerView
    private lateinit var verticalRecyclerView: RecyclerView

    private val viewModel: FriendsViewModel by activityViewModels { FriendsViewModel.FACTORY }


    private var listOfMySelectableUserData = emptyList<MySelectableUserData>()


    private lateinit var binding: MultiUserPickListFragmentBinding
    private lateinit var verticalAdapter: VerticalAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = MultiUserPickListFragmentBinding.bind(view)
        searchView = binding.searchView
        horizontalRecyclerView = binding.horizontalRecyclerView
        verticalRecyclerView = binding.verticalRecyclerView
        setObserver()

        val horizontalAdapter = HorizontalAdapter()
        horizontalRecyclerView.layoutManager =
            WrapperLinearLayoutManager(requireActivity(), LinearLayoutManager.HORIZONTAL, false)
        horizontalRecyclerView.adapter = horizontalAdapter

        val userId = arguments?.getLong("userId")
        viewModel.getMyFriends(userId!!)
        verticalAdapter = VerticalAdapter()
        verticalRecyclerView.layoutManager =
            LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false)
        verticalRecyclerView.adapter = verticalAdapter

        searchView.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val searchText = s.toString().lowercase(Locale.ROOT).trim()
                val filteredList = listOfMySelectableUserData.filter {
                    it.name.lowercase(Locale.ROOT).contains(searchText)
                }
                verticalAdapter.updateData(filteredList)
            }
        })
    }


    private fun setObserver() {

        viewModel.friendsList.observe(viewLifecycleOwner) {
            when (it) {
                is PresentationLayerResponse.Success -> {
                    val list = it.data.map { userData ->
                        MySelectableUserData(
                            userData.userId,
                            userData.name
                        )
                    }
                    listOfMySelectableUserData = list
                    verticalAdapter.updateData(list)
                }

                is PresentationLayerResponse.Error -> {

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
                .inflate(R.layout.search_user_list_layout_1, parent, false)
            return ViewHolder(SearchUserListLayout1Binding.bind(view))
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = asyncListDiffer.currentList[position]
            holder.bind(item)

            holder.itemView.setOnClickListener {
                if (item.isSelected) {
                    item.isSelected = false
                    viewModel.selectedList = viewModel.selectedList.filter {
                        it.userId != item.userId
                    }.toMutableList()
                    holder.binding.main.backgroundTintList =
                        ContextCompat.getColorStateList(requireActivity(), R.color.transparent)
                    holder.binding.checkIcon.visibility = View.INVISIBLE
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

            listOfMySelectableUserData.forEach {
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
                textView.text = item.name
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

        override fun onLayoutChildren(
            recycler: RecyclerView.Recycler?,
            state: RecyclerView.State?
        ) {
//            try {
                super.onLayoutChildren(recycler, state)
//            } catch (e: IndexOutOfBoundsException ) {
//                Log.e("TAG", "Inconsistency detected");
//            }

        }
        override fun supportsPredictiveItemAnimations(): Boolean {
            return false
        }
    }
}
