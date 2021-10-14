package io.agora.edu.uikit.impl.options

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.Rect
import android.util.DisplayMetrics
import android.view.*
import android.widget.*
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.agora.edu.R
import io.agora.edu.uikit.impl.tool.AgoraUIApplianceType
import io.agora.edu.uikit.impl.tool.ColorOptions
import io.agora.edu.uikit.interfaces.protocols.AgoraUIDrawingConfig

class AgoraUIWhiteboardOptionDialog(context: Context,
                                    private val width: Int,
                                    private val height: Int,
                                    private val config: AgoraUIDrawingConfig)
    : Dialog(context, R.style.agora_dialog) {

    companion object {
        var listener: AgoraUIWhiteboardOptionListener? = null

        fun hasSubOptions(type: AgoraUIApplianceType): Boolean {
            return type == AgoraUIApplianceType.Pen ||
                    type == AgoraUIApplianceType.Rect ||
                    type == AgoraUIApplianceType.Circle ||
                    type == AgoraUIApplianceType.Line ||
                    type == AgoraUIApplianceType.Text
        }
    }

    internal val applianceSpanCount = 5
    internal val colorSpanCount = 6
    internal val textSizeSpanCount = 4
    internal val thicknessSpanCount = 5
    internal val thicknessMax = 38

    internal var windowWidth = 0
    internal var windowHeight = 0
    internal var contentWidth = 0
    internal var contentHeight = 0
    internal var windowLowHeight = 0
    internal var marginVertical = 0
    internal var marginHorizontal = 0

    private lateinit var divider1: View
    private lateinit var divider2: View
    private lateinit var subItemLayout: RelativeLayout
    private lateinit var colorPlateRecycler: RecyclerView

    private var applianceAdapter: ApplianceItemAdapter? = null

    internal var textAdapter: TextSizeItemAdapter? = null
    internal var thickAdapter: ThicknessItemAdapter? = null

    private var anchor: View? = null
    private var showMargin: Int = 0

    init {
        windowWidth = width
        windowHeight = height
    }

    fun show(anchor: View, margin: Int) {
        this.anchor = anchor
        this.showMargin = margin

        setCancelable(true)
        setCanceledOnTouchOutside(true)
        initView()
        adjustPosition(anchor, margin)
        super.show()
    }

    override fun dismiss() {
        this.anchor = null
        super.dismiss()
    }

    private fun initView() {
        setContentView(R.layout.agora_option_whiteboard_dialog_layout)
        subItemLayout = findViewById(R.id.agora_option_whiteboard_sub_item_layout)
        colorPlateRecycler = findViewById(R.id.agora_option_whiteboard_color_plate_recycler)

        val border = context.resources.getDimensionPixelSize(
            R.dimen.agora_tool_popup_layout_margin)
        val elevation = border * 3f / 5

        setMeasureValues(border)
        setDialogBorder(elevation)
        initDividers()
        initColorPlateRecycler()
        initSubRecycler()
        initApplianceRecycler()
        showOrHideSubOptionLayout(hasSubOptions(config.activeAppliance))
    }

    private fun setDialogBorder(elevation: Float) {
        findViewById<RelativeLayout>(R.id.agora_option_whiteboard_dialog_border_layout)?.let {
            it.clipToOutline = true
            it.elevation = elevation
        }
    }

    private fun setMeasureValues(border: Int) {
        contentWidth = width - border * 2
        contentHeight = height - border * 2
        windowLowHeight = (contentHeight * 136f / 310).toInt() + border * 2
    }

    private fun initApplianceRecycler() {
        findViewById<RecyclerView>(R.id.agora_option_whiteboard_appliance_item_recycler)?.let { recycler ->
            recycler.layoutManager = GridLayoutManager(context, applianceSpanCount)
            ApplianceItemAdapter.marginHorizontal = (contentWidth * 15f / 280).toInt()
            ApplianceItemAdapter.marginVertical = (contentWidth * 20f / 280).toInt()
            ApplianceItemAdapter.iconSize = (contentWidth * 38f / 280).toInt()
            ApplianceItemAdapter.itemSpacing = (contentWidth * 15f / 280).toInt()

            applianceAdapter = ApplianceItemAdapter(recycler.context, config, this,
                object : ApplianceItemClickListener {
                    override fun onApplianceClicked(type: AgoraUIApplianceType) {
                        initSubRecycler()
                    }
                })

            recycler.adapter = applianceAdapter
            (recycler.layoutParams as? ViewGroup.MarginLayoutParams)?.let { param ->
                param.topMargin = ApplianceItemAdapter.marginVertical
                param.bottomMargin = ApplianceItemAdapter.marginVertical
                param.leftMargin = ApplianceItemAdapter.marginHorizontal
                param.rightMargin = ApplianceItemAdapter.marginHorizontal
                param.width = ViewGroup.MarginLayoutParams.MATCH_PARENT
                param.height = ViewGroup.MarginLayoutParams.WRAP_CONTENT
                recycler.layoutParams = param
            }

            for (i in 0 until recycler.itemDecorationCount) {
                recycler.removeItemDecorationAt(i)
            }

            recycler.addItemDecoration(object : RecyclerView.ItemDecoration() {
                override fun getItemOffsets(outRect: Rect, view: View,
                    parent: RecyclerView, state: RecyclerView.State) {
                    outRect.bottom = ApplianceItemAdapter.itemSpacing

                    val position = parent.getChildAdapterPosition(view)
                    val count = parent.adapter?.itemCount ?: 0
                    val residual = count % applianceSpanCount
                    val lastRowCount = if (residual == 0) applianceSpanCount else residual
                    if (position in count - lastRowCount until count) {
                        outRect.bottom = 0
                    }

                    outRect.left = ((position % applianceSpanCount) *
                            ApplianceItemAdapter.itemSpacing / (applianceSpanCount.toFloat())).toInt()
                }
            })

            applianceAdapter?.layoutChangedListener = object : WindowContentLayoutChangedListener {
                override fun onWindowContentLayoutChanged(type: AgoraUIApplianceType, hasSubContent: Boolean) {
                    showOrHideSubOptionLayout(hasSubContent)
                    anchor?.let { adjustPosition(it, showMargin) }
                }
            }
        }
    }

    private fun initColorPlateRecycler() {
        colorPlateRecycler.let { recycler ->
            recycler.visibility = View.VISIBLE
            recycler.layoutManager = AutoMeasureGridLayoutManager(context, colorSpanCount)

            ColorPlateItemAdapter.marginHorizontal = (contentWidth * 18f / 280).toInt()
            ColorPlateItemAdapter.marginVertical = (contentWidth * 20f / 280).toInt()
            ColorPlateItemAdapter.iconSize = (contentWidth * 28f / 280).toInt()
            ColorPlateItemAdapter.itemSpacing = (contentWidth * 15f / 280).toInt()
            ColorPlateItemAdapter.layoutHeight = (contentHeight * 112f / 310).toInt()

            recycler.adapter = ColorPlateItemAdapter(context, config, this)
            (recycler.layoutParams as? ViewGroup.MarginLayoutParams)?.let { param ->
                param.topMargin = ColorPlateItemAdapter.marginVertical
                param.bottomMargin = ColorPlateItemAdapter.marginVertical
                param.leftMargin = ColorPlateItemAdapter.marginHorizontal
                param.rightMargin = ColorPlateItemAdapter.marginHorizontal
                param.width = ViewGroup.MarginLayoutParams.MATCH_PARENT
                param.height = ViewGroup.MarginLayoutParams.WRAP_CONTENT
                recycler.layoutParams = param
            }

            for (i in 0 until recycler.itemDecorationCount) {
                recycler.removeItemDecorationAt(i)
            }

            recycler.addItemDecoration(object : RecyclerView.ItemDecoration() {
                override fun getItemOffsets(outRect: Rect, view: View,
                                            parent: RecyclerView, state: RecyclerView.State) {
                    outRect.bottom = ColorPlateItemAdapter.itemSpacing

                    val position = parent.getChildAdapterPosition(view)
                    val count = parent.adapter?.itemCount ?: 0
                    val residual = count % colorSpanCount
                    val lastRowCount = if (residual == 0) colorSpanCount else residual
                    if (position in count - 1 - lastRowCount until count) {
                        outRect.bottom = 0
                    }

                    outRect.left = ((position % colorSpanCount) *
                            ColorPlateItemAdapter.itemSpacing / (colorSpanCount.toFloat())).toInt()
                }
            })
        }
    }

    private fun hideColorRecycler() {
        colorPlateRecycler.visibility = View.GONE
    }

    private fun initSubRecycler() {
        subItemLayout.visibility = View.VISIBLE
        if (config.activeAppliance == AgoraUIApplianceType.Text) {
            initTextSizeRecycler(subItemLayout)
        } else {
            initThicknessRecycler(subItemLayout)
        }
    }

    private fun hideSubRecycler() {
        subItemLayout.visibility = View.GONE
    }

    private fun showOrHideSubOptionLayout(hasContent: Boolean) {
        if (hasContent) {
            initSubRecycler()
            initColorPlateRecycler()
            showDividers()
        } else {
            hideDividers()
            hideSubRecycler()
            hideColorRecycler()
        }
    }

    private fun initTextSizeRecycler(parent: RelativeLayout) {
        parent.removeAllViews()
        textAdapter = null
        thickAdapter = null

        TextSizeItemAdapter.marginHorizontal = (contentWidth * 22f / 280).toInt()
        TextSizeItemAdapter.itemSpacing = (contentWidth * 28f / 280).toInt()
        TextSizeItemAdapter.iconSize = (contentWidth * 38f / 280).toInt()

        val recyclerView = RecyclerView(parent.context)
        val param = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT)
        param.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE)
        param.leftMargin = TextSizeItemAdapter.marginHorizontal
        param.rightMargin = TextSizeItemAdapter.marginHorizontal
        parent.addView(recyclerView, param)

        recyclerView.layoutManager = AutoMeasureGridLayoutManager(context, textSizeSpanCount)
        textAdapter = TextSizeItemAdapter(config, this)
        recyclerView.adapter = textAdapter

        for (i in 0 until recyclerView.itemDecorationCount) {
            recyclerView.removeItemDecorationAt(i)
        }

        recyclerView.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(outRect: Rect, view: View,
                parent: RecyclerView, state: RecyclerView.State) {
                val position = parent.getChildAdapterPosition(view)
                outRect.left = ((position % textSizeSpanCount) *
                        TextSizeItemAdapter.itemSpacing / (textSizeSpanCount.toFloat())).toInt()
            }
        })
    }

    private fun initThicknessRecycler(parent: RelativeLayout) {
        parent.removeAllViews()
        textAdapter = null
        thickAdapter = null

        ThicknessItemAdapter.itemSpacing = (contentWidth * 15f / 280).toInt()
        ThicknessItemAdapter.marginHorizontal = (contentWidth * 20f / 280).toInt()
        ThicknessItemAdapter.iconSize = (contentWidth * 20f / 280).toInt()

        val recyclerView = RecyclerView(parent.context)
        val param = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT)
        param.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE)
        param.leftMargin = ThicknessItemAdapter.marginHorizontal
        param.rightMargin = ThicknessItemAdapter.marginHorizontal
        parent.addView(recyclerView, param)

        recyclerView.layoutManager = AutoMeasureGridLayoutManager(context, thicknessSpanCount)
        thickAdapter = ThicknessItemAdapter(config, this)
        recyclerView.adapter = thickAdapter
        recyclerView.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(outRect: Rect, view: View,
                                        parent: RecyclerView, state: RecyclerView.State) {
                val position = parent.getChildAdapterPosition(view)
                outRect.left = (ThicknessItemAdapter.itemSpacing *
                        position.toFloat() / thicknessSpanCount).toInt()
            }
        })
    }

    private fun initDividers() {
        val margin = (width * 15f / 280).toInt()
        divider1 = findViewById(R.id.agora_option_whiteboard_divider1)
        (divider1.layoutParams as ViewGroup.MarginLayoutParams).let {
            it.leftMargin = margin
            it.rightMargin = margin
            divider1.layoutParams = it
        }

        divider2 = findViewById(R.id.agora_option_whiteboard_divider2)
        (divider2.layoutParams as ViewGroup.MarginLayoutParams).let {
            it.leftMargin = margin
            it.rightMargin = margin
            divider2.layoutParams = it
        }
    }

    private fun showDividers() {
        divider1.visibility = View.VISIBLE
        divider2.visibility = View.VISIBLE
    }

    private fun hideDividers() {
        divider1.visibility = View.GONE
        divider2.visibility = View.GONE
    }

    private fun hideStatusBar(window: Window) {
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = Color.TRANSPARENT
        val flag = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        window.decorView.systemUiVisibility = (flag or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
    }

    /**
     * Adjust current window size to current active appliance type
     */
    private fun adjustPosition(anchor: View, margin: Int) {
        if (hasSubOptions(config.activeAppliance)) {
            adjustPosition(anchor, windowWidth, windowHeight, margin)
        } else {
            adjustPosition(anchor, windowWidth, windowLowHeight, margin)
        }
    }

    private fun adjustPosition(anchor: View, width: Int, height: Int, margin: Int) {
        val window = window
        val params = window!!.attributes
        hideStatusBar(window)

        params.width = width
        params.height = height
        params.gravity = Gravity.BOTTOM or Gravity.END

        val metric = DisplayMetrics()
        window.windowManager.defaultDisplay.getRealMetrics(metric)
        val anchorLoc = IntArray(2)
        anchor.getLocationOnScreen(anchorLoc)

        params.x = metric.widthPixels - anchorLoc[0] + margin
        params.y = metric.heightPixels - (anchorLoc[1] + anchor.height)
        window.attributes = params
    }
}

