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


private val commandIdentifier: String = "confirm_delete_media"
private val description: String = "Asks the user if he's sure he wants to delete a picture."

/**
 * @author liketechnik
 * @version 1.0
 * @date 21 of Mai 2017
 */
class ConfirmDeleteMediaCommand : BotCommand(commandIdentifier, description) {

    val LOGTAG = "CONFIRMDELETEMEDIACOMMAND"
    private val packageName: String = getPackageName(this)
    private val commandName: String = getCommandName(this)

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
