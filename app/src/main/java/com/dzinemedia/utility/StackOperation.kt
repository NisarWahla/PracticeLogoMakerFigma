package com.dzinemedia.utility

import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.ViewGroup
import android.widget.TextView
import com.dzinemedia.logomakerpracticetask.MainActivity
import com.dzinemedia.models.ElementModel
import com.dzinemedia.models.ParentModel
import java.util.*
import kotlin.collections.ArrayList


object StackOperation {
    private const val TAG = "StackOperation"
    val viewPropertiesList: ArrayList<ElementModel> = ArrayList()

    fun saveOperation(viewGroup: ViewGroup) {
        val newParentModel = createParentModel(viewGroup)
        Log.i(TAG, "saveOperation: $newParentModel")
        //MainActivity.undoStack.push(newParentModel)
        //MainActivity.redoStack.clear()
        /*if (parentModel == null || parentModel != newParentModel) {
            parentModel = newParentModel

        }*/
    }

    fun createParentModel(viewGroup: ViewGroup): ParentModel {
        viewPropertiesList.clear()
        val height = viewGroup.height
        val width = viewGroup.width
        val colorDrawable = viewGroup.background as ColorDrawable
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
        return ParentModel(width, height, color, viewPropertiesList)
    }
}