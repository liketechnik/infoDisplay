package org.telegram.bot.commands.pinVideoCommand;

import Config.Bot;
import org.glassfish.jersey.internal.ServiceFinder;
import org.telegram.bot.commands.SendOnErrorOccurred;
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

/**
 * @author liketechnik
 * @version ${VERSION}
 * @date 07 of Februar 2017
 */
public class SendVideo extends BotCommand {

    public static final String LOGTAG = "PINVIDEOCOMMAND_SENDVIDEO";

    public SendVideo() {
        super("send_video", "Save a new video as displayFile.");
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {

        SendMessage answer = new SendMessage();

        try {

            DatabaseManager databaseManager = DatabaseManager.getInstance();

            boolean addHelp;
            SituationalMessage situationalMessage = new SituationalMessage(this.getCommandIdentifier() + "_command");

            if (arguments[0].equals(Config.Bot.HAS_VIDEO)) {

                databaseManager.createNewDisplayFile(absSender, user.getId(), arguments[1],
                        Bot.DISPLAY_FILE_TYPE_VIDEO);

                databaseManager.setUserCommandState(user.getId(), Bot.NO_COMMAND);

                situationalMessage.setMessageName(this.getClass().getPackage().getName()
                                .replaceAll("org.telegram.bot.commands.", ""), this.getCommandIdentifier() + "_command",
                        "picture");
                addHelp = true;
            } else {
                situationalMessage.setMessageName(this.getClass().getPackage().getName()
                                .replaceAll("org.telegram.bot.commands.", ""), this.getCommandIdentifier() + "_command",
                        "no_picture");
                addHelp = false;
            }

            answer.setChatId(chat.getId().toString());
            answer.setText(situationalMessage.getContent(user.getId(), addHelp));
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