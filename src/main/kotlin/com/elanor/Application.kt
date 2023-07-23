package com.elanor

import com.elanor.plugins.configureSecurity
import com.elanor.plugins.configureSerialization
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.routing.*
import kotlinx.coroutines.delay
import kotlin.time.DurationUnit
import kotlin.time.toDuration


fun main(args: Array<String>): Unit = io.ktor.server.cio.EngineMain.main(args)

fun Application.module() {
    val notionDB = environment.config.property("ktor.NOTION").getString()
    val telegramKey = environment.config.property("ktor.TELEGRAM").getString()
    val secret = environment.config.property("ktor.SECRET").getString()
//    val port = environment.config.property("ktor.PORT").toString().toInt()
    val pauseNotion = environment.config.property("ktor.PAUSENOTION").getString()
    val pauseTelegram = environment.config.property("ktor.PAUSETELEGRAM").getString()

    val tBot = TBot(telegramKey)
    val bot = Bot(tBot, notionDB, secret)


    routing {
        get("/") {
            while (true) {
                delay(pauseNotion.toInt().toDuration(DurationUnit.MILLISECONDS))
                bot.run(tBot)
            }
        }
        get("/telegram") {
            while (true) {
                delay(pauseTelegram.toInt().toDuration(DurationUnit.MILLISECONDS))
                tBot.getUpdates()
            }
        }
    }
    configureSerialization()
    configureSecurity()
//    configureRouting()
}


