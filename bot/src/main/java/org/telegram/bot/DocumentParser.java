/*
 * Copyright (C) 2016-2017  Florian Warzecha <flowa2000@gmail.com>
 *
 * This file is part of infoDisplay.
 *
 * infoDisplay is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * infoDisplay is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * infoDisplay uses TelegramBots Java API <https://github.com/rubenlagus/TelegramBots> by Ruben Bermudez.
 * TelegramBots API is licensed under GNU General Public License version 3 <https://www.gnu.org/licenses/gpl-3.0.de.html>.
 *
 * infoDisplay uses parts of the Apache Commons project <https://commons.apache.org/>.
 * Apache commons is licensed under the Apache License Version 2.0 <http://www.apache.org/licenses/>.
 *
 * infoDisplay uses vlcj library <http://capricasoftware.co.uk/#/projects/vlcj>.
 * vlcj is licensed under GNU General Public License version 3 <https://www.gnu.org/licenses/gpl-3.0.de.html>.
 *
 * Thanks to all the people who contributed to the projects that make this
 * program possible.
 */

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

            switch (userCommandState) {
                case Bot.NO_COMMAND:
                    return false;
                case Bot.PIN_PICTURE_COMMAND_SEND_PICTURE:
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
                            arguments.add(Bot.DISPLAY_FILE_TG_TYPE_IMAGE);
                        } else if (message.getDocument().getMimeType().contains("image")) {
                            arguments.add(message.getDocument().getFileId());
                            arguments.add(Bot.DISPLAY_FILE_TG_TYPE_AS_DOCUMENT);
                        }
                        this.arguments = arguments.toArray(new String[]{});
                    } else {
                        this.arguments = new String[]{Bot.HAS_NO_PHOTO};
                    }
                    this.commandConstructor = (Constructor) SendPicture.class.getConstructor();
                    return true;
                case Bot.PIN_VIDEO_COMMAND_SEND_VIDEO:
                    if (hasVideo(message)) {
                        List<String> arguments = new ArrayList<>();
                        arguments.add(Bot.HAS_VIDEO);
                        if (message.getVideo() != null) {
                            arguments.add(message.getVideo().getFileId());
                            arguments.add(Bot.DISPLAY_FILE_TG_TYPE_VIDEO);
                        } else if (message.getDocument().getMimeType().contains("video")) {
                            arguments.add(message.getDocument().getFileId());
                            arguments.add(Bot.DISPLAY_FILE_TG_TYPE_AS_DOCUMENT);
                        }
                        this.arguments = arguments.toArray(new String[]{});
                    } else {
                        this.arguments = new String[]{Bot.HAS_NO_VIDEO};
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
