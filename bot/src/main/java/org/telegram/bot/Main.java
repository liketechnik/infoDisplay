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

import org.telegram.bot.api.SendMessages;
import org.telegram.bot.commands.CancelCommand;
import org.telegram.bot.commands.HelpCommand;
import org.telegram.telegrambots.ApiContext;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Chat;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.bots.AbsSender;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import org.telegram.telegrambots.generics.BotSession;
import org.telegram.telegrambots.logging.BotLogger;
import org.telegram.telegrambots.logging.BotsFileHandler;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;

/**
 * @author Florian Warzecha
 * @version 1.0.1
 * @date 21 of October of 2016
 */
public class Main {
    private static final String LOGTAG = "MAIN";


    /* Set up the logger and register the bot */
    public static void main (String args[]) {
        BotLogger.setLevel(Level.ALL);
        BotLogger.registerLogger(new ConsoleHandler());
        try {
            BotLogger.registerLogger(new BotsFileHandler());
        } catch (IOException e) {
            BotLogger.severe(LOGTAG, e);
        }

        ApiContextInitializer.init();

        try {
            TelegramBotsApi telegramBotsApi = createTelegramBotsApi();
            try {
                BotSession displayBot = telegramBotsApi.registerBot(new DisplayBot());
                BotLogger.info(LOGTAG, "Starting bot now!");
                SendMessages.getInstance().start();
                while (Files.notExists(FileSystems.getDefault().getPath("./stop"))) {
                    Thread.sleep(5000);
                }
                displayBot.stop();
                //System.exit(0);
            } catch (TelegramApiException e) {
                BotLogger.error(LOGTAG, e);
            }
        } catch (Exception e) {
            BotLogger.error(LOGTAG, e);
        }
    }

    /**
     * Create new {@link TelegramBotsApi}.
     * @return {@link TelegramBotsApi}
     * @throws TelegramApiException An error in the API, for example network problems.
     */
    private static TelegramBotsApi createTelegramBotsApi() throws TelegramApiException {
        TelegramBotsApi telegramBotsApi;
        telegramBotsApi = createLongPollingTelegramBotsApi();
        return telegramBotsApi;
    }

    /**
     * Create new {@link TelegramBotsApi long polling TelegramBotsApi}.
     * @return {@link TelegramBotsApi long polling TelegramBotsApi}
     */
    private static TelegramBotsApi createLongPollingTelegramBotsApi() {
        return new TelegramBotsApi();
    }

    /**
     * Checks the different possibilities for a username and returns the preferred one.
     * If a user has his last and his first name defined, both are returned. If the last name
     * is missing, only the first name is returned. If no user name is defined, the userID is
     * used as Name.
     * @param user The user from who we want to know the username.
     * @return The username, preferred as combination of his first and last name.
     */
    public static String getFilteredUsername(User user) {
        if (user.getLastName() != null && user.getFirstName() != null) {
            return user.getFirstName() + " " + user.getLastName();
        } else if (user.getLastName() == null) {
            return user.getFirstName();
        } else {
            if (user.getUserName() != null) {
                return user.getUserName();
            } else {
                return user.getId().toString();
            }
        }
    }

    /**
     * Checks the different possibilities for a username and returns the preferred one.
     * If a user has defined a telegram username, this username is returned. Otherwise the first name is
     * returned, if possible in addition of the last name. If nothing is found, the UserID is used.
     * @param user The user from who we want to know the username.
     * @return The username, preferred the telegram username.
     */
    public static String getSpecialFilteredUsername(User user) {
        StringBuilder usernameBuilder = new StringBuilder();

        if(user.getUserName() != null) {
            usernameBuilder.append("@").append(user.getUserName());
        } else if(user.getFirstName() != null && user.getLastName() != null) {
            usernameBuilder.append(user.getFirstName()).append(" ").append(user.getLastName());
        } else if(user.getFirstName() != null) {
            usernameBuilder.append(user.getFirstName());
        } else {
            usernameBuilder.append(user.getId());
        }

        return  usernameBuilder.toString();
    }

    /**
     * This method is called when an error occurs in one of the bot commands.
     * It tells the user about the occurrence of an error and prints out the help message.
     * @param absSender Needed to send a message to the user.
     * @param user The user the message should go to.
     * @param chat The chat the message should be send to.
     * @param LOGTAG The LOGTAG of the command the error occurred in.
     */
    public static void sendOnErrorOccurred(AbsSender absSender, User user, Chat chat, String LOGTAG) {

        StringBuilder messageBuilder = new StringBuilder();
        SendMessage answer = new SendMessage();

        messageBuilder.append("Es ist ein interner Fehler aufgetreten, bitte informiere den Administrator dieses " +
                "Bots darüber.").append("\n").append("/help");

        answer.setChatId(chat.getId().toString());
        answer.setText(messageBuilder.toString());

        try {
            absSender.sendMessage(answer);
        } catch (TelegramApiException e) {
            BotLogger.error(LOGTAG, e);
        }

        new CancelCommand().execute(absSender, user, chat, new String[]{});
    }

    public static Exception mergeExceptions(Exception[] exceptions) {
        Exception exception = exceptions[0];
        for (int x = 1; x < exceptions.length; x++) {
            exception.addSuppressed(exceptions[x]);
        }
        return exception;
    }
}

