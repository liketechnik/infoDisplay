package org.telegram.bot.commands.pinVideoCommand;

import org.telegram.bot.commands.SendOnErrorOccurred;
import org.telegram.bot.database.DatabaseManager;
import org.telegram.bot.messages.Message;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Chat;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.bots.AbsSender;
import org.telegram.telegrambots.bots.commands.BotCommand;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import org.telegram.telegrambots.logging.BotLogger;

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

        SendMessage answer = new SendMessage();

        try {

            DatabaseManager databaseManager = DatabaseManager.getInstance();

            StringBuilder messageBuilder = new StringBuilder();

            if (!databaseManager.getUserRegistrationState(user.getId())) {
                messageBuilder.append(Message.pinVideoCommand.getPinVideoMessage(user, false));

                answer.setText(messageBuilder.toString());

                try {
                    absSender.sendMessage(answer);
                } catch (Exception e) {
                    BotLogger.error(LOGTAG, e);
                }

                return;
            }

            databaseManager.setUserCommandState(user.getId(), Config.Bot.PIN_VIDEO_COMMAND_SEND_TITLE);

            messageBuilder.append(Message.pinVideoCommand.getPinVideoMessage(user, true));

            answer.setChatId(chat.getId().toString());
            answer.setText(messageBuilder.toString());
        } catch (Exception e) {
            BotLogger.error(LOGTAG, e);

            new SendOnErrorOccurred().execute(absSender, user, chat, new String[]{LOGTAG});

            return;
        }

        try {
            absSender.sendMessage(answer);
        } catch (TelegramApiException e) {
            BotLogger.error(LOGTAG, e);
        }
    }
}