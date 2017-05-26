/*
 * Copyright (C) 2016-2017  Florian Warzecha <flowa2000@gmail.com>
 *
 * This file is part of infoDisplay.
 *
 * infoDisplay is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * infoDisplay is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * infoDisplay uses TelegramBots Java API <https://github.com/rubenlagus/TelegramBots> by Ruben Bermudez.
 * TelegramBots API is licensed under GNU General Public License version 3 <https://www.gnu.org/licenses/gpl-3.0.de.html>.
 *
 * infoDisplay uses parts of the Apache Commons project <https://commons.apache.org/>.
 * Apache commons is licensed under the Apache License Version 2.0 <http://www.apache.org/licenses/>.
 *
 * infoDisplay uses vlcj library <http://capricasoftware.co.uk/#/projects/vlcj>.
 * vlcj is licensed under GNU General Public License version 3 <https://www.gnu.org/licenses/gpl-3.0.de.html>.
 *
 * Thanks to all the people who contributed to the projects that make this
 * program possible.
 */

package org.telegram.bot.commands.deleteMediaCommand

import org.telegram.bot.api.SendMessages
import org.telegram.bot.commands.SendOnErrorOccurred
import org.telegram.bot.database.DatabaseException
import org.telegram.bot.database.DatabaseManager
import org.telegram.bot.messages.ContentMessage
import org.telegram.bot.messages.InlineKeyboard
import org.telegram.bot.utils.convertDeleteMediaKeyboard
import org.telegram.bot.utils.getCommandName
import org.telegram.bot.utils.getPackageName
import org.telegram.telegrambots.api.objects.Chat
import org.telegram.telegrambots.api.objects.User
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton
import org.telegram.telegrambots.bots.AbsSender
import org.telegram.telegrambots.bots.commands.BotCommand
import org.telegram.telegrambots.logging.BotLogger
import java.util.*
import kotlin.collections.HashMap

/**
 * The identifier of this command
 */
private val commandIdentifier: String = "confirm_delete_media"
/**
 * A short description of this command.
 */
private val description: String = "Asks the user if he's sure he wants to delete a picture."

/**
 * @author Florian Warzecha
 * @version 1.0
 * @date 21 of May 2017
 * @since 2.0.0
 * @see DeleteMediaCommand
 */
class ConfirmDeleteMediaCommand : BotCommand(commandIdentifier, description) {

    val LOGTAG = "CONFIRMDELETEMEDIACOMMAND"
    private val packageName: String = getPackageName(this)
    private val commandName: String = getCommandName(this)

    /**
     * Send the user a message with the selected media file and ask for confirmation.
     */
    override fun execute(absSender: AbsSender, user: User, chat: Chat, arguments: Array<out String>) {
        try {
            val databaseManager = DatabaseManager.getInstance()

            val index: Int = getIndex(arguments)

            val inlineKeyboard = InlineKeyboard(commandName)
            inlineKeyboard.setMessageName(packageName, commandName)

            var keyboardList: List<List<InlineKeyboardButton>> = inlineKeyboard.getKeyboard(user.id)
            val regexReplacementMap: HashMap<String, Int> = HashMap()
            regexReplacementMap.put(timestampRegex, System.currentTimeMillis().toInt())
            regexReplacementMap.put(indexRegex, index)
            keyboardList = convertDeleteMediaKeyboard(regexReplacementMap, keyboardList)

            val keyboard: InlineKeyboardMarkup = InlineKeyboardMarkup()
            keyboard.keyboard = keyboardList

            val displayFiles: List<String> = if (databaseManager.adminUserId == user.id) databaseManager.displayFiles else databaseManager.getDisplayFiles(user.id)

            val message: ContentMessage = ContentMessage(commandName)
            message.setMessageName(packageName, commandName)
            message.setAdditionalContent(getAdditionalContent(displayFiles[index]))

            val messageText:String = message.getContent(user.id, false)

            val fileId:String = databaseManager.getDisplayFileId(displayFiles[index])

            SendMessages.getInstance().addDeleteMessage(chat.id.toString(), arguments[1].toInt(), absSender)
            val displayFileTgType: String = databaseManager.getDisplayFileTgType(displayFiles[index])
            sendMediaMessage(displayFileTgType, message.calculateHash(), messageText, chat.id.toString(), absSender, fileId, Optional.of(keyboard))
        } catch (e: Exception) {
            if (e is InterruptedException || e is DatabaseException) {
                BotLogger.error(LOGTAG, e)

                SendOnErrorOccurred().execute(absSender, user, chat, arrayOf(LOGTAG))

                return
            } else {
                throw e
            }
        }
    }
}
