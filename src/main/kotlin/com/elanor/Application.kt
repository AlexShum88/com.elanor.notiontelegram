package com.elanor

import com.elanor.plugins.configureRouting
import com.elanor.plugins.configureSecurity
import com.elanor.plugins.configureSerialization
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.routing.*
import kotlinx.coroutines.delay


fun main() {
    val notionDB = System.getenv("notion")
    val telegramKey = System.getenv("telegram")
    val secret = System.getenv("secret")
    val port = System.getenv("PORT").toInt()

    embeddedServer(CIO, port = port) {
        val tBot = TBot(telegramKey)
        val bot = Bot(tBot, notionDB, secret)


        routing {
            get("/") {
                while (true) {
                    delay(100000)
                    bot.run(tBot)
                }
            }
            get("/telegram") {
                while (true){
                    delay(100000)
                    tBot.getUpdates()
                }
            }
        }
    }.start(wait = true)
}


fun Application.module() {
    configureSerialization()
    configureSecurity()
    configureRouting()
}


