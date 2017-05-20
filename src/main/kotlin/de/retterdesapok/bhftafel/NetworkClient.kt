package de.retterdesapok.bhftafel

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.jsoup.Jsoup
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.io.InputStreamReader
import java.io.BufferedReader
import java.text.SimpleDateFormat
import jdk.nashorn.internal.objects.NativeDate.getTime
import java.util.*

val JSON = ObjectMapper().registerModule(KotlinModule())

fun main(args: Array<String>) {
    var station = "München Hbf"
    var date = "19.05.17"
    var time = "23:54"
    var productsFilter = "1111111111111111"

    val simpleDateFormat = SimpleDateFormat("dd.MM.yy HH:mm")
    simpleDateFormat.timeZone = TimeZone.getTimeZone("Europe/Berlin")
    val startDate = simpleDateFormat.parse("${date} ${time}")

    var html = getHtmlData(station, date, time, productsFilter, 100)

    var htmlDocument = Jsoup.parse(html);
    val mainDiv = htmlDocument.select("div.clicktable")
    val entries = mainDiv.select("div.sqdetailsDep.trow")

    val resultList = entries.map() {

        var line = it.select("a span").text().replace("\\s*", "")
        var destination = it.childNode(1).outerHtml().substringAfter("&gt;&gt;").replace("\\s+,", "")
        var scheduledTime = it.select("span.bold")[1].text().replace("\\s*", "")
        var delay: Int? = null
        if (it.select("span").size >= 3) {
            delay = it.select("span")[2].text().replace("\\s+", "").toInt()
        }

        var scheduledDate = simpleDateFormat.parse("${date} ${scheduledTime}")

        // The "API" is not clear on that. I think it returns only dates after now, so we can just add one day if the given time is in the past.
        if(scheduledDate.before(startDate)) {
            val calendar = Calendar.getInstance()
            calendar.time = scheduledDate
            calendar.add(Calendar.DATE, 1)
            scheduledDate = calendar.time
        }

        val responseItem = ResponseItem(line, scheduledDate, delay, destination)

        return@map responseItem
    }

    resultList.forEach{
        println(it.toJson())
    }
}

fun getHtmlData(station: String,
                date: String,
                time: String,
                productsFilter: String,
                numberOfJourneys: Int = 10): String {

    val urlParameters = "" +
            "input=${station}&inputRef=%23" +
            "&date=${date}" +
            "&time=${time}" +
            "&productsFilter=${productsFilter}" +
            "&REQTrain_name=" +
            "&maxJourneys=${numberOfJourneys}" +
            "&start=Suchen" +
            "&boardType=Abfahrt" +
            "&ao=yes"

    val postData = urlParameters.toByteArray(StandardCharsets.UTF_8)
    val postDataLength = postData.size
    val requestURL = "https://mobile.bahn.de/bin/mobil/bhftafel.exe/dox?country=DEU&rt=1&use_realtime_filter=1&webview=&"
    val url = URL(requestURL)
    val conn = url.openConnection() as HttpURLConnection
    conn.setDoOutput(true)
    conn.setInstanceFollowRedirects(false)
    conn.setRequestMethod("POST")
    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
    conn.setRequestProperty("charset", "utf-8")
    conn.setRequestProperty("Content-Length", Integer.toString(postDataLength))
    conn.setUseCaches(false)
    conn.setDoOutput(true)
    conn.getOutputStream().write(postData)

    val reader = BufferedReader(InputStreamReader(conn.inputStream))
    val sb = StringBuilder()

    var line = reader.readLine()

    while (line != null) {
        sb.append(line)
        line = reader.readLine()
    }

    return sb.toString()
}

data class ResponseItem(val line : String,
                        val scheduledDate: Date,
                        val delay: Int?,
                        val destination: String) {

    fun printDescription() {
        println("$line \t\t\t\t$destination\t\t\t\t$scheduledDate\t\t\t\t$delay")
    }

    fun toJson(): String {
        return JSON.writeValueAsString(this)
    }
}