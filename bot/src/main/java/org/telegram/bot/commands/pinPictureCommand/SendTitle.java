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

package org.telegram.bot.commands.pinPictureCommand;


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

import java.nio.file.FileAlreadyExistsException;
import java.util.Optional;

/**
 * @author Florian Warzecha
 * @version 1.0.1
 * @date 01 of November 2016
 *
 * This class handles an incoming message containing the title for a new picture.
 */
public class SendTitle extends BotCommand {

    public static final String LOGTAG = "PINPICTURECOMMAND_SENDTITLE";

    /**
     * Set the identifier for the command and a short description.
     */
    public SendTitle() {
        super("send_title", "Evaluates the answer after a user executed " +
                "/pin_picture.");
    }

    /**
     * Evaluate a message send by a user.
     * This is done by saving the title sent by the user and telling him to proceed with the next step (sending the
     * description). If a file with the sent name already exists the user is asked to choose another one.
     * @param absSender
     * @param user
     * @param chat
     * @param arguments
     */
    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {

        try {

            DatabaseManager databaseManager = DatabaseManager.getInstance();

            String message = arguments[0];
            String displayFileName;
            displayFileName = message + ".jpg";

            SituationalMessage situationalMessage = new SituationalMessage(this.getCommandIdentifier() + "_command");
            situationalMessage.setMessageName(this.getClass().getPackage().getName()
                    .replaceAll("org.telegram.bot.commands.", ""), this.getCommandIdentifier() + "_command",
                    "new_name");

            try {
                databaseManager.setCurrentPictureTitle(user.getId(), displayFileName);
                databaseManager.setUserCommandState(user.getId(), Config.Bot.PIN_PICTURE_COMMAND_SEND_DESCRIPTION);
            } catch (FileAlreadyExistsException e) {
                situationalMessage.setMessageName(this.getClass().getPackage().getName()
                                .replaceAll("org.telegram.bot.commands.", ""),
                        this.getCommandIdentifier() + "_command","already_used");
            }

            String messageText = situationalMessage.getContent(user.getId(), false);
            SendMessages.getInstance().addMessage(situationalMessage.calculateHash(), messageText, chat.getId().toString(), absSender, Optional.empty(), Optional.empty());
        } catch (DatabaseException | InterruptedException e) {
            BotLogger.error(LOGTAG, e);

            new SendOnErrorOccurred().execute(absSender, user, chat, new String[]{LOGTAG});

            return;
        }
    }
}