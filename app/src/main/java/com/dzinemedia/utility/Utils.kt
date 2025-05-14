package com.dzinemedia.utility

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import com.dzinemedia.callback.saveCompleted
import com.dzinemedia.models.ImageProperties
import com.dzinemedia.models.ParentModel
import com.dzinemedia.models.ViewProperties
import com.google.gson.Gson
import org.json.JSONArray
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter
import java.io.IOException

object Utils {
    private const val TAG = "Utils"
    fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun readJsonFromAssets(context: Context, fileName: String): String? {
        var jsonString: String? = null
        try {
            jsonString = context.assets.open(fileName).bufferedReader().use { it.readText() }
        } catch (ioException: IOException) {
            ioException.printStackTrace()
        }
        return jsonString
    }

    fun getColorFromJson(fillsArray: JSONArray): Int {
        val firstFillObject = fillsArray.getJSONObject(0)
        val colorObject = firstFillObject.getJSONObject("color")
        Log.i(TAG, "getColorFromJson: $colorObject")
        val r = colorObject.getDouble("r").toFloat()
        val g = colorObject.getDouble("g").toFloat()
        val b = colorObject.getDouble("b").toFloat()
        Log.i(TAG, "getColorFromJson: $r, $g, $b")

        // Convert RGB values from 0-1 range to 0-255 range
        val red = (r * 255).toInt()
        val green = (g * 255).toInt()
        val blue = (b * 255).toInt()
        Log.i(TAG, "getColorFromJson: $red, $green, $blue")
        return Color.rgb(red, green, blue)
    }

    fun calculateNewViewPosition(
        originalCanvasSize: Pair<Int, Int>,
        originalViewPosition: Pair<Int, Int>,
        originalViewSize: Pair<Int, Int>,
        newCanvasSize: Pair<Int, Int>
    ): ImageProperties {
        val (originalWidth, originalHeight) = originalCanvasSize
        val (newWidth, newHeight) = newCanvasSize
        val (originalX, originalY) = originalViewPosition
        val (originalViewWidth, originalViewHeight) = originalViewSize

        // Calculate scaling factors
        val widthScale = newWidth.toFloat() / originalWidth.toFloat()
        val heightScale = newHeight.toFloat() / originalHeight.toFloat()

        // Calculate new view position
        val newX = originalX * widthScale
        val newY = originalY * heightScale
        // Calculate new view size
        val newViewWidth = originalViewWidth * widthScale
        val newViewHeight = originalViewHeight * heightScale

        return ImageProperties(newX, newY, newViewWidth, newViewHeight)
    }

    fun calculateNewViewProperties(
        originalCanvasSize: Pair<Int, Int>,
        originalViewPosition: Pair<Int, Int>,
        originalTextSize: Float,
        newCanvasSize: Pair<Int, Int>
    ): ViewProperties {
        val (originalWidth, originalHeight) = originalCanvasSize
        val (newWidth, newHeight) = newCanvasSize
        val (originalX, originalY) = originalViewPosition

        // Calculate scaling factors
        val widthScale = newWidth.toFloat() / originalWidth.toFloat()
        val heightScale = newHeight.toFloat() / originalHeight.toFloat()

        // Calculate new view position
        val newX = originalX * widthScale
        val newY = originalY * heightScale

        // Use the average of width and height scaling factors for text size scaling
        val scaleFactor = (widthScale + heightScale) / 2
        val newTextSize = originalTextSize * scaleFactor

        return ViewProperties(newX, newY, newTextSize)
    }

    fun captureViewToBitmap(view: View, targetWidth: Int, targetHeight: Int): Bitmap {
        val originalBitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(originalBitmap)
        view.draw(canvas)
        return if (targetWidth > 0 && targetHeight > 0) {
            val scaledBitmap =
                Bitmap.createScaledBitmap(originalBitmap, targetWidth, targetHeight, true)
            originalBitmap.recycle()
            scaledBitmap
        } else {
            originalBitmap
        }
    }

    fun saveBitmapToExternalFilesDir(
        context: Context,
        bitmap: Bitmap,
        fileName: String, subDirectory: String
    ): Boolean {
        val externalFilesDir = if (subDirectory == "Draft") {
            context.getExternalFilesDir(subDirectory)
        } else {
            context.getExternalFilesDir(null)
        }
        Log.i(TAG, "saveBitmapToExternalFilesDir: directory $externalFilesDir")
        if (!externalFilesDir?.exists()!!) {
            externalFilesDir.mkdirs()
        }
        val file = File(externalFilesDir, "$fileName.png")
        var fos: FileOutputStream? = null
        return try {
            fos = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
            showToast(context, "Saved Successfully!")
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        } finally {
            try {
                fos?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
    /*parentModel?.let {
            parentList.add(parentModel!!)
        }*/

    @SuppressLint("ClickableViewAccessibility")
    fun makeViewMovable(view: View) {
        var dX = 0f
        var dY = 0f

        view.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    dX = v.x - event.rawX
                    dY = v.y - event.rawY
                }
                MotionEvent.ACTION_MOVE -> {
                    v.animate()
                        .x(event.rawX + dX)
                        .y(event.rawY + dY)
                        .setDuration(0)
                        .start()
                }
                MotionEvent.ACTION_UP -> {
                    // Optionally handle the view release
                }
                else -> return@setOnTouchListener false
            }
            true
        }
    }

    fun saveParentModelToJsonFile(context: Context, parentModel: ParentModel, fileName: String, param: saveCompleted) {
        val gson = Gson()
        val userJson = gson.toJson(parentModel)

        val file = File(context.filesDir, "/Draft/json")
        Log.i(TAG, "saveParentModelToJsonFile: ${file.absolutePath}")
        if (!file.exists()) {
            file.mkdirs()
        }
        val newFile = File(file,"draft.txt")
        Log.i(TAG, "saveParentModelToJsonFile: ${file.absolutePath}")
        FileOutputStream(newFile).use {
            it.write(userJson.toByteArray())
        }
        param.onSaveCompleted()
    }
}