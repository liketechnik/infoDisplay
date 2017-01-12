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

/**
 * @author Florian Warzecha
 * @version 1.0.1
 * @date 01 of November of 2016
 *
 * This command gets executed if a user executed the {@link AnswerCommand}. It sends the user the requested question.
 */
public class ChooseNumber extends BotCommand {

    public static final String LOGTAG = "ANSWERCOMMAND_CHOOSENUMBER";

    /**
     * Set the identifier and a short description for the command.
     */
    public  ChooseNumber() {
        super("choose_number",
                "Evaluate the answer of user who executed /answer.");
    }

    /**
     * Evaluate the message send by a user.
     * This is done by checking if the selected question is available and sending the user the status back.
     * If the check is successful an entry is added to the users configuration file. If the check is unsuccessful
     * the user gets informed about it and needs to choose a different question.
     * @param absSender
     * @param user
     * @param chat
     * @param arguments
     */
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {

        SendMessage answer = new SendMessage();

        try {
            int selectedQuestion;

            String message = arguments[0];
            DatabaseManager databaseManager = DatabaseManager.getInstance();

            StringBuilder messageBuilder = new StringBuilder();

            try {
                selectedQuestion = Integer.parseInt(message);
                if (selectedQuestion - 1 >= databaseManager.getNumberOfQuestions()) {
                    throw new NumberFormatException("This question is not available.");
                }
            } catch (NumberFormatException e) {
                messageBuilder.append(Message.answerCommand.getChooseNumberMessage(user));

                answer.setChatId(chat.getId().toString());
                answer.setText(messageBuilder.toString());

                try {
                    absSender.sendMessage(answer);
                } catch (TelegramApiException e1) {
                    BotLogger.error(LOGTAG, e1);
                }

                return;
            }

            databaseManager.setUserCommandState(user.getId(),
                    Config.Bot.ANSWER_COMMAND_WRITE_ANSWER);
            databaseManager.setSelectedQuestion(user.getId(), selectedQuestion);

            messageBuilder.append(Message.answerCommand.getChooseNumberMessage(user, selectedQuestion));

            answer.setChatId(chat.getId().toString());
            answer.setText(messageBuilder.toString());
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
