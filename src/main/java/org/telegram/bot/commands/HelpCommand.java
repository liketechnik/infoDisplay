/*
 * Copyright (C) 2016  liketechnik <flowa2000@gmail.com>
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
 * TelegramBots API is licensed under GNU General Public License version 3 <https://github.com/rubenlagus/TelegramBots>.
 *
 * infoDisplay uses parts of the Apache Commons project <https://commons.apache.org/>.
 * Apache commons is licensed under the Apache License Version 2.0 <http://www.apache.org/licenses/>.
 */

package org.telegram.bot.commands;

import org.apache.commons.collections.KeyValue;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.telegram.bot.Config;
import org.telegram.bot.database.DatabaseManager;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Chat;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.bots.AbsSender;
import org.telegram.telegrambots.bots.commands.BotCommand;
import org.telegram.telegrambots.bots.commands.ICommandRegistry;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import org.telegram.telegrambots.logging.BotLogger;

import java.awt.event.KeyEvent;
import java.io.IOException;

import static org.telegram.bot.Main.sendOnErrorOccurred;

/**
 * @author liketechnik
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
        super("help", "Wozu dient dieser Bot und wie kannst du den Bot kontrollieren?");
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

            StringBuilder messageBuilder = new StringBuilder();

            messageBuilder.append("<b>Hilfe</b>").append("\n\n");
            messageBuilder.append("Beschreibung:").append("\n");
            messageBuilder.append("Mit diesem Bot kannst Du Bilder an das virtuelle Brett hochladen. Als Administrator " +
                    "kannst du auch veraltete Bilder anderer löschen und deren Eigenschaften bearbeiten.").append("\n\n");

            messageBuilder.append("So kannst du mich nutzen:").append("\n");

            for (BotCommand botCommand : commandRegistry.getRegisteredCommands()) {
                if (!botCommand.getCommandIdentifier().equals("help") &&
                        !botCommand.getCommandIdentifier().equals("start") &&
                        !botCommand.getCommandIdentifier().equals("stop") &&
                        !botCommand.getCommandIdentifier().equals("ids") &&
                        !botCommand.getCommandIdentifier().equals("answer")) {

                    messageBuilder.append("/").append(botCommand.getCommandIdentifier()).append(":\n    ")
                            .append(botCommand.getDescription()).append("\n");
                }
            }

            if (user.getId().equals(Config.Bot.ADMIN_USER_ID)) {
                messageBuilder.append("/").append(commandRegistry.getRegisteredCommand("answer")
                        .getCommandIdentifier()).append(":\n    ").append(commandRegistry.getRegisteredCommand("answer")
                        .getDescription()).append("\n");
            }

            answer.setChatId(chat.getId().toString());
            answer.enableHtml(true);
            answer.setText(messageBuilder.toString());

        } catch (Exception e) {
            BotLogger.error(LOGTAG, e);

            answer.setText("Der Bot ist aufgrund eines Fehlers beendet worden. Bitte informiere den Administrator.");

            try {
                absSender.sendMessage(answer);
            } catch (TelegramApiException e1) {
                BotLogger.error(LOGTAG, e1);
            }

            System.exit(1);
        }

        try {
            absSender.sendMessage(answer);
        } catch (TelegramApiException e) {
            BotLogger.error(LOGTAG, e);
        }
    }
}
