package com.dzinemedia.logomakerpracticetask

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dzinemedia.adapters.LayerAdapter
import com.dzinemedia.callback.saveCompleted
import com.dzinemedia.logomakerpracticetask.databinding.ActivityMainBinding
import com.dzinemedia.models.ElementModel
import com.dzinemedia.models.ParentModel
import com.dzinemedia.utility.StackOperation
import com.dzinemedia.utility.Utils
import com.dzinemedia.utility.Utils.readJsonFromAssets
import com.google.gson.Gson
import kotlinx.coroutines.*
import org.json.JSONObject
import java.io.File
import java.util.*


class MainActivity : AppCompatActivity() {
    var allViews: MutableList<View> = mutableListOf()
    var viewPropertiesList: ArrayList<ElementModel> = ArrayList()
    var parentModel: ParentModel? = null
    private var layerAdapter: LayerAdapter? = null

    private var jsonString: String? = null
    private var model: ElementModel? = null
    private val TAG = "MainActivity"
    private lateinit var binding: ActivityMainBinding
    private var list: ArrayList<ElementModel> = ArrayList()
    var targetWidth: Int = 0
    var targetHeight: Int = 0

    companion object {
        val undoStack: Stack<ParentModel> = Stack()
        val redoStack: Stack<ParentModel> = Stack()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        if (intent.hasExtra("isFrom")) {
            if (intent.getStringExtra("isFrom").equals("draft")) {
                val file = File(filesDir, "/Draft/json/draft.txt")
                if (!file.exists()) {
                    return
                }
                val text = file.readText()
                val gson = Gson()
                val json = gson.fromJson(text, ParentModel::class.java)
                makeCanvasFromDraft(json)
            }

        } else {
            val jsonFileName = "scenes/json/Frame 322689.json"
            jsonString = readJsonFromAssets(this, jsonFileName)
            val displayMetrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(displayMetrics)
            makeCanvasFromJson(jsonString, displayMetrics.widthPixels)
        }
        binding.relative.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_UP -> {
                    //StackOperation.saveOperation(binding.relative)
                    // Save current state to undo stack and clear redo stack
                    val newParentModel = StackOperation.createParentModel(binding.relative)
                    undoStack.push(newParentModel)
                    redoStack.clear()
                }
            }
            true
        }
        setViewClickListener()
    }

    private fun setViewClickListener() {
        binding.btnSave.setOnClickListener {
            val bitmap = Utils.captureViewToBitmap(binding.relative, targetWidth, targetHeight)
            Utils.saveBitmapToExternalFilesDir(
                this,
                bitmap,
                "Logo" + System.currentTimeMillis(),
                ""
            )
        }
        binding.radioGroup.setOnCheckedChangeListener { group, checkedId ->
            val selectedRadioButton = findViewById<RadioButton>(checkedId)
            when (selectedRadioButton.text) {
                "512 * 512" -> {
                    targetWidth = 512
                    targetHeight = 512
                }
                "1024*1024" -> {
                    targetWidth = 1024
                    targetHeight = 1024
                }
                else -> {
                    targetWidth = 2048

                    targetHeight = 2048
                }
            }
        }
        binding.btnMenuBar.setOnClickListener {
            getAllChildViews(binding.relative)
            showPopupMenu(binding.btnMenuBar)
        }
        binding.btnDraft.setOnClickListener {
            val bitmap = Utils.captureViewToBitmap(binding.relative, 0, 0)
            Utils.saveBitmapToExternalFilesDir(
                this,
                bitmap,
                "DraftLogo",
                "Draft"
            )
            getViewsProperties(binding.relative, object : saveCompleted {
                override fun onSaveCompleted() {
                    val intent = Intent(this@MainActivity, LayersActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            })
        }
        binding.btnUndo.setOnClickListener {
            for (i in 0 until undoStack.size) {
                Log.i(TAG, "setViewClickListener: ${undoStack[i]}")
            }
            undo()
        }
        binding.btnRedo.setOnClickListener {
            redo()
        }
    }

    private fun undo() {
        if (undoStack.isNotEmpty()) {
            val currentModel: ParentModel = undoStack.pop()
            redoStack.push(currentModel)

            if (undoStack.isNotEmpty()) {
                val parentModel = undoStack.peek()
                makeCanvasFromDraft(parentModel)
            }
        }
    }

    private fun redo() {
        if (redoStack.isNotEmpty()) {
            val parentModel: ParentModel = redoStack.pop()
            undoStack.push(parentModel)
            makeCanvasFromDraft(parentModel)
        }
    }

    private fun getViewsProperties(viewGroup: ViewGroup, param: saveCompleted) {
        val height = viewGroup.height
        val width = viewGroup.width
        var colorDrawable = viewGroup.background as ColorDrawable
        val color = colorDrawable.color

        for (i in 0 until viewGroup.childCount) {
            val child = viewGroup.getChildAt(i)
            val elementModel = if (child is TextView) {
                ElementModel(
                    child.text.toString(),
                    "TEXT",
                    child.x.toInt(),
                    child.y.toInt(),
                    child.width,
                    child.height,
                    "",
                    child.textSize.toInt()
                )
            } else {
                ElementModel(
                    "",
                    "RECTANGLE",
                    child.x.toInt(),
                    child.y.toInt(),
                    child.width,
                    child.height,
                    child.tag.toString(),
                    0
                )

            }
            viewPropertiesList.add(elementModel)
        }
        parentModel = ParentModel(width, height, color, viewPropertiesList)
        parentModel?.let {
            Utils.saveParentModelToJsonFile(this@MainActivity, parentModel!!, "draft.json", param)
        }
    }


    private fun pxToDp(context: Context, px: Float): Float {
        val metrics = context.resources.displayMetrics
        return px / (metrics.densityDpi / 160f)
    }

    private fun makeCanvasFromDraft(model: ParentModel) {
        binding.relative.removeAllViews()
        val layoutParams = RelativeLayout.LayoutParams(
            model.width,
            model.height
        )
        binding.relative.layoutParams = layoutParams
        binding.relative.setBackgroundColor(model.color)
        list.clear()
        list = model.arrayList
        if (list.size > 0) {
            for (i in 0 until list.size) {
                if (list[i].type == "TEXT") {
                    val textView = TextView(this@MainActivity).apply {
                        text = list[i].name
                        val textLayoutParams = RelativeLayout.LayoutParams(
                            RelativeLayout.LayoutParams.WRAP_CONTENT,
                            RelativeLayout.LayoutParams.WRAP_CONTENT
                        )
                        this.layoutParams = textLayoutParams
                        setTextColor(Color.DKGRAY)
                        x = list[i].xPosition.toFloat()
                        y = list[i].yPosition.toFloat()
                        textSize = pxToDp(binding.root.context, list[i].fontSize.toFloat())
                    }
                    Utils.makeViewMovable(textView)
                    binding.relative.addView(textView)

                } else {
                    val imageView = ImageView(this@MainActivity).apply {
                        x = list[i].xPosition.toFloat()
                        y = list[i].yPosition.toFloat()
                        val imageLayoutParams = RelativeLayout.LayoutParams(
                            list[i].width,
                            list[i].height
                        )
                        this.layoutParams = imageLayoutParams
                        tag = list[i].src
                        val newSrc = "file:///android_asset/scenes/" + list[i].src
                        Glide.with(this@MainActivity).load(newSrc).into(this)

                    }
                    Utils.makeViewMovable(imageView)
                    binding.relative.addView(imageView)
                }
            }
        }
        binding.relative.requestLayout()
    }


    private fun makeCanvasFromJson(jsonString: String?, screenWidth: Int) {
        CoroutineScope(Dispatchers.Main).async {
            jsonString?.let {
                Log.d("MainActivity", it)
                binding.relative.removeAllViews()
                val json = JSONObject(it)
                val aspectRatio = json.getInt("width") / json.getInt("height")
                val screenHeight = screenWidth / aspectRatio
                Log.i(TAG, "onCreate: $screenWidth and $screenHeight")

                /*val width = pxToDp(binding.root.context, json.getInt("width").toFloat())
            val height = pxToDp(binding.root.context, json.getInt("height").toFloat())*/
                val layoutParams = RelativeLayout.LayoutParams(
                    screenWidth,
                    screenHeight
                )
                // Apply the updated LayoutParams back to the RelativeLayout
                binding.relative.layoutParams = layoutParams
                binding.relative.requestLayout()
                binding.relative.invalidate()
                val color = Utils.getColorFromJson(json.getJSONArray("fills"))
                Utils.showToast(this@MainActivity, color.toString())
                binding.relative.setBackgroundColor(color)

                val childrenArray = json.getJSONArray("children")
                for (i in 0 until childrenArray.length()) {
                    val child = childrenArray.getJSONObject(i)

                    if (child.getString("type") == "TEXT") {
                        model = ElementModel(
                            child.getString("name"),
                            child.getString("type"),
                            child.getInt("x"),
                            child.getInt("y"),
                            child.getInt("width"),
                            child.getInt("height"),
                            "",
                            child.getInt("fontSize")
                        )
                    } else {
                        val childColor = child.getJSONArray("fills")
                        var src = ""
                        for (j in 0 until childColor.length()) {
                            val srcChild = childColor.getJSONObject(j)
                            src = srcChild.getString("src")
                        }
                        Log.i(TAG, "onCreate: $src")
                        model = ElementModel(
                            child.getString("name"),
                            child.getString("type"),
                            child.getInt("x"),
                            child.getInt("y"),
                            child.getInt("width"),
                            child.getInt("height"),
                            src,
                            0
                        )
                    }
                    model?.let { elementModel ->
                        list.add(elementModel)
                    }
                }
                if (list.size > 0) {
                    for (i in 0 until list.size) {
                        if (list[i].type == "TEXT") {
                            val textView = TextView(this@MainActivity)
                            textView.text = list[i].name
                            val textLayoutParams = RelativeLayout.LayoutParams(
                                RelativeLayout.LayoutParams.WRAP_CONTENT,
                                RelativeLayout.LayoutParams.WRAP_CONTENT
                            )
                            textView.layoutParams = textLayoutParams
                            val originalCanvasSize =
                                Pair(json.getInt("width"), json.getInt("height"))
                            val originalViewPosition = Pair(list[i].xPosition, list[i].yPosition)
                            Log.i(TAG, "onCreate: ")
                            val newCanvasSize = Pair(screenWidth, screenHeight)
                            val newTextSize = Utils.calculateNewViewProperties(
                                originalCanvasSize,
                                originalViewPosition,
                                list[i].fontSize.toFloat(),
                                newCanvasSize
                            )
                            textView.setTextColor(Color.DKGRAY)
                            textView.x = newTextSize.x
                            textView.y = newTextSize.y
                            textView.textSize = pxToDp(binding.root.context, newTextSize.textSize)
                            //Log.i(TAG, "onCreate: ${newTextSize.textSize} ${newTextSize.x} ${newTextSize.y}")
                            //pxToDp(binding.root.context, newTextSize)
                            Utils.makeViewMovable(textView)
                            binding.relative.addView(textView)

                        } else {
                            val imageView = ImageView(this@MainActivity)
                            val originalCanvasSize =
                                Pair(json.getInt("width"), json.getInt("height"))
                            val originalViewPosition = Pair(list[i].xPosition, list[i].yPosition)
                            val originalViewSize = Pair(list[i].width, list[i].height)
                            val newCanvasSize = Pair(screenWidth, screenHeight)
                            val imagePropertiesModel = Utils.calculateNewViewPosition(
                                originalCanvasSize,
                                originalViewPosition,
                                originalViewSize,
                                newCanvasSize
                            )
                            imageView.x = imagePropertiesModel.x
                            imageView.y = imagePropertiesModel.y
                            val imageLayoutParams = RelativeLayout.LayoutParams(
                                imagePropertiesModel.newWidth.toInt(),
                                imagePropertiesModel.newHeight.toInt()
                            )
                            imageView.layoutParams = imageLayoutParams
                            imageView.tag = list[i].src
                            val newSrc = "file:///android_asset/scenes/" + list[i].src
                            Glide.with(this@MainActivity).load(newSrc).into(imageView)
                            Utils.makeViewMovable(imageView)
                            binding.relative.addView(imageView)
                        }
                    }
                }
            } ?: run {
                Log.e("MainActivity", "Failed to read JSON file")
            }
        }
    }

    private fun getAllChildViews(viewGroup: ViewGroup): ArrayList<View> {
        allViews = mutableListOf()
        for (i in 0 until viewGroup.childCount) {
            val child = viewGroup.getChildAt(i)
            allViews.add(child)
            if (child is ViewGroup) {
                allViews.addAll(getAllChildViews(child))
            }
        }
        return allViews as ArrayList<View>
    }

    private fun showPopupMenu(anchorView: View) {
        // Inflate the popup menu layout
        val inflater = LayoutInflater.from(this)
        val popupView = inflater.inflate(R.layout.activity_layers, null)

        // Create the PopupWindow
        val popupWindow = PopupWindow(
            popupView,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT, true
        )

        // Set up RecyclerView inside the popup menu
        layerAdapter = LayerAdapter(allViews as ArrayList<View>)
        val recyclerView = popupView.findViewById<RecyclerView>(R.id.layerRecyclerView)
        val layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = layerAdapter
        val helper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0
        ) {
            @SuppressLint("NotifyDataSetChanged")
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                Log.i(TAG, "onMove: ${viewHolder.adapterPosition} || ${target.adapterPosition}")
                if (viewHolder.adapterPosition < target.adapterPosition) {
                    for (i in viewHolder.adapterPosition until target.adapterPosition) {
                        Collections.swap(allViews, i, i + 1)
                    }
                } else {
                    for (i in viewHolder.adapterPosition downTo target.adapterPosition + 1) {
                        Collections.swap(allViews, i, i - 1)
                    }
                }
                layerAdapter?.notifyItemMoved(viewHolder.adapterPosition, target.adapterPosition)
                binding.relative.removeAllViews()
                for (k in 0 until allViews.size) {
                    binding.relative.addView(allViews[k])
                }
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

            }

        })
        helper.attachToRecyclerView(recyclerView)

        // Show the popup window
        popupWindow.showAsDropDown(anchorView)
    }

}