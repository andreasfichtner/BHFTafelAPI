package de.retterdesapok.bhftafel

import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets


fun getHtmlData(station: String,
                day: String,
                time: String,
                productsFilter: String,
                numberOfJourneys: Int = 10): String {

    val urlParameters = "" +
            "input=${station}&inputRef=%23" +
            "&date=${day}" +
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
