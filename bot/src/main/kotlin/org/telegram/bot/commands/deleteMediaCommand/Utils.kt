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
import org.telegram.bot.database.DatabaseManager
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.bots.AbsSender
import java.util.*

/**
 * @author liketechnik
 * @version 1.0
 * @date 21 of Mai 2017
 */

internal val currentIndexRegex: String = "/current_index>"
internal val lastIndexRegex: String = "/last_index>"
internal val nextIndexRegex: String = "/next_index>"

internal val indexRegex: String = "/index>"
internal val timestampRegex: String = "/timestamp>"

/**
 * Get a [HashMap] made for [org.telegram.bot.messages.ContentMessage].
 *
 * This function sets the key value pairs to replace title, description etc when retrieving the message text from a class in [org.telegram.bot.messages].
 *
 * @param displayFileName The name of the display file the information belongs to.
 * @return A [HashMap] containing the key value pairs for title, description, duration and uploadInfo
 * @author Florian Warzecha
 * @since 2.0.0
 * @see org.telegram.bot.commands.deleteMediaCommand.DeleteMediaCommand
 * @see org.telegram.bot.commands.deleteMediaCommand.ConfirmDeleteMediaCommand
 * @see org.telegram.bot.messages.ContentMessage
 */
internal fun  getAdditionalContent(displayFileName: String): HashMap<String, String> {
    val databaseManager = DatabaseManager.getInstance()
    val additionalContent = HashMap<String, String>()
    additionalContent.put("title", displayFileName)
    additionalContent.put("description", databaseManager.getDisplayFileDescription(displayFileName))
    additionalContent.put("duration", databaseManager.getDisplayFileDuration(displayFileName).toString())
    additionalContent.put("uploadInfo", databaseManager.getDisplayFileUploadInfoName(displayFileName))
    return additionalContent
}

internal fun getIndex(arguments: Array<out String>): Int {
    val index: Int
    val argumentsSplit: List<String> = arguments[0].split("_")
    index = argumentsSplit.last().toInt()
    return index
}

internal fun sendMediaMessage(tgType: String, messageHash: Int, messageText: String, chatId: String, absSender: AbsSender,
                              fileId: String, keyboardMarkup: Optional<InlineKeyboardMarkup>) {
    if (tgType == Config.Bot.DISPLAY_FILE_TG_TYPE_AS_DOCUMENT) {
        SendMessages.getInstance().addDocumentMessage(messageHash, messageText, chatId, absSender, fileId, keyboardMarkup)
    } else if (tgType == Config.Bot.DISPLAY_FILE_TG_TYPE_IMAGE) {
        SendMessages.getInstance().addImageMessage(messageHash, messageText, chatId, absSender, fileId, keyboardMarkup)
    } else if (tgType == Config.Bot.DISPLAY_FILE_TG_TYPE_VIDEO) {
        SendMessages.getInstance().addVideoMessage(messageHash, messageText, chatId, absSender, fileId, keyboardMarkup)
    }
}