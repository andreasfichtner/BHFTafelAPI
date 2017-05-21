package de.retterdesapok.bhftafel

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.jsoup.Jsoup
import java.text.SimpleDateFormat
import java.util.*

fun getJsonDataForStation(station : String): String? {

    // Set date format
    val simpleDateFormat = SimpleDateFormat()
    simpleDateFormat.timeZone = TimeZone.getTimeZone("Europe/Berlin")

    val now = Date()
    simpleDateFormat.applyPattern("HH:mm")
    val time = simpleDateFormat.format(now)
    simpleDateFormat.applyPattern("dd.MM.yy")
    val day = simpleDateFormat.format(now)
    simpleDateFormat.applyPattern("dd.MM.yy HH:mm")

    val productsFilter = "1111111111111111"

    val html = getHtmlData(station, day, time, productsFilter, 10)

    val htmlDocument = Jsoup.parse(html);
    val mainDiv = htmlDocument.select("div.clicktable")
    val entries = mainDiv.select("div.sqdetailsDep.trow")

    val resultList = entries.map() {

        val line = it.select("a span").text().replace("\\s*", "")
        val destination = it.childNode(1).outerHtml().substringAfter("&gt;&gt;").replace("\\s+,", "")
        val scheduledTime = it.select("span.bold")[1].text().replace("\\s*", "")
        var delay: Int? = null
        if (it.select("span").size >= 3) {
            delay = it.select("span")[2].text().replace("\\s+", "").toInt()
        }

        var scheduledDate = simpleDateFormat.parse("${day} ${scheduledTime}")

        // The "API" is not clear on that. I think it returns only dates after now, so we can just add one day if the given time is in the past.
        if(scheduledDate.before(now)) {
            val calendar = Calendar.getInstance()
            calendar.time = scheduledDate
            calendar.add(Calendar.DATE, 1)
            scheduledDate = calendar.time
        }

        val responseItem = ResponseItem(line, scheduledDate, delay, destination)

        return@map responseItem
    }

    val JSON = ObjectMapper().registerModule(KotlinModule())
    return JSON.writeValueAsString(resultList)
}
