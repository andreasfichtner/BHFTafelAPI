package de.retterdesapok.bhftafel

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.apache.commons.io.FileUtils
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit




@RestController
class APIController {
    @RequestMapping(value = "/getDepartures")
    fun getDepartures(@RequestParam(value="station") station : String) : String {
        val resultList = getDataForStation(station)

        val JSON = ObjectMapper().registerModule(KotlinModule())
        return JSON.writeValueAsString(resultList)
    }

    @RequestMapping(value = "/getDeparturesAsHtml")
    fun getDeparturesAsHtml(@RequestParam(value="station") station : String) : String {
        val resultList = getDataForStation(station)
        val htmlTemplateFile = File("html/departuresTemplate.html")
        val htmlString = FileUtils.readFileToString(htmlTemplateFile)
        val htmlTableRows = StringBuilder()

        val now = Date()
        val simpleDateFormat = SimpleDateFormat("HH:mm")
        simpleDateFormat.timeZone = TimeZone.getTimeZone("Europe/Berlin")

        resultList.forEach{

            val delayInfo = if (it.delay == null || it.delay <= 0) "" else " (+${it.delay})"
            val delayedClass = if (it.delay != null && it.delay > 0) "class='delayed'" else ""

            htmlTableRows.append("<tr>" +
                    "<td>${it.line}</td>" +
                    "<td>${it.destination}</td>" +
                    "<td ${delayedClass}'>${simpleDateFormat.format(it.scheduledDate)}${delayInfo}</td>" +
                    "</tr>")
        }

        return htmlString.replace("%tablerows%", htmlTableRows.toString()).replace("%station%", station)
    }
}
