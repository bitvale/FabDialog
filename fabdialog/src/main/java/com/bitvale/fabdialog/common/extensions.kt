package com.bitvale.fabdialog.common

import android.support.annotation.DimenRes
import android.view.View

/**
 * Created by Alexander Kolpakov on 16.07.2018
 */

fun View.getFloatDimen(@DimenRes res: Int) = context.resources.getDimension(res)

fun View.getIntDimen(@DimenRes res: Int) = context.resources.getDimensionPixelOffset(res)