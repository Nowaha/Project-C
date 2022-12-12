package xyz.nowaha.chengetawildlife.util

import kotlin.math.floor

object TimeUtils {

    fun getRelativeTimeString(date1: Long, date2: Long): String {
        val timeDifference = ((date2 - date1) / 1000.0)
        val hours = floor(timeDifference / 3600.0).toInt()
        val minutes = floor((timeDifference - (hours * 3600.0)) / 60.0).toInt()
        val seconds = timeDifference - (hours * 3600) - (minutes * 60)

        var timeString = "${seconds.toInt()}s ago"
        if (minutes > 0 || hours > 0) {
            timeString = "${minutes}m " + timeString
        }
        if (hours > 0) {
            timeString = "${hours}h " + timeString
        }

        return timeString
    }

}
