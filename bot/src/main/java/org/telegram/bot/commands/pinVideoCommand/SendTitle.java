package org.telegram.bot.commands.pinVideoCommand;

import org.telegram.bot.api.SendMessages;
import org.telegram.bot.commands.SendOnErrorOccurred;
import org.telegram.bot.database.DatabaseException;
import org.telegram.bot.database.DatabaseManager;
import org.telegram.bot.messages.Message;
import org.telegram.bot.messages.SituationalMessage;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Chat;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.bots.AbsSender;
import org.telegram.telegrambots.bots.commands.BotCommand;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import org.telegram.telegrambots.logging.BotLogger;

import java.nio.file.FileAlreadyExistsException;

/**
 * @author liketechnik
 * @version 1.2.1
 * @date 07 of Februar 2017
 */
public class SendTitle extends BotCommand {

    public static final String LOGTAG = "PINVIDEOCOMMAND_SENDTITLE";

    public SendTitle() {
        super("send_title", "Save the title of a new video file.");
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {

        try {

            DatabaseManager databaseManager = DatabaseManager.getInstance();

            String message = arguments[0];
            String displayFileName;
            displayFileName = message + ".mp4";


            SituationalMessage situationalMessage = new SituationalMessage(this.getCommandIdentifier() + "_command");
            situationalMessage.setMessageName(this.getClass().getPackage().getName()
                            .replaceAll("org.telegram.bot.commands.", ""), this.getCommandIdentifier() + "_command",
                    "new_name");

            try {
                databaseManager.setCurrentVideoTitle(user.getId(), displayFileName);
                databaseManager.setUserCommandState(user.getId(), Config.Bot.PIN_VIDEO_COMMAND_SEND_DESCRIPTION);
            } catch (FileAlreadyExistsException e) {
                situationalMessage.setMessageName(this.getClass().getPackage().getName()
                                .replaceAll("org.telegram.bot.commands.", ""),
                        this.getCommandIdentifier() + "_command","already_used");
            }

            String messageText = situationalMessage.getContent(user.getId(), false);
            SendMessages.getInstance().addMessage(situationalMessage.calculateHash(), messageText, chat.getId().toString(), absSender);
        } catch (DatabaseException | InterruptedException e) {
            BotLogger.error(LOGTAG, e);

            new SendOnErrorOccurred().execute(absSender, user, chat, new String[]{LOGTAG});

            return;
        }
    }
}