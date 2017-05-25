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

package org.telegram.bot.api;

import org.telegram.bot.commands.HelpCommand;
import org.telegram.bot.messages.Message;
import org.telegram.telegrambots.api.objects.Chat;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.bots.commands.BotCommand;
import org.telegram.telegrambots.logging.BotLogger;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;

/**
 * @author Florian Warzecha
 * @version 1.0
 * @date 13 of April 2017
 */
public abstract class Parser implements Runnable {

    public final String LOGTAG = "PARSER";

    protected Update update;
    protected TelegramLongPollingThreadBot bot;

    protected Constructor<BotCommand> commandConstructor;

    protected User user;
    protected Chat chat;
    protected String[] arguments;

    public Parser(Update update, TelegramLongPollingThreadBot bot) {
        this.update = update;
        this.bot = bot;
    }

    /**
     * Create a new instance of a BotCommand and run it.
     */
    protected void executeCommand() {
        try {
            BotCommand command = this.commandConstructor.newInstance();
            command.execute(this.bot, this.user, this.chat, this.arguments); // every bot is an absSender
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
            BotLogger.error(LOGTAG, e);
        }
    }

    /**
     * Parse a new update.
     *
     * Parses a new update for information on how to set parameters of a command and the choice of the command.
     * @return If the message was successfully parsed.
     */
    protected abstract boolean parse();


    public void run() {
        if (!this.parse()) {
            Message message = new Message("unknown");
            String messageText = message.getContent(user.getId(), false);
            try {
                SendMessages.getInstance().addMessage(message.calculateHash(), messageText, chat.getId().toString(), this.bot, Optional.empty(), Optional.empty());
            } catch (InterruptedException e) {
                BotLogger.error(LOGTAG, e);
            }

            this.arguments = new String[]{};
            try {
                this.commandConstructor = (Constructor) HelpCommand.class.getConstructor();
            } catch (NoSuchMethodException e) {
                BotLogger.error(LOGTAG, e);
            }
        }
        this.executeCommand();
    }

}
