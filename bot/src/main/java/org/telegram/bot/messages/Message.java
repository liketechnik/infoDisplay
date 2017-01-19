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
import org.apache.commons.configuration2.resolver.CatalogResolver;
import org.apache.commons.configuration2.resolver.DefaultEntityResolver;
import org.apache.commons.configuration2.tree.xpath.XPathExpressionEngine;
import org.telegram.bot.database.DatabaseManager;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.logging.BotLogger;
import org.xml.sax.EntityResolver;
import org.xml.sax.SAXException;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Pattern;

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

    public static final Path location = FileSystems.getDefault().getPath(
            Message.class.getClassLoader().getResource("language.xsd").toString()).getParent();

    private static XMLConfiguration getXmlConfiguration(int userID) {

        BotLogger.info(LOGTAG, location.toString());

        String language = null;

        FileBasedConfigurationBuilder<XMLConfiguration> builder;
        XMLConfiguration config = null;

        XMLBuilderParameters params = new Parameters().xml();
        params.setBasePath(location.toString());
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

        builder = new FileBasedConfigurationBuilder<XMLConfiguration>(XMLConfiguration.class)
                .configure(params.setFileName(location.toString() + "/" + language + ".xml"));

        try {
            config = builder.getConfiguration();
        } catch (ConfigurationException e) {
            BotLogger.error(LOGTAG, e);
            System.exit(2);
        }

        return  config;
    }

    public static String getFinalizedMessage(StringBuilder message) {
        if (!message.toString().contains("/help")) {
            message.append("\n\n");
            message.append("/help");
        }
        return message.toString();
    }

    public static String getStartMessage(User user, boolean userKnown) {

        final String startMessageQuarry = "command_message[@command='start_command']/";

        StringBuilder startMessage = new StringBuilder();

        XMLConfiguration config = getXmlConfiguration(user.getId());


        if (userKnown) {
             startMessage.append(config.getString(startMessageQuarry + "case[@case='userKnown']/part[@position=1]")
                     .replaceAll("/n>", "\n"));
             startMessage.append(" ");
             startMessage.append(getFilteredUsername(user));
             startMessage.append(config.getString(startMessageQuarry + "case[@case='userKnown']/part[@position=2]")
                     .replaceAll("/n>", "\n"));
        } else {
             startMessage.append(config.getString(startMessageQuarry + "case[@case='userUnknown']/part[@position=1]")
                     .replaceAll("/n>", "\n"));
             startMessage.append(" ");
             startMessage.append(getFilteredUsername(user));
             startMessage.append(config.getString(startMessageQuarry + "case[@case='userUnknown']/part[@position=2]")
                     .replaceAll("/n>", "\n"));
        }

        return getFinalizedMessage(startMessage);
    }

    public static String getStopMessage(User user) {

        final String stopMessageQuarry = "command_message[@command='stop_command']/";

        StringBuilder stopMessage = new StringBuilder();

        XMLConfiguration config = getXmlConfiguration(user.getId());

        stopMessage.append(config.getString(stopMessageQuarry + "part[@position=1]").replaceAll("/n>", "\n"));
        stopMessage.append(" ");
        stopMessage.append(getFilteredUsername(user));
        stopMessage.append(config.getString(stopMessageQuarry + "part[@position=2]").replaceAll("/n>", "\n"));

        return getFinalizedMessage(stopMessage);
    }

    public static String getSendOnErrorOccurredMessage(User user, boolean terminating) {

        final String onErrorOccurredMessageQuarry = "command_message[@command='send_on_error_occurred_command']/";

        String onErrorOccurredMessage;

        XMLConfiguration config = getXmlConfiguration(user.getId());

        if (terminating) {
            onErrorOccurredMessage = config.getString(onErrorOccurredMessageQuarry + "case[@case='terminating']/" +
                    "part[@position=1]");
        } else {
            onErrorOccurredMessage = config.getString(onErrorOccurredMessageQuarry + "case[@case=" +
                    "'not_terminating']/part[@position=1]");
        }

        return onErrorOccurredMessage;
    }

    public static String getRegisterMessage(User user, String ifClause) {
        final String registerMessageQuarry = "command_message[@=command='register_command']/";

        StringBuilder registerMessage = new StringBuilder();

        XMLConfiguration config = getXmlConfiguration(user.getId());

        if (ifClause.equals(Config.registerCommandIfClauses.alreadyRegisterd)) {
            registerMessage.append(config.getString(registerMessageQuarry + "case[@case='" +
                    Config.registerCommandIfClauses.alreadyRegisterd + "']/part[@position=1]").replaceAll("/n>", "\n"));
            registerMessage.append(" ");
            registerMessage.append(user.getId());
        } else if (ifClause.equals(Config.registerCommandIfClauses.registrationRequestSent)) {
            registerMessage.append(config.getString(registerMessageQuarry + "case[@case='" +
                    Config.registerCommandIfClauses.registrationRequestSent + "']/part[@position=1]")
                    .replaceAll("/n>", "\n"));
            registerMessage.append(" ");
            registerMessage.append(user.getId());
        } else if (ifClause.equals(Config.registerCommandIfClauses.sendRegistrationRequest)) {
            registerMessage.append(config.getString(registerMessageQuarry + "case[@case='" +
                    Config.registerCommandIfClauses.sendRegistrationRequest + "']/part[@position=1]")
                    .replaceAll("/n>", "\n"));
            registerMessage.append(" ");
            registerMessage.append(user.getId());
        } else if (ifClause.equals(Config.registerCommandIfClauses.toAdmin)) {
            registerMessage.append(config.getString(registerMessageQuarry + "case[@case='" +
                    Config.registerCommandIfClauses.toAdmin + "']/part[@position=1]").replaceAll("/n>", "\n"));
            registerMessage.append(" ");
            registerMessage.append(getSpecialFilteredUsername(user));
            registerMessage.append(" ");
            registerMessage.append(config.getString(registerMessageQuarry + "case[@case='" +
                    Config.registerCommandIfClauses.toAdmin + "']/part[@position=2]").replaceAll("/n>", "\n"));
            registerMessage.append(" ");
            registerMessage.append(user.getId());
        }

        registerMessage.append("\n").append("/help");

        return getFinalizedMessage(registerMessage);
    }

    public static String getIdMessage(User user, Long chatID) {
        final String idMessageQuarry = "command_message[@command='id_command']/";

        StringBuilder idMessage = new StringBuilder();

        XMLConfiguration config = getXmlConfiguration(user.getId());

        idMessage.append(config.getString(idMessageQuarry + "part[@position=1]").replaceAll("/n>", "\n"));
        idMessage.append(" ");
        idMessage.append(user.getId());
        idMessage.append("\n");
        idMessage.append(config.getString(idMessageQuarry + "part[@position=2]").replaceAll("/n>", "\n"));
        idMessage.append(" ");
        idMessage.append(chatID);

        return getFinalizedMessage(idMessage);
    }

    public static String getHelpMessage(User user) {
        final String helpMessageQuarry = "command_message[@command='help_command']/";

        XMLConfiguration config = getXmlConfiguration(user.getId());

        StringBuilder helpMessage = new StringBuilder();

        helpMessage.append(config.getString(helpMessageQuarry + "part[@position=1]").replaceAll("/n>", "\n"));

        return helpMessage.toString();
    }

    public static String getAdministratorMessage(User user) {
        final String administratorMessageQuarry   = "command_message[@command='administrator_command']/";

        XMLConfiguration config = getXmlConfiguration(user.getId());

        StringBuilder administratorMessage = new StringBuilder();

        administratorMessage.append(config.getString(administratorMessageQuarry + "part[@position=1]")
                .replaceAll("/n>", "\n"));

        return getFinalizedMessage(administratorMessage);
    }

    public static class pinPictureCommand {
        static final String pinPictureQuarry = "command_package[@command='pinPictureCommand']/";

        public static String getPinPictureMessage(User user, boolean hasPermission) {
            final String pinPictureMessageQuarry = pinPictureQuarry + "command_message[@command='pin_picture_command']/";

            XMLConfiguration config = getXmlConfiguration(user.getId());

            StringBuilder pinPictureMessage = new StringBuilder();

            if (hasPermission) {
                pinPictureMessage.append(config.getString(pinPictureMessageQuarry + "case[@case='has_permission']/" +
                        "part[@position=1]").replaceAll("/n>", "\n"));
            } else {
                pinPictureMessage.append(config.getString(pinPictureMessageQuarry + "case[@case='has_no_" +
                        "permission']/part[@position=1]").replaceAll("/n>", "\n"));
            }

            return getFinalizedMessage(pinPictureMessage);
        }

        public static String getSendDescriptionMessage(User user) {
            final String sendDescriptionMessageQuarry = pinPictureQuarry + "command_message[@command='send_description_" +
                    "command']/";

            XMLConfiguration config = getXmlConfiguration(user.getId());

            StringBuilder sendDescriptionMessage = new StringBuilder();

            sendDescriptionMessage.append(config.getString(sendDescriptionMessageQuarry + "part[@position=1]")
                    .replaceAll("/n>", "\n"));

            return getFinalizedMessage(sendDescriptionMessage);
        }

        public static String getSendDurationMessage(User user, boolean validDuration) {
            final String sendDurationMessageQuarry = pinPictureQuarry + "command_message[@command='send_duration_" +
                    "command']/";

            XMLConfiguration config = getXmlConfiguration(user.getId());

            StringBuilder sendDurationMessage = new StringBuilder();

            if (validDuration) {
                sendDurationMessage.append(config.getString(sendDurationMessageQuarry + "case[@case='valid_" +
                        "duration']/" +
                        "part[@position=1]").replaceAll("/n>", "\n"));
            } else {
                sendDurationMessage.append(config.getString(sendDurationMessageQuarry + "case[@case='invalid_" +
                        "duration']/part[@position=1]").replaceAll("/n>", "\n"));
            }

            return getFinalizedMessage(sendDurationMessage);
        }

        public static String getSendPictureMessage(User user, boolean hasPicture) {
            final String sendPictureMessageQuarry = pinPictureQuarry + "command_message[@command='send_picture_" +
                    "command']/";

            XMLConfiguration config = getXmlConfiguration(user.getId());

            StringBuilder sendPictureMessage = new StringBuilder();

            if (hasPicture) {
                sendPictureMessage.append(config.getString(sendPictureMessageQuarry + "case[@case='picture']/" +
                        "part[@position=1]").replaceAll("/n>", "\n"));
            } else {
                sendPictureMessage.append(config.getString(sendPictureMessageQuarry + "case[@case='no_picture']/" +
                        "part[@position=1]").replaceAll("/n>", "\n"));
            }

            return getFinalizedMessage(sendPictureMessage);
        }

        public static String getSendTitleMessage(User user, boolean newName) {
            final String sendTitleMessageQuarry = pinPictureQuarry + "command_message[@command='send_title_" +
                    "command']/";

            XMLConfiguration config = getXmlConfiguration(user.getId());

            StringBuilder sendTitleMessage = new StringBuilder();

            if (newName) {
                sendTitleMessage.append(config.getString(sendTitleMessageQuarry + "case[@case='new_name']/" +
                        "part[@position=1]").replaceAll("/n>", "\n"));
            } else {
                sendTitleMessage.append(config.getString(sendTitleMessageQuarry + "case[@case='already_" +
                        "used']/part[@position=1]").replaceAll("/n>", "\n"));
            }

            return getFinalizedMessage(sendTitleMessage);
        }
    }

    public static class askCommand {
        static final String askCommandQuarry = "command_package[@command='askCommand']/";

        public static String getAskMessage(User user) {
            final String askMessageQuarry = askCommandQuarry + "command_message[@command='ask_command']/";

            XMLConfiguration config = getXmlConfiguration(user.getId());

            return config.getString(askMessageQuarry + "part[@position=1]").replaceAll("/n>", "\n");
        }

        public static String getWriteQuestionMessage(User user, String question) {
            final String writeQuestionQuarry = askCommandQuarry + "command_message[@command='write_question_" +
                    "command']/case[@case='question']/";

            XMLConfiguration config = getXmlConfiguration(user.getId());

            StringBuilder writeQuestionMessage = new StringBuilder();

            writeQuestionMessage.append(question);
            writeQuestionMessage.append(config.getString(writeQuestionQuarry + "part[@position=1]")
                    .replaceAll("/n>", "\n"));
            writeQuestionMessage.append(" ").append(getSpecialFilteredUsername(user));
            writeQuestionMessage.append(config.getString(writeQuestionQuarry + "part[@position=2]")
                    .replaceAll("/n>", "\n"));

            return getFinalizedMessage(writeQuestionMessage);
        }

        public static String getWriteQuestionMessage(User user) {
            final String writeQuestionQuarry = askCommandQuarry + "command_message[@command='write_question_" +
                    "command']/case[@case='response']/";

            XMLConfiguration config = getXmlConfiguration(user.getId());

            return config.getString(writeQuestionQuarry + "part[@position=1]").replaceAll("/n>", "\n");
        }
    }

    public static class answerCommand {
        static final String answerCommandQuarry = "command_package[@command='answerCommand']/";

        public static String getAnswerMessage(User user, String questions) {
            final String answerQuarry = answerCommandQuarry + "command_message[@command='answer_command']/";

            XMLConfiguration config = getXmlConfiguration(user.getId());

            StringBuilder answerMessage = new StringBuilder();

            answerMessage.append(config.getString(answerQuarry + "part[@position=1]").replaceAll("/n>",
                    "\n"));

            answerMessage.append(questions);

            answerMessage.append(config.getString(answerQuarry + "part[@position=2]").replaceAll("/n>",
                    "\n"));

            return  getFinalizedMessage(answerMessage);
        }

        public static String getChooseNumberMessage(User user) {
            final String chooseNumberQuarry = answerCommandQuarry + "command_message[@command='choose_number_" +
                    "command']/case[@case='invalid']/";

            XMLConfiguration config = getXmlConfiguration(user.getId());

            StringBuilder chooseNumberMessage = new StringBuilder();

            chooseNumberMessage.append(config.getString(chooseNumberQuarry + "part[@position=1]")
                    .replaceAll("/n>", "\n"));

            return getFinalizedMessage(chooseNumberMessage);
        }

        public static String getChooseNumberMessage(User user, int selectedQuestion) {
            final String chooseNumberQuarry = answerCommandQuarry + "command_message[@command='choose_number_" +
                    "command']/case[@case='valid']/";

            XMLConfiguration config = getXmlConfiguration(user.getId());

            StringBuilder chooseNumberMessage = new StringBuilder();

            chooseNumberMessage.append(config.getString(chooseNumberQuarry + "part[@position=1]")
                    .replaceAll("/n>", "\n"));

            chooseNumberMessage.append(" ").append(selectedQuestion);

            chooseNumberMessage.append(config.getString(chooseNumberQuarry + "part[@position=2]")
                    .replaceAll("/n>", "\n"));

            return getFinalizedMessage(chooseNumberMessage);
        }

        public static String getWriteAnswerMessage(User user, int selectedQuestion, String message) {
            final String writeAnswerQuarry = answerCommandQuarry + "command_message[@command='write_answer_command']/" +
                    "case[@case='explanation']/";

            XMLConfiguration config = getXmlConfiguration(user.getId());

            StringBuilder writeAnswerMessage = new StringBuilder();

            writeAnswerMessage.append(config.getString(writeAnswerQuarry + "part[@position=1]")
                    .replaceAll("/n>", "\n"));

            try {
                writeAnswerMessage.append(DatabaseManager.getInstance().getQuestion(selectedQuestion -1));
            } catch (Exception e) {
                BotLogger.error(LOGTAG, "Failed to get question.");
            }

            writeAnswerMessage.append("\n\n");

            writeAnswerMessage.append(config.getString(writeAnswerQuarry + "part[@position=2]")
                    .replaceAll("/n>", "\n"));

            writeAnswerMessage.append(message);

            return getFinalizedMessage(writeAnswerMessage);
        }

        public static String getWriteAnswerMessage(User user) {
            final String writeAnswerQuarry = answerCommandQuarry + "command_message[@command='write_answer_command']/" +
                    "case[@case='answer']/";

            XMLConfiguration config = getXmlConfiguration(user.getId());

            StringBuilder writeAnswerMessage = new StringBuilder();

            writeAnswerMessage.append(config.getString(writeAnswerQuarry + "part[@position=1]")
                    .replaceAll("/n>", "\n"));

            return getFinalizedMessage(writeAnswerMessage);
        }
    }

    public static String getAboutCommandMessage(User user) {
        final String aboutCommandQuarry = "command_message[@command='about_command']/part[@position=1]";

        XMLConfiguration config = getXmlConfiguration(user.getId());

        return config.getString(aboutCommandQuarry).replaceAll("/n>", "\n");
    }
}
