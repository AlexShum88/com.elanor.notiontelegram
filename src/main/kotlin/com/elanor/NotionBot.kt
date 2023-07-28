package com.elanor

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import java.time.Instant

class NotionBot(
    val notionDB: String,
    val secret: String,
    val usersDB: String,

    ) {

    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                coerceInputValues = true
            })
        }
    }

    val users = mutableListOf<NotionUser>()


    data class NotionUser(
        val chatId: String = "",
        val notionName: String,
        var lastCheck: String = "",
        val pageId: String = ""
    )

    suspend fun retrieveUsers() {
        checkUserInNotionTable().forEach { users.add(it) }
    }


    suspend fun checkUserInNotionTable(): List<NotionUser> {
        val response = client.post("https://api.notion.com/v1/databases/${usersDB}/query") {

            contentType(ContentType.Application.Json)
            header(HttpHeaders.Accept, ContentType.Application.Json)
            header("Notion-Version", "2022-06-28")
            header(HttpHeaders.Authorization, "Bearer ${secret}")

        }

        return response.call.body<NotionPage<Properties.UserProperties>>().results.map {
            NotionUser(
                notionName = it.properties.name.rich_text.map { it.plain_text }.first(),
                chatId = it.properties.chat_id.title.map { it.text.content }.first(),
                lastCheck = it.properties.last_check.date.start,
                pageId = it.id
            )
        }.also { println(it) }
    }

    suspend fun findPagesAfterLastCheckForUser(
        userName: String,
        lastCheck: String
    ): List<Page<Properties.MainProperties>> {
        val response = client.post("https://api.notion.com/v1/databases/${notionDB}/query") {

            contentType(ContentType.Application.Json)
            header(HttpHeaders.Accept, ContentType.Application.Json)
            header("Notion-Version", "2022-06-28")
            header(HttpHeaders.Authorization, "Bearer ${secret}")

            setBody(

                """
                    {
                "filter":{
                "and": [
                {
                "timestamp": "last_edited_time",
                "last_edited_time": {
                "on_or_after": "$lastCheck"
                }
                },
                {"property": "assigned_to",
                "people": {
                
                "contains": "$userName"
                    }
                }
                ]
                }
                }
            """.trimIndent()
            )

        }
        println(response.call.body<String>())
        val resp = response.call.body<NotionPage<Properties.MainProperties>>().results
        changeTimeInDBUsers(users.filter { it.notionName == userName }.map { it.pageId }.first())
//        println(resp)
        return resp
    }

    suspend fun changeTimeInDBUsers(pageID: String) {
        val time = Instant.now()
        val response = client.patch("https://api.notion.com/v1/pages/${pageID}") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Accept, ContentType.Application.Json)
            header("Notion-Version", "2022-06-28")
            header(HttpHeaders.Authorization, "Bearer ${secret}")
            setBody(
                """
                    {
                    "properties": {
                    "last_check":{
                    "date": {
			      "start": "$time"}
			    }
                    }
                    }
                """.trimIndent()
            )
        }
        println("status of change time on user DB ${response.status}")
        if (response.status.value == 200) users.filter { it.pageId == pageID }
            .forEach { it.lastCheck = time.toString() }
    }
}