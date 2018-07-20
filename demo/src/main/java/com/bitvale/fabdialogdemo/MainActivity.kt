package com.bitvale.fabdialogdemo

import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.util.DiffUtil
import android.support.v7.widget.LinearLayoutManager
import android.widget.SeekBar
import android.widget.TextView
import com.bitvale.fabdialog.widget.FabDialog
import com.bitvale.fabdialogdemo.DataProvider.Lang
import kotlinx.android.synthetic.main.activity_main.*

/**
 * Created by Alexander Kolpakov on 16.07.2018
 */
class MainActivity : AppCompatActivity(), FabDialog.FabDialogListener {

    private var stateFlag = false
    private lateinit var dataSet: List<Lang>
    private lateinit var recyclerAdapter: DemoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        dataSet = DataProvider.getData()
        initRecycler()
        savedInstanceState?.let {
            stateFlag = !it.getBoolean("filter")
        }
        initFabDialog()
    }

    private fun initRecycler() {
        with(recycler) {
            layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.VERTICAL, false)
            recyclerAdapter = DemoAdapter(dataSet)
            adapter = recyclerAdapter
        }
    }

    private fun initFabDialog() {
        if (stateFlag) {
            initFilter()
        } else {
            initDefault()
        }
        stateFlag = !stateFlag
    }

    private fun initDefault() {
        with(fab_dialog) {
            setUseDefaultView()
            setTitle(R.string.dialog_title)
            setMessage(R.string.dialog_message)
            setDialogIcon(R.drawable.android_icon)
            setFabBackgroundColor(ContextCompat.getColor(context, R.color.fabColor))
            setPositiveButton(R.string.positive_btn) {
                this.collapseDialog()
                initFabDialog()
            }
            setFabIcon(R.drawable.android_icon)
            setNegativeButton(R.string.negative_btn) { this.collapseDialog() }
            setOnClickListener { this.expandDialog() }
            setListener(this@MainActivity)
        }
    }

    private fun initFilter() {
        var questionCount = 50
        with(fab_dialog) {
            setTitle(R.string.filter_dialog_title)
            setDialogIcon(R.drawable.filter_icon)
            setFabBackgroundColor(ContextCompat.getColor(context, R.color.filterFabColor))
            setFabIcon(R.drawable.filter_icon)
            setPositiveButton(R.string.positive_btn) {
                updateRecyclerData(recyclerAdapter.dataSet, getFilteredDataSet(questionCount))
                this.collapseDialog()
                initFabDialog()
            }
            setContentView(R.layout.filter_view)

            val seekBar = findDialogViewById<SeekBar>(R.id.seek_bar)
            val seekBarProgress = findDialogViewById<TextView>(R.id.tv_sb_progress)
            seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    questionCount = progress
                    if (questionCount == 0) questionCount = 50
                    seekBarProgress.text = questionCount.toString()
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                }
            })
            seekBar.max = 250
            seekBar.progress = 50
        }
    }

    private fun updateRecyclerData(oldData: List<Lang>, newData: List<Lang>) {
        recyclerAdapter.dataSet = newData
        calculateDiff(oldData, newData)
    }

    private fun calculateDiff(oldData: List<Lang>, newData: List<Lang>) {
        DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
                    oldData[oldItemPosition].name == newData[newItemPosition].name

            override fun getOldListSize() = oldData.size

            override fun getNewListSize() = newData.size

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
                    oldData[oldItemPosition] == newData[newItemPosition]
        }).dispatchUpdatesTo(recycler.adapter)
    }

    private fun getFilteredDataSet(number: Int) = dataSet.filter { it.questions >= number }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("filter", stateFlag)
    }

    override fun onCollapsed() {
        // Do something
    }

    override fun onExpanded() {
        // Do something
    }

}
