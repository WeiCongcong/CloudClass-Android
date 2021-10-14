package io.agora.edu.uikit.impl.video

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.view.*
import android.widget.RelativeLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.agora.edu.R
import io.agora.edu.core.context.EduContextUserDetailInfo
import io.agora.edu.core.context.EduContextUserRole

@SuppressLint("InflateParams")
class AgoraUIVideoWindowDialog(info: EduContextUserDetailInfo, context: Context, private val optionListener: IAgoraOptionListener) : Dialog(context, R.style.agora_dialog) {
    private val tag = "AgoraUIOptionDialog"

    private val width = context.resources.getDimensionPixelSize(R.dimen.agora_tool_popup_layout_width)
    private val elevation = 10

    private var roleInfo = info
    fun show(anchor: View?) {
        init(anchor)
        super.show()
    }

    private fun init(anchor: View?) {
        setCancelable(true)
        setCanceledOnTouchOutside(true)

        initStyleDialog(anchor)
    }

    private fun initStyleDialog(anchor: View?) {
        val layout = LayoutInflater.from(context).inflate(
                R.layout.agora_teacher_option_dialog_layout, null, false)
        setContentView(layout)

        val dialog = layout.findViewById<RelativeLayout>(R.id.agora_tool_popup_layout)
        dialog.clipToOutline = true
        dialog.elevation = elevation.toFloat()
//
        val recycler = layout.findViewById<RecyclerView>(R.id.agora_teacher_option_dialog_recycler)
        recycler.layoutManager = if (roleInfo.user.role == EduContextUserRole.Student) {
            GridLayoutManager(context, 5)
        } else {
            GridLayoutManager(context, 3)
        }
        recycler.adapter = OptionAdapter(recycler)

        val height = context.resources.getDimensionPixelSize(R.dimen.agora_status_bar_height)
        anchor?.let { OptionDialogUtil.adjustPosition(this.window!!, it, width, height) }
    }

    private class OptionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icon: AppCompatImageView = itemView.findViewById(R.id.agora_tool_popup_style_item_icon)
    }

    private inner class OptionAdapter(private val recyclerView: RecyclerView) : RecyclerView.Adapter<OptionViewHolder>() {

        var iconsRes: Array<OptionItem> = if (roleInfo.user.role == EduContextUserRole.Teacher) {
            arrayOf(
                    OptionItem(R.drawable.agora_option_icon_audio, "voice"),
                    OptionItem(R.drawable.agora_option_icon_video, "video"),
                    OptionItem(R.drawable.agora_option_icon_cohost, "cohost"))
        } else {
            arrayOf(
                    OptionItem(R.drawable.agora_option_icon_audio, "voice"),
                    OptionItem(R.drawable.agora_option_icon_video, "video"),
                    OptionItem(R.drawable.agora_option_icon_cohost, "cohost"),
                    OptionItem(R.drawable.agora_option_icon_grant, "grant"),
                    OptionItem(R.drawable.agora_option_icon_reward, "reward"))
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OptionViewHolder {
            return OptionViewHolder(LayoutInflater.from(parent.context).inflate(
                    R.layout.agora_teacher_option_item_layout, parent, false))
        }

        override fun onBindViewHolder(holder: OptionViewHolder, position: Int) {

            val itemWidth = if (roleInfo.user.role == EduContextUserRole.Teacher) {
                recyclerView.width / 3
            } else {
                recyclerView.width / 5
            }
            var params = holder.itemView.layoutParams as ViewGroup.MarginLayoutParams
            params.width = itemWidth
            params.height = itemWidth
            val margin = (recyclerView.height - params.height) / 2
            params.topMargin = margin
            params.bottomMargin = margin
            holder.itemView.layoutParams = params

            params = holder.icon.layoutParams as RelativeLayout.LayoutParams
            params.height = (recyclerView.height * 2f / 3).toInt()
            params.width = params.height
            params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE)
            holder.icon.layoutParams = params


            val pos = holder.absoluteAdapterPosition
            val item = iconsRes[pos]
            holder.icon.setImageResource(item.res)

            holder.itemView.setOnClickListener {
                when (item.itemName) {
                    "voice" -> {
                        holder.icon.isActivated = !holder.icon.isActivated
                        optionListener.onAudioUpdated()
                    }
                    "video" -> {
                        holder.icon.isActivated = !holder.icon.isActivated
                        optionListener.onVideoUpdated()
                    }
                    "cohost" -> {
                        holder.icon.isActivated = !holder.icon.isActivated
                        optionListener.onCohostUpdated()
                    }
                    "grant" -> {
                        holder.icon.isActivated = !holder.icon.isActivated
                        optionListener.onGrantUpdated()
                    }
                    "reward" -> {
                        holder.icon.isActivated = !holder.icon.isActivated
                        optionListener.onRewardUpdated()
                    }
                }

            }
        }

        override fun getItemCount(): Int {
            return iconsRes.size
        }
    }

    data class OptionItem(val res: Int, val itemName: String)
}

private object OptionDialogUtil {
    // window：指的就是这个dialog
    // anchor：指的就是点击的view
    fun adjustPosition(window: Window, anchor: View, width: Int, height: Int) {
        val params = window.attributes
        hideStatusBar(window)

        params.width = width
        params.height = height
        params.gravity = Gravity.TOP or Gravity.START

        val locationsOnScreen = IntArray(2)
        anchor.getLocationOnScreen(locationsOnScreen)
        params.x = locationsOnScreen[0] + anchor.width / 2 - width / 2
        params.y = locationsOnScreen[1] + anchor.height + 2
        window.attributes = params
    }

    private fun hideStatusBar(window: Window) {
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = Color.TRANSPARENT
        val flag = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        window.decorView.systemUiVisibility = (flag or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
    }
}