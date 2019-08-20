package com.github.arnaudroger.bjug201908220

import com.fasterxml.jackson.databind.JsonNode
import org.jooq.DSLContext
import org.springframework.web.bind.annotation.*

@RestController
class DemoController (private val dslContext : DSLContext) {

    @GetMapping("/hello")
    fun hello() = "Hello world " + dslContext;

    @GetMapping("/films", produces = ["application/json"])
    fun listFilms() = null;


    @GetMapping("/films/{id}", produces = ["application/json"])
    fun getFilm(@PathVariable("id")  id :  Int) = null
    /*
curl  -X POST -H "Content-Type: application/json" -d '{"description":"desc", "title":"title", "languageId" : 1}' http://localhost:8080/done/films
*/
    @PostMapping("/films")
    fun createFilm(@RequestBody film : JsonNode): Int = 0
}