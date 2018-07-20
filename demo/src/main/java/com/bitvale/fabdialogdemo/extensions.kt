package com.bitvale.fabdialogdemo

import android.support.annotation.LayoutRes
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

/**
 * Created by Alexander Kolpakov on 17.07.2018
 */

fun ViewGroup.inflate(@LayoutRes layoutId: Int, attachToRoot: Boolean = false): View {
    return LayoutInflater.from(context).inflate(layoutId, this, attachToRoot)
}