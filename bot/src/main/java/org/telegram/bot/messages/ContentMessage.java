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


import java.util.*;

/**
 * An extended {@link Message} that allows replacing of predefined Strings in the language files with modular content
 * via {@code HashMap}s.
 * @author Florian Warzecha
 * @version 1.2.1
 * @date 09 of February 2017
 * @see SituationalContentMessage
 */
public class ContentMessage extends Message {
    private HashMap<String, String> additionalContent;

    /**
     * Initialize new Message object.
     * @param command Command the message gets requested for.
     * @see org.telegram.bot.messages.Message#Message(String)
     * @see #setMessageName(String)
     * @see #setMessageName(String, String)
     */
    public ContentMessage(String command) {
        super(command);
    }

    /**
     * Set which Strings from the language files should be replaced with which content.
     * @param addition A {@code HashMap} containing mappings for the strings from the language files to their 'real' content.
     * @see #getAdditionalContent()
     */
    public void setAdditionalContent(HashMap<String, String> addition) {
        this.additionalContent = addition;
    }

    /**
     * Get the {@code HashMap} containing the mappings from predefined strings to their modular content.
     * @return The {@code HashMap} with specified mappings.
     * @see #setAdditionalContent(HashMap)
     */
    public Map<String, String> getAdditionalContent() {
        if (this.additionalContent != null) {
            return this.additionalContent;
        } else {
            throw new IllegalStateException("No additional content added yet!");
        }
    }

    /**
     * Get the content from the language file and replace the specified strings with their modular content.
     * @param userId Get the language from a users config file.
     * @param addHelp If a link to the help command should be provided at the end
     *                of a message.
     * @return The text from the language file in a users foreign language. The specially marked strings are replaced
     * by their modular mapping.
     * @see #setAdditionalContent(HashMap)
     * @see org.telegram.bot.messages.Message#getContent(int, boolean)
     */
    public String getContent(int userId, boolean addHelp) {
        if (super.xmlQuarry == null) {
            throw new IllegalStateException("No xml quarry set yet!");
        }

        if (super.message == null) {
            XMLConfiguration config = super.getXmlConfiguration(userId);
            super.message = config.getString(super.xmlQuarry).replaceAll("/n>", "\n");

            if (this.additionalContent != null) {
                for (String key : this.additionalContent.keySet()) {
                    super.message = super.message.replaceAll("/" + key + ">", this.additionalContent.get(key));
                }
            }
        }

        if (addHelp) {
            return super.message + "\n\n/help";
        } else {
            return super.message;
        }
    }
}
