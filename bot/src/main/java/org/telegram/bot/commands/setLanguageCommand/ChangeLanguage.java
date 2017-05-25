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

import Config.CallbackData;
import Config.Languages;
import org.telegram.bot.api.SendMessages;
import org.telegram.bot.commands.SendOnErrorOccurred;
import org.telegram.bot.database.DatabaseException;
import org.telegram.bot.database.DatabaseManager;
import org.telegram.bot.messages.SituationalMessage;
import org.telegram.telegrambots.api.objects.Chat;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.bots.AbsSender;
import org.telegram.telegrambots.bots.commands.BotCommand;
import org.telegram.telegrambots.logging.BotLogger;

import java.util.Optional;

/**
 * @author liketechnik
 * @version 1.0
 * @date 25 of March 2017
 */
public class ChangeLanguage extends BotCommand {

    public final String LOGTAG = "CHANGELANGUAGE";

    public ChangeLanguage() {
        super("change_language", "Change the language of a user corresponding to the callback data.");
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {

        try {

            DatabaseManager databaseManager = DatabaseManager.getInstance();

            SituationalMessage message = new SituationalMessage(this.getCommandIdentifier() + "_command");
            message.setMessageName(this.getClass().getPackage().getName()
                            .replaceAll("org.telegram.bot.commands.", ""), this.getCommandIdentifier() + "_command",
                    "language");

            if (arguments[0].equals(CallbackData.SET_LANGUAGE.set_language_english.name())) {
                databaseManager.setUserLanguage(user.getId(), Languages.ENGLISH);
            } else if (arguments[0].equals(CallbackData.SET_LANGUAGE.set_language_german.name())) {
                databaseManager.setUserLanguage(user.getId(), Languages.GERMAN);
            }

            if (arguments[0].equals(CallbackData.SET_LANGUAGE.set_language_default.name())) {
                databaseManager.setUserLanguage(user.getId(), Languages.NONE);

                message.setMessageName(this.getClass().getPackage().getName()
                                .replaceAll("org.telegram.bot.commands.", ""), this.getCommandIdentifier() + "_command",
                        "default");

            }

            String messageText = message.getContent(user.getId(), true);
            SendMessages.getInstance().addEditMessage(message.calculateHash(), messageText, chat.getId().toString(),
                    absSender, Integer.valueOf(arguments[1]), Optional.empty(), Optional.empty());
        } catch (DatabaseException | InterruptedException e) {
            BotLogger.error(LOGTAG, e);

            new SendOnErrorOccurred().execute(absSender, user, chat, new String[]{LOGTAG, arguments[2]});

            return;
        }
    }
}
