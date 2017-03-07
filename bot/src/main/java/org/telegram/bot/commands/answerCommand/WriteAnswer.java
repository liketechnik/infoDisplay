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

package org.telegram.bot.commands.answerCommand;


import org.telegram.bot.commands.SendOnErrorOccurred;
import org.telegram.bot.database.DatabaseManager;
import org.telegram.bot.messages.Message;
import org.telegram.bot.messages.SituationalContentMessage;
import org.telegram.bot.messages.SituationalMessage;
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
 * @date 01 of November 2016
 *
 * This command handles the answer of a chosen question.
 */
public class WriteAnswer extends BotCommand {

    public static final String LOGTAG = "ANSWERCOMMAND_WRITEANSWER";

    /**
     * Set the identifier and a short description of the command.
     */
    public WriteAnswer() {
        super("write_answer",
                "Evaluate the answer after a user executed /answer (and ChooseNumber).");
    }

    /**
     * Evaluate the message send by a user.
     * This is done by taking the message that contains the answer and send it to the user who asked the question.
     * @param absSender
     * @param user
     * @param chat
     * @param arguments
     */
    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {

        SendMessage answer = new SendMessage();
        SendMessage confirmation = new SendMessage();

        try {

            DatabaseManager databaseManager = DatabaseManager.getInstance();

            int selectedQuestion = databaseManager.getSelectedQuestion(user.getId());

            HashMap<String, String> additionalContent = new HashMap<>();
            additionalContent.put("question", databaseManager.getQuestion(selectedQuestion - 1));
            additionalContent.put("answer", arguments[0]);

            SituationalContentMessage explanationMessage = new SituationalContentMessage(this.getCommandIdentifier() +
                "_command");
            explanationMessage.setMessageName(this.getClass().getPackage().getName().replaceAll("org.telegram.bot.commands.", ""),
                    this.getCommandIdentifier() + "_command", "explanation");
            explanationMessage.setAdditionalContent(additionalContent);

            SituationalMessage confirmationMessage = new SituationalMessage(this.getCommandIdentifier() + "_command");
            confirmationMessage.setMessageName(this.getClass().getPackage().getName().replaceAll("org.telegram.bot.commands.", ""),
                    this.getCommandIdentifier() + "_command", "answer");


            answer.setChatId(databaseManager.getQuestionChatID(selectedQuestion - 1).toString());
            answer.setText(explanationMessage.getContent(user.getId(), false));

            confirmation.setChatId(databaseManager.getAdminChatId().toString());
            confirmation.setText(confirmationMessage.getContent(user.getId(), true));

            databaseManager.deleteQuestion(selectedQuestion - 1);
            databaseManager.setSelectedQuestion(user.getId(), -1);
        } catch (Exception e) {
            BotLogger.error(LOGTAG, e);

            new SendOnErrorOccurred().execute(absSender, user, chat, new String[]{LOGTAG});

            return;
        }

        try {
            absSender.sendMessage(confirmation);
            absSender.sendMessage(answer);
        } catch (TelegramApiException e) {
            BotLogger.error(LOGTAG, e);
        }
    }
}