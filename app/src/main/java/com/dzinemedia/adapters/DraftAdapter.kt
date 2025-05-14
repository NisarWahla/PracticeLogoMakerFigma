package com.dzinemedia.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.dzinemedia.callback.DraftCallback
import com.dzinemedia.logomakerpracticetask.R
import com.dzinemedia.logomakerpracticetask.databinding.DraftItemBinding

class DraftAdapter(
    val dataList: ArrayList<String>,
    val draftCallback: DraftCallback
) :
    RecyclerView.Adapter<DraftAdapter.ViewHolder>() {

    class ViewHolder(val binding: DraftItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int, draftCallback: DraftCallback) {
            binding.root.setOnClickListener {
                draftCallback.draftClick()
            }
        }
    }


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val binding: DraftItemBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.draft_item,
            parent,
            false
        )
        return ViewHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = dataList[position]
        holder.binding.layerImage.setImageURI(model.toUri())
        holder.bind(position, draftCallback)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }
}