package com.example.facedetectionapp.presentation.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.facedetectionapp.R
import com.example.facedetectionapp.data.local.entities.EmotionResult
import com.example.facedetectionapp.databinding.ResultItemsBinding
import com.example.facedetectionapp.util.GenericDiffUtil


class ResultAdapter :
    ListAdapter<EmotionResult, ResultAdapter.ViewHolder>(GenericDiffUtil<EmotionResult>(
        myItemsTheSame = { oldItem, newItem -> oldItem.id == newItem.id },
        myContentsTheSame = { oldItem, newItem -> oldItem == newItem }
    )) {


    inner class ViewHolder(private val binding: ResultItemsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: EmotionResult) {
            with(binding) {
                testNumber.text = "TEST NUMBER: ${item.id}"
                left.text = "LEFT: ${item.left}"
                right.text = "RIGHT: ${item.right}"
                smile.text = "SMILE: ${item.smile}"
                neutral.text = "NEUTRAL: ${item.neutral}"
            }

            setDrawableForBooleanValue(
                binding.left,
                item.left,
                R.drawable.smile_svgrepo_com__1___,
                R.drawable.sad_svgrepo_com
            )
            setDrawableForBooleanValue(
                binding.right,
                item.right,
                R.drawable.smile_svgrepo_com__1___,
                R.drawable.sad_svgrepo_com
            )
            setDrawableForBooleanValue(
                binding.smile,
                item.smile,
                R.drawable.smile_svgrepo_com__1___,
                R.drawable.sad_svgrepo_com
            )
            setDrawableForBooleanValue(
                binding.neutral,
                item.neutral,
                R.drawable.smile_svgrepo_com__1___,
                R.drawable.sad_svgrepo_com
            )


        }
    }

    private fun setDrawableForBooleanValue(
        textView: TextView,
        value: Boolean,
        trueDrawableResId: Int,
        falseDrawableResId: Int
    ) {
        val drawableResId = if (value) trueDrawableResId else falseDrawableResId
        textView.setCompoundDrawablesWithIntrinsicBounds(0, 0, drawableResId, 0)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = ResultItemsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))

    }

}
