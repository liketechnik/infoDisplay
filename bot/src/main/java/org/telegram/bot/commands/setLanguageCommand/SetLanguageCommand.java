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

package org.telegram.bot.commands.setLanguageCommand;

import org.telegram.bot.api.SendMessages;
import org.telegram.bot.commands.SendOnErrorOccurred;
import org.telegram.bot.messages.InlineKeyboard;
import org.telegram.telegrambots.api.objects.Chat;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.bots.AbsSender;
import org.telegram.telegrambots.bots.commands.BotCommand;
import org.telegram.telegrambots.logging.BotLogger;

import java.util.Optional;

/**
 * Let the user change the language of the messages from the bot.
 * @author Florian Warzecha
 * @version 1.0
 * @date 24 of March 2017
 */
public class SetLanguageCommand extends BotCommand {

    public static final String LOGTAG = "SETLANGUAGECOMMAND";

    /**
     * Set identifier and a short description of this command.
     */
    public SetLanguageCommand() {
        super("set_language", "Choose the language the bot uses to respond.");
    }

    /**
     * Send a keyboard to the user which presents him the available languages.
     * @param absSender
     * @param user
     * @param chat
     * @param arguments
     */
    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {

        try {

            org.telegram.bot.messages.Message message = new org.telegram.bot.messages.Message(this.getCommandIdentifier() +
                "_command");
            message.setMessageName(this.getClass().getPackage().getName()
                    .replaceAll("org.telegram.bot.commands.", ""), this.getCommandIdentifier() + "_command");

            InlineKeyboard inlineKeyboard = new InlineKeyboard(this.getCommandIdentifier() + "_command");
            inlineKeyboard.setMessageName(this.getClass().getPackage().getName()
                    .replaceAll("org.telegram.bot.commands.", ""), this.getCommandIdentifier() + "_command");

            InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
            inlineKeyboardMarkup.setKeyboard(inlineKeyboard.getKeyboard(user.getId()));

            String messageText = message.getContent(user.getId(), false);
            SendMessages.getInstance().addMessage(message.calculateHash(), messageText, chat.getId().toString(), absSender,
                    Optional.empty(), Optional.of(inlineKeyboardMarkup));
        } catch (InterruptedException e) {
            BotLogger.error(LOGTAG, e);

            new SendOnErrorOccurred().execute(absSender, user, chat, new String[]{LOGTAG});

            return;
        }
    }
}