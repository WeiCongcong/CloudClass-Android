package com.hyphenate.easeim.modules.utils

import android.content.Context
import android.net.Uri
import android.text.Spannable
import android.text.style.ImageSpan
import com.hyphenate.easeim.modules.view.ui.emoji.EaseDefaultEmojiconDatas
import com.hyphenate.easeim.modules.view.ui.emoji.EaseEmojicon
import java.io.File
import java.util.HashMap
import java.util.regex.Pattern


object EaseSmileUtils {
    const val ee_1 = "[):]"
    const val ee_2 = "[:D]"
    const val ee_3 = "[;)]"
    const val ee_4 = "[:-o]"
    const val ee_5 = "[:p]"
    const val ee_6 = "[(H)]"
    const val ee_7 = "[:@]"
    const val ee_8 = "[:s]"
    const val ee_9 = "[:$]"
    const val ee_10 = "[:(]"
    const val ee_11 = "[:'(]"
    const val ee_12 = "[:|]"
    const val ee_13 = "[(a)]"
    const val ee_14 = "[8o|]"
    const val ee_15 = "[8-|]"
    const val ee_16 = "[+o(]"
    const val ee_17 = "[<o)]"
    const val ee_18 = "[|-)]"
    const val ee_19 = "[*-)]"
    const val ee_20 = "[:-#]"
    const val ee_21 = "[:-*]"
    const val ee_22 = "[^o)]"
    const val ee_23 = "[8-)]"
    const val ee_24 = "[(|)]"
    const val ee_25 = "[(u)]"
    const val ee_26 = "[(S)]"
    const val ee_27 = "[(*)]"
    const val ee_28 = "[(#)]"
    const val ee_29 = "[(R)]"
    const val ee_30 = "[({)]"
    const val ee_31 = "[(})]"
    const val ee_32 = "[(k)]"
    const val ee_33 = "[(F)]"
    const val ee_34 = "[(W)]"
    const val ee_35 = "[(D)]"

    private val spannableFactory = Spannable.Factory.getInstance()
    private val emoticons: MutableMap<Pattern, Any> = HashMap()

    /**
     * add text and icon to the map
     * @param emojiText-- text of emoji
     * @param icon -- resource id or local path
     */
    fun addPattern(emojiText: String?, icon: Any) {
        emoticons[Pattern.compile(Pattern.quote(emojiText))] = icon
    }

    /**
     * replace existing spannable with smiles
     * @param context
     * @param spannable
     * @return
     */
    fun addSmiles(context: Context?, spannable: Spannable): Boolean {
        var hasChanges = false
        for ((key, value) in emoticons) {
            val matcher = key.matcher(spannable)
            while (matcher.find()) {
                var set = true
                for (span in spannable.getSpans(
                    matcher.start(),
                    matcher.end(), ImageSpan::class.java
                )) if (spannable.getSpanStart(span) >= matcher.start()
                    && spannable.getSpanEnd(span) <= matcher.end()
                ) spannable.removeSpan(span) else {
                    set = false
                    break
                }
                if (set) {
                    hasChanges = true
                    if (value is String && !value.startsWith("http")) {
                        val file = File(value)
                        if (!file.exists() || file.isDirectory) {
                            return false
                        }
                        spannable.setSpan(
                            ImageSpan(context!!, Uri.fromFile(file)),
                            matcher.start(), matcher.end(),
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    } else {
                        spannable.setSpan(
                            ImageSpan(
                                context!!,
                                (value as Int)
                            ),
                            matcher.start(), matcher.end(),
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }
                }
            }
        }
        return hasChanges
    }

    fun getSmiledText(context: Context?, text: CharSequence?): Spannable {
        val spannable = spannableFactory.newSpannable(text)
        addSmiles(context, spannable)
        return spannable
    }

    fun containsKey(key: String?): Boolean {
        var b = false
        for ((key1) in emoticons) {
            val matcher = key1.matcher(key)
            if (matcher.find()) {
                b = true
                break
            }
        }
        return b
    }

    fun getSmilesSize(): Int {
        return emoticons.size
    }

    init {
        val emojicons: Array<EaseEmojicon> = EaseDefaultEmojiconDatas.data
        for (emojicon in emojicons) {
            addPattern(emojicon.emojiText, emojicon.icon)
        }
    }
}