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

import Config.Bot;
import org.apache.commons.collections.KeyValue;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.lang3.ArrayUtils;

import org.telegram.bot.database.DatabaseManager;
import org.telegram.bot.messages.CommandDescription;
import org.telegram.bot.messages.ContentMessage;
import org.telegram.bot.messages.Message;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Chat;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.bots.AbsSender;
import org.telegram.telegrambots.bots.commands.BotCommand;
import org.telegram.telegrambots.bots.commands.ICommandRegistry;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import org.telegram.telegrambots.logging.BotLogger;

import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * @author Florian Warzecha
 * @version 1.0.1
 * @date 23 of October of 2016
 *
 * This commands gets executed if a user sends '/help' to the bot or if the information about the bot and its
 * commands is needed.
 */
public class HelpCommand extends BotCommand {

    private static final String LOGTAG = "HELPCOMMAND";

    private final ICommandRegistry commandRegistry;

    /**
     * Set the identifier and a short description of this bot.
     * @param commandRegistry
     */
    public HelpCommand(ICommandRegistry commandRegistry) {
        super("help", "Send description of the bot together with a usage guide.");
        this.commandRegistry = commandRegistry;
    }

    /**
     * Iterate through the registered commands and send them together with their description to the user.
     * @param absSender
     * @param user
     * @param chat
     * @param arguments
     */
    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {

        SendMessage answer = new SendMessage();

        try {
            DatabaseManager.getInstance().setUserState(user.getId(), true);

            StringBuilder additionalContentBuilder = new StringBuilder();

            for (BotCommand botCommand : commandRegistry.getRegisteredCommands()) {
                if (!botCommand.getCommandIdentifier().equals("help") &&
                        !botCommand.getCommandIdentifier().equals("start") &&
                        !botCommand.getCommandIdentifier().equals("stop") &&
                        !botCommand.getCommandIdentifier().equals("ids") &&
                        !botCommand.getCommandIdentifier().equals("answer")) {

                    additionalContentBuilder.append("/").append(botCommand.getCommandIdentifier().replace("_",
                            Matcher.quoteReplacement("\\_")));
                    additionalContentBuilder.append(":\n    ");

                    CommandDescription commandDescription =
                            new CommandDescription(botCommand.getCommandIdentifier() + "_command");

                    // check if the command is inside a package or not
                    if (botCommand.getClass().getPackage() != StartCommand.class.getPackage()) {
                        //                        additionalContentBuilder.append(commandDescription.getContent(user.getId(), false));
                        // find out the package name and with its identifier parse it from xml file via CommandDescription class
                        commandDescription.setMessageName(
                                botCommand.getClass().getPackage().getName().replaceAll(
                                        "org.telegram.bot.commands.", ""), botCommand.getCommandIdentifier() +
                                            "_command");
                    }

                    additionalContentBuilder.append(commandDescription.getContent(user.getId(), false));
                    additionalContentBuilder.append("\n");
                }
            }

            if (user.getId().equals(DatabaseManager.getInstance().getAdminUserId())) {
                additionalContentBuilder.append("/").append(commandRegistry.getRegisteredCommand("answer")
                        .getCommandIdentifier()).append(":\n    ").append(commandRegistry.getRegisteredCommand("answer")
                        .getDescription()).append("\n");
            }

            HashMap<String, String> additionalContent = new HashMap<>();
            additionalContent.put("commandDescriptions", additionalContentBuilder.toString());

            ContentMessage contentMessage = new ContentMessage(this.getCommandIdentifier() + "_command");
            contentMessage.setAdditionalContent(additionalContent);

//            BotLogger.info(LOGTAG, contentMessage.getContent(user.getId(), false));

            answer.setChatId(chat.getId().toString());
            answer.enableMarkdown(true);
            answer.setText(contentMessage.getContent(user.getId(), false));

        } catch (Exception e) {
            BotLogger.error(LOGTAG, e);

            new SendOnErrorOccurred().execute(absSender, user, chat, new String[]{LOGTAG});
        }

        try {
            absSender.sendMessage(answer);
        } catch (TelegramApiException e) {
            BotLogger.error(LOGTAG, e);
        }
    }
}
