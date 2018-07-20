package com.bitvale.fabdialog.widget

import android.graphics.PointF
import android.graphics.Rect
import android.view.View
import android.view.ViewGroup


/**
 * Created by Alexander Kolpakov on 06.07.2018
 */
class FabDialogSettings {

    var x = 0f
    var y = 0f
    var translationX = 0f
    var translationY = 0f
    var width = 0
    var height = 0
    var maxWidth = 0
    var maxHeight = 0
    var radius = 0f
    var animRadius = 0f
    var minDimension = 0f
    var center = PointF(0f, 0f)
    var isInitialized = false
    val bmpRect = Rect(0, 0, 0, 0)
    var currentWidth = 0f
    var currentHeight = 0f
    var parentHeight = 0

    fun initialize(view: View) {
        x = view.x
        y = view.y
        translationX = view.translationX
        translationY = view.translationY
        width = view.width
        height = view.height
        center.x = width / 2f
        center.y = height / 2f
        minDimension = Math.min(width.toFloat(), height.toFloat())
        radius = minDimension / 2f
        animRadius = radius
        bmpRect.left = view.paddingLeft
        bmpRect.top = view.paddingTop
        bmpRect.right = view.right - view.left - view.paddingRight
        bmpRect.bottom = view.bottom - view.top - view.paddingBottom
        maxWidth = view.resources.displayMetrics.widthPixels
        maxHeight = view.resources.displayMetrics.heightPixels
        isInitialized = true
        parentHeight = (view.parent as ViewGroup).height
        currentHeight = height.toFloat()
        currentWidth = width.toFloat()
    }
}
