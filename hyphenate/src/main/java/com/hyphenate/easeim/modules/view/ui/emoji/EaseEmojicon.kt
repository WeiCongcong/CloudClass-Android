package com.hyphenate.easeim.modules.view.ui.emoji

class EaseEmojicon {

    /**
     * constructor
     * @param icon- resource id of the icon
     * @param emojiText- text of emoji icon
     */
    constructor(icon: Int, emojiText: String?) {
        this.icon = icon
        this.emojiText = emojiText
    }
    /**
     * get identity code
     * @return
     */
    /**
     * set identity code
     * @param identityCode
     */
    /**
     * identity code
     */
    var identityCode: String? = null
    /**
     * get the resource id of the icon
     * @return
     */
    /**
     * set the resource id of the icon
     * @param icon
     */
    /**
     * static icon resource id
     */
    var icon = 0
    /**
     * get text of emoji icon
     * @return
     */
    /**
     * set text of emoji icon
     * @param emojiText
     */
    /**
     * text of emoji, could be null for big icon
     */
    var emojiText: String? = null
    /**
     * get name of emoji icon
     * @return
     */
    /**
     * set name of emoji icon
     * @param name
     */
    /**
     * name of emoji icon
     */
    var name: String? = null
    /**
     * get icon path
     * @return
     */
    /**
     * set icon path
     * @param iconPath
     */
    /**
     * path of icon
     */
    var iconPath: String? = null

    /**
     * path of big icon
     */
    private val bigIconPath: String? = null

    companion object {
        fun newEmojiText(codePoint: Int): String {
            return if (Character.charCount(codePoint) == 1) {
                codePoint.toString()
            } else {
                String(Character.toChars(codePoint))
            }
        }
    }
}