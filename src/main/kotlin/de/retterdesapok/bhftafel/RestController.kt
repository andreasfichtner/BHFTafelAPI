package de.retterdesapok.bhftafel

import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class APIController {
    @RequestMapping(value = "/")
    fun simpleTest(@RequestParam(value="station") station : String) : String {
        var json = getJsonDataForStation(station)
        return json ?: "error, no data"
    }
}