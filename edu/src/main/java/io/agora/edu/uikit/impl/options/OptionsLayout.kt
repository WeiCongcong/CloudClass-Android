package io.agora.edu.uikit.impl.options

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.appcompat.widget.AppCompatImageView
import io.agora.edu.R
import io.agora.edu.core.context.*
import io.agora.edu.core.internal.education.impl.Constants.Companion.AgoraLog
import io.agora.edu.extensions.widgets.UiWidgetManager
import io.agora.edu.uikit.handlers.UserHandler
import io.agora.edu.uikit.impl.chat.ChatPopupWidget
import io.agora.edu.uikit.impl.chat.ChatPopupWidgetListener
import io.agora.edu.uikit.impl.chat.tabs.ChatTabConfig
import io.agora.edu.uikit.impl.container.AbsUIContainer
import io.agora.edu.uikit.impl.container.AgoraUIConfig
import io.agora.edu.uikit.impl.tool.AgoraUIApplianceType
import io.agora.edu.uikit.impl.users.RosterType
import io.agora.edu.uikit.interfaces.protocols.AgoraUIDrawingConfig
import io.agora.edu.uikit.util.TextPinyinUtil

/**
 * Business-related options on the right side of container
 */
class OptionsLayout : LinearLayout, View.OnClickListener {
    var popUpListener: OptionPopupListener? = null

    private var eduContext: EduContextPool? = null

    private lateinit var settingItem: DeviceSettingLayoutPopupItem
    private var toolboxItem: ToolBoxLayoutPopupItem? = null
    private var rosterItem: RosterLayoutPopupItem? = null
    private var chatItem: ChatLayoutPopupItem? = null
    private var handsUpItem: OptionsLayoutHandsUpItem? = null
    private var boardToolItem: OptionsLayoutWhiteboardItem? = null

    // Current active option layout item that has a popup
    // and the popup shows.
    private var curPopupItem: OptionsLayoutPopupItem? = null

    private var role = EduContextUserRole.Student

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    companion object {
        var listener: OptionsLayoutListener? = null
    }

    private val userHandler = object : UserHandler() {
        override fun onUserListUpdated(list: MutableList<EduContextUserDetailInfo>) {
            rosterItem?.onUserListUpdated(list)
        }
    }

