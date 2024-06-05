package com.example.kanakubook.pre.adapter.diffutil

import androidx.recyclerview.widget.DiffUtil
import com.example.kanakubook.pre.fragment.MultiUserPickListFragment

class MyDiffCallback(private val oldList: List<MultiUserPickListFragment.MySelectableUserData>, private val newList: List<MultiUserPickListFragment.MySelectableUserData>) : DiffUtil.Callback() {

    override fun getOldListSize(): Int {
        return oldList.size
    }

    override fun getNewListSize(): Int {
        return newList.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}