package com.oussama_chatri.productivityx.core.util

import android.content.Context
import com.oussama_chatri.productivityx.R
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

object DateTimeUtils {

    private val dateFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy")
    private val dateShortFormatter = DateTimeFormatter.ofPattern("MMM d")
    private val timeFormatter = DateTimeFormatter.ofPattern("h:mm a")
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("MMM d 'at' h:mm a")
    private val fullFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")

    fun formatDate(instant: Instant, zoneId: ZoneId = ZoneId.systemDefault()): String =
        dateFormatter.format(instant.atZone(zoneId).toLocalDate())

    fun formatDateShort(instant: Instant, zoneId: ZoneId = ZoneId.systemDefault()): String =
        dateShortFormatter.format(instant.atZone(zoneId).toLocalDate())

    fun formatTime(instant: Instant, zoneId: ZoneId = ZoneId.systemDefault()): String =
        timeFormatter.format(instant.atZone(zoneId).toLocalTime())

    fun formatDateTime(instant: Instant, zoneId: ZoneId = ZoneId.systemDefault()): String =
        dateTimeFormatter.format(instant.atZone(zoneId))

    fun formatFull(instant: Instant, zoneId: ZoneId = ZoneId.systemDefault()): String =
        fullFormatter.format(instant.atZone(zoneId))

    fun formatDate(date: LocalDate): String = dateFormatter.format(date)

    fun formatTime(time: LocalTime): String = timeFormatter.format(time)

    fun relativeTime(context: Context, instant: Instant): String {
        val now = Instant.now()
        val minutes = ChronoUnit.MINUTES.between(instant, now)
        val hours = ChronoUnit.HOURS.between(instant, now)
        val days = ChronoUnit.DAYS.between(instant, now)
        val weeks = days / 7
        val months = ChronoUnit.MONTHS.between(instant, now)

        return when {
            minutes < 1 -> context.getString(R.string.time_just_now)
            minutes < 60 -> context.getString(R.string.time_minutes_ago, minutes.toInt())
            hours < 24 -> context.getString(R.string.time_hours_ago, hours.toInt())
            days == 1L -> context.getString(R.string.time_yesterday)
            days < 7 -> context.getString(R.string.time_days_ago, days.toInt())
            weeks == 1L -> context.getString(R.string.time_weeks_ago, weeks.toInt())
            weeks < 4 -> context.getString(R.string.time_weeks_ago_plural, weeks.toInt())
            months == 1L -> context.getString(R.string.time_months_ago, months.toInt())
            else -> context.getString(R.string.time_months_ago_plural, months.toInt())
        }
    }

    fun isToday(date: LocalDate): Boolean = date == LocalDate.now()

    fun isTomorrow(date: LocalDate): Boolean = date == LocalDate.now().plusDays(1)

    fun isYesterday(date: LocalDate): Boolean = date == LocalDate.now().minusDays(1)

    fun friendlyDate(context: Context, date: LocalDate): String = when {
        isToday(date) -> context.getString(R.string.today)
        isTomorrow(date) -> context.getString(R.string.tomorrow)
        isYesterday(date) -> context.getString(R.string.yesterday)
        else -> dateShortFormatter.format(date)
    }

    fun focusDurationLabel(totalMinutes: Int): String {
        val h = totalMinutes / 60
        val m = totalMinutes % 60
        return when {
            h > 0 && m > 0 -> "${h}h ${m}m"
            h > 0 -> "${h}h"
            else -> "${m}m"
        }
    }
}