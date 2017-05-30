package de.retterdesapok.bhftafel

import java.util.*


data class ResponseItem(val line : String,
                        val scheduledDate: Date,
                        val delay: Int?,
                        val canceled: Boolean,
                        val destination: String) {

    fun printDescription() {
        println("$line \t\t\t\t$destination\t\t\t\t$scheduledDate\t\t\t\t$delay")
    }
}