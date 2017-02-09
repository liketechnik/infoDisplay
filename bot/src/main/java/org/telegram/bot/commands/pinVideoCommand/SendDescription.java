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
 * @version 1.2.1
 * @date 07 of Februar 2017
 */
public class SendDescription extends BotCommand {

    public static final String LOGTAG = "PINVIDEOCOMMAND_SENDDESCRIPTION";

    public SendDescription() {
        super("send_description",
                "Set the description of a new video file.");
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {

        SendMessage answer = new SendMessage();

        try {

            DatabaseManager databaseManager = DatabaseManager.getInstance();

            StringBuilder messageBuilder = new StringBuilder();

            String message = arguments[0];

            databaseManager.setCurrentVideoDescription(user.getId(), message);

            messageBuilder.append(Message.pinVideoCommand.getSendDescriptionMessage(user));

            databaseManager.setUserCommandState(user.getId(), Config.Bot.PIN_VIDEO_COMMAND_SEND_VIDEO);


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