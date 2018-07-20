package com.bitvale.fabdialog.widget

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.support.annotation.*
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.bitvale.fabdialog.R
import com.bitvale.fabdialog.common.BitmapUtil
import com.bitvale.fabdialog.common.Constant.COLLAPSED
import com.bitvale.fabdialog.common.Constant.COLLAPSE_DURATION
import com.bitvale.fabdialog.common.Constant.COLLAPSE_MOTION_DURATION
import com.bitvale.fabdialog.common.Constant.COLOR_ANIMATION_DURATION
import com.bitvale.fabdialog.common.Constant.DIALOG_SIZE_PROPERTY
import com.bitvale.fabdialog.common.Constant.ELEVATION_ANIMATION_DURATION
import com.bitvale.fabdialog.common.Constant.EXPANDED
import com.bitvale.fabdialog.common.Constant.EXPAND_DURATION
import com.bitvale.fabdialog.common.Constant.EXPAND_MOTION_DURATION
import com.bitvale.fabdialog.common.Constant.FAB_POSITION_PROPERTY
import com.bitvale.fabdialog.common.Constant.KEY_FAB_STATE
import com.bitvale.fabdialog.common.Constant.KEY_IS_EXPANDED
import com.bitvale.fabdialog.common.Constant.PROGRESSING
import com.bitvale.fabdialog.common.getFloatDimen
import com.bitvale.fabdialog.common.getIntDimen

/**
 * Created by Alexander Kolpakov on 05.07.2018
 */
class FabDialog : ConstraintLayout {

    interface FabDialogListener {
        fun onCollapsed()
        fun onExpanded()
    }

    private var state = COLLAPSED

    private val settings = FabDialogSettings()
    private var defElevation = 0f
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val circlePath = Path()
    private var fubIconBitmap: Bitmap? = null

    private var rectF: RectF = RectF(0f, 0f, 0f, 0f)
    private var helperRectF: RectF = RectF(0f, 0f, 0f, 0f)

    private var radius = 0f

    private var expandPath = Path()
    private var collapsePath = Path()

    private var dialogHeight = 0
    private var dialogWidth = 0

    private var drawFabIcon = true

    private var dimView: View? = null
    private var contentView: View? = null

    @Dimension
    private var dialogCornerRadius = 0f
    @ColorInt
    private var dialogBackgroundColor = 0
    @ColorInt
    private var fabBackgroundColor = 0
    @BoolRes
    private var dimBackgroundEnabled = true
    @ColorInt
    private var dimBackgroundColor = 0
    @BoolRes
    private var closeOnTouchOutside = true

    private var fabDialogListener: FabDialogListener? = null

    fun setListener(listener: FabDialogListener) {
        this.fabDialogListener = listener
    }

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {

        setContentView(R.layout.include_dialog_layout)

        attrs?.let { retrieveAttributes(attrs, defStyleAttr) }

        defElevation = getFloatDimen(R.dimen.elevation)
        elevation = defElevation

        setBackgroundColor(Color.TRANSPARENT)
        isClickable = true
        initOnTouchListener()
    }

