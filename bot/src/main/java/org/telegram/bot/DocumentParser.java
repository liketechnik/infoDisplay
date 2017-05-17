package org.telegram.bot;

import Config.Bot;
import org.telegram.bot.api.TelegramLongPollingThreadBot;
import org.telegram.bot.api.Parser;
import org.telegram.bot.commands.pinPictureCommand.SendPicture;
import org.telegram.bot.commands.pinVideoCommand.SendVideo;
import org.telegram.bot.database.DatabaseException;
import org.telegram.bot.database.DatabaseManager;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.PhotoSize;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.logging.BotLogger;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

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
            } else if (userCommandState.equals(Bot.PIN_PICTURE_COMMAND_SEND_PICTURE)) {
                if (hasPhoto(message)) {
                    List<String> arguments = new ArrayList<>();
                    arguments.add(Bot.HAS_PHOTO);
                    if (message.hasPhoto()) {
                        List<PhotoSize> photos = update.getMessage().getPhoto();

                        int width = 0;
                        int height = 0;

                        int biggestPhoto = 0;

                        for (int x = 0; x < photos.size(); x++) {
                            if (width < photos.get(x).getWidth() || height < photos.get(x).getHeight()) {
                                biggestPhoto = x;
                            }
                        }

                        arguments.add(message.getPhoto().get(biggestPhoto).getFileId());
                    } else if (message.getDocument().getMimeType().contains("image")) {
                        arguments.add(message.getDocument().getFileId());
                    }
                    this.arguments = arguments.toArray(new String[]{});
                } else {
                    this.arguments = new String[] {Bot.HAS_NO_PHOTO};
                }
                this.commandConstructor = (Constructor) SendPicture.class.getConstructor();
                return true;
            } else if (userCommandState.equals(Bot.PIN_VIDEO_COMMAND_SEND_VIDEO)) {
                if (hasVideo(message)) {
                    List<String> arguments = new ArrayList<>();
                    arguments.add(Bot.HAS_VIDEO);
                    if (message.getVideo() != null) {
                        arguments.add(message.getVideo().getFileId());
                    } else if (message.getDocument().getMimeType().contains("video")) {
                        arguments.add(message.getDocument().getFileId());
                    }
                    this.arguments = arguments.toArray(new String[]{});
                } else {
                    this.arguments = new String[] {Bot.HAS_NO_VIDEO};
                }
                this.commandConstructor = (Constructor) SendVideo.class.getConstructor();
                return true;
            }

            return false;

        } catch (DatabaseException | NoSuchMethodException e) {
            BotLogger.error(LOGTAG, e);
            return false;
        }
    }

    private boolean hasPhoto(Message message) {
//        if (message.hasDocument() || message.hasPhoto()) {
//            if (message.getDocument().getMimeType().contains("image") || message.hasPhoto()) {
//                return true;
//            } else {
//                return false;
//            }
//        } else {
//            return false;
//        }
        if (message.hasDocument()) {
            if (message.getDocument().getMimeType().contains("image")) {
                return true;
            }
        } else if (message.hasPhoto()) {
            return true;
        }
        return false;
    }

    private boolean hasVideo(Message message) {
//        if (message.hasDocument() || message.getVideo() != null) {
//            if (message.getDocument().getMimeType().contains("video") || message.getVideo() != null) {
//                return true;
//            } else {
//                return false;
//            }
//        } else {
//            return false;
//        }
        if (message.hasDocument()) {
            if (message.getDocument().getMimeType().contains("video")) {
                return true;
            }
        } else if (message.getVideo() != null) {
            return true;
        }
        return false;
    }
}
