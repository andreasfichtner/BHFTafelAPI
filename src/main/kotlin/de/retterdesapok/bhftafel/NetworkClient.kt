package de.retterdesapok.bhftafel
import org.jsoup.Jsoup
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.io.InputStreamReader
import java.io.BufferedReader
import java.net.URLDecoder


fun main(args: Array<String>) {
    val urlParameters = "input=Unterschlei%DFheim&inputRef=%23&date=Fr%2C+19.05.17&time=21%3A28&productsFilter=1111101000000000&REQTrain_name=&maxJourneys=10&start=Suchen&boardType=Abfahrt&ao=yes"
    val postData = urlParameters.toByteArray(StandardCharsets.UTF_8)
    val postDataLength = postData.size
    val request = "https://mobile.bahn.de/bin/mobil/bhftafel.exe/dox?country=DEU&rt=1&use_realtime_filter=1&webview=&"
    val url = URL(request)
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

    var foundTable = false;

    var currentResponseItem : ResponseItem? = null;
    var lineWithinCurrentItem = 0;

    var line = reader.readLine()
    var decodedLine : String?

    while (line != null) {
        decodedLine = URLDecoder.decode(line, "UTF-8")
        sb.append(decodedLine)
        line = reader.readLine()
    }

    var htmlDocument = Jsoup.parse(sb.toString());
    val mainDiv = htmlDocument.select("div.clicktable")
    val entries = mainDiv.select("div.sqdetailsDep.trow")

    entries.forEach{
        var line = it.select("a span").text().replace("\\s*","")
        var destination = it.ownText().substringAfter(">>").substringBefore("Gl").replace("\\s+","")
        var scheduledTime = it.select("span.bold")[1].text().replace("\\s+","")
        var delay = it.select("span")[1].text().replace("\\s+","")

        println("Line $line to $destination is scheduled for $scheduledTime and currently delayed by $delay minutes.")
    }
}

data class ResponseItem (val time:String,val delay:String, val destination:String)