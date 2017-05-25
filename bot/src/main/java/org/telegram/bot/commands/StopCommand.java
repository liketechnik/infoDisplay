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

package org.telegram.bot.commands;

import org.telegram.bot.api.SendMessages;
import org.telegram.bot.database.DatabaseException;
import org.telegram.bot.database.DatabaseManager;
import org.telegram.bot.messages.ContentMessage;
import org.telegram.telegrambots.api.objects.Chat;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.bots.AbsSender;
import org.telegram.telegrambots.bots.commands.BotCommand;
import org.telegram.telegrambots.logging.BotLogger;

import java.util.HashMap;
import java.util.Optional;

import static org.telegram.bot.Main.getFilteredUsername;

/**
 * @author Florian Warzecha
 * @version 1.0.1
 * @date 23 of October of 2016
 *
 * this command gets executed if a user stops the bot.
 */
public class StopCommand extends BotCommand {

    public static final String LOGTAG = "STOPCOMMAND";

    /**
     * Set the identifier and a short description.
     */
    public StopCommand() {
        super("stop", "Stops the bot from interacting with you.");
    }

    /**
     * Save that the user is inactive now and tell him goodbye.
     * @param absSender
     * @param user
     * @param chat
     * @param arguments
     */
    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {

        try {
            DatabaseManager databaseManager = DatabaseManager.getInstance();


            try {
                databaseManager.setUserState(user.getId(), false);
            } catch (DatabaseException e) {
                BotLogger.error(LOGTAG, "Error saving new user state for user: " + user.getId(), e);
            }
            // Attention: This needs to be changed if we're going to work with the user active/inactive state

            try {
                databaseManager.setUserCommandState(user.getId(), Config.Bot.NO_COMMAND);
            } catch (DatabaseException e) {
                BotLogger.error(LOGTAG, e);
            }

            HashMap<String, String> additionalContent  = new HashMap<String, String>();
            additionalContent.put("userName", getFilteredUsername(user));

            ContentMessage contentMessage = new ContentMessage(this.getCommandIdentifier() + "_command");
            contentMessage.setAdditionalContent(additionalContent);


            String messageText = contentMessage.getContent(user.getId(), false);
            SendMessages.getInstance().addMessage(contentMessage.calculateHash(), messageText, chat.getId().toString(), absSender, Optional.empty(), Optional.empty());

        } catch (InterruptedException e) {
            BotLogger.error(LOGTAG, e);

            new SendOnErrorOccurred().execute(absSender, user, chat, new String[]{LOGTAG});

            return;
        }
    }
}
