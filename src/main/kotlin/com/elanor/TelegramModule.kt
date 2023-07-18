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


class TBot(val token: String) {

    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    coerceInputValues = true
                }
            )
        }
    }

    val gamers = mutableMapOf<Long, String>()
    var offset: Long = 0

    suspend fun getUpdates(): List<Update> {
        val response = client.get("https://api.telegram.org/bot${token}/getUpdates") {
            parameter("timeout", 10)
            parameter("offset", offset)
//            parameter("allowed_updates", "message")
        }
        val updates = Json.decodeFromString(GetUpdatesResponse.serializer(), response.call.body())
        updates.result.forEach { checkCommand(it) }
        return updates.result
    }

    suspend fun checkCommand(update: Update){
        when(update.message?.text){
            "/start" -> sendMessage(update.message.chat.id, "please print /user {your name in notion}")
            else -> checkForUserName(update)
        }
        offset = update.update_id+1
    }


    suspend fun checkForUserName(update: Update)
//    : Pair<Long, String>
    {
        println(update)
        var name = "no user name here"
        if (update.message?.text?.contains("/user ") == true) {
            name = update.message.text.substringAfter("/user ")
        }
        gamers[update.message?.chat?.id!!] = name
//        return update.message?.chat?.id!! to name

    }

    suspend fun sendMessage(chatId: Long, text: String) {

        val url = "https://api.telegram.org/bot$token/sendMessage"

        client.post(url) {
            contentType(ContentType.Application.Json)
            setBody(TelegramSendMessage(chatId, text))
        }


    }
}


@Serializable
data class Update(
    val update_id: Long,
    val message: Message? = null
)


@Serializable
data class TEntity(
    val offset: Int,
    val length: Int,
    val type: String
)

@Serializable
data class Message(
    val message_id: Long,
    val from: TUser? = null,
    val chat: TChat,
    val date: Int,
    val text: String? = null,
    val entities: List<TEntity>? = null
)

@Serializable
data class TUser(
    val id: Long,
    val is_bot: Boolean,
    val first_name: String,
    val username: String? = null,
    val language_code: String? = null
)

@Serializable
data class TChat(
    val id: Long,
    val type: String,
    val username: String? = null,
    val first_name: String? = null,
)

@Serializable
data class GetUpdatesResponse(
    val ok: Boolean,
    val result: List<Update>
)

@Serializable
data class TelegramSendMessage(
    val chat_id: Long?,
    val text: String
)