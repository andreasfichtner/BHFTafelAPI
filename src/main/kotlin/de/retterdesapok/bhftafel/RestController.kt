package de.retterdesapok.bhftafel

import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class APIController {
    @RequestMapping(value = "/")
    fun simpleTest() = {
        getJsonDataForStation("München Hbf")
    }
}