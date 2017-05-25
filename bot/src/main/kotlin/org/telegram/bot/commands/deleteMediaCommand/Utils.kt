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