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
import org.telegram.bot.messages.ContentMessage;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Chat;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.bots.AbsSender;
import org.telegram.telegrambots.bots.commands.BotCommand;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import org.telegram.telegrambots.logging.BotLogger;

import java.util.HashMap;


/**
 * @author Florian Warzecha
 * @version 1.0.1
 * @date 24 of October of 2016
 *
 * This command gets executed if a user sent '/ids'.
 */
public class IDCommand extends BotCommand {

    public static final String LOGTAG = "IDCOMMAND";

    /**
     * Set the identifier and a short description.
     */
    public IDCommand() {
        super("ids", "Send the Telegram user and chat ID.");
    }

    /**
     * Get the chat and user id of the user and send them back to him.
     * @param absSender
     * @param user
     * @param chat
     * @param arguments
     */
    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {

        try {
            HashMap<String, String> additionalContent  = new HashMap<>();
            additionalContent.put("userId", user.getId().toString());
            additionalContent.put("chatId", chat.getId().toString());

            ContentMessage contentMessage = new ContentMessage(this.getCommandIdentifier() + "_command");
            contentMessage.setAdditionalContent(additionalContent);

            String messageText = contentMessage.getContent(user.getId(), true);
            SendMessages.getInstance().addMessage(contentMessage.calculateHash(), messageText, chat.getId().toString(), absSender);

        } catch (Exception e) {
            BotLogger.error(LOGTAG, e);

            new SendOnErrorOccurred().execute(absSender, user, chat, new String[]{LOGTAG});

            return;
        }
    }
}