    fun init(eduContext: EduContextPool?, parent: RelativeLayout, role: EduContextUserRole,
             width: Int, right: Int, mode: OptionLayoutMode? = OptionLayoutMode.Joint,
             container: AbsUIContainer, widgetManager: UiWidgetManager? = null,
             tabConfigs: List<ChatTabConfig>? = null) {
        this.eduContext = eduContext
        this.role = role
        orientation = VERTICAL

        val isTeacher = role != EduContextUserRole.Student

        settingItem = DeviceSettingLayoutPopupItem(parent, width, right, eduContext, this)
        addPopupButton(settingItem, width, R.drawable.agora_option_icon_setting)

        if (isTeacher) {
            ToolBoxLayoutPopupItem(parent, width, right, eduContext, this).let {
                addPopupButton(it, width, R.drawable.agora_option_icon_toolbox)
                toolboxItem = it
            }
        }

        rosterItem = RosterLayoutPopupItem(parent, width, right, eduContext, role, this)
        addPopupButton(rosterItem!!, width, R.drawable.agora_option_icon_roster)

        widgetManager?.let {
            chatItem = ChatLayoutPopupItem(parent, container, width, right, eduContext, it)
            addPopupButton(chatItem!!, width, R.drawable.agora_option_icon_chat)
        }

        addHandsUpButton(context, width, role,
                R.drawable.agora_options_icon_handsup_default,
                eduContext)

        if (mode == OptionLayoutMode.Joint) {
            // Whiteboard tool item is a part of options layout,
            // its right margin and bottom margin is 0
            addWhiteboardToolButton(parent, width, 0, eduContext)
        }

        grantPermission(this.role == EduContextUserRole.Teacher)

        val params = RelativeLayout.LayoutParams(width,
                RelativeLayout.LayoutParams.WRAP_CONTENT)
        params.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE)
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE)
        params.rightMargin = right
        parent.addView(this, params)

        eduContext?.userContext()?.addHandler(userHandler)
    }

    private fun addPopupButton(item: OptionsLayoutPopupItem, width: Int, res: Int) {
        item.initIcon()
        item.setOnClickListener(this)
        item.setIconResource(res)
        addView(item, width, width)
    }

    private fun addHandsUpButton(context: Context, width: Int,
                                 role: EduContextUserRole, res: Int,
                                 eduContext: EduContextPool?) {
        OptionsLayoutHandsUpItem(context, width, role, eduContext).let {
            handsUpItem = it
            it.initIcon()
            it.setIconResource(res)
            addView(it, width, width)
        }
    }

    private fun addWhiteboardToolButton(parent: RelativeLayout,
                                        size: Int,
                                        popupMargin: Int,
                                        eduContext: EduContextPool?) {
        OptionsLayoutWhiteboardItem(parent, size, 0, 0, popupMargin, eduContext).let {
            boardToolItem = it
            it.initIcon()
            it.setOnClickListener(this)
            addView(it, size, size)
        }
    }

    override fun onClick(v: View?) {
        v?.let {
            checkPopupState(it)
            checkDialogState(it)
        }
    }

    private fun grantPermission(granted: Boolean) {
        if (this.role == EduContextUserRole.Student) {
            boardToolItem?.let {
                it.visibility = if (granted) VISIBLE else GONE
            }
        }
    }

    private fun checkPopupState(view: View) {
        when (view) {
            settingItem -> {
                changePopupItemStateAndCallback(settingItem)
            }
            toolboxItem -> {
                changePopupItemStateAndCallback(toolboxItem!!)
            }
            rosterItem -> {
                changePopupItemStateAndCallback(rosterItem!!)
            }
            chatItem -> {
                changePopupItemStateAndCallback(chatItem!!)
            }
        }
    }

    private fun checkDialogState(view: View) {
        when (view) {
            boardToolItem -> {
                dismissCurrentPopup()
                boardToolItem!!.toggleDialog()
            }
        }
    }

    fun dismissCurrentPopup() {
        curPopupItem?.let {
            popUpListener?.onPopupDismiss(it.getType())
            it.isActivated = false
            it.dismissPopup()
            curPopupItem = null
        }
    }

    private fun changePopupItemStateAndCallback(item: OptionsLayoutPopupItem) {
        if (item == curPopupItem) {
            popUpListener?.onPopupDismiss(item.getType())
            curPopupItem?.isActivated = false
            curPopupItem?.dismissPopup()
            curPopupItem = null
        } else if (curPopupItem != null) {
            popUpListener?.onPopupDismiss(curPopupItem!!.getType())
            curPopupItem?.dismissPopup()
            curPopupItem?.isActivated = false
            curPopupItem = null
            popUpListener?.onPopupShow(item.getType())
            curPopupItem = item
            curPopupItem?.showPopup()
            curPopupItem?.isActivated = true
        } else {
            popUpListener?.onPopupShow(item.getType())
            curPopupItem = item
            curPopupItem?.showPopup()
            curPopupItem?.isActivated = true
        }
    }

    fun clear() {
        curPopupItem?.dismissPopup()
        curPopupItem?.isActivated = false
        curPopupItem = null
    }

    fun release() {
        this.eduContext?.userContext()?.removeHandler(userHandler)
    }
}

enum class OptionLayoutMode {
    // Only part of the options are inserted into option layout.
    // How to set the option items is determined by business
    // requirements
    Separate,

    // All options are inserted into option layout
    Joint
}

interface OptionPopupListener {
    fun onPopupShow(item: OptionItemType)

    fun onPopupDismiss(item: OptionItemType)
}

enum class OptionItemType {
    Setting, Toolbox, Roster, Chat, HandsUp, Whiteboard
}

@SuppressLint("ViewConstructor")
open class OptionsLayoutItem(context: Context?,
                             private val type: OptionItemType,
                             private val itemWidth: Int) : RelativeLayout(context) {
    protected val icon: AppCompatImageView = AppCompatImageView(getContext())

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(itemWidth, itemWidth)
    }

    fun initIcon() {
        icon.scaleType = ImageView.ScaleType.FIT_XY
        addView(icon, itemWidth, itemWidth)
    }

    fun setIconResource(res: Int) {
        icon.setImageResource(res)
    }

    fun getType(): OptionItemType {
        return type
    }
}

