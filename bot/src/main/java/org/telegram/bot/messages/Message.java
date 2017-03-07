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


import Config.Bot;
import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.builder.fluent.XMLBuilderParameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.tree.xpath.XPathExpressionEngine;
import org.telegram.bot.database.DatabaseManager;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.logging.BotLogger;

import java.nio.file.FileSystems;
import java.nio.file.Path;

import static org.telegram.bot.Main.getFilteredUsername;
import static org.telegram.bot.Main.getSpecialFilteredUsername;

/**
 * @author Florian Warzecha
 * @version 1.0
 * @date 09 of November of 2016
 *
 * Get messages in a users foreign language.
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
    }

    /**
     * Get the message for the specified command.
     * @param userId Get the language from a users config file.
     * @param addHelp If a link to the help command should be provided at the end
     *                of a message.
     * @return The text of the message.
     */
    public String getContent(int userId, boolean addHelp) {
//        BotLogger.info(LOGTAG, this.xmlQuarry);

        if (this.xmlQuarry == null) {
            BotLogger.error(LOGTAG, "Can't load message text without setting " +
                    "message name");
            return null;
        }

//        BotLogger.info(LOGTAG, this.xmlQuarry);

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
            BotLogger.warn(LOGTAG, "Not set message name requested!");
            return "No name set.";
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
            BotLogger.warn(LOGTAG, "Not set xmlConfiguration requested!");
            return  "No xmlQuarry set.";
        }
    }

    /**
     * Load a message file in a user specific language.
     * @param userID The user who's language is used.
     * @return XMLConfiguration in the users language.
     */
    static XMLConfiguration getXmlConfiguration(int userID) {

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
        } catch (Exception e) {
            BotLogger.error(LOGTAG, e);
            System.exit(10);
        }

//        BotLogger.info(LOGTAG, resources.toString());
//        BotLogger.info(LOGTAG, language);
//        BotLogger.info(LOGTAG, resources.toString() + "/" + language + ".xml");

        builder = new FileBasedConfigurationBuilder<XMLConfiguration>(XMLConfiguration.class)
                .configure(params.setFileName(resources.toString() + "/" + language + ".xml"));

        try {
            config = builder.getConfiguration();
        } catch (ConfigurationException e) {
            BotLogger.error(LOGTAG, e);
            System.exit(2);
        }

//        BotLogger.info(LOGTAG, config.toString());

        return config;
    }
}