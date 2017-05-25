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

package org.telegram.bot.messages;

import org.apache.commons.configuration2.XMLConfiguration;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

/**
 * @author liketechnik
 * @version ${VERSION}
 * @date 24 of März 2017
 */
public class  InlineKeyboard extends Message {

    List<List<InlineKeyboardButton>> keyboard;

    public InlineKeyboard(String command) {
        super(command);
        super.messageName = command + "_reply_keyboard";
        super.xmlQuarry = "command_message[@command='" + command + "']/reply_keyboard/keyboard_button";
    }

    @Override
    public void setMessageName(String command) {
        super.messageName = command + "_reply_keyboard";
        super.xmlQuarry = "command_message[@command='" + command + "']/reply_keyboard/keyboard_button";
        this.keyboard = null; // force reload
    }

    @Override
    public void setMessageName(String commandPackage, String command) {
        super.messageName = command + "_reply_keyboard";
        super.xmlQuarry = "command_package[@command='" + commandPackage + "']/command_message[@command='" + command +
                "']/reply_keyboard/keyboard_button";
        this.keyboard = null; // force reload
    }

    @Override
    @Deprecated
    public String getContent(int userId, boolean addHelp) {
        throw new UnsupportedOperationException("No content available, user getKeyboard()");
    }

    public List<List<InlineKeyboardButton>> getKeyboard(int userId) {

        if (super.xmlQuarry == null) {
            throw new IllegalStateException("No xml quarry set yet!");
        }

        if (this.keyboard == null || this.keyboard.isEmpty()) {
            XMLConfiguration config = super.getXmlConfiguration(userId);
            this.keyboard = new ArrayList<>();

            int row = 1;

            String rowQuarry = super.xmlQuarry + "[@row='" + row + "']";

            while (config.containsKey(rowQuarry)) {
                     //   @columns + columns
               // language/command_package[5]/command_message/reply_keyboard/keyboard_button[1]/@callback_data
                int column = 1;
                List<InlineKeyboardButton> inlineRow = new ArrayList<>();

                String columnQuarry = rowQuarry + "[@column='" + column + "']";

                while (config.containsKey(columnQuarry)) {
                    inlineRow.add(new InlineKeyboardButton().setText(config.getString(columnQuarry))
                            .setCallbackData(config.getString(columnQuarry + "/@callback_data")));

                    column += 1;
                    columnQuarry = rowQuarry + "[@column='" + column + "']";
                }

                this.keyboard.add(inlineRow);

                row += 1;
                rowQuarry = super.xmlQuarry + "[@row='" + row + "']";
            }
        }

        return this.keyboard;
    }

    @Override
    @Deprecated
    public Integer calculateHash() throws InterruptedException {
        throw new UnsupportedOperationException();
    }
}
