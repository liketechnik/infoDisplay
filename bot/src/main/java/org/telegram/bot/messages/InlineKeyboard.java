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
 * Wrapper around {@link Message} to get the keyboard text from the language files in a users foreign language.
 * It works by changing the xmlQuarries from the usual content path to keyboard path.
 * @author liketechnik
 * @version 1.0
 * @date 24 of March 2017
 * @see org.telegram.bot.messages.Message
 */
public class  InlineKeyboard extends Message {

    List<List<InlineKeyboardButton>> keyboard;

    /**
     * Initialize a new {@code InlineKeyboard}.
     * @param command Command the keyboard belongs to.
     * @see org.telegram.bot.messages.Message#Message(String)
     * @see #setMessageName(String)
     * @see #setMessageName(String, String)
     */
    public InlineKeyboard(String command) {
        super(command);
        super.messageName = command + "_reply_keyboard";
        super.xmlQuarry = "command_message[@command='" + command + "']/reply_keyboard/keyboard_button";
    }

    /**
     * Handle the xmlQuarry and set the name of the message.
     * @param command Command the keyboard gets requested for.
     * @see #setMessageName(String, String)
     */
    @Override
    public void setMessageName(String command) {
        super.messageName = command + "_reply_keyboard";
        super.xmlQuarry = "command_message[@command='" + command + "']/reply_keyboard/keyboard_button";
        this.keyboard = null; // force reload
    }

    /**
     * Handle the xmlQuarry and set the name of the message.
     * @param commandPackage The collection of commands the command belongs to.
     * @param command Command in the {@code commandPackage} the keyboard gets requested for.
     */
    @Override
    public void setMessageName(String commandPackage, String command) {
        super.messageName = command + "_reply_keyboard";
        super.xmlQuarry = "command_package[@command='" + commandPackage + "']/command_message[@command='" + command +
                "']/reply_keyboard/keyboard_button";
        this.keyboard = null; // force reload
    }

    /**
     * Inherited fro {@link Message} but senseless in this class as it represents a keyboard and not simple text.
     * @throws UnsupportedOperationException If this method is used. Reason stated above.
     */
    @Override
    @Deprecated
    public String getContent(int userId, boolean addHelp) {
        throw new UnsupportedOperationException("No content available, user getKeyboard()");
    }

    /**
     * Create the keyboard from the buttons and their text specified at the {@link #xmlQuarry}. The text of the buttons
     * is in a users foreign language.
     * @param userId Get the language from this users config file.
     * @return The constructed keyboard.
     * @see org.telegram.bot.messages.Message#getContent(int, boolean) The base method after which this method is created.
     */
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

    /**
     * Inherited from {@link Message} but senseless in this class because a keyboard is always send together with a message,
     * so the hash comes from the message. Also it would be very unclear how to calculate the hash of a keyboard.
     * @throws UnsupportedOperationException When used. Reasons stated above.
     */
    @Override
    @Deprecated
    public Integer calculateHash() throws InterruptedException {
        throw new UnsupportedOperationException();
    }
}
