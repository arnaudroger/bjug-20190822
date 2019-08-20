package com.github.arnaudroger.bjug201908220

import org.jooq.DSLContext
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class DemoController (private val dslContext : DSLContext) {

    @GetMapping("/hello")
    fun hello() = "Hello world " + dslContext;

}