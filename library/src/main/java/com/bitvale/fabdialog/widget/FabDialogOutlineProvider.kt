package com.bitvale.fabdialog.widget

import android.graphics.Outline
import android.view.View
import android.view.ViewOutlineProvider

/**
 * Created by Alexander Kolpakov on 15.07.2018
 */
class FabDialogOutlineProvider(width: Int, height: Int, radius: Float) : ViewOutlineProvider() {

    var currentWidth = width
    var currentHeight = height
    var currentRadius = radius

    override fun getOutline(view: View, outline: Outline) {
        outline.setRoundRect(0, 0, currentWidth, currentHeight, currentRadius)
    }
}