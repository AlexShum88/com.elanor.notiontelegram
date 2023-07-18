package com.elanor

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.time.Instant

class Bot(
    tBot: TBot,
    val notionDB: String,
    val secret: String
) {

    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                coerceInputValues = true
            })
        }
    }

    val gamers = tBot.gamers
    var lastTime = Instant.now().toString()


    suspend fun run(telbot: TBot) {
        val pages = findPagesAfterLastCheck(lastTime)
        checkUsersInUpdate(pages).forEach { telbot.sendMessage(it.first, it.second.toString()) }
        lastTime = Instant.now().minusSeconds(100).toString()
        println("run of run ended")
    }

    fun checkUsersInUpdate(update: List<Page>): List<Pair<Long, List<String>>> {
        val res = mutableListOf<Pair<Long, List<String>>>()
        gamers.forEach { gamer ->
            update.forEach { u ->
                u.properties.assigned_to.people.forEach {
                    if (it.name == gamer.value) {
                        res.add(gamer.key to u.properties.Name.title.map { it.text.content })
                    }
                }
            }
        }
        return res
    }

    suspend fun findInNotionByUserName(username: String): String {

        val response = client.post("https://api.notion.com/v1/databases/${notionDB}/query") {

            contentType(ContentType.Application.Json)
            header(HttpHeaders.Accept, ContentType.Application.Json)
            header("Notion-Version", "2022-06-28")
            header(HttpHeaders.Authorization, "Bearer secret_p8dxjKNdTlYAQ4lPSWH7iue3XZiaoFLjsfDjpqt7AuO")

//            setBody(
//                query(filterByPeople("assigned_to", peopleSet(true, name = username)))
//            )

        }
//        println(response.call.body<String>().toString())
        val pageId = response.call.body<NotionPage>().results.first().id

        println(pageId)
        return pageId
    }

    suspend fun findPagesAfterLastCheck(time: String): List<Page> {
        val response = client.post("https://api.notion.com/v1/databases/${notionDB}/query") {

            contentType(ContentType.Application.Json)
            header(HttpHeaders.Accept, ContentType.Application.Json)
            header("Notion-Version", "2022-06-28")
            header(HttpHeaders.Authorization, "Bearer ${secret}")

            setBody(
                query(filterByLastDate("last_edited_time", lastEditedTime(time)))
            )

        }

        return response.call.body<NotionPage>().results
    }

}

@Serializable
data class query(
    val filter: filterByLastDate
)

@Serializable
data class filterByPeople(
    val property: String,
    val people: peopleSet
)

@Serializable
data class filterByLastDate(
    val timestamp: String,
    val last_edited_time: lastEditedTime
)

@Serializable
data class lastEditedTime(
    val after: String
)

@Serializable
data class peopleSet(
    val is_not_empty: Boolean,
    val name: String
)