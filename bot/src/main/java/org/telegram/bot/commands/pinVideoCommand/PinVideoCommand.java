package org.telegram.bot.commands.pinVideoCommand;

import org.telegram.bot.api.SendMessages;
import org.telegram.bot.commands.SendOnErrorOccurred;
import org.telegram.bot.database.DatabaseException;
import org.telegram.bot.database.DatabaseManager;
import org.telegram.bot.messages.SituationalMessage;
import org.telegram.telegrambots.api.objects.Chat;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.bots.AbsSender;
import org.telegram.telegrambots.bots.commands.BotCommand;
import org.telegram.telegrambots.logging.BotLogger;

import java.util.Optional;

/**
 * @author liketechnik
 * @version 1.0
 * @date 30 of Januar 2017
 */
public class PinVideoCommand extends BotCommand {

    public static final String LOGTAG = "PINVIDEOCOMMAND";

    public PinVideoCommand() {
        super("pin_video", "Upload a video to the virtual board.");
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {

        try {

            DatabaseManager databaseManager = DatabaseManager.getInstance();

            SituationalMessage situationalMessage = new SituationalMessage(this.getCommandIdentifier() + " _command");

            if (!databaseManager.getUserRegistrationState(user.getId())) {
                situationalMessage.setMessageName(this.getClass().getPackage().getName()
                                .replaceAll("org.telegram.bot.commands.", ""), this.getCommandIdentifier() + "_command",
                        "has_no_permission");
            } else {
                databaseManager.setUserCommandState(user.getId(), Config.Bot.PIN_VIDEO_COMMAND_SEND_TITLE);

                situationalMessage.setMessageName(this.getClass().getPackage().getName()
                                .replaceAll("org.telegram.bot.commands.", ""), this.getCommandIdentifier() + "_command",
                        "has_permission");
            }

            String messageText = situationalMessage.getContent(user.getId(), false);
            SendMessages.getInstance().addMessage(situationalMessage.calculateHash(), messageText, chat.getId().toString(), absSender, Optional.empty(), Optional.empty());
        } catch (DatabaseException | InterruptedException e) {
            BotLogger.error(LOGTAG, e);

            new SendOnErrorOccurred().execute(absSender, user, chat, new String[]{LOGTAG});

            return;
        }
    }
}