/**
 * @param container the parent container of the popup window.
 * Note: it is not the parent of the popup item, which should
 * be the options layout itself.
 */
@SuppressLint("ViewConstructor")
abstract class OptionsLayoutPopupItem(private val container: RelativeLayout,
                                      type: OptionItemType, width: Int)
    : OptionsLayoutItem(container.context, type, width) {

    private var popupView: ViewGroup? = null
    private val scaleFactor = 1.1f

    init {
        addTouchScaling()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun addTouchScaling() {
        setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    v.scaleX = scaleFactor
                    v.scaleY = scaleFactor
                }
                MotionEvent.ACTION_UP -> {
                    v.scaleX = 1f
                    v.scaleY = 1f
                }
            }

            false
        }
    }

    open fun showPopup() {
        dismissPopup()
        this.post {
            onCreatePopupView().let {
                popupView = it
                val rect = onPopupRect()
                val param = LayoutParams(
                        LayoutParams.WRAP_CONTENT,
                        LayoutParams.WRAP_CONTENT)
                param.addRule(ALIGN_PARENT_RIGHT, TRUE)
                param.topMargin = rect.top
                param.rightMargin = rect.right
                param.width = rect.width()
                param.height = rect.height()
                container.addView(it, param)
            }
        }
    }

    fun dismissPopup() {
        onDismissPopupView()
    }

    abstract fun onCreatePopupView(): ViewGroup

    /**
     * @return a pair of coordinates in the parent container,
     * the first of which is the top margin, and the second
     * is the right margin.
     */
    abstract fun onPopupRect(): Rect

    fun getPopupView(): ViewGroup? {
        return popupView
    }

    open fun onDismissPopupView() {
        this.post {
            popupView?.let { pop ->
                pop.parent?.let { parent ->
                    (parent as? ViewGroup)?.removeView(pop)
                }
            }
            popupView = null
        }
    }
}

@SuppressLint("ViewConstructor")
class DeviceSettingLayoutPopupItem(private val container: RelativeLayout,
                                   private val itemWidth: Int,
                                   private val rightMargin: Int,
                                   private val eduContext: EduContextPool?,
                                   private val optionLayout: OptionsLayout)
    : OptionsLayoutPopupItem(container, OptionItemType.Setting, itemWidth) {

    // The fixed width over height ratio of the popup window
    private val ratio = 200 / 281f
    private val shadow = 10

    override fun onCreatePopupView(): ViewGroup {
        AgoraUIDeviceSettingPopUp(container.context).let {
            it.setEduContextPool(eduContext)
            it.leaveRoomRunnable = Runnable {
                isActivated = false
                optionLayout.clear()
                OptionsLayout.listener?.onLeave()
            }

            it.uploadLogRunnable = Runnable {
                isActivated = false
                optionLayout.clear()
                OptionsLayout.listener?.onUploadLog()
            }
            return it
        }
    }

    override fun onPopupRect(): Rect {
        val containerH = container.height
        if (AgoraUIConfig.isLargeScreen) {
            // For tablets, device setting popup window aligns
            // its top to the top of current item button.
            // Popup window and the button may not be siblings,
            // thus they cannot directly align to each other.
            val right = itemWidth + rightMargin
            val containerPos = intArrayOf(0, 0)
            container.getLocationOnScreen(containerPos)
            val itemPos = intArrayOf(0, 0)
            this.getLocationOnScreen(itemPos)
            val top = itemPos[1] - containerPos[1]
            val height = calculateTabletPopupHeight(containerH)
            val width = (height * ratio).toInt()
            initView(width, height, shadow)
            return Rect(right - width, top, right, top - height)
        } else {
            // For phones, setting window stays at the
            // vertical center of the container, with
            // a margin to the top and bottom
            val margin = (containerH / 16f).toInt()
            val height = containerH - margin * 2
            val width = (height * ratio).toInt()
            val right = itemWidth + rightMargin
            val left = right - width
            initView(width, height, shadow)
            return Rect(left, margin, right, containerH - margin)
        }
    }

    /**
     * @param containerH the height of popup window's container
     */
    private fun calculateTabletPopupHeight(containerH: Int): Int {
        if (containerH <= 800) {
            return (containerH * 2f / 3).toInt()
        } else if (containerH <= 1080) {
            return (containerH * 3f / 5).toInt()
        } else if (containerH <= 1600) {
            return containerH / 3
        } else {
            return 800
        }
    }

    private fun initView(width: Int, height: Int, shadow: Int) {
        (getPopupView() as? AgoraUIDeviceSettingPopUp)?.initView(container, width, height, shadow)
    }
}

