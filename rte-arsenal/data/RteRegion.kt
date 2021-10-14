package io.agora.edu.core.internal.rte.data

import io.agora.rtc2.Constants

object RteRegion {
    fun rtcRegion(region: String?): Constants.AreaCode {
        return when (region) {
            RtcRegion.AREA_NA.name -> Constants.AreaCode.AREA_CODE_NA
            RtcRegion.AREA_EUR.name -> Constants.AreaCode.AREA_CODE_EU
            RtcRegion.AREA_AS.name -> Constants.AreaCode.AREA_CODE_AS
            else -> Constants.AreaCode.AREA_CODE_GLOB
        }
    }

    fun rtmRegion(region: String?): Int {
        return when (region) {
            RtmRegion.AREA_NA.name -> RtmRegion.AREA_NA.value
            RtmRegion.AREA_EUR.name -> RtmRegion.AREA_EUR.value
            RtmRegion.AREA_AS.name -> RtmRegion.AREA_AS.value
            else -> RtmRegion.AREA_GLOBAL.value
        }
    }
}

enum class RtcRegion(val value: Int) {
    AREA_GLOBAL(-1),
    AREA_NA(2),
    AREA_EUR(4),
    AREA_AS(8)
}

enum class RtmRegion(val value: Int) {
    AREA_GLOBAL(-1),
    AREA_NA(2),
    AREA_EUR(4),
    AREA_AS(8)
}