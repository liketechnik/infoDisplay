package org.telegram.bot.commands.deleteMediaCommand

import org.telegram.bot.api.SendMessages
import org.telegram.bot.commands.SendOnErrorOccurred
import org.telegram.bot.database.DatabaseException
import org.telegram.bot.database.DatabaseManager
import org.telegram.bot.messages.SituationalMessage
import org.telegram.bot.utils.getCommandName
import org.telegram.bot.utils.getPackageName
import org.telegram.telegrambots.api.objects.Chat
import org.telegram.telegrambots.api.objects.User
import org.telegram.telegrambots.bots.AbsSender
import org.telegram.telegrambots.bots.commands.BotCommand
import org.telegram.telegrambots.logging.BotLogger
import java.util.*

private val commandIdentifier: String = "deleted_media"
private val description: String = "Send confirmation on taken actions of a user while deleting a media file."
internal enum class FUNCTIONS {
    delete, cancel
}

/**
 * @author liketechnik
 * @version 1.0
 * @date 24 of Mai 2017
 */
class DeletedMediaCommand : BotCommand(commandIdentifier, description) {

    val LOGTAG = "DELETEDMEDIACOMMAND"
    private val packageName: String = getPackageName(this)
    private val commandName: String = getCommandName(this)

    private enum class SITUATIONS {
        yes, cancel
    }

    override fun execute(absSender: AbsSender, user: User, chat: Chat, arguments: Array<out String>) {
        try {
            if (arguments[0] == FUNCTIONS.delete.name) {
                val argumentsSplit: List<String> = arguments[1].split("_")

                val sendTime: Int = argumentsSplit[argumentsSplit.size - 2].toInt() // access the second last element containing the timestamp
                val receiveTime: Int = System.currentTimeMillis().toInt()

                if (receiveTime - sendTime <= 1 * 60 * 1000) { // check that the confirmation message was sent max. one minute ago
                    delete(absSender, user, chat, arguments.copyOfRange(1, 3))
                } else {
                    cancel(absSender, user, chat, arguments.copyOfRange(2, 3))
                }
            } else {
                cancel(absSender, user, chat, arguments.copyOfRange(1, 2))
            }


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

    private fun cancel(absSender: AbsSender, user: User, chat: Chat, arguments: Array<out String>) {
        val message: SituationalMessage = SituationalMessage(commandName)
        message.setMessageName(packageName, commandName, SITUATIONS.cancel.name)

        val messageText: String = message.getContent(user.id, true)

        SendMessages.getInstance().addDeleteMessage(chat.id.toString(), arguments[0].toInt(), absSender)
        SendMessages.getInstance().addMessage(message.calculateHash(), messageText, chat.id.toString(), absSender, Optional.empty(), Optional.empty())
    }

    private fun delete(absSender: AbsSender, user: User, chat: Chat, arguments: Array<out String>) {
        val databaseManager = DatabaseManager.getInstance()

        val index: Int = getIndex(arguments)

        val displayFiles: List<String> = if (databaseManager.adminUserId == user.id) databaseManager.displayFiles else
            databaseManager.getDisplayFiles(user.id)

        val fileId: String = databaseManager.getDisplayFileId(displayFiles[index])
        val displayFileTgType: String = databaseManager.getDisplayFileTgType(displayFiles[index])
        databaseManager.deleteDisplayFile(displayFiles[index])

        val message: SituationalMessage = SituationalMessage(commandName)
        message.setMessageName(packageName, commandName, SITUATIONS.yes.name)

        val messageText: String = message.getContent(user.id, true)

        SendMessages.getInstance().addDeleteMessage(chat.id.toString(), arguments[1].toInt(), absSender)
        sendMediaMessage(displayFileTgType, message.calculateHash(), messageText, chat.id.toString(), absSender, fileId, Optional.empty())
    }

}