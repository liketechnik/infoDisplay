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
import Config.CallbackData;
import javafx.scene.control.TextFormatter;
import org.apache.commons.io.FileUtils;
import org.telegram.bot.commands.*;
import org.telegram.bot.commands.answerCommand.AnswerCommand;
import org.telegram.bot.commands.answerCommand.ChooseNumber;
import org.telegram.bot.commands.answerCommand.WriteAnswer;
import org.telegram.bot.commands.askCommand.AskCommand;
import org.telegram.bot.commands.askCommand.WriteQuestion;
import org.telegram.bot.commands.pinPictureCommand.*;
import org.telegram.bot.commands.pinPictureCommand.SendDescription;
import org.telegram.bot.commands.pinPictureCommand.SendTitle;
import org.telegram.bot.commands.pinVideoCommand.*;
import org.telegram.bot.commands.setLanguageCommand.ChangeLanguage;
import org.telegram.bot.commands.setLanguageCommand.SetLanguageCommand;
import org.telegram.bot.database.DatabaseManager;
import org.telegram.telegrambots.api.methods.GetFile;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.*;
import org.telegram.telegrambots.bots.TelegramLongPollingCommandBot;
import org.telegram.telegrambots.bots.commands.ICommandRegistry;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import org.telegram.telegrambots.logging.BotLogger;

import javax.security.auth.login.Configuration;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.List;



/**
 * @author Florian Warzecha
 * @version 1.0.1
 * @date 22 of October of 2016
 */
public class DisplayBot extends TelegramLongPollingCommandBot {

    public static final String LOGTAG = "DISPLAYBOT";

    /**
     * Register my commands and set an default action for unknown commands send to the bot.
     * Default action executes the {@link HelpCommand}.
     */
    public DisplayBot() {
        register(new StartCommand());
        register(new StopCommand());
        register(new IDCommand());
        register(new RegisterCommand());
        register(new AdministratorCommand());
        register(new AskCommand());
        register(new AnswerCommand());
        register(new PinPictureCommand());
        register(new PinVideoCommand());
        register(new AboutCommand());
        register(new SetLanguageCommand());
        register(new CancelCommand(this));

        HelpCommand helpCommand = new HelpCommand(this);
        register(helpCommand);

        /**
         * This gets executed when an unknown command is send to the bot.
         * It tells the user that the command is not known and sends it the content of the help command.
         */
        registerDefaultAction(((absSender, message) -> {
            SendMessage commandUnknownMessage = new SendMessage();
            commandUnknownMessage.setChatId(message.getChatId().toString());
            commandUnknownMessage.setText("Der Befehl '" + message.getText() + "' ist diesem Bot nicht bekannt. Hier der " +
                    "Hilfetext:");

            try {
                absSender.sendMessage(commandUnknownMessage);
            } catch (TelegramApiException e) {
                BotLogger.error(LOGTAG, e);
            }

            helpCommand.execute(absSender, message.getFrom(), message.getChat(), new String[] {});
        }));
    }

    /**
     * Returns the {@link ICommandRegistry} of this bot instance.
     * @return The {@link ICommandRegistry} of this bot instance.
     */
    public ICommandRegistry getICommandRegistry() {
        return this;
    }

