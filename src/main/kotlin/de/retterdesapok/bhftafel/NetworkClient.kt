package de.retterdesapok.bhftafel

import org.jsoup.Jsoup
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.io.InputStreamReader
import java.io.BufferedReader
import java.net.URLDecoder


fun main(args: Array<String>) {
    var station = "Ostbahnhof"
    var date = "20.05.17"
    var time = "01:24"
    var productsFilter = "1111111111111111"

    var html = getHtmlData(station, date, time, productsFilter)

    var htmlDocument = Jsoup.parse(html);
    val mainDiv = htmlDocument.select("div.clicktable")
    val entries = mainDiv.select("div.sqdetailsDep.trow")

    entries.forEach {
        var line = it.select("a span").text().replace("\\s*", "")
        var destination = it.childNode(1).outerHtml().substringAfter("&gt;&gt;").replace("\\s+,", "")

        var scheduledTime = it.select("span.bold")[1].text().replace("\\s*", "")
        var delay: Int? = null

        if (it.select("span").size >= 3) {
            delay = it.select("span")[2].text().replace("\\s+", "").toInt()
        }


        println("$line \t\t\t\t$destination\t\t\t\t$scheduledTime\t\t\t\t$delay")
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
    conn.setDoOutput(true);
    conn.getOutputStream().write(postData);

    val reader = BufferedReader(InputStreamReader(conn.inputStream))
    val sb = StringBuilder()

    var line = reader.readLine()

    while (line != null) {
        sb.append(line)
        line = reader.readLine()
    }

    return sb.toString()
}

data class ResponseItem(val time: String, val delay: String?, val destination: String)