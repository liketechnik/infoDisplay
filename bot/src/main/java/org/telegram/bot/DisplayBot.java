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

import org.telegram.bot.api.TelegramLongPollingThreadBot;
import Config.Bot;
import Config.CallbackData;
import org.telegram.bot.commands.*;
import org.telegram.bot.commands.answerCommand.AnswerCommand;
import org.telegram.bot.commands.askCommand.AskCommand;
import org.telegram.bot.commands.pinPictureCommand.*;
import org.telegram.bot.commands.pinVideoCommand.*;
import org.telegram.bot.commands.setLanguageCommand.ChangeLanguage;
import org.telegram.bot.commands.setLanguageCommand.SetLanguageCommand;
import org.telegram.bot.database.DatabaseManager;
import org.telegram.bot.database.SaveThread;
import org.telegram.telegrambots.api.objects.*;
import org.telegram.telegrambots.logging.BotLogger;

import java.lang.reflect.Constructor;


/**
 * @author Florian Warzecha
 * @version 1.0.1
 * @date 22 of October of 2016
 */
public class DisplayBot extends TelegramLongPollingThreadBot {

    public static final String LOGTAG = "DISPLAYBOT";

    /**
     * Register my commands and set an default action for unknown commands send to the bot.
     * Default action executes the {@link HelpCommand}.
     */
    public DisplayBot() {
        registerCommand(StartCommand.class);
        registerCommand(StopCommand.class);
        registerCommand(IDCommand.class);
        registerCommand(RegisterCommand.class);
        registerCommand(AdministratorCommand.class);
        registerCommand(AskCommand.class);
        registerCommand(AnswerCommand.class);
        registerCommand(PinPictureCommand.class);
        registerCommand(PinVideoCommand.class);
        registerCommand(AboutCommand.class);
        registerCommand(CancelCommand.class);
        registerCommand(SetLanguageCommand.class);
        registerCommand(HelpCommand.class);
        SaveThread.getInstance(this).start();
    }

    /**
     * Gets called if the username of the bot is needed.
     * The returned value is defined in the config file.
     * @return The bot's username.
     */
    @Override
    public String getBotUsername() {
        try {
            return DatabaseManager.getInstance().getBotUsername();
        } catch (Exception e) {
            BotLogger.error(this.LOGTAG, "Error getting bot's username.", e);
            System.exit(1);
        }
        return null;
    }

    /**
     * Gets called if the token of the bot is needed.
     * The returned value is defined in the config file.
     * @return The bot's token
     */
    @Override
    public String getBotToken() {
        try {
            return DatabaseManager.getInstance().getBotToken();
        } catch (Exception e) {
            BotLogger.error(this.LOGTAG, "Error getting bot's token.", e);
        }
        return null;
    }

    @Override
    public Constructor getDocumentParser() {
        try {
            return DocumentParser.class.getConstructor(Update.class, TelegramLongPollingThreadBot.class);
        } catch (NoSuchMethodException e) {
            BotLogger.error(LOGTAG, e);
            System.exit(20);
        }
        return null;
    }

    @Override
    public Constructor getMessageParser() {
        try {
            return MessageParser.class.getConstructor(Update.class, TelegramLongPollingThreadBot.class);
        } catch (NoSuchMethodException e) {
            BotLogger.error(LOGTAG, e);
            System.exit(20);
        }
        return null;
    }

    @Override
    public Constructor getCallbackParser() {
        try {
            return CallbackParser.class.getConstructor(Update.class, TelegramLongPollingThreadBot.class);
        } catch (NoSuchMethodException e) {
            BotLogger.error(LOGTAG, e);
            System.exit(20);
        }
        return null;
    }
}