    /**
     * Gets called when a text message is received, instead of a command.
     * It checks if the user used a command which needs further information by the user and
     * if that is the case, calls the corresponding method and gives it the update.
     * @param update The received update.
     */
    @Override
    public void processNonCommandUpdate(Update update) {


        DatabaseManager databaseManager = DatabaseManager.getInstance();
        try {

            if (update.hasMessage()) {

                if (update.getMessage().hasText()) {
                    if (databaseManager.getUserCommandState(update.getMessage().getFrom().getId())
                            .equals(Config.Bot.ASK_COMMAND_WRITE_QUESTION)) {

                        new WriteQuestion().execute(this, update.getMessage().getFrom(), update.getMessage().getChat(),
                                new String[]{update.getMessage().getText()});


                    } else if (databaseManager.getUserCommandState(update.getMessage().getFrom().getId())
                            .equals(Config.Bot.ANSWER_COMMAND_CHOOSE_NUMBER)) {

                        new ChooseNumber().execute(this, update.getMessage().getFrom(), update.getMessage().getChat(),
                                new String[]{update.getMessage().getText()});

                    } else if (databaseManager.getUserCommandState(update.getMessage().getFrom().getId())
                            .equals(Config.Bot.ANSWER_COMMAND_WRITE_ANSWER)) {

                        new WriteAnswer().execute(this, update.getMessage().getFrom(), update.getMessage().getChat(),
                                new String[]{update.getMessage().getText()});

                    } else if (databaseManager.getUserCommandState(update.getMessage().getFrom().getId())
                            .equals(Config.Bot.PIN_PICTURE_COMMAND_SEND_TITLE)) {

                        new SendTitle().execute(this, update.getMessage().getFrom(), update.getMessage().getChat(),
                                new String[]{update.getMessage().getText()});

                    } else if (databaseManager.getUserCommandState(update.getMessage().getFrom().getId())
                            .equals(Config.Bot.PIN_PICTURE_COMMAND_SEND_DESCRIPTION)) {

                        new SendDescription().execute(this, update.getMessage().getFrom(), update.getMessage().getChat(),
                                new String[]{update.getMessage().getText()});

                    } else if (databaseManager.getUserCommandState(update.getMessage().getFrom().getId())
                            .equals(Config.Bot.PIN_PICTURE_COMMAND_SEND_DURATION)) {

                        new SendDuration().execute(this, update.getMessage().getFrom(), update.getMessage().getChat(),
                                new String[]{update.getMessage().getText()});

                    } else if (databaseManager.getUserCommandState(update.getMessage().getFrom().getId())
                            .equals(Bot.PIN_PICTURE_COMMAND_SEND_PICTURE)) {

                        new SendPicture().execute(this, update.getMessage().getFrom(), update.getMessage().getChat(),
                                new String[]{Bot.HAS_NO_PHOTO});

                    } else if (databaseManager.getUserCommandState(update.getMessage().getFrom().getId())
                            .equals(Bot.PIN_VIDEO_COMMAND_SEND_VIDEO)) {

                        new SendVideo().execute(this, update.getMessage().getFrom(), update.getMessage().getChat(),
                                new String[]{Bot.HAS_NO_VIDEO});

                    } else if (databaseManager.getUserCommandState(update.getMessage().getFrom().getId())
                            .equals(Bot.PIN_VIDEO_COMMAND_SEND_DESCRIPTION)) {

                        new org.telegram.bot.commands.pinVideoCommand.SendDescription().execute(this,
                                update.getMessage().getFrom(), update.getMessage().getChat(),
                                new String[]{update.getMessage().getText()});

                    } else if (databaseManager.getUserCommandState(update.getMessage().getFrom().getId())
                            .equals(Bot.PIN_VIDEO_COMMAND_SEND_TITLE)) {

                        new org.telegram.bot.commands.pinVideoCommand.SendTitle().execute(this,
                                update.getMessage().getFrom(), update.getMessage().getChat(),
                                new String[]{update.getMessage().getText()});

                    }
                } else if (update.getMessage().hasPhoto() || hasPhoto(update.getMessage())) {
                    if (databaseManager.getUserCommandState(update.getMessage().getFrom().getId())
                            .equals(Bot.PIN_PICTURE_COMMAND_SEND_PICTURE)) {

                        String fileId = null;

                        if (update.getMessage().hasPhoto()) {

                            List<PhotoSize> photos = update.getMessage().getPhoto();

                            int width = 0;
                            int height = 0;

                            int biggestPhoto = 0;


                            for (int x = 0; x < photos.size(); x++) {
                                if (width < photos.get(x).getWidth() || height < photos.get(x).getHeight()) {
                                    biggestPhoto = x;
                                }
                            }

                            fileId = photos.get(biggestPhoto).getFileId();
                        } else if (hasPhoto(update.getMessage())) {
                            fileId = update.getMessage().getDocument().getFileId();
                        }

                        new SendPicture().execute(this, update.getMessage().getFrom(), update.getMessage().getChat(),
                                new String[]{Config.Bot.HAS_PHOTO, fileId});
                    }

                } else if (hasVideo(update.getMessage())) {
                    if (databaseManager.getUserCommandState(update.getMessage().getFrom().getId())
                            .equals(Bot.PIN_VIDEO_COMMAND_SEND_VIDEO)) {

                        new SendVideo().execute(this, update.getMessage().getFrom(), update.getMessage().getChat(),
                                new String[]{Bot.HAS_VIDEO, update.getMessage().getDocument().getFileId()});
                    }
                }
//                else if (databaseManager.getUserCommandState(update.getMessage().getFrom().getId())
//                        .equals(Config.Bot.PIN_PICTURE_COMMAND_SEND_PICTURE)) {
//
//                    if (update.getMessage().getPhoto() != null) {
//
//                        List<PhotoSize> photos = update.getMessage().getPhoto();
//
//                        int width = 0;
//                        int height = 0;
//
//                        int biggestPhoto = 0;
//
//
//                        for (int x = 0; x < photos.size(); x++) {
//                            if (width < photos.get(x).getWidth() || height < photos.get(x).getHeight()) {
//                                biggestPhoto = x;
//                            }
//                        }
//
//                        new SendPicture().execute(this, update.getMessage().getFrom(), update.getMessage().getChat(),
//                                new String[]{Config.Bot.HAS_PHOTO, photos.get(biggestPhoto).getFileId()});
//                    } else if (update.getMessage().getDocument() != null &&
//                            (update.getMessage().getDocument().getMimeType().equals("image/jpeg") ||
//                                    update.getMessage().equals("image/png"))) {
//
//                        if (update.getMessage().getDocument() != null &&
//                                (update.getMessage().getDocument().getMimeType().equals("image/jpeg") ||
//                                        update.getMessage().equals("image/png"))) {
//
//                            new SendPicture().execute(this, update.getMessage().getFrom(), update.getMessage().getChat(),
//                                    new String[]{Config.Bot.HAS_PHOTO,
//                                            update.getMessage().getDocument().getFileId().toString()});
//                        } else {
//                            new SendPicture().execute(this, update.getMessage().getFrom(), update.getMessage().getChat(),
//                                    new String[]{Config.Bot.HAS_NO_PHOTO});
//                        }
//
//                    } else {
//                        new SendPicture().execute(this, update.getMessage().getFrom(), update.getMessage().getChat(),
//                                new String[]{Config.Bot.HAS_NO_PHOTO});
//                    }
//                }
            } else if (update.hasCallbackQuery()) {
                if (databaseManager.getUserCommandState(update.getMessage().getFrom().getId())
                        .equals(Bot.SET_LANGUAGE_COMMAND)) {
                    CallbackQuery callbackQuery = update.getCallbackQuery();

                    if (callbackQuery.getData().equals(CallbackData.SET_LANGUAGE_DEFAULT)
                            || callbackQuery.getData().equals(CallbackData.SET_LANGUAGE_GERMAN)
                            || callbackQuery.getData().equals(CallbackData.SET_LANGUAGE_ENGLISH)) {
                        new ChangeLanguage().execute(this, callbackQuery.getFrom(), new Chat(), new String[]{callbackQuery.getData(), callbackQuery.getMessage().getMessageId().toString(), callbackQuery.getChatInstance()});
                    }
                }
            }
        } catch (Exception e) {
            BotLogger.error("PROCESSNONCOMMANDUPDATE", e);
        }
    }

    /**
     * Gets called if the username of the bot is needed.
     * The returned value is defined in the config file.
     * @return The bot's username.
     */
    @Override
    public String getBotUsername() {
        try {
            return DatabaseManager.getInstance().getBotUsername();
        } catch (Exception e) {
            BotLogger.error(this.LOGTAG, "Error getting bot's username.", e);
            System.exit(1);
        }
        return null;
    }

    /**
     * Gets called if the token of the bot is needed.
     * The returned value is defined in the config file.
     * @return The bot's token
     */
    @Override
    public String getBotToken() {
        try {
            return DatabaseManager.getInstance().getBotToken();
        } catch (Exception e) {
            BotLogger.error(this.LOGTAG, "Error getting bot's token.", e);
        }
        return null;
    }

    private boolean hasPhoto(Message message) {
        if (message.hasDocument()) {
            if (message.getDocument().getMimeType().contains("image")) {
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
