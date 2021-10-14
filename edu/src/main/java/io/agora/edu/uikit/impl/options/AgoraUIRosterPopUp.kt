package io.agora.edu.uikit.impl.options

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckedTextView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.core.view.forEach
import androidx.recyclerview.widget.*
import io.agora.edu.R
import io.agora.edu.core.context.*
import io.agora.edu.uikit.impl.container.AgoraUIConfig
import io.agora.edu.uikit.impl.users.RosterType
import kotlin.math.min

class AgoraUIRosterPopUp : RelativeLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attr: AttributeSet) : super(context, attr)
    constructor(context: Context, attr: AttributeSet, defStyleAttr: Int) : super(context, attr, defStyleAttr)

    private val rewardCount = 1
    private val tag = "AgoraUIRosterPopUp"

    private var parent: ViewGroup? = null

    private var recyclerView: RecyclerView? = null
    private var userListAdapter: UserListAdapter? = null
    private var tvTeacherName: TextView? = null

    private var eduContext: EduContextPool? = null
    private var rosterType = RosterType.SmallClass
    private var role: EduContextUserRole = EduContextUserRole.Student

    fun setEduContext(eduContextPool: EduContextPool?) {
        this.eduContext = eduContextPool
    }

    fun initView(parent: ViewGroup, role: EduContextUserRole) {
        this.parent = parent
        this.role = role
        LayoutInflater.from(parent.context).inflate(getLayoutRes(this.rosterType), this)

        recyclerView = findViewById(R.id.recycler_view)
        tvTeacherName = findViewById(R.id.tv_teacher_name)

        if (role == EduContextUserRole.Student) {
            findViewById<View>(R.id.userlist_title_kickout)?.visibility = GONE
        }

        userListAdapter = UserListAdapter(object : UserItemClickListener {
            override fun onUserCoHostStateChanged(item: EduContextUserDetailInfo, isCoHost: Boolean) {
                eduContext?.userContext()?.updateCoHost(item.user.userUuid, isCoHost)
            }

            override fun onAccessStateChanged(item: EduContextUserDetailInfo, hasAccess: Boolean) {
                eduContext?.userContext()?.updateBoardGranted(item.user.userUuid, hasAccess)
            }

            override fun onCameraEnabled(item: EduContextUserDetailInfo, enabled: Boolean) {
                if (item.isSelf) {
                    eduContext?.userContext()?.muteVideo(!enabled)
                } else {
                    eduContext?.userContext()?.muteRemoteVideo(item.streamUuid, !enabled)
                }
            }

            override fun onMicEnabled(item: EduContextUserDetailInfo, enabled: Boolean) {
                if (item.isSelf) {
                    eduContext?.userContext()?.muteAudio(!enabled)
                } else {
                    eduContext?.userContext()?.muteRemoteAudio(item.streamUuid, !enabled)
                }
            }

            override fun onReward(item: EduContextUserDetailInfo, count: Int) {
                eduContext?.userContext()?.rewardUsers(item.user.userUuid, count)
            }

            override fun onUserKickout(item: EduContextUserDetailInfo) {
                OptionsLayout.listener?.onKickout(item.user.userUuid, item.user.userName)
            }
        })

        recyclerView?.addItemDecoration(
            DividerItemDecoration(context, DividerItemDecoration.VERTICAL).apply {
                setDrawable(ContextCompat.getDrawable(context, R.drawable.agora_userlist_divider)!!)
            })

        recyclerView?.addItemDecoration(object : RecyclerView.ItemDecoration() {
            val itemHeight = context.resources.getDimensionPixelSize(R.dimen.agora_userlist_row_height)
            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                val layoutParams = view.layoutParams
                layoutParams.width = parent.measuredWidth
                layoutParams.height = itemHeight
                view.layoutParams = layoutParams
                super.getItemOffsets(outRect, view, parent, state)
            }
        })

        // remove the animator when refresh item
        recyclerView?.itemAnimator?.addDuration = 0
        recyclerView?.itemAnimator?.changeDuration = 0
        recyclerView?.itemAnimator?.moveDuration = 0
        recyclerView?.itemAnimator?.removeDuration = 0
        (recyclerView?.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false

        recyclerView?.adapter = userListAdapter
    }

    fun setType(type: RosterType) {
        this.rosterType = type
    }

    private fun getLayoutRes(type: RosterType) = when (type) {
        RosterType.SmallClass -> R.layout.agora_userlist_dialog_layout1
        RosterType.LargeClass -> R.layout.agora_userlist_largeclass_dialog_layout
    }

    private fun getLayoutWidth(type: RosterType): Int = resources.getDimensionPixelSize(
        when (type) {
            RosterType.SmallClass -> R.dimen.agora_userlist_dialog_width
            RosterType.LargeClass -> R.dimen.agora_userlist_largeclass_dialog_width
        })

    fun getLayoutWidth(): Int {
        return getLayoutWidth(rosterType)
    }

    private fun getLayoutHeight(type: RosterType): Int = resources.getDimensionPixelSize(
        when (type) {
            RosterType.SmallClass -> R.dimen.agora_userlist_dialog_height
            RosterType.LargeClass -> R.dimen.agora_userlist_dialog_height
        }
    )

    fun getLayoutHeight(): Int {
        return getLayoutHeight(rosterType)
    }

    fun dismiss() {
        parent?.let { parent ->
            var contains = false
            parent.forEach {
                if (it == this) contains = true
            }
            if (contains) parent.removeView(this)
            this.removeAllViews()
        }
    }

    fun onUserListUpdated(list: MutableList<EduContextUserDetailInfo>) {
        updateUserListAdapter(list)
    }

    private fun updateUserListAdapter(list: MutableList<EduContextUserDetailInfo>) {
        val studentList = mutableListOf<EduContextUserDetailInfo>()
        list.forEach { item ->
            if (item.user.role == EduContextUserRole.Student) {
                studentList.add(item)
            } else if (item.user.role == EduContextUserRole.Teacher) {
                updateTeacher(item)
            }
        }

        post { userListAdapter?.submitList(ArrayList(studentList)) }
    }

    private fun updateTeacher(info: EduContextUserDetailInfo) {
        tvTeacherName?.post { tvTeacherName?.text = info.user.userName }
    }

    private fun updateStudent(info: EduContextUserDetailInfo) {
        val index = findIndex(info)
        if (index >= 0) {
            userListAdapter?.currentList?.set(index, info)
            userListAdapter?.notifyItemChanged(index)
        }
    }

    private fun findIndex(info: EduContextUserDetailInfo): Int {
        var index = 0
        var foundIndex = -1;
        for (item in userListAdapter?.currentList!!) {
            if (item.user.userUuid == info.user.userUuid) {
                foundIndex = index
                break
            }
            index++
        }
        return foundIndex
    }

    @SuppressLint("InflateParams")
    private fun createItemViewHolder(type: RosterType,
                                     parent: ViewGroup,
                                     listener: UserItemClickListener
    ): BaseUserHolder {
        // Roster popup has a slightly different UI design so it
        // uses another layout xml for all roster types
        return ClassUserHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.agora_userlist_dialog_list_item1, null),
            role == EduContextUserRole.Student, listener)
    }

    private class UserListDiff : DiffUtil.ItemCallback<EduContextUserDetailInfo>() {
        override fun areItemsTheSame(oldItem: EduContextUserDetailInfo, newItem: EduContextUserDetailInfo): Boolean {
            return oldItem == newItem && oldItem.user.userUuid == newItem.user.userUuid
        }

        override fun areContentsTheSame(oldItem: EduContextUserDetailInfo, newItem: EduContextUserDetailInfo): Boolean {
            return oldItem.user.userName == newItem.user.userName
                    && oldItem.onLine == newItem.onLine
                    && oldItem.coHost == newItem.coHost
                    && oldItem.boardGranted == newItem.boardGranted
                    && oldItem.cameraState == newItem.cameraState
                    && oldItem.microState == newItem.microState
                    && oldItem.enableAudio == newItem.enableAudio
                    && oldItem.enableVideo == newItem.enableVideo
                    && oldItem.rewardCount == newItem.rewardCount
        }
    }

    private abstract inner class BaseUserHolder(
        private val type: RosterType,
        val view: View, val listener: UserItemClickListener
    ) : RecyclerView.ViewHolder(view) {
        abstract fun bind(item: EduContextUserDetailInfo)
    }

    private inner class UserListAdapter(val listener: UserItemClickListener)
        : ListAdapter<EduContextUserDetailInfo, BaseUserHolder>(UserListDiff()) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            createItemViewHolder(rosterType, parent, listener)

        override fun onBindViewHolder(holder: BaseUserHolder, position: Int) {
            holder.bind(getItem(position))
        }
    }

    private interface UserItemClickListener {
        fun onUserCoHostStateChanged(item: EduContextUserDetailInfo, isCoHost: Boolean)
        fun onAccessStateChanged(item: EduContextUserDetailInfo, hasAccess: Boolean)
        fun onCameraEnabled(item: EduContextUserDetailInfo, enabled: Boolean)
        fun onMicEnabled(item: EduContextUserDetailInfo, enabled: Boolean)
        fun onReward(item: EduContextUserDetailInfo, count: Int)
        fun onUserKickout(item: EduContextUserDetailInfo)
    }

    private inner class ClassUserHolder(view: View,
                                        // Local user role, not the list item's role
                                        private val isStudent: Boolean,
                                        listener: UserItemClickListener
    ) : BaseUserHolder(rosterType, view, listener) {

        private val name: TextView? = view.findViewById(R.id.roster_item_user_name)
        private val desktopIcon: AppCompatImageView = view.findViewById(R.id.roster_item_desktop_icon)
        private val accessIcon: AppCompatImageView = view.findViewById(R.id.roster_item_access_icon)
        private val cameraIcon: AppCompatImageView = view.findViewById(R.id.roster_item_camera_icon)
        private val micIcon: AppCompatImageView = view.findViewById(R.id.roster_item_mic_icon)
        private val startIcon: CheckedTextView = view.findViewById(R.id.roster_item_star_icon_text)

        override fun bind(item: EduContextUserDetailInfo) {
            name?.text = item.user.userName

            desktopIcon.isEnabled = item.coHost
            if (!isStudent) {
                view.findViewById<ViewGroup>(R.id.roster_item_desktop_touch_area)?.let {
                    it.setOnClickListener {
                        listener.onUserCoHostStateChanged(item, !item.coHost)
                    }
                }
            }

            accessIcon.isEnabled = item.boardGranted
            if (!isStudent) {
                view.findViewById<ViewGroup>(R.id.roster_access_touch_area)?.let {
                    it.setOnClickListener {
                        listener.onAccessStateChanged(item, !item.boardGranted)
                    }
                }
            }

            handleCameraState(view, item, isStudent, listener)
            handleMicState(view, item, isStudent, listener)

            val tmp = min(item.rewardCount, 99)
            startIcon.text = view.resources.getString(R.string.agora_video_reward, tmp)
            if (!isStudent) {
                view.findViewById<RelativeLayout>(R.id.agora_roster_list_item_star_layout)?.let {
                    it.setOnClickListener {
                        listener.onReward(item, rewardCount)
                    }
                }
            }

            if (!isStudent) {
                view.findViewById<RelativeLayout>(R.id.roster_item_kickout_touch_area)?.let {
                    it.visibility = VISIBLE
                    it.setOnClickListener {
                        listener.onUserKickout(item)
                    }
                }
            } else {
                view.findViewById<RelativeLayout>(R.id.agora_roster_list_item_kickout_layout)?.let {
                    it.visibility = GONE
                }
            }
        }

        private fun handleCameraState(layout: View, item: EduContextUserDetailInfo,
                                      isStudent: Boolean, listener: UserItemClickListener) {
            cameraIcon.isEnabled = (item.cameraState == EduContextDeviceState.Available ||
                    item.cameraState == EduContextDeviceState.Open) && item.coHost
            cameraIcon.isActivated = (item.cameraState == EduContextDeviceState.Available ||
                    item.cameraState == EduContextDeviceState.Open) &&
                    (item.enableVideo && item.coHost || !item.coHost)

            if (!isStudent || item.isSelf && item.coHost) {
                layout.findViewById<RelativeLayout>(R.id.roster_item_camera_touch_area)?.let { area ->
                    area.setOnClickListener {
                        area.isClickable = false
                        area.postDelayed({ area.isClickable = true }, AgoraUIConfig.clickInterval)
                        listener.onCameraEnabled(item, !cameraIcon.isActivated)
                    }
                }
            }
        }

        private fun handleMicState(layout: View, item: EduContextUserDetailInfo,
                                   isStudent: Boolean, listener: UserItemClickListener) {
            micIcon.isEnabled = (item.microState == EduContextDeviceState.Available ||
                    item.microState == EduContextDeviceState.Open) && item.coHost
            micIcon.isActivated = (item.microState == EduContextDeviceState.Available ||
                    item.microState == EduContextDeviceState.Open) &&
                    (item.enableAudio && item.coHost || !item.coHost)

            if (!isStudent || item.isSelf && item.coHost) {
                layout.findViewById<RelativeLayout>(R.id.roster_item_mic_touch_area)?.let { area ->
                    area.setOnClickListener {
                        area.isClickable = false
                        area.postDelayed({ area.isClickable = true }, AgoraUIConfig.clickInterval)
                        listener.onMicEnabled(item, !micIcon.isActivated)
                    }
                }
            }
        }
    }
}