class ApplianceItemAdapter(context: Context,
                           private val config: AgoraUIDrawingConfig,
                           private val dialog: AgoraUIWhiteboardOptionDialog,
                           private val itemClickListener: ApplianceItemClickListener? = null)
    : RecyclerView.Adapter<ApplianceItemViewHolder>() {

    companion object {
        var marginVertical: Int = 0
        var marginHorizontal: Int = 0
        var iconSize: Int = 0
        var itemSpacing: Int = 0
    }

    private val iconRes = arrayListOf(
        R.drawable.agora_option_whiteboard_dialog_appliance_icon_clicker,
        R.drawable.agora_option_whiteboard_dialog_appliance_icon_selector,
        R.drawable.agora_option_whiteboard_dialog_appliance_icon_text,
        R.drawable.agora_option_whiteboard_dialog_appliance_icon_eraser,
        R.drawable.agora_option_whiteboard_dialog_appliance_icon_laser,
        R.drawable.agora_option_whiteboard_dialog_appliance_icon_pen,
        R.drawable.agora_option_whiteboard_dialog_appliance_icon_line,
        R.drawable.agora_option_whiteboard_dialog_appliance_icon_rect,
        R.drawable.agora_option_whiteboard_dialog_appliance_icon_circle)

    private val appliances = arrayListOf(
        AgoraUIApplianceType.Clicker,
        AgoraUIApplianceType.Select,
        AgoraUIApplianceType.Text,
        AgoraUIApplianceType.Eraser,
        AgoraUIApplianceType.Laser,
        AgoraUIApplianceType.Pen,
        AgoraUIApplianceType.Line,
        AgoraUIApplianceType.Rect,
        AgoraUIApplianceType.Circle)

    private val iconMargin = context.resources.getDimensionPixelSize(R.dimen.margin_small)
    private var lastSelected = -1

    internal var layoutChangedListener: WindowContentLayoutChangedListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ApplianceItemViewHolder {
        return ApplianceItemViewHolder(LayoutInflater.from(parent.context).inflate(
            R.layout.agora_tool_popup_color_item_layout, parent, false))
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: ApplianceItemViewHolder, position: Int) {
        // Let recycler layout manager automatically calculate item width and height
        holder.itemView.layoutParams?.let {
            it.width = iconSize
            it.height = iconSize
            holder.itemView.layoutParams = it
        }

        holder.itemView.setOnClickListener {
            val index = holder.absoluteAdapterPosition
            if (index != lastSelected) {
                val lastAppliance = config.activeAppliance
                config.activeAppliance = appliances[index]
                lastSelected = index
                notifyDataSetChanged()
                AgoraUIWhiteboardOptionDialog.listener?.onApplianceSelected(config.activeAppliance)
                itemClickListener?.onApplianceClicked(config.activeAppliance)
                checkLayoutChanged(lastAppliance, config.activeAppliance)

                if (shouldDismissDialog()) {
                    dialog.dismiss()
                }
            }
        }

        val index = holder.absoluteAdapterPosition
        holder.icon.setImageResource(iconRes[index])
        holder.icon.isActivated = config.activeAppliance == appliances[index]

        (holder.icon.layoutParams as? RelativeLayout.LayoutParams)?.let {
            it.width = RelativeLayout.LayoutParams.MATCH_PARENT
            it.height = RelativeLayout.LayoutParams.MATCH_PARENT
            it.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE)
            holder.icon.layoutParams = it
        }
    }

    private fun shouldDismissDialog(): Boolean {
        return config.activeAppliance == AgoraUIApplianceType.Clicker ||
                config.activeAppliance == AgoraUIApplianceType.Select ||
                config.activeAppliance == AgoraUIApplianceType.Eraser ||
                config.activeAppliance == AgoraUIApplianceType.Laser
    }

    private fun checkLayoutChanged(before: AgoraUIApplianceType,
                                   after: AgoraUIApplianceType) {

        val hasSubContentBefore = AgoraUIWhiteboardOptionDialog.hasSubOptions(before)
        val hasSubContentAfter = AgoraUIWhiteboardOptionDialog.hasSubOptions(after)
        if (hasSubContentBefore != hasSubContentAfter) {
            layoutChangedListener?.onWindowContentLayoutChanged(after, hasSubContentAfter)
        }
    }

    override fun getItemCount(): Int {
        return iconRes.size
    }
}

class ApplianceItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val icon: AppCompatImageView = itemView.findViewById(R.id.agora_tool_popup_color_item_icon)
    init {
        icon.scaleType = ImageView.ScaleType.FIT_XY
    }
}

/**
 * Used to indicate the appliance type has changed to
 * outside world.
 */
interface ApplianceItemClickListener {
    fun onApplianceClicked(type: AgoraUIApplianceType)
}

/**
 * Used to indicate that the size of dialog has changed
 * due to content change, used inside the dialog
 */
internal interface WindowContentLayoutChangedListener {
    fun onWindowContentLayoutChanged(type: AgoraUIApplianceType, hasSubContent: Boolean)
}

class TextSizeItemAdapter(private val config: AgoraUIDrawingConfig,
                          private val dialog: AgoraUIWhiteboardOptionDialog) : RecyclerView.Adapter<TextSizeViewHolder>() {

    companion object {
        var marginHorizontal: Int = 0
        var itemSpacing: Int = 0
        var iconSize: Int = 0
    }

    private val textSizes = listOf(10, 14, 18, 24)
    private var lastSelected = -1

    private val textSizeIcons = listOf(
        R.drawable.agora_option_whiteboard_text_size_1,
        R.drawable.agora_option_whiteboard_text_size_2,
        R.drawable.agora_option_whiteboard_text_size_3,
        R.drawable.agora_option_whiteboard_text_size_4
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TextSizeViewHolder {
        return TextSizeViewHolder(LayoutInflater.from(parent.context).inflate(
            R.layout.agora_tool_popup_color_item_layout, parent, false))
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: TextSizeViewHolder, position: Int) {
        holder.itemView.layoutParams?.let {
            it.width = iconSize
            it.height = iconSize
            holder.itemView.layoutParams = it
        }

        holder.itemView.setOnClickListener {
            val index = holder.absoluteAdapterPosition
            if (index != lastSelected) {
                lastSelected = index
                config.fontSize = textSizes[index]
                notifyDataSetChanged()
                AgoraUIWhiteboardOptionDialog.listener?.onTextSizeSelected(config.fontSize) }
        }

        val index = holder.absoluteAdapterPosition
        holder.icon.setImageResource(textSizeIcons[index])
        holder.icon.isActivated = config.fontSize == textSizes[index]
        (holder.icon.layoutParams as? RelativeLayout.LayoutParams)?.let {
            it.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE)
            it.width = RelativeLayout.LayoutParams.MATCH_PARENT
            it.height = RelativeLayout.LayoutParams.MATCH_PARENT
            holder.icon.layoutParams = it
        }
    }

    override fun getItemCount(): Int {
       return textSizeIcons.size
    }
}

class TextSizeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val icon: AppCompatImageView = itemView.findViewById(R.id.agora_tool_popup_color_item_icon)
    init {
        icon.scaleType = ImageView.ScaleType.FIT_XY
    }
}

class ThicknessItemAdapter(private val config: AgoraUIDrawingConfig,
                           private val dialog: AgoraUIWhiteboardOptionDialog) : RecyclerView.Adapter<ThicknessViewHolder>() {

    companion object {
        var marginHorizontal: Int = 0
        var itemSpacing: Int = 0
        var iconSize: Int = 0
    }

    private val thickValues = listOf(6, 8, 12, 16, 20)
    private val iconSizes = listOf(6, 8, 12, 16, 20)
    private var lastSelected = -1
    private val defaultColor = Color.parseColor("#E1E1EA")

    // When the selected color is white, we need to create a
    // ring icon to indicate the selected state
    private val ringSize = 28
    private val ringWidth = 4
    private val whiteIconSelected = ColorOptions.makeDrawable(
        Color.WHITE, ringSize, defaultColor, ringWidth, Color.WHITE, ringWidth)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ThicknessViewHolder {
        return ThicknessViewHolder(LayoutInflater.from(parent.context).inflate(
            R.layout.agora_tool_popup_color_item_layout, parent, false))
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: ThicknessViewHolder, position: Int) {
        holder.itemView.layoutParams?.let {
            val size = (dialog.windowWidth * 28f / 280).toInt()
            it.width = size
            it.height = size
            holder.itemView.layoutParams = it
        }

        holder.itemView.setOnClickListener {
            val index = holder.absoluteAdapterPosition
            if (index != lastSelected) {
                lastSelected = index
                config.thick = thickValues[index]
                notifyDataSetChanged()
                AgoraUIWhiteboardOptionDialog.listener?.onThicknessSelected(config.thick) }
        }

        val index = holder.absoluteAdapterPosition
        holder.icon.isActivated = config.thick == thickValues[index]
        setIconImage(holder.icon, holder.icon.isActivated, config.color, defaultColor)

        (holder.icon.layoutParams as? RelativeLayout.LayoutParams)?.let {
            it.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE)
            val size = getWeightedIconSize(index)
            it.width = size
            it.height = size
            holder.icon.layoutParams = it
        }
    }

    private fun getWeightedIconSize(index: Int): Int {
        return ((iconSizes[index] / 280f) * dialog.contentWidth).toInt()
    }

    private fun setIconImage(icon: AppCompatImageView, activated: Boolean,
                             color: Int, colorDefault: Int) {
        if (activated && color == Color.WHITE) {
            icon.setImageDrawable(whiteIconSelected)
        } else {
            icon.setImageDrawable(ColorOptions.makeCircleDrawable(if (activated) color else colorDefault))
        }
    }

    override fun getItemCount(): Int {
        return dialog.thicknessSpanCount
    }
}

class ThicknessViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val icon: AppCompatImageView = itemView.findViewById(R.id.agora_tool_popup_color_item_icon)
    init {
        icon.scaleType = ImageView.ScaleType.FIT_XY
    }
}

class ColorPlateItemAdapter(context: Context,
                            private val config: AgoraUIDrawingConfig,
                            private val dialog: AgoraUIWhiteboardOptionDialog)
    : RecyclerView.Adapter<ColorPlateItemViewHolder>() {

    companion object {
        var marginHorizontal: Int = 0
        var marginVertical: Int = 0
        var iconSize: Int = 0
        var layoutHeight: Int = 0
        var itemSpacing: Int = 0
    }

    private val iconColorStrings = context.resources.getStringArray(R.array.agora_tool_color_plate)
    private val colorValues: IntArray = IntArray(iconColorStrings.size)

    private val icons = listOf(
        R.drawable.agora_option_layout_color_item1,
        R.drawable.agora_option_layout_color_item2,
        R.drawable.agora_option_layout_color_item3,
        R.drawable.agora_option_layout_color_item4,
        R.drawable.agora_option_layout_color_item5,
        R.drawable.agora_option_layout_color_item6,
        R.drawable.agora_option_layout_color_item7,
        R.drawable.agora_option_layout_color_item8,
        R.drawable.agora_option_layout_color_item9,
        R.drawable.agora_option_layout_color_item10,
        R.drawable.agora_option_layout_color_item11,
        R.drawable.agora_option_layout_color_item12)

    private var lastSelected = -1

    init {
        for (i in colorValues.indices) {
            colorValues[i] = Color.parseColor(iconColorStrings[i])
        }
    }

    @SuppressLint("InflateParams")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorPlateItemViewHolder {
        return ColorPlateItemViewHolder(LayoutInflater.from(parent.context).inflate(
                R.layout.agora_tool_popup_color_item_layout, parent, false))
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: ColorPlateItemViewHolder, position: Int) {
        holder.itemView.layoutParams?.let {
            it.width = iconSize
            it.height = iconSize
            holder.itemView.layoutParams = it
        }

        holder.itemView.setOnClickListener {
            val index = holder.absoluteAdapterPosition
            if (lastSelected != index) {
                lastSelected = index
                config.color = colorValues[index]
                notifyDataSetChanged()
                notifyColorChanged()
                AgoraUIWhiteboardOptionDialog.listener?.onColorSelected(config.color)
            }
        }

        val index = holder.absoluteAdapterPosition
        holder.icon.setImageResource(icons[index])
        holder.icon.isActivated = config.color == colorValues[index]

        (holder.icon.layoutParams as? RelativeLayout.LayoutParams)?.let {
            it.width = RelativeLayout.LayoutParams.MATCH_PARENT
            it.height = RelativeLayout.LayoutParams.MATCH_PARENT
            holder.icon.layoutParams = it
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun notifyColorChanged() {
        dialog.thickAdapter?.notifyDataSetChanged()
        dialog.textAdapter?.notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return iconColorStrings.size
    }
}

class ColorPlateItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val icon: AppCompatImageView = itemView.findViewById(R.id.agora_tool_popup_color_item_icon)
    init {
        icon.scaleType = ImageView.ScaleType.FIT_XY
    }
}

interface AgoraUIWhiteboardOptionListener {
    fun onApplianceSelected(type: AgoraUIApplianceType)

    fun onColorSelected(color: Int)

    fun onTextSizeSelected(size: Int)

    fun onThicknessSelected(thick: Int)
}

class AutoMeasureGridLayoutManager(context: Context, spanCount: Int) : GridLayoutManager(context, spanCount) {
    override fun isAutoMeasureEnabled(): Boolean {
        return true
    }
}