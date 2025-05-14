package com.dzinemedia.utility
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import java.util.Stack
class ChatGptUndoRedo {

        private lateinit var relativeLayout: RelativeLayout
        private val undoStack: Stack<List<ViewState>> = Stack()
        private val redoStack: Stack<List<ViewState>> = Stack()

        fun onCreate() {
            //for saving state call where first time save state
            saveState()
            redoStack.clear()

            //undo function call in undo button click
            //redo function call in redo button click
        }

        private fun saveState() {
            relativeLayout.children
            val state = relativeLayout.children.map { view ->
                ViewState(
                    id = view.id,
                    visibility = view.visibility,
                    positionX = view.x,
                    positionY = view.y,
                    width = view.width,
                    height = view.height
                )
            }.toList()
            undoStack.push(state)
        }

        private fun undo() {
            if (undoStack.isNotEmpty()) {
                val currentState = captureCurrentState()
                redoStack.push(currentState)
                val lastState = undoStack.pop()
                restoreState(lastState)
            }
        }

        private fun redo() {
            if (redoStack.isNotEmpty()) {
                val currentState = captureCurrentState()
                undoStack.push(currentState)
                val nextState = redoStack.pop()
                restoreState(nextState)
            }
        }

        private fun captureCurrentState(): List<ViewState> {
            return relativeLayout.children.map { view ->
                ViewState(
                    id = view.id,
                    visibility = view.visibility,
                    positionX = view.x,
                    positionY = view.y,
                    width = view.width,
                    height = view.height
                )
            }.toList()
        }

        private fun restoreState(state: List<ViewState>) {
            state.forEach { viewState ->
                val view = relativeLayout.findViewById<View>(viewState.id)
                view?.apply {
                    visibility = viewState.visibility
                    x = viewState.positionX
                    y = viewState.positionY
                    layoutParams.width = viewState.width
                    layoutParams.height = viewState.height
                    requestLayout()
                }
            }
        }

        data class ViewState(
            val id: Int,
            val visibility: Int,
            val positionX: Float,
            val positionY: Float,
            val width: Int,
            val height: Int
        )

}