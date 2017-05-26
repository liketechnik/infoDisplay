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
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.builder.fluent.XMLBuilderParameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.tree.xpath.XPathExpressionEngine;
import org.telegram.bot.database.DatabaseException;
import org.telegram.bot.database.DatabaseManager;
import org.telegram.telegrambots.logging.BotLogger;

import java.nio.file.FileSystems;
import java.nio.file.Path;

/**
 * Get messages in a users foreign language.
 *
 * @author Florian Warzecha
 * @version 1.0
 * @date 09 of November of 2016
 */
public class Message {

    public static final String LOGTAG = "MESSAGE";

    private static final Path resources = FileSystems.getDefault().getPath(
            Message.class.getClassLoader().getResource("language.xsd").toString()).getParent();

    String message;
    String messageName;
    String xmlQuarry;

    /**
     * Initialize new Message object.
     * @param command Command the message gets requested for.
     */
    public Message(String command) {
        this.messageName = command;
        this.xmlQuarry = "command_message[@command='" + command + "']/command_content";
    }

    /**
     * Handles the xmlQuarry and sets the name of the message.
     * @param command Command the message gets requested for.
     */
    public void setMessageName(String command) {
        this.messageName = command;
        this.xmlQuarry = "command_message[@command='" + command + "']/command_content";
        this.message = null; // force reload of the message
    }

    /**
     * Handle the xmlQuarry and sets the name of the message.
     * @param commandPackage The collection of commands the command belongs to.
     * @param command Command in the commandPackage the message gets requested for.
     */
    public void setMessageName(String commandPackage, String command) {
        this.messageName = command;
        this.xmlQuarry = "command_package[@command='" + commandPackage +
                "']/command_message[@command='" + command + "']/command_content";
        this.message = null; // force reload of the message
    }

    /**
     * Get the message for the specified command.
     * @param userId Get the language from a users config file.
     * @param addHelp If a link to the help command should be provided at the end
     *                of a message.
     * @return The text of the message.
     */
    public String getContent(int userId, boolean addHelp) {
        if (this.xmlQuarry == null) {
            throw new IllegalStateException("No xml quarry set yet!");
        }

        if (this.message == null) {
            XMLConfiguration config = this.getXmlConfiguration(userId);
            this.message = config.getString(this.xmlQuarry).replaceAll("/n>", "\n");
        }

        if (addHelp) {
            return this.message + "\n\n/help";
        } else {
            return this.message;
        }
    }


    /**
     * Get the command name the message belongs to.
     * @return Command name the message belongs to.
     */
    public String getMessageName() {
        if (this.messageName != null) {
            return this.messageName;
        } else {
            throw new IllegalStateException("No message set yet!");

        }
    }

    /**
     * Get the xmlQuarry for the current command.
     * @return The xmlQuarry for the current command.
     */
    public String getXmlQuarry() {
        if (this.xmlQuarry != null) {
            return this.xmlQuarry;
        } else {
            throw new IllegalStateException("No xml quarry set yet!");
        }
    }

    /**
     * Calculate a unique hash from the text of the message.
     * Uniqueness is guaranteed by synchronizing this method to {@code Message.class}, adding the {@link System#currentTimeMillis()}
     * to the message text before calculating the hash and waiting one millisecond before the hash is returned.
     * @return The unique hash of the message text.
     * @throws InterruptedException If sleeping before returning the message is interrupted.
     */
    public Integer calculateHash() throws InterruptedException {
        synchronized (Message.class) {
            String hash = this.message + System.currentTimeMillis();
            Thread.sleep(1);
            return hash.hashCode();
        }
    }

    /**
     * Load a message file in a user specific language.
     * @param userID The user who's language is used.
     * @return XMLConfiguration in the users language.
     */
    public static XMLConfiguration getXmlConfiguration(int userID) {

        String language = null;

        FileBasedConfigurationBuilder<XMLConfiguration> builder;
        XMLConfiguration config = null;

        XMLBuilderParameters params = new Parameters().xml();
        params.setBasePath(resources.toString());
        params.setSchemaValidation(true);
        params.setExpressionEngine(new XPathExpressionEngine());

        try {
            language = DatabaseManager.getInstance().getUserLanguage(userID);
        } catch (IllegalArgumentException e) {
            language = Config.Languages.ENGLISH;
        } catch (DatabaseException e) {
            BotLogger.error(LOGTAG, e);
            System.exit(10);
        }

        params.setFileName(resources.toString() + "/" + language + ".xml");
        params.setFile(FileSystems.getDefault().getPath(resources.toString() + "/" + language + ".xml").toFile());

        builder = new FileBasedConfigurationBuilder<XMLConfiguration>(XMLConfiguration.class)
                .configure(params);

        try {
            config = builder.getConfiguration();
        } catch (ConfigurationException e) {
            BotLogger.error(LOGTAG, e);
            System.exit(2);
        }

        return config;
    }
}