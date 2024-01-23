package com.example.facedetectionapp.util

import androidx.recyclerview.widget.DiffUtil

class GenericDiffUtil<T>(
    private val myItemsTheSame: (oldItem: T, newItem: T) -> Boolean,
    private val myContentsTheSame: (oldItem: T, newItem: T) -> Boolean
) : DiffUtil.ItemCallback<T>() {
    override fun areItemsTheSame(oldItem: T & Any, newItem: T & Any): Boolean {
        return myItemsTheSame(oldItem, newItem)
    }

    override fun areContentsTheSame(oldItem: T & Any, newItem: T & Any): Boolean {
        return myContentsTheSame(oldItem, newItem)
    }

}