@SuppressLint("ViewConstructor")
class ToolBoxLayoutPopupItem(private val container: RelativeLayout,
                             private val itemWidth: Int,
                             private val rightMargin: Int,
                             private val eduContextPool: EduContextPool?,
                             private val optionLayout: OptionsLayout)
    : OptionsLayoutPopupItem(container, OptionItemType.Toolbox, itemWidth) {

    // The fixed width over height ratio of the popup window
    private val ratio = 243f / 131f
    private val shadow = 10

    override fun onCreatePopupView(): ViewGroup {
        return AgoraUIToolBoxPopUp(container.context).apply {
            this.eduContext = eduContextPool
        }
    }

    override fun onPopupRect(): Rect {
        val containerH = container.height
        if (AgoraUIConfig.isLargeScreen) {
            val right = itemWidth + rightMargin
            val containerPos = intArrayOf(0, 0)
            container.getLocationOnScreen(containerPos)
            val itemPos = intArrayOf(0, 0)
            this.getLocationOnScreen(itemPos)
            val top = itemPos[1] - containerPos[1]
            val height = calculateTabletPopupHeight(containerH)
            val width = (height * ratio).toInt()
            initView(width, height, shadow)
            return Rect(right - width, top, right, top - height)
        } else {
            val margin = (containerH / 5f).toInt()
            val height = containerH - margin * 2
            val width = (height * ratio).toInt()
            val right = itemWidth + rightMargin
            val left = right - width
            initView(width, height, shadow)
            return Rect(left, margin, right, containerH - margin)
        }
    }

    /**
     * @param containerH the height of popup window's container
     */
    private fun calculateTabletPopupHeight(containerH: Int): Int {
        if (containerH <= 800) {
            return (containerH * 2f / 3).toInt()
        } else if (containerH <= 1080) {
            return (containerH * 3f / 5).toInt()
        } else if (containerH <= 1600) {
            return containerH / 3
        } else {
            return 800
        }
    }

    private fun initView(width: Int, height: Int, shadow: Int) {
        (getPopupView() as? AgoraUIToolBoxPopUp)?.initView(container, width, height, shadow)
    }
}

@SuppressLint("ViewConstructor")
class RosterLayoutPopupItem(private val container: RelativeLayout,
                            private val itemWidth: Int,
                            private val rightMargin: Int,
                            private val eduContext: EduContextPool?,
                            private val role: EduContextUserRole,
                            private val optionLayout: OptionsLayout)
    : OptionsLayoutPopupItem(container, OptionItemType.Roster, itemWidth) {

    private var userList: MutableList<EduContextUserDetailInfo> = mutableListOf()
    private var popup: AgoraUIRosterPopUp? = null

    override fun onCreatePopupView(): ViewGroup {
        val view = AgoraUIRosterPopUp(container.context)
        view.setType(RosterType.SmallClass)
        view.setEduContext(eduContext)
        view.initView(container, role)
        popup = view
        onUserListUpdated(userList)
        return view
    }

    override fun onPopupRect(): Rect {
        val right = itemWidth + rightMargin
        var left = 0
        var top = 0
        var bottom = 0

        if (AgoraUIConfig.isLargeScreen) {
            // On tablets, roster window stays at the position
            // where the item button is in the middle of the
            // window height vertically.
            val height = getTabletPopupHeight(container.height)
            val width = getPopupWidth(height)
            left = right - width

            val containerPos = intArrayOf(0, 0)
            container.getLocationOnScreen(containerPos)
            val itemPos = intArrayOf(0, 0)
            this.getLocationOnScreen(itemPos)
            top = itemPos[1] + this.height / 2 - height / 2 - containerPos[1]
            bottom = top + height
        } else {
            // On phones, roster window stays in the center
            // of container vertically, with a certain
            // top and bottom margin
            val margin = container.height / 10
            top = margin
            bottom = container.height - margin
            val height = container.height - margin * 2
            val width = getPopupWidth(height)
            left = right - width
        }
        return Rect(left, top, right, bottom)
    }

    private fun getTabletPopupHeight(parentHeight: Int): Int {
        if (parentHeight <= 800) {
            return (parentHeight * 2f / 3).toInt()
        } else if (parentHeight <= 1200) {
            return parentHeight / 3
        } else {
            return 480
        }
    }

    private fun getPopupWidth(height: Int): Int {
        return if (role == EduContextUserRole.Student) {
            (height * 7f / 5).toInt()
        } else {
            (height * 5f / 3).toInt()
        }
    }

    override fun onDismissPopupView() {
        super.onDismissPopupView()
        popup = null
    }

    fun onUserListUpdated(list: MutableList<EduContextUserDetailInfo>) {
        userList = sort(list)
        popup?.onUserListUpdated(userList)
    }
}

