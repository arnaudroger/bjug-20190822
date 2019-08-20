package com.github.arnaudroger.bjug201908220

import com.fasterxml.jackson.databind.JsonNode
import com.github.arnaudroger.bjug201908220.model.Tables
import com.github.arnaudroger.bjug201908220.model.Tables.*
import org.jooq.*
import org.jooq.impl.DSL
import org.simpleflatmapper.jdbc.JdbcMapper
import org.simpleflatmapper.jdbc.JdbcMapperFactory
import org.simpleflatmapper.util.TypeReference
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletRequest
import kotlin.streams.toList

@RestController()
class AllDoneController (private val dslContext : DSLContext) {


    @GetMapping("/done/hello")
    fun hello() = "Hello world " + dslContext

    @GetMapping("/done/films", produces = ["application/json"])
    fun films() =
            dslContext
                    .select(FILM.FILM_ID, FILM.DESCRIPTION, FILM.TITLE)
                    .from(FILM)
                    .fetchInto(Film::class.java).toList()

    /*
    http://localhost:8080/done/films2?title=ARK
     */
    @GetMapping("/done/films2", produces = ["application/json"])
    fun filmsSearch(request: HttpServletRequest): List<Film> {
        val from = dslContext
                .select(FILM.FILM_ID, FILM.DESCRIPTION, FILM.TITLE)
                .from(FILM)

        val conditions : MutableList<Condition> = ArrayList();
        request.parameterMap.forEach{ (k, vs) ->  conditions.add(toCondition(k, vs))}

        return from.where(conditions)
                .fetchInto(Film::class.java).toList()
    }

    private fun toCondition(k: String?, vs: Array<String>?): Condition {
        val field : Field<String> =
                when(k) {
                    "description"  -> FILM.DESCRIPTION
                    "title"  -> FILM.TITLE
                    else -> throw IllegalArgumentException("Name required")
                }
        if (vs != null && vs.isNotEmpty()) {
            return field.like("%" + vs[0] + "%")
        } else {
            throw java.lang.IllegalArgumentException("Invalid query filter ${k} ${vs}")
        }
    }

    @GetMapping("/done/films/{id}", produces = ["application/json"])
    fun film1(@PathVariable("id")  id :  Int) =
            dslContext
                    .select(FILM.FILM_ID, FILM.DESCRIPTION, FILM.TITLE)
                    .from(FILM)
                    .where(FILM.FILM_ID.eq(id))
                    .fetchOneInto(Film::class.java)

    private val films2Mapper : JdbcMapper<Pair<Film, List<Actor>>> =
            JdbcMapperFactory.newInstance()
                    .addKeys(FILM.FILM_ID.name, ACTOR.ACTOR_ID.name)
                    .newMapper(object : TypeReference<Pair<Film, List<Actor>>>() {})


    @GetMapping("/done/filmAndActors/{id}", produces = ["application/json"])
    fun film2(@PathVariable("id")  id :  Int) =
        dslContext
                .select(FILM.FILM_ID, FILM.DESCRIPTION, FILM.TITLE, ACTOR.ACTOR_ID, ACTOR.FIRST_NAME, ACTOR.LAST_NAME)
                .from(FILM)
                .join(FILM_ACTOR).on(FILM_ACTOR.FILM_ID.eq(FILM.FILM_ID.cast(Short::class.java)))
                .join(ACTOR).on(ACTOR.ACTOR_ID.eq(FILM_ACTOR.ACTOR_ID.cast(Int::class.java)))
                .where(FILM.FILM_ID.eq(id))
                .fetchResultSet().use {
                    films2Mapper.stream(it).toList()
                }


    /*
 curl  -X POST -H "Content-Type: application/json" -d '{"description":"desc", "title":"title", "languageId" : 1}' http://localhost:8080/done/films

 */
    @PostMapping("/done/films")
    fun createFilm(@RequestBody film : JsonNode): Int {
        return dslContext.transactionResult { cfg -> _createFilm(film, cfg) }
    }

    fun _createFilm(film : JsonNode, cfg : Configuration):Int {
        val filmRecord = DSL.using(cfg).newRecord(FILM)
        filmRecord.description = film.get("description").asText()
        filmRecord.title = film.get("title").asText()
        filmRecord.languageId = film.get("languageId").asInt().toShort()

        filmRecord.store()

        return filmRecord.filmId
    }
}

data class Film(val filmId: Int, val description: String, val title: String);
data class Actor(val actorId: Int, val firstName: String, val lastName: String);

