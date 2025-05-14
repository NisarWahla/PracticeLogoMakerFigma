package com.dzinemedia.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.dzinemedia.logomakerpracticetask.R
import com.dzinemedia.logomakerpracticetask.databinding.LayerItemBinding
import com.dzinemedia.utility.Utils

class LayerAdapter(
    val dataList: ArrayList<View>
) :
    RecyclerView.Adapter<LayerAdapter.ViewHolder>() {

    class ViewHolder(val binding: LayerItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int, ownerPropertyModel: View) {

        }
    }


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val binding: LayerItemBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.layer_item,
            parent,
            false
        )
        return ViewHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = dataList[position]
        val bitmap = Utils.captureViewToBitmap(model, 0, 0)
        holder.binding.layerImage.setImageBitmap(bitmap)
        holder.bind(position, model)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }
}