fun sort(list: MutableList<EduContextUserDetailInfo>): MutableList<EduContextUserDetailInfo> {
    var coHosts = mutableListOf<EduContextUserDetailInfo>()
    val users = mutableListOf<EduContextUserDetailInfo>()
    list.forEach {
        if (it.coHost) {
            coHosts.add(it)
        } else {
            users.add(it)
        }
    }
    coHosts = sort2(coHosts)
    val list1 = sort2(users)
    coHosts.addAll(list1)
    return coHosts
}

fun sort2(list: MutableList<EduContextUserDetailInfo>): MutableList<EduContextUserDetailInfo> {
    val numList = mutableListOf<EduContextUserDetailInfo>()
    val listIterator = list.iterator()
    while (listIterator.hasNext()) {
        val info = listIterator.next()
        val tmp = info.user.userName[0]
        if (!TextPinyinUtil.isChinaString(tmp.toString()) && tmp.toInt() in 48..57) {
            numList.add(info)
            listIterator.remove()
        }
    }

    numList.sortWith(object : Comparator<EduContextUserDetailInfo> {
        override fun compare(o1: EduContextUserDetailInfo?, o2: EduContextUserDetailInfo?): Int {
            if (o1 == null) {
                return -1
            }
            if (o2 == null) {
                return 1
            }
            return o1.user.userName.compareTo(o2.user.userName)
        }
    })

    list.sortWith(object : Comparator<EduContextUserDetailInfo> {
        override fun compare(o1: EduContextUserDetailInfo?, o2: EduContextUserDetailInfo?): Int {
            if (o1 == null) {
                return -1
            }
            if (o2 == null) {
                return 1
            }
            var ch1 = ""
            if (TextPinyinUtil.isChinaString(o1.user.userName)) {
                TextPinyinUtil.getPinyin(o1.user.userName).let {
                    ch1 = it
                }
            } else {
                ch1 = o1.user.userName
            }
            var ch2 = ""
            if (TextPinyinUtil.isChinaString(o2.user.userName)) {
                TextPinyinUtil.getPinyin(o2.user.userName).let {
                    ch2 = it
                }
            } else {
                ch2 = o2.user.userName
            }
            return ch1.compareTo(ch2)
        }
    })
    list.addAll(numList)
    return list
}

/**
 * Chat widget popups have different show/dismiss process
 * than other popup windows
 */
