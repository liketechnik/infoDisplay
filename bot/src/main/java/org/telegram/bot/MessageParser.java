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
import org.telegram.bot.api.TelegramLongPollingThreadBot;
import org.telegram.bot.api.Parser;
import org.telegram.bot.commands.CancelCommand;
import org.telegram.bot.commands.StartCommand;
import org.telegram.bot.commands.StopCommand;
import org.telegram.bot.commands.answerCommand.ChooseNumber;
import org.telegram.bot.commands.answerCommand.WriteAnswer;
import org.telegram.bot.commands.askCommand.WriteQuestion;
import org.telegram.bot.commands.pinPictureCommand.SendDescription;
import org.telegram.bot.commands.pinPictureCommand.SendDuration;
import org.telegram.bot.commands.pinPictureCommand.SendPicture;
import org.telegram.bot.commands.pinPictureCommand.SendTitle;
import org.telegram.bot.commands.pinVideoCommand.SendVideo;
import org.telegram.bot.database.DatabaseException;
import org.telegram.bot.database.DatabaseManager;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.logging.BotLogger;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

/**
 * @author Florian Warzecha
 * @version 1.0
 * @date 14 of April 2017
 */
public class MessageParser extends Parser {

    public final String LOGTAG = "MESSAGEPARSER";

    public MessageParser(Update update, TelegramLongPollingThreadBot bot) {
        super(update, bot);
    }

    /** Parsing taken from {@link org.telegram.telegrambots.bots.commands.CommandRegistry} **/
    @Override
    protected boolean parse() {
        Message message = this.update.getMessage();
        this.user = message.getFrom();
        this.chat = message.getChat();

        String text = message.getText();
        // Check if the text message calls a registered command
        if (text.startsWith("/")) {
            String command = text.substring(1);
            String[] commandSplit = command.split(" ");

            try {
                // check if either the user sent a new, known command or he sent the cancel command
//                if ((this.bot.getCommandsMap().containsKey(commandSplit[0]) &&
//                        DatabaseManager.getInstance().getUserCommandState(this.user.getId()).equals(Bot.NO_COMMAND))
//                        || commandSplit[0].equals(CancelCommand.class.getConstructor().newInstance().getCommandIdentifier())
//                        || commandSplit[0].equals(StartCommand.class.getConstructor().newInstance().getCommandIdentifier())
//                        || commandSplit[0].equals(StopCommand.class.getConstructor().newInstance().getCommandIdentifier())) {
                String userC = DatabaseManager.getInstance().getUserCommandState(user.getId());
                if (DatabaseManager.getInstance().getUserCommandState(user.getId()).equals(Bot.NO_COMMAND)
                        || commandSplit[0].equals(CancelCommand.class.getConstructor().newInstance().getCommandIdentifier())
                        || commandSplit[0].equals(StartCommand.class.getConstructor().newInstance().getCommandIdentifier())
                        || commandSplit[0].equals(StopCommand.class.getConstructor().newInstance().getCommandIdentifier())) {
                    if (this.bot.getCommandsMap().containsKey(commandSplit[0])) {
                        this.arguments = Arrays.copyOfRange(commandSplit, 1, commandSplit.length);
                        this.commandConstructor = this.bot.getRegisteredCommand(commandSplit[0]);
                        return true;
                    }
                }
            } catch (DatabaseException | IllegalAccessException | InstantiationException | InvocationTargetException | NoSuchMethodException e) {
                BotLogger.error(LOGTAG, e);
            }
            return false;
        // Check if the user executed a command and the message is a response
        } else {
            try {
                String userCommandState = DatabaseManager.getInstance().getUserCommandState(this.user.getId());
                if (userCommandState.equals(Bot.NO_COMMAND)) {
                    return false;
                }

                this.arguments = new String[]{text};

                switch (userCommandState) {
                    case Bot.ASK_COMMAND_WRITE_QUESTION:
                        this.commandConstructor = (Constructor) WriteQuestion.class.getConstructor();
                        break;
                    case Bot.ANSWER_COMMAND_CHOOSE_NUMBER:
                        this.commandConstructor = (Constructor) ChooseNumber.class.getConstructor();
                        break;
                    case Bot.ANSWER_COMMAND_WRITE_ANSWER:
                        this.commandConstructor = (Constructor) WriteAnswer.class.getConstructor();
                        break;
                    case Bot.PIN_PICTURE_COMMAND_SEND_TITLE:
                        this.commandConstructor = (Constructor) SendTitle.class.getConstructor();
                        break;
                    case Bot.PIN_PICTURE_COMMAND_SEND_DESCRIPTION:
                        this.commandConstructor = (Constructor) SendDescription.class.getConstructor();
                        break;
                    case Bot.PIN_PICTURE_COMMAND_SEND_DURATION:
                        this.commandConstructor = (Constructor) SendDuration.class.getConstructor();
                        break;
                    case Bot.PIN_PICTURE_COMMAND_SEND_PICTURE:
                        this.arguments = new String[]{Bot.HAS_NO_PHOTO};
                        this.commandConstructor = (Constructor) SendPicture.class.getConstructor();
                        break;
                    case Bot.PIN_VIDEO_COMMAND_SEND_VIDEO:
                        this.arguments = new String[]{Bot.HAS_NO_VIDEO};
                        this.commandConstructor = (Constructor) SendVideo.class.getConstructor();
                        break;
                    case Bot.PIN_VIDEO_COMMAND_SEND_DESCRIPTION:
                        this.commandConstructor = (Constructor) org.telegram.bot.commands.pinVideoCommand.
                                SendDescription.class.getConstructor();
                        break;
                    case Bot.PIN_VIDEO_COMMAND_SEND_TITLE:
                        this.commandConstructor = (Constructor) org.telegram.bot.commands.pinVideoCommand.
                                SendTitle.class.getConstructor();
                        break;
                }

                return true;
            } catch (DatabaseException | NoSuchMethodException e) {
                BotLogger.error(LOGTAG, e);
                return false;
            }
        }
    }
}
