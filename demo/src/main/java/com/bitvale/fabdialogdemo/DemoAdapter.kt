package com.bitvale.fabdialogdemo

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.bitvale.fabdialogdemo.DataProvider.Lang
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.lang_item.*

/**
 * Created by Alexander Kolpakov on 17.07.2018
 */
class DemoAdapter(var dataSet: List<Lang>) : RecyclerView.Adapter<DemoAdapter.DemoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = DemoViewHolder(parent)

    override fun getItemCount() = dataSet.size

    override fun onBindViewHolder(holder: DemoViewHolder, position: Int) = holder.bind(dataSet[position])

    class DemoViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(parent.inflate(R.layout.lang_item)),
            LayoutContainer {

        override val containerView: View?
            get() = itemView

        val colors: IntArray = itemView.context.resources.getIntArray(R.array.colors)

        fun bind(item: Lang) {
            tv_name.text = item.name
            tv_questions.text = containerView?.context?.getString(R.string.questions, item.questions)
            img_background.setBackgroundColor(colors[item.id])
        }
    }

}