@SuppressLint("ViewConstructor")
class ChatLayoutPopupItem(private val parent: RelativeLayout,
                          private val container: AbsUIContainer,
                          private val itemWidth: Int,
                          private val rightMargin: Int,
                          private val eduContext: EduContextPool?,
                          private val widgetManager: UiWidgetManager,
                          private val tagConfigs: List<ChatTabConfig>? = null)
    : OptionsLayoutPopupItem(parent, OptionItemType.Chat, itemWidth) {

    private val ratio = 340 / 430f
    private lateinit var window: ChatPopupWidget
    private var layout: ViewGroup? = null

    init {
        val chat = widgetManager.create(UiWidgetManager.DefaultWidgetId.Chat.name, eduContext)
        (chat as? ChatPopupWidget)?.let {
            window = it
            window.setContainer(container)
            window.chatWidgetListener = object : ChatPopupWidgetListener {
                override fun onChatPopupWidgetClosed() {
                    this@ChatLayoutPopupItem.isActivated = false
                }
            }

            try {
                layout = onCreatePopupView()
            } catch (e: RuntimeException) {
                AgoraLog.e("Init chat layout popup item fails: ${e.message}")
            }
        }

        if (!::window.isInitialized || layout == null) {
            AgoraLog.e("Init chat layout item fails, do you register a chat widget correctly?")
        }
    }

    @Throws(RuntimeException::class)
    override fun onCreatePopupView(): ViewGroup {
        if (::window.isInitialized) {
            window.setEduContext(eduContext)
            window.init(parent, 0, 0, 0, 0)
            window.getLayout()?.let {
                return it
            }
            throw RuntimeException("No layout initialized in chat window: id $window")
        } else {
            throw RuntimeException("No layout initialized in chat window: id $window")
        }
    }

    override fun onPopupRect(): Rect {
        val containerH = parent.height
        val containerW = parent.width

        val top: Int
        val bottom: Int
        val left: Int
        val right: Int

        if (AgoraUIConfig.isLargeScreen) {
            val height = calculateTabletPopupHeight(containerH)
            val width = (height * ratio).toInt()
            val rightMargin = itemWidth + rightMargin
            top = (containerH - height) / 2
            right = containerW - rightMargin
            left = right - width
            bottom = containerH - top
        } else {
            top = (containerH / 16f).toInt()
            val height = containerH - top * 2
            val width = (height * ratio).toInt()
            right = containerW - rightMargin - itemWidth
            left = right - width
            bottom = containerH - top
        }

        return Rect(left, top, right, bottom)
    }

    private fun calculateTabletPopupHeight(containerH: Int): Int {
        return (containerH * 2f / 3).toInt()
    }

    override fun showPopup() {
        layout?.let { layout ->
            layout.setBackgroundColor(Color.CYAN)
            window.setRect(onPopupRect())
        }
    }

    override fun onDismissPopupView() {
        // Because of the possible implementations of chat widget,
        // we do not remove the pop window layout from container,
        // but we set the layout to zero size instead.
        layout?.let { _ ->
            window.setRect(Rect(0, 0, 0, 0))
        }
    }
}

@SuppressLint("ViewConstructor")
class OptionsLayoutHandsUpItem(context: Context?,
                               width: Int,
                               val role: EduContextUserRole,
                               val eduContext: EduContextPool?)
    : OptionsLayoutItem(context, OptionItemType.HandsUp, width) {

}

