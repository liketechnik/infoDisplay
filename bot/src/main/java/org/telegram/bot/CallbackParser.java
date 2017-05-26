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

import Config.CallbackData;
import org.telegram.bot.api.TelegramLongPollingThreadBot;
import org.telegram.bot.api.Parser;
import org.telegram.bot.commands.deleteMediaCommand.*;
import org.telegram.bot.commands.setLanguageCommand.ChangeLanguage;
import org.telegram.telegrambots.api.objects.CallbackQuery;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.logging.BotLogger;

import java.lang.reflect.Constructor;

/**
 * A {@link Parser} specialized for callbacks.
 * @author liketechnik
 * @version 1.0
 * @date 14 of April 2017
 */
public class CallbackParser extends Parser {

    public final String LOGTAG = "CALLBACKPARSER";

    /**
     * Create a new parser that can parse the {@code update} and then run a command from the {@code bot}.
     * @param update The update to parse.
     * @param bot The bot whose commands are used.
     */
    public CallbackParser(Update update, TelegramLongPollingThreadBot bot) {
        super(update, bot);
    }

    @Override
    protected boolean parse() {
        CallbackQuery callbackQuery = this.update.getCallbackQuery();
        this.user = callbackQuery.getFrom();
        this.chat = callbackQuery.getMessage().getChat();

        try {
            String text = callbackQuery.getData();

            for (CallbackData.SET_LANGUAGE language : CallbackData.SET_LANGUAGE.values()) {
                if (text.equals(language.toString())) {
                    this.arguments = new String[]{text, callbackQuery.getMessage().getMessageId().toString()};
                    this.commandConstructor = (Constructor) ChangeLanguage.class.getConstructor();
                    return true;
                }
            }
            if (text.matches(CallbackData.DELETE_MEDIA.delete_media_delete_.name() + "\\d")) {
                this.arguments = new String[]{text, callbackQuery.getMessage().getMessageId().toString()};
                this.commandConstructor = (Constructor) ConfirmDeleteMediaCommand.class.getConstructor();
                return true;
            }
            else if (text.equals(CallbackData.DELETE_MEDIA.delete_media_first.name()) ||
                    text.equals(CallbackData.DELETE_MEDIA.delete_media_last.name()) ||
                    text.equals(CallbackData.DELETE_MEDIA.delete_media_middle.name()) ||
                    text.matches(CallbackData.DELETE_MEDIA.delete_media_.name() + "\\d") ||
                    text.matches(CallbackData.CONFIRM_DELETE_MEDIA.confirm_delete_media_back_.name()+ "\\d")) {
                this.arguments = new String[]{text, callbackQuery.getMessage().getMessageId().toString()};
                this.commandConstructor = (Constructor) DeleteMediaCommand.class.getConstructor();
                return true;
            } else if (text.equals(CallbackData.CONFIRM_DELETE_MEDIA.confirm_delete_media_cancel.name())) {
                this.arguments = new String[]{FUNCTIONS.cancel.name(), callbackQuery.getMessage().getMessageId().toString()};
                this.commandConstructor = (Constructor) DeletedMediaCommand.class.getConstructor();
                return true;
            } else if (text.matches(CallbackData.CONFIRM_DELETE_MEDIA.confirm_delete_media_yes_.name() + "\\d+_\\d")) {
                this.arguments = new String[]{FUNCTIONS.delete.name(), text, callbackQuery.getMessage().getMessageId().toString()};
                this.commandConstructor = (Constructor) DeletedMediaCommand.class.getConstructor();
                return true;
            }


            return false;
        } catch (NoSuchMethodException e) {
            BotLogger.error(LOGTAG, e);
            return false;
        }
    }
}
