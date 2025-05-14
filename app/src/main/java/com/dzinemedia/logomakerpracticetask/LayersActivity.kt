package com.dzinemedia.logomakerpracticetask

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import com.dzinemedia.adapters.DraftAdapter
import com.dzinemedia.callback.DraftCallback
import com.dzinemedia.logomakerpracticetask.databinding.ActivityLayersBinding

class LayersActivity : AppCompatActivity() {
    private var layerAdapter: DraftAdapter? = null
    private var draftList: ArrayList<String> = ArrayList()
    private lateinit var binding: ActivityLayersBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_layers)
        setAdapter()

    }

    private fun setAdapter() {
        layerAdapter =
            DraftAdapter(draftList, object : DraftCallback {
                override fun draftClick() {
                    val intent = Intent(this@LayersActivity, MainActivity::class.java)
                    intent.putExtra("isFrom", "draft")
                    startActivity(intent)
                    finish()
                }

            })
        val layoutManager =
            GridLayoutManager(binding.root.context, 3)
        binding.layerRecyclerView.layoutManager = layoutManager
        binding.layerRecyclerView.adapter = layerAdapter
        getFiles()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun getFiles() {
        val directory = getExternalFilesDir("Draft")
        if (directory?.exists() == true) {
            val files = directory.listFiles()
            files?.let {
                for (file in files) {
                    draftList.add(file.absolutePath)
                }
                layerAdapter?.notifyDataSetChanged()
            }
        }
    }
}