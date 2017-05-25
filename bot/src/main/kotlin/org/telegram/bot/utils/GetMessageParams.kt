package org.telegram.bot.utils

import org.telegram.telegrambots.bots.commands.BotCommand

/**
 * @author liketechnik
 * @version 1.0
 * @date 24 of Mai 2017
 */
fun getPackageName(command: BotCommand): String =
        command::class.java.`package`.name.replace("org.telegram.bot.commands.", "")

fun getCommandName(command: BotCommand): String = command.commandIdentifier + "_command"