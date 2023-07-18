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
    val notionDB = "0385d4fffa2c4a90ab1cee309784be3d"
    val telegramKey = "6186829942:AAHW5h37n3hdTVfeadICFB_Lrsi7xZlYj6M"
    val secret = "secret_p8dxjKNdTlYAQ4lPSWH7iue3XZiaoFLjsfDjpqt7AuO"

    embeddedServer(CIO, port = 8080) {
        val tBot = TBot(telegramKey)
        val bot = Bot(tBot, notionDB, secret)


        routing {
            get("/") {
                while (true) {
                    delay(10000)
                    bot.run(tBot)
                }
            }
            get("/telegram") {
                while (true){
                    delay(10000)
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