    private fun retrieveAttributes(attrs: AttributeSet, defStyleAttr: Int) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.FabDialog, defStyleAttr, R.style.FabDialog)

        dialogBackgroundColor = typedArray.getColor(R.styleable.FabDialog_dialogBackgroundColor, 0)
        fabBackgroundColor = typedArray.getColor(R.styleable.FabDialog_fabBackgroundColor, 0)
        dialogCornerRadius = typedArray.getDimension(R.styleable.FabDialog_dialogCornerRadius, 0f)
        dimBackgroundColor = typedArray.getColor(R.styleable.FabDialog_dimBackgroundColor, 0)
        dimBackgroundEnabled = typedArray.getBoolean(R.styleable.FabDialog_dimBackgroundEnabled, true)
        closeOnTouchOutside = typedArray.getBoolean(R.styleable.FabDialog_closeOnTouchOutside, true)

        paint.color = fabBackgroundColor

        val src = typedArray.getDrawable(R.styleable.FabDialog_fabIcon)
        fubIconBitmap = BitmapUtil.getBitmapFromDrawable(src)

        typedArray.recycle()
    }

    private fun calculateDialogSize() {
        val windowVerticalPadding = getIntDimen(R.dimen.dialog_vertical_margin)
        val windowHorizontalPadding = getIntDimen(R.dimen.dialog_horizontal_margin)
        val windowHeight = resources.displayMetrics.heightPixels
        val windowWidth = resources.displayMetrics.widthPixels

        val maxDialogWidth = getIntDimen(R.dimen.dialog_max_width)
        val calculatedWidth = windowWidth - windowHorizontalPadding * 2
        dialogWidth = Math.min(calculatedWidth, maxDialogWidth)

        val widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(dialogWidth, View.MeasureSpec.AT_MOST)
        val heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        contentView?.measure(widthMeasureSpec, heightMeasureSpec)

        val calculatedHeight = contentView?.measuredHeight as Int

        maxHeight = windowHeight - windowVerticalPadding * 2
        dialogHeight = Math.min(calculatedHeight, maxHeight)

        dialogHeight = if (dialogHeight % 2 == 0) dialogHeight else dialogHeight + 1 // prevent incorrect outline shadow with odd height
    }

    /**
     * Expand dialog with animation.
     */
    fun expandDialog() {
        initPath()
        startExpandMotion()
    }

    /**
     * Collapse dialog with animation.
     */
    fun collapseDialog() {
        collapse()
    }

    private fun startExpandMotion() {
        ObjectAnimator.ofMultiFloat(this, FAB_POSITION_PROPERTY, expandPath).apply {
            duration = EXPAND_MOTION_DURATION
            interpolator = DecelerateInterpolator()
            addUpdateListener {
                invalidate()
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator?, isReverse: Boolean) {
                    state = PROGRESSING
                    animateElevation(elevation, 0f)
                }

                override fun onAnimationEnd(animation: Animator?) {
                    showDimBackground()
                    expand()
                }
            })
            start()
        }
    }

    private fun expand() {
        val width = width.toFloat()
        val height = height.toFloat()
        val toX = dialogWidth / 2 - width / 2
        val toY = dialogHeight / 2 - height / 2
        val toRadius = radius - dialogCornerRadius
        val animatedValues = arrayOf(floatArrayOf(0f, 0f, 0f), floatArrayOf(toX, toY, toRadius))

        ObjectAnimator.ofMultiFloat(this, DIALOG_SIZE_PROPERTY, animatedValues).apply {
            duration = EXPAND_DURATION
            interpolator = DecelerateInterpolator()

            addUpdateListener {
                invalidate()
            }

            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator?) {
                    animateBackgroundColor(fabBackgroundColor, dialogBackgroundColor)
                    settings.currentWidth = dialogWidth.toFloat()
                    settings.currentHeight = dialogHeight.toFloat()

                    drawFabIcon = false

                    val r = toX + width
                    val b = toY + height

                    layoutParams.width = dialogWidth
                    layoutParams.height = dialogHeight

                    rectF.set(toX, toY, r, b)
                    helperRectF.set(toX, toY, r, b)

                    setupCoordinates()
                    requestLayout()
                }

                override fun onAnimationEnd(animation: Animator?) {
                    contentView?.visibility = View.VISIBLE
                    setupPadding()
                    updateOutline()
                    animateElevation(0f, defElevation)
                    state = EXPANDED
                    fabDialogListener?.onExpanded()
                }
            })
            start()
        }
    }

    private fun collapse() {
        val width = settings.width.toFloat()
        val height = settings.height.toFloat()
        val toX = -(dialogWidth / 2 - width / 2)
        val toY = -(dialogHeight / 2 - height / 2)
        val toRadius = dialogCornerRadius - settings.radius
        val animatedValues = arrayOf(floatArrayOf(0f, 0f, 0f), floatArrayOf(toX, toY, toRadius))

        ObjectAnimator.ofMultiFloat(this, DIALOG_SIZE_PROPERTY, animatedValues)
                .apply {
                    duration = COLLAPSE_DURATION
                    interpolator = DecelerateInterpolator()
                    addUpdateListener {
                        invalidate()
                    }
                    addListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationStart(animation: Animator?) {
                            state = PROGRESSING
                            removeDimBackground()
                            settings.currentWidth = width
                            settings.currentHeight = height
                            elevation = 0f
                            contentView?.visibility = View.INVISIBLE
                            drawFabIcon = true
                            helperRectF.set(rectF)
                        }

                        override fun onAnimationEnd(animation: Animator?) {
                            layoutParams.width = width.toInt()
                            layoutParams.height = height.toInt()
                            rectF.set(0f, 0f, width, height)
                            setupCoordinates()
                            requestLayout()
                            setupPadding()
                            updateOutline()
                            startCollapseMotion()
                        }
                    })
                    start()
                }
    }

    private fun startCollapseMotion() {
        ObjectAnimator.ofMultiFloat(this, FAB_POSITION_PROPERTY, collapsePath).apply {
            duration = COLLAPSE_MOTION_DURATION
            interpolator = DecelerateInterpolator()
            addUpdateListener {
                invalidate()
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator?, isReverse: Boolean) {
                    animateElevation(0f, defElevation)
                }

                override fun onAnimationEnd(animation: Animator?) {
                    fabDialogListener?.onCollapsed()
                    animateBackgroundColor(dialogBackgroundColor, fabBackgroundColor)
                    state = COLLAPSED
                    fabDialogListener?.onCollapsed()
                }
            })
            start()
        }
    }

    @SuppressLint("ObjectAnimatorBinding")
    @SuppressWarnings("unused")
    private fun setFubPosition(x: Float, y: Float) {
        this.x = x
        this.y = y
    }

    @SuppressLint("ObjectAnimatorBinding")
    @SuppressWarnings("unused")
    private fun setDialogSize(x: Float, y: Float, radius: Float) {
        val left = helperRectF.left - x
        val right = helperRectF.right + x
        val top = helperRectF.top - y
        val bottom = helperRectF.bottom + y
        this.radius = Math.abs(settings.radius - radius)
        rectF.set(left, top, right, bottom)
    }

    private fun animateBackgroundColor(@ColorInt fromColor: Int, @ColorInt toColor: Int) {
        if (dialogBackgroundColor != fabBackgroundColor) {
            ValueAnimator.ofArgb(fromColor, toColor).apply {
                duration = COLOR_ANIMATION_DURATION
                addUpdateListener {
                    paint.color = animatedValue as Int
                    invalidate()
                }
                start()
            }
        }
    }

    private fun showDimBackground() {
        if (!dimBackgroundEnabled && !closeOnTouchOutside) return
        if (dimView == null) dimView = View(context)
        val parent = parent as ViewGroup
        val lp = parent.layoutParams
        lp.width = settings.maxWidth
        lp.height = settings.maxHeight
        if (dimBackgroundEnabled) dimView?.setBackgroundColor(dimBackgroundColor)
        if (closeOnTouchOutside) dimView?.setOnClickListener { collapseDialog() }
        dimView?.layoutParams = lp
        parent.addView(dimView)
    }

    private fun removeDimBackground() {
        (parent as ViewGroup).removeView(dimView)
    }

    private fun setupCoordinates() {
        addOnLayoutChangeListener(object : OnLayoutChangeListener {
            override fun onLayoutChange(v: View?, left: Int, top: Int, right: Int, bottom: Int, oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int) {
                removeOnLayoutChangeListener(this)
                val screenCenterX = settings.maxWidth / 2f
                val screenCenterY = settings.parentHeight / 2f
                x = screenCenterX - settings.currentWidth / 2
                y = screenCenterY - settings.currentHeight / 2
            }
        })
    }

    private fun setupPadding() {
        val padding = if (state == COLLAPSED) getIntDimen(R.dimen.fab_padding) else 0
        setPadding(padding, padding, padding, padding)
    }

    private fun updateOutline() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val outlineWidth = settings.currentWidth.toInt()
            val outlineHeight = settings.currentHeight.toInt()
            if (outlineProvider !is FabDialogOutlineProvider) {
                outlineProvider = FabDialogOutlineProvider(outlineWidth, outlineHeight, radius)
            } else {
                (outlineProvider as FabDialogOutlineProvider).apply {
                    currentWidth = outlineWidth
                    currentHeight = outlineHeight
                    currentRadius = radius
                }
                invalidateOutline()
            }
        }
    }

    private fun initPath() {
        expandPath.reset()
        val screenCenterX = settings.maxWidth / 2f
        val screenCenterY = settings.parentHeight / 2f
        expandPath.moveTo(x, y)
        var x1 = screenCenterX - width / 2
        var y1 = y
        var x2 = screenCenterX - width / 2
        var y2 = screenCenterY - height / 2
        expandPath.quadTo(x1, y1, x2, y2)

        collapsePath.reset()
        collapsePath.moveTo(screenCenterX - width / 2, screenCenterY - height / 2)
        x1 = x
        y1 = screenCenterY - height / 2
        x2 = settings.x
        y2 = settings.y
        collapsePath.quadTo(x1, y1, x2, y2)
    }

    private fun animateElevation(from: Float, to: Float, delay: Long = ELEVATION_ANIMATION_DURATION) {
        ValueAnimator.ofFloat(from, to).apply {
            addUpdateListener { elevation = it.animatedValue as Float }
            duration = delay
        }.start()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (!settings.isInitialized) {
            rectF.set(0f, 0f, w.toFloat(), h.toFloat())
            settings.initialize(this)
            radius = settings.radius
            updateOutline()
        }
    }

    override fun onDraw(canvas: Canvas?) {
        circlePath.addRoundRect(rectF, radius, radius, Path.Direction.CCW)
        canvas?.drawPath(circlePath, paint)
        if (drawFabIcon) {
            drawBitmap(canvas)
        }
        circlePath.reset()
    }

    private fun drawBitmap(canvas: Canvas?) {
        fubIconBitmap?.let {
            val w = settings.bmpRect.width()
            val h = settings.bmpRect.height()
            settings.bmpRect.left = rectF.centerX().toInt() - w / 2
            settings.bmpRect.top = rectF.centerY().toInt() - h / 2
            settings.bmpRect.right = rectF.centerX().toInt() + w / 2
            settings.bmpRect.bottom = rectF.centerY().toInt() + h / 2
            canvas?.drawBitmap(fubIconBitmap, null, settings.bmpRect, null)
        }
    }

    private fun initOnTouchListener() {
        setOnTouchListener(OnTouchListener { v, event ->
            if (state != COLLAPSED) return@OnTouchListener true

            when (event?.action) {
                MotionEvent.ACTION_DOWN -> {
                    v.scaleX = 0.99f
                    v.scaleY = 0.99f
                    v?.z = elevation / 2
                }
                MotionEvent.ACTION_UP -> {
                    v.scaleX = 1.0f
                    v.scaleY = 1.0f
                    v?.z = elevation
                }
            }
            false
        })
    }

    /**
     * Set the title to display on default dialog layout.
     */
    fun setTitle(title: String) {
        contentView?.findViewById<TextView>(R.id.tv_dialog_title)?.text = title
    }

    /**
     * Set the title to display using the given resource id.
     */
    fun setTitle(@StringRes titleId: Int) {
        val title = context.getString(titleId)
        setMessage(title)
    }

    /**
     * Set the message to display on default dialog layout.
     */
    fun setMessage(message: String) {
        contentView?.findViewById<TextView>(R.id.tv_dialog_msg)?.text = message
        calculateDialogSize()
    }

    /**
     * Set the message to display using the given resource id.
     */
    fun setMessage(@StringRes messageId: Int) {
        val msg = context.getString(messageId)
        setMessage(msg)
    }

    /**
     * Set the icon to display on default dialog layout.
     *
     * * @param icon The drawable of the icon
     */
    fun setDialogIcon(icon: Drawable?) {
        getIconImageView()?.setImageDrawable(icon)
    }

    /**
     * Set the icon to display on default dialog layout.
     *
     * @param icon The drawable resource id of the icon
     */
    fun setDialogIcon(@DrawableRes icon: Int) {
        getIconImageView()?.setImageResource(icon)
    }

    private fun getIconImageView(): ImageView? {
        val imageView = contentView?.findViewById<ImageView>(R.id.img_icon)
        imageView?.visibility = View.VISIBLE
        return imageView
    }

    /**
     * Set an action to be invoked when the positive button of the dialog is pressed.
     *
     * @param textId The resource id of the text to display in the positive button
     * @param action The action to be invoked.
     */
    fun setPositiveButton(@StringRes textId: Int, action: () -> Unit) {
        setPositiveButton(context.getText(textId), action)
    }

    /**
     * Set an action to be invoked when the negative button of the dialog is pressed.
     *
     * @param textId The resource id of the text to display in the negative button
     * @param action The action to be invoked.
     */
    fun setNegativeButton(@StringRes textId: Int, action: () -> Unit) {
        setNegativeButton(context.getText(textId), action)
    }

    /**
     * Set an action to be invoked when the positive button of the dialog is pressed.
     *
     * &nbsp;
     *
     * For working with custom layout just provide a button with id:
     * ```
     *     android:id="@+id/btn_positive"
     * ```
     * @param text The text to display in thepositive button
     * @param action The action to be invoked.
     */
    fun setPositiveButton(text: CharSequence, action: () -> Unit) {
        val button = findViewById<Button?>(R.id.btn_positive)
        button?.text = text
        button?.setOnClickListener { action() }
    }

    /**
     * Set an action to be invoked when the negative button of the dialog is pressed.
     *
     * &nbsp;
     *
     * For working with custom layout just provide a button with id:
     * ```
     *     android:id="@+id/btn_negative"
     * ```
     * @param text The text to display in the negative button
     * @param action The action to be invoked.
     */
    fun setNegativeButton(text: CharSequence, action: () -> Unit) {
        val button = findViewById<Button?>(R.id.btn_negative)
        button?.text = text
        button?.setOnClickListener { action() }
    }

    /**
     * Set the dialog content from a layout resource. The resource will be
     *
     * inflated and added to dialog top-view. This method has no effect if called
     *
     * after [expandDialog]
     *
     * &nbsp;
     *
     * IMPORTANT: the top-level view of the inflated layout resource must be with id:
     * ```
     *     android:id="@+id/dialog_content"
     * ```
     * @param layoutResID Resource ID to be inflated.
     */
    fun setContentView(@LayoutRes layoutResID: Int) {
        if (state != COLLAPSED) return
        removeAllViews()
        LayoutInflater.from(context).inflate(layoutResID, this)
        contentView = findViewById(R.id.dialog_content)
        calculateDialogSize()
        contentView?.visibility = View.INVISIBLE
    }

    /**
     * Sets the background color for dialog.
     *
     * @param color the color of the background
     */
    fun setDialogBackground(@ColorInt color: Int) {
        dialogBackgroundColor = color
    }

    /**
     * Sets the background color for fab.
     *
     * @param color the color of the background
     */
    fun setFabBackground(@ColorInt color: Int) {
        fabBackgroundColor = color
        paint.color = color
    }

    /**
     * Sets the background color for dialog dim background.
     *
     * @param color the color of the background
     */
    fun setDimBackground(@ColorInt color: Int) {
        dimBackgroundColor = color
    }

    /**
     * Sets whether this dialog should be show dim background. Default is true.
     *
     * @param isEnabled Whether the dialog should be show dim background.
     */
    fun setDimBackgroundEnabled(isEnabled: Boolean) {
        dimBackgroundEnabled = isEnabled
    }

    /**
     * Sets the dialog corner radius.
     *
     * @param radius for dialog corner
     */
    fun setDialogCornerRadius(@Dimension radius: Float) {
        dialogCornerRadius = radius
    }

    /**
     * Sets whether this dialog is canceled when touched outside the window's
     * bounds. If setting to true, the dialog is set to be cancelable if not
     * already set.
     *
     * @param cancel Whether the dialog should be canceled when touched outside
     * the window.
     */
    fun setCanceledOnTouchOutside(cancel: Boolean) {
        closeOnTouchOutside = cancel
    }

    /**
     * Finds a view that was identified by the `android:id` XML attribute
     *
     * @param id the ID to search for
     * @return a view with given ID if found, or `null` otherwise
     */
    @Nullable
    fun <T : View> findDialogViewById(@IdRes id: Int): T {
        return findViewById(id)
    }

    /**
     * @return Whether the dialog is currently expanded.
     */
    fun isExpanded() = state == EXPANDED

    override fun onSaveInstanceState(): Parcelable {
        super.onSaveInstanceState()
        return Bundle().apply {
            putBoolean(KEY_IS_EXPANDED, state == EXPANDED)
            putParcelable(KEY_FAB_STATE, super.onSaveInstanceState())
        }
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is Bundle) {
            super.onRestoreInstanceState(state.getParcelable(KEY_FAB_STATE))
            val isExpanded = state.getBoolean(KEY_IS_EXPANDED)
            if (isExpanded) {
                (parent as ViewGroup).addOnLayoutChangeListener(object : OnLayoutChangeListener {
                    override fun onLayoutChange(v: View?, left: Int, top: Int, right: Int, bottom: Int, oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int) {
                        v?.removeOnLayoutChangeListener(this)
                        forceExpand()
                    }
                })
            }
        }
    }

    private fun forceExpand() {
        initPath()

        state = EXPANDED

        rectF.set(0f, 0f, dialogWidth.toFloat(), dialogHeight.toFloat())
        radius = dialogCornerRadius
        paint.color = dialogBackgroundColor
        drawFabIcon = false

        settings.currentWidth = dialogWidth.toFloat()
        settings.currentHeight = dialogHeight.toFloat()
        layoutParams.width = dialogWidth
        layoutParams.height = dialogHeight

        contentView?.visibility = View.VISIBLE

        setupPadding()
        updateOutline()
        showDimBackground()
        setupCoordinates()
        requestLayout()
    }
}