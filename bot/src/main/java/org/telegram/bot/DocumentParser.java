package org.telegram.bot;

import Config.Bot;
import org.telegram.bot.api.TelegramLongPollingThreadBot;
import org.telegram.bot.api.Parser;
import org.telegram.bot.commands.pinPictureCommand.SendPicture;
import org.telegram.bot.commands.pinVideoCommand.SendVideo;
import org.telegram.bot.database.DatabaseManager;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.logging.BotLogger;

import java.lang.reflect.Constructor;

/**
 * @author liketechnik
 * @version 1.0
 * @date 14 of April 2017
 */
public class DocumentParser extends Parser {

    public final String LOGTAG = "DOCUMENTPARSER";

    public DocumentParser(Update update, TelegramLongPollingThreadBot bot) {
        super(update, bot);
    }

    @Override
    protected boolean parse() {
        Message message = this.update.getMessage();
        this.user = message.getFrom();
        this.chat = message.getChat();

        try {
            String userCommandState = DatabaseManager.getInstance().getUserCommandState(this.user.getId());

            if (userCommandState.equals(Bot.NO_COMMAND)) {
                return false;
            }

            if (hasPhoto(message)) {
                if (userCommandState.equals(Bot.PIN_PICTURE_COMMAND_SEND_PICTURE)) {
                    this.arguments = new String[] {Bot.HAS_PHOTO, message.getDocument().getFileId()}    ;
                    this.commandConstructor = (Constructor) SendPicture.class.getConstructor();
                } else {
                    return false;
                }
                return true;
            } else if (hasVideo(message)) {
                if (userCommandState.equals(Bot.PIN_PICTURE_COMMAND_SEND_PICTURE)) {
                    this.arguments = new String[] {Bot.HAS_VIDEO, message.getDocument().getFileId()}    ;
                    this.commandConstructor = (Constructor) SendVideo.class.getConstructor();
                } else {
                    return false;
                }
                return true;
            }
            return false;

        } catch (Exception e) {
            BotLogger.error(LOGTAG, e);
            return false;
        }
    }

    private boolean hasPhoto(Message message) {
        if (message.hasDocument()) {
            if (message.getDocument().getMimeType().contains("image") || message.hasPhoto()) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private boolean hasVideo(Message message) {
        if (message.hasDocument()) {
            if (message.getDocument().getMimeType().contains("video")) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
}
