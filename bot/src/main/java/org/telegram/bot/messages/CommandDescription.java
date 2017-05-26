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

/**
 * Wrapper around {@link Message} to get the command description in a users foreign language.
 * It works by changing the xmlQuarries from the standard content path to the description path in the xml files.
 * @author Florian Warzecha
 * @version 1.0
 * @date 24 of February 2017
 * @see Message
 */
public class CommandDescription extends Message {

    /**
     * Initialize a new {@link CommandDescription}.
     * @param command Command the description gets requested for.
     * @see org.telegram.bot.messages.Message#Message(String)
     * @see #setMessageName(String)
     * @see #setMessageName(String, String)
     */
    public CommandDescription(String command) {
        super(command);
        super.messageName = command + "_description";
        super.xmlQuarry = "command_message[@command='" + command + "']/command_description";
    }

    /**
     * Handles the xmlQuarry and set the name of the description.
     * @param command Command the description gets requested for.
     * @see #setMessageName(String, String)
     */
    @Override
    public void setMessageName(String command) {
        super.messageName = command + "_description";
        super.xmlQuarry = "command_message[@command='" + command + "']/command_description";
        this.message = null; // force reload of the message
    }

    /**
     * Handles the xmlQuarry and sets the name of the message.
     * @param commandPackage The package of commands the {@code command} belongs to.
     * @param command Command the description gets requested for.
     * @see #setMessageName(String)
     */
    @Override
    public void setMessageName(String commandPackage, String command) {
        super.messageName = command + "_description";
        super.xmlQuarry = "command_package[@command='" + commandPackage + "']/command_message[@command='" + command +
                "']/command_description";
        this.message = null; // force reload of the message
    }
}
