package com.example.facedetectionapp.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.facedetectionapp.data.local.entities.EmotionResult
import com.example.facedetectionapp.databinding.ResultItemsBinding
import com.example.facedetectionapp.util.GenericDiffUtil


class ResultAdapter: ListAdapter<EmotionResult, ResultAdapter.ViewHolder>(GenericDiffUtil<EmotionResult>(
    myItemsTheSame = { oldItem, newItem -> oldItem.id == newItem.id },
    myContentsTheSame = { oldItem, newItem -> oldItem == newItem }
)) {


    inner class ViewHolder(private val binding: ResultItemsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: EmotionResult) {
            with(binding) {
                testNumber.text=item.id.toString()
                left.text=item.left.toString()
                right.text=item.right.toString()
                smile.text=item.smile.toString()
                neutral.text=item.neutral.toString()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = ResultItemsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))

    }

}
//class ResultAdapter() : RecyclerView.Adapter<ResultAdapter.ResultViewHolder>() {
//    inner class ResultViewHolder(private val binding: ResultItemsBinding) :
//        RecyclerView.ViewHolder(binding.root) {
//        fun bind(item: EmotionResult) {
//            with(binding) {
//                testNumber.text=item.id.toString()
//                left.text=item.left.toString()
//                right.text=item.right.toString()
//                smile.text=item.smile.toString()
//                neutral.text=item.neutral.toString()
//
//
//            }
//
//    }}
//
//
//    private val differCallBack = object : DiffUtil.ItemCallback<EmotionResult>() {
//        override fun areItemsTheSame(oldItem: EmotionResult, newItem: EmotionResult): Boolean {
//            return oldItem.id == newItem.id
//        }
//
//        override fun areContentsTheSame(oldItem: EmotionResult, newItem: EmotionResult): Boolean {
//            return oldItem == newItem
//        }
//    }
//
//    val differ = AsyncListDiffer(this, differCallBack)
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultViewHolder {
//        val inflater = LayoutInflater.from(parent.context)
//        val binding = ResultItemsBinding.inflate(inflater,parent,false)
//        return ResultViewHolder(binding)
//    }
//
//    override fun getItemCount(): Int {
//        return differ.currentList.size
//    }
//
//    override fun onBindViewHolder(holder: ResultViewHolder, position: Int) {
//        val results = differ.currentList[position]
//        holder.bind(getItemId(results))
//
//    }
//
//
//}
