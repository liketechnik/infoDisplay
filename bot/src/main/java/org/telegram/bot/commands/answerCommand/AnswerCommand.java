/*
 * Copyright (C) 2017  Florian Warzecha <flowa2000@gmail.com>
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

package org.telegram.bot.commands.answerCommand;

import org.apache.commons.configuration2.ex.ConfigurationException;

import org.telegram.bot.commands.SendOnErrorOccurred;
import org.telegram.bot.database.DatabaseManager;
import org.telegram.bot.messages.Message;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Chat;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.bots.AbsSender;
import org.telegram.telegrambots.bots.commands.BotCommand;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import org.telegram.telegrambots.logging.BotLogger;

import javax.xml.crypto.Data;


/**
 * @author Florian Warzecha
 * @version 1.0.1
 * @date 25 of October of 2016
 *
 * This command gets executed if  a user sends '/answer' to the bot and lets the administrator answer asked questions.
 */
public class AnswerCommand extends BotCommand {

    public final String LOGTAG = "ANSWERCOMMAND";

    /**
     * Set the identifier for the command and a short description.
     */
    public AnswerCommand() {
        super("answer", "Answer to questions of users.");
    }

    /**
     * Evaluate a message send by a user.
     * This is done by sending the user all questions that has not been answered yet and asking him to choose one.
     * @param absSender
     * @param user
     * @param chat
     * @param arguments
     */
    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {

        String message;
        StringBuilder questionsBuilder = new StringBuilder();
        SendMessage answer = new SendMessage();

        try {
            DatabaseManager databaseManager = DatabaseManager.getInstance();

            if (!user.getId().equals(databaseManager.getAdminUserId())) {
                return;
            }


            int questions = 1;

            for (String question : databaseManager.getQuestions()) {
                questionsBuilder.append(questions).append(". ").append(question).append("\n");
                questions++;
            }

            message = Message.answerCommand.getAnswerMessage(user, questionsBuilder.toString());

            answer.setChatId(chat.getId().toString());
            answer.setText(message);

            databaseManager.setUserCommandState(user.getId(), Config.Bot.ANSWER_COMMAND_CHOOSE_NUMBER);
        } catch (Exception e) {
            BotLogger.error(LOGTAG, e);

            new SendOnErrorOccurred().execute(absSender, user, chat, new String[]{LOGTAG});

            return;
        }

        try {
            absSender.sendMessage(answer);
        } catch (TelegramApiException e) {
            BotLogger.error(LOGTAG, e);
        }
    }
}