@SuppressLint("ViewConstructor")
class OptionsLayoutWhiteboardItem(private val container: RelativeLayout,
                                  private val size: Int,
                                  private val rightMargin: Int,
                                  private val bottomMargin: Int,
                                  private val popupMargin: Int,
                                  val eduContext: EduContextPool?)
    : OptionsLayoutItem(container.context, OptionItemType.Whiteboard, size) {
    private val scaleFactor = 1.1f

    private var dialogWidth = 0
    private var dialogHeight = 0
    private var dialog: AgoraUIWhiteboardOptionDialog? = null
    private var config = AgoraUIDrawingConfig()

    private val applianceIconResource = mutableMapOf<AgoraUIApplianceType, Int>()

    private val whiteboardDialogListener = object : AgoraUIWhiteboardOptionListener {
        override fun onApplianceSelected(type: AgoraUIApplianceType) {
            setConfigIcon()
            eduContext?.whiteboardContext()?.selectAppliance(toWhiteboardApplianceType(type))
        }

        override fun onColorSelected(color: Int) {
            eduContext?.whiteboardContext()?.selectColor(color)
        }

        override fun onTextSizeSelected(size: Int) {
            eduContext?.whiteboardContext()?.selectFontSize(size)
        }

        override fun onThicknessSelected(thick: Int) {
            eduContext?.whiteboardContext()?.selectThickness(thick)
        }
    }

    private fun toWhiteboardApplianceType(type: AgoraUIApplianceType): WhiteboardApplianceType {
        return when (type) {
            AgoraUIApplianceType.Select -> WhiteboardApplianceType.Select
            AgoraUIApplianceType.Text -> WhiteboardApplianceType.Text
            AgoraUIApplianceType.Pen -> WhiteboardApplianceType.Pen
            AgoraUIApplianceType.Line -> WhiteboardApplianceType.Line
            AgoraUIApplianceType.Rect -> WhiteboardApplianceType.Rect
            AgoraUIApplianceType.Circle -> WhiteboardApplianceType.Circle
            AgoraUIApplianceType.Eraser -> WhiteboardApplianceType.Eraser
            AgoraUIApplianceType.Clicker -> WhiteboardApplianceType.Clicker
            AgoraUIApplianceType.Laser -> WhiteboardApplianceType.Laser
        }
    }

    init {
        initApplianceIconResource()
        initConfig(config)
        setConfigIcon()
        addTouchScaling()
        AgoraUIWhiteboardOptionDialog.listener = whiteboardDialogListener
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun addTouchScaling() {
        setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    v.scaleX = scaleFactor
                    v.scaleY = scaleFactor
                }
                MotionEvent.ACTION_UP -> {
                    v.scaleX = 1f
                    v.scaleY = 1f
                }
            }

            false
        }
    }

    /**
     * Option layout needs its own drawing config initial values
     */
    private fun initConfig(config: AgoraUIDrawingConfig) {
        config.activeAppliance = AgoraUIApplianceType.Clicker
        config.fontSize = 18

        eduContext?.whiteboardContext()?.selectAppliance(
            toWhiteboardApplianceType(config.activeAppliance))
        eduContext?.whiteboardContext()?.selectFontSize(config.fontSize)
    }

    private fun setConfigIcon() {
        applianceIconResource[config.activeAppliance]?.let { setIconResource(it) }
    }

    private fun initApplianceIconResource() {
        applianceIconResource[AgoraUIApplianceType.Clicker] =
            R.drawable.agora_option_layout_whiteboard_appliance_icon_clicker
        applianceIconResource[AgoraUIApplianceType.Select] =
            R.drawable.agora_option_layout_whiteboard_appliance_icon_selector
        applianceIconResource[AgoraUIApplianceType.Text] =
            R.drawable.agora_option_layout_whiteboard_appliance_icon_text
        applianceIconResource[AgoraUIApplianceType.Eraser] =
            R.drawable.agora_option_layout_whiteboard_appliance_icon_eraser
        applianceIconResource[AgoraUIApplianceType.Laser] =
            R.drawable.agora_option_layout_whiteboard_appliance_icon_laser
        applianceIconResource[AgoraUIApplianceType.Pen] =
            R.drawable.agora_option_layout_whiteboard_appliance_icon_pen
        applianceIconResource[AgoraUIApplianceType.Line] =
            R.drawable.agora_option_layout_whiteboard_appliance_icon_line
        applianceIconResource[AgoraUIApplianceType.Rect] =
            R.drawable.agora_option_layout_whiteboard_appliance_icon_rect
        applianceIconResource[AgoraUIApplianceType.Circle] =
            R.drawable.agora_option_layout_whiteboard_appliance_icon_circle
    }

    private fun calculateDialogSize() {
        if (AgoraUIConfig.isLargeScreen) {
            // On tablets, roster window stays at the position
            // where the item button is in the middle of the
            // window height vertically.
            dialogHeight = getTabletPopupHeight(container.height)
            dialogWidth = getPopupWidth(dialogHeight)
        } else {
            // On phones, roster window stays in the center
            // of container vertically, with a certain
            // top and bottom margin
            val margin = container.height / 10
            dialogHeight = container.height - margin * 2
            dialogWidth = getPopupWidth(dialogHeight)
        }
    }

    private fun getTabletPopupHeight(parentHeight: Int): Int {
        if (parentHeight <= 800) {
            return (parentHeight * 2f / 3).toInt()
        } else if (parentHeight <= 1200) {
            return parentHeight / 3
        } else {
            return 480
        }
    }

    private fun getPopupWidth(height: Int): Int {
        return (height * 28f / 31).toInt()
    }

    fun toggleDialog() {
        if (dialog?.isShowing == true) {
            dismiss()
        } else {
            showDialog()
        }
    }

    fun showDialog() {
        if (dialog?.isShowing == true) {
            return
        }

        calculateDialogSize()
        dialog = AgoraUIWhiteboardOptionDialog(
            container.context, dialogWidth, dialogHeight, config)
        dialog?.show(this, rightMargin)
    }

    fun dismiss() {
        if (dialog != null && dialog?.isShowing == true) {
            dialog?.dismiss()
            dialog = null
        }
    }
}

interface OptionsLayoutListener {
    fun onLeave()

    fun onUploadLog()

    fun onKickout(userId: String, userName: String)
}