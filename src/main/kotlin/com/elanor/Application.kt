package com.elanor

import com.elanor.plugins.configureSecurity
import com.elanor.plugins.configureSerialization
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.routing.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration


fun main(args: Array<String>): Unit = io.ktor.server.cio.EngineMain.main(args)

fun Application.module() {
    val notionDB = environment.config.property("ktor.NOTION").getString()
    val usersDB = environment.config.property("ktor.USERSDB").getString()
    val telegramKey = environment.config.property("ktor.TELEGRAM").getString()
    val secret = environment.config.property("ktor.SECRET").getString()
    val pauseNotion = environment.config.property("ktor.PAUSENOTION").getString()
    val pauseTelegram = environment.config.property("ktor.PAUSETELEGRAM").getString()

    val tBot = TBot(telegramKey)
    val bot = Bot(tBot, notionDB, secret)
    val notionBot = NotionBot(notionDB, secret, usersDB)
    launch { notionBot.retrieveUsers() }

    routing {
        get("/") {
//            launch {
//                telegram(pauseTelegram.toInt().toDuration(DurationUnit.MILLISECONDS), tBot)
//
//            }
//            launch {
//                notion(pauseNotion.toInt().toDuration(DurationUnit.MILLISECONDS), tBot, bot)
//            }
//            NotionBot(notionDB, secret, usersDB).checkUserInNotionTable()

            notionBot.users.forEach {
                launch {
                    notionBot.findPagesAfterLastCheckForUser(it.notionName, it.lastCheck)
                }
            }
        }


//        get("/telegram") {
//            while (true) {
//                delay(pauseTelegram.toInt().toDuration(DurationUnit.MILLISECONDS))
//                tBot.getUpdates()
//            }
//        }
    }
    configureSerialization()
    configureSecurity()
//    configureRouting()
}


suspend fun telegram(pause: Duration, tBot: TBot) {
    while (true) {
        delay(pause)
        tBot.getUpdates()
    }
}

suspend fun notion(pause: Duration, tBot: TBot, bot: Bot) {
    while (true) {
        delay(pause)
        bot.run(tBot)
    }
}