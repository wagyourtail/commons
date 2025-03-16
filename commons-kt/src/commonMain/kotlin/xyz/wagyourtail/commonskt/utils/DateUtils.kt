package xyz.wagyourtail.commonskt.utils

import kotlinx.datetime.*

val LocalDate.sundayBefore: LocalDate
    get() {
        if (dayOfWeek == DayOfWeek.SUNDAY) {
            return this
        }
        var day = this
        while (day.dayOfWeek != DayOfWeek.SUNDAY) {
            day = day.minus(1, DateTimeUnit.DAY)
        }
        return day
    }

val LocalDate.mondayBefore: LocalDate
    get() {
        if (dayOfWeek == DayOfWeek.MONDAY) {
            return this
        }
        var day = this
        while (day.dayOfWeek != DayOfWeek.MONDAY) {
            day = day.minus(1, DateTimeUnit.DAY)
        }
        return day
    }

val LocalDate.saturdayAfter: LocalDate
    get() {
        if (dayOfWeek == DayOfWeek.SATURDAY) {
            return this
        }
        var day = this
        while (day.dayOfWeek != DayOfWeek.SATURDAY) {
            day = day.plus(1, DateTimeUnit.DAY)
        }
        return day
    }

val LocalDate.sundayAfter: LocalDate
    get() {
        if (dayOfWeek == DayOfWeek.SUNDAY) {
            return this
        }
        var day = this
        while (day.dayOfWeek != DayOfWeek.SUNDAY) {
            day = day.plus(1, DateTimeUnit.DAY)
        }
        return day
    }

operator fun LocalDate.rangeTo(end: LocalDate) = iterator {
    var date = this@rangeTo
    while (date <= end) {
        yield(date)
        date = date.plus(1, DateTimeUnit.DAY)
    }
}
