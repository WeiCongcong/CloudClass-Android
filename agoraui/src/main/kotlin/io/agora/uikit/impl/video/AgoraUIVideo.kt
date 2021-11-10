package io.agora.uikit.impl.video

import android.content.Context
import android.graphics.Outline
import android.graphics.drawable.Drawable
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.view.View.*
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.cardview.widget.CardView
import androidx.vectordrawable.graphics.drawable.Animatable2Compat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import io.agora.educontext.EduContextDeviceState
import io.agora.educontext.EduContextUserDetailInfo
import io.agora.educontext.EduContextUserRole
import io.agora.uikit.*
import io.agora.uikit.interfaces.listeners.IAgoraUIVideoListener
import io.agora.uikit.component.toast.AgoraUIToastManager
import io.agora.uikit.impl.container.AgoraUIConfig
import io.agora.uikit.impl.container.AgoraUIConfig.clickInterval
import io.agora.uikit.impl.container.AgoraUIConfig.isLargeScreen
import io.agora.uikit.impl.container.AgoraUIConfig.videoOptionIconSizeMax
import io.agora.uikit.impl.container.AgoraUIConfig.videoOptionIconSizeMaxWithLargeScreen
import io.agora.uikit.impl.container.AgoraUIConfig.videoOptionIconSizePercent
import io.agora.uikit.impl.container.AgoraUIConfig.videoPlaceHolderImgSizePercent
import kotlin.math.min

internal class AgoraUIVideo(
        context: Context,
        parent: ViewGroup,
        left: Float,
        top: Float,
        shadowWidth: Float) : OnClickListener {
    private val tag = "AgoraUIVideo"

    var videoListener: IAgoraUIVideoListener? = null

    private val view: View = LayoutInflater.from(context).inflate(R.layout.agora_video_layout, parent, false)
    private val cardView: CardView = view.findViewById(R.id.cardView)
    private val videoContainer: FrameLayout = view.findViewById(R.id.videoContainer)
    private val videoOffLayout: LinearLayout = view.findViewById(R.id.video_off_layout)
    private val videoOffImg: AppCompatImageView = view.findViewById(R.id.video_off_img)
    private val offLineLoadingLayout: LinearLayout = view.findViewById(R.id.offLine_loading_layout)
    private val offLineLoadingImg: AppCompatImageView = view.findViewById(R.id.offLine_loading_img)
    private val noCameraLayout: LinearLayout = view.findViewById(R.id.no_camera_layout)
    private val noCameraImg: AppCompatImageView = view.findViewById(R.id.no_camera_img)
    private val cameraDisableLayout: LinearLayout = view.findViewById(R.id.camera_disable_layout)
    private val handWavingLayout: RelativeLayout = view.findViewById(R.id.hand_waving_layout)
    private val cameraDisableImg: AppCompatImageView = view.findViewById(R.id.camera_disable_img)
    private val handWavingImg: AppCompatImageView = view.findViewById(R.id.hand_waving_img)
    private val trophyLayout: LinearLayout = view.findViewById(R.id.trophy_Layout)
    private val trophyText: AppCompatTextView = view.findViewById(R.id.trophy_Text)
    private val audioLayout: RelativeLayout = view.findViewById(R.id.audio_Layout)
    private val volumeLayout: LinearLayout = view.findViewById(R.id.volume_Layout)
    private val audioIc: AppCompatImageView = view.findViewById(R.id.audio_ic)
    private val videoNameLayout: RelativeLayout = view.findViewById(R.id.videoName_Layout)
    private val videoIc: AppCompatImageView = view.findViewById(R.id.video_ic)
    private val nameText: AppCompatTextView = view.findViewById(R.id.name_Text)
    private val boardGrantedIc: AppCompatImageView = view.findViewById(R.id.boardGranted_ic)

    private var userDetailInfo: EduContextUserDetailInfo? = null //拿到视频区用户具体信息

    init {
        view.x = left
        view.y = top
        cardView.z = 0.0f
        cardView.cardElevation = shadowWidth
        val radius = context.resources.getDimensionPixelSize(R.dimen.agora_video_view_corner)
        cardView.radius = radius.toFloat()
        val layoutParams = cardView.layoutParams as ViewGroup.MarginLayoutParams
        val margin = (shadowWidth / 1.0f).toInt()
        layoutParams.setMargins(margin, margin, margin, margin)
        parent.addView(view)
        nameText.setShadowLayer(context.resources.getDimensionPixelSize(R.dimen.shadow_width).toFloat(),
                2.0f, 2.0f, context.resources.getColor(R.color.theme_text_color_black))

        videoContainer.setOnHierarchyChangeListener(object : ViewGroup.OnHierarchyChangeListener {
            override fun onChildViewAdded(parentView: View?, child: View?) {
                child?.let {
                    if (child is TextureView || child is SurfaceView) {
                        setTextureViewRound(child)
                    }
                }
            }

            override fun onChildViewRemoved(p0: View?, p1: View?) {
            }
        })

        cardView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                cardView.viewTreeObserver.removeOnGlobalLayoutListener(this)

                val videoStateIconSize = if (isLargeScreen) {
                    (parent.height * 66 / 100f).toInt()
                } else {
                    (parent.height * 50 / 100f).toInt()
                }

                videoOffLayout.layoutParams.width = videoStateIconSize
                videoOffLayout.layoutParams.height = videoStateIconSize
                videoOffLayout.layoutParams = videoOffLayout.layoutParams
                offLineLoadingLayout.layoutParams.width = videoStateIconSize
                offLineLoadingLayout.layoutParams.height = videoStateIconSize
                offLineLoadingLayout.layoutParams = offLineLoadingLayout.layoutParams
                noCameraLayout.layoutParams.width = videoStateIconSize
                noCameraLayout.layoutParams.height = videoStateIconSize
                noCameraLayout.layoutParams = noCameraLayout.layoutParams
                cameraDisableLayout.layoutParams.width = videoStateIconSize
                cameraDisableLayout.layoutParams.height = videoStateIconSize
                cameraDisableLayout.layoutParams = cameraDisableLayout.layoutParams

                val smallIconSize = (parent.height * 18f / 100).toInt()

                audioLayout.layoutParams.width = smallIconSize
                audioLayout.layoutParams = audioLayout.layoutParams

                audioIc.layoutParams.width = smallIconSize
                audioIc.layoutParams.height = smallIconSize
                audioIc.layoutParams = audioIc.layoutParams
                videoNameLayout.layoutParams.height = smallIconSize
                videoNameLayout.layoutParams = videoNameLayout.layoutParams
                videoIc.layoutParams.width = smallIconSize
                videoIc.layoutParams.height = smallIconSize
                videoIc.layoutParams = videoIc.layoutParams
                boardGrantedIc.layoutParams.width = smallIconSize
                boardGrantedIc.layoutParams.height = smallIconSize
                boardGrantedIc.layoutParams = boardGrantedIc.layoutParams

                nameText.layoutParams.width = (nameText.textSize * 6).toInt()
                nameText.setTextSize(TypedValue.COMPLEX_UNIT_PX, parent.height * 14f / 100)
            }
        })

        audioIc.isEnabled = false
        videoIc.isEnabled = false
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.audio_ic -> {
                audioIc.isClickable = false
                userDetailInfo?.let {
                    videoListener?.onUpdateAudio(!it.enableAudio)
                }
                audioIc.postDelayed({ audioIc.isClickable = true }, clickInterval)
            }
            R.id.video_ic -> {
                videoIc.isClickable = false
                userDetailInfo?.let {
                    videoListener?.onUpdateVideo(!it.enableVideo)
                }
                videoIc.postDelayed({ videoIc.isClickable = true }, clickInterval)
            }
        }
    }

    private fun setTextureViewRound(view: View) {
        val radius: Float = view.context.resources.getDimensionPixelSize(R.dimen.agora_video_view_corner).toFloat()
        val textureOutlineProvider = VideoTextureOutlineProvider(radius)
        view.outlineProvider = textureOutlineProvider
        view.clipToOutline = true
    }

    private fun setCameraState(info: EduContextUserDetailInfo) {
        if (!info.onLine || info.cameraState == EduContextDeviceState.UnAvailable
                || info.cameraState == EduContextDeviceState.Closed) {
            videoIc.isEnabled = false
            videoIc.isSelected = false
        } else {
            videoIc.isEnabled = true
            videoIc.isSelected = info.enableVideo
        }
    }

    private fun setVideoPlaceHolder(info: EduContextUserDetailInfo) {
        videoContainer.visibility = GONE
        videoOffLayout.visibility = GONE
        offLineLoadingLayout.visibility = GONE
        noCameraLayout.visibility = GONE
        cameraDisableLayout.visibility = GONE
        handWavingLayout.visibility = GONE
        if (!info.onLine) {
            offLineLoadingLayout.visibility = VISIBLE
        } else if (info.cameraState == EduContextDeviceState.Closed) {
            cameraDisableLayout.visibility = VISIBLE
            showWaving(info)
        } else if (info.cameraState == EduContextDeviceState.UnAvailable) {
            noCameraLayout.visibility = VISIBLE
            showWaving(info)
        } else if (info.cameraState == EduContextDeviceState.Available) {
            if (info.enableVideo) {
                videoContainer.visibility = VISIBLE
                showWaving(info)
            } else {
                showWaving(info)
                videoOffLayout.visibility = VISIBLE
            }
        }
    }

    private fun showWaving(info: EduContextUserDetailInfo){
        if(info.isWaving){ // 用户正在挥手状态
            handWavingLayout.visibility = VISIBLE
            handWavingImg.visibility = VISIBLE

            Glide.with(view).asGif().skipMemoryCache(true)
                    .load(R.drawable.agora_handsup_waving)
                    .into(handWavingImg)
        }else{
            handWavingLayout.visibility = GONE
            handWavingImg.visibility = GONE
        }
    }
    private fun setMicroState(info: EduContextUserDetailInfo) {
        if (!info.onLine || info.microState == EduContextDeviceState.UnAvailable
                || info.microState == EduContextDeviceState.Closed) {
            audioIc.isEnabled = false
            audioIc.isSelected = false
            volumeLayout.visibility = GONE
        } else {
            audioIc.isEnabled = true
            audioIc.isSelected = info.enableAudio
            volumeLayout.visibility = if (info.enableAudio) VISIBLE else GONE
        }
    }

    fun upsertUserDetailInfo(info: EduContextUserDetailInfo) {
        Log.e(tag, "upsertUserDetailInfo->")

        this.view.post {
            if (info.user.role == EduContextUserRole.Student) {
                audioIc.setOnClickListener(this)
                videoIc.setOnClickListener(this)
                val reward = info.rewardCount
                if (reward > 0) {
                    trophyLayout.visibility = if (info.coHost) VISIBLE else GONE
                    trophyText.text = String.format(view.context.getString(R.string.agora_video_reward),
                            min(reward, 99))
                    trophyText.text = String.format(view.context.getString(R.string.agora_video_reward), info.rewardCount)
                } else {
                    trophyLayout.visibility = GONE
                }

                boardGrantedIc.visibility = if (info.boardGranted) VISIBLE else INVISIBLE
                setCameraState(info)
                videoIc.visibility = if (info.coHost) GONE else VISIBLE
            } else {
                trophyLayout.visibility = GONE
                videoIc.visibility = GONE
            }

            setMicroState(info)
            nameText.text = info.user.userName
            setVideoPlaceHolder(info)

            val currentVideoOpen: Boolean = userDetailInfo?.let {
                it.onLine && it.enableVideo && it.cameraState == EduContextDeviceState.Available
            } ?: false

            val newVideoOpen = info.onLine && info.enableVideo && info.cameraState == EduContextDeviceState.Available

            if (!currentVideoOpen && newVideoOpen) {
                videoListener?.onRendererContainer(videoContainer, info.streamUuid)
            } else if (currentVideoOpen && !newVideoOpen) {
                videoListener?.onRendererContainer(null, info.streamUuid)
            } else {
                val parent = if(newVideoOpen) videoContainer else null
                videoListener?.onRendererContainer(parent, info.streamUuid)
            }

            this.userDetailInfo = info
        }
    }

    fun updateAudioVolumeIndication(value: Int, streamUuid: String) {
        view.post {
            volumeLayout.removeAllViews()
            var volumeLevel = 0
            if (value > -1) {
                volumeLevel = (value / 36.0f).toInt()
            }
            if (volumeLevel == 0) {
                volumeLevel = 1
            } else if (volumeLevel > 7) {
                volumeLevel = 7
            }

            val volumeMargin = view.context.resources
                .getDimensionPixelSize(R.dimen.agora_video_volume_margin_top)

            for (i in 1..volumeLevel) {
                val volumeIc = AppCompatImageView(view.context)
                volumeIc.setImageResource(R.drawable.agora_video_ic_volume_on)
                volumeIc.scaleType = ImageView.ScaleType.FIT_XY
                val layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT)
                layoutParams.height = volumeMargin
                layoutParams.leftMargin = volumeMargin
                layoutParams.rightMargin = volumeMargin
                layoutParams.topMargin = volumeMargin
                volumeIc.layoutParams = layoutParams
                volumeLayout.addView(volumeIc)
            }
        }
    }

    fun updateMediaMessage(msg: String) {
        AgoraUIToastManager.showShort(msg)
    }

    fun updateReward(reward: Int) {
        trophyText.post {
            if (reward <= 0) {
                trophyLayout.visibility = GONE
            } else {
                trophyText.text = String.format(view.context.getString(R.string.agora_video_reward),
                        min(reward, 99))
            }
        }
    }

    fun updateGrantedStatus(granted: Boolean) {
        boardGrantedIc?.post {
            boardGrantedIc.visibility = if (granted) VISIBLE else INVISIBLE
        }
    }
}

class VideoTextureOutlineProvider(private val mRadius: Float) : ViewOutlineProvider() {
    override fun getOutline(view: View, outline: Outline) {
        outline.setRoundRect(0, 0, view.width, view.height, mRadius)
    }
}