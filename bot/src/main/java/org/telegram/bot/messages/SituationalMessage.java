package org.telegram.bot.messages;

import org.apache.commons.configuration2.XMLConfiguration;
import org.telegram.telegrambots.logging.BotLogger;

/**
 * @author liketechnik
 * @version 1.2.1
 * @date 09 of Februar 2017
 */
public class SituationalMessage extends Message {
    private String situation;

    /**
     * Initialize new Message object.
     *
     * @param command Command the message gets requested for.
     */
    public SituationalMessage(String command) {
        super(command);
    }

    public void setMessageName(String command, String situation) {
        super.messageName =  command;
        this.situation = situation;

        super.xmlQuarry = "command_message[@command='" + command + "']/case[@case='" +
                situation +  "']/command_content";
        this.message = null; // force reload of the message
    }

    public void setMessageName(String commandPackage, String command, String situation) {
        super.messageName =  command;
        this.situation = situation;

        super.xmlQuarry = "command_package[@command='" +  commandPackage +
                "']/command_message[@command='" + command + "']/case[@case='" +
                situation + "']/command_content";
        this.message = null; // force reload of the message
    }

    public String getSituation() {
        if (this.situation != null) {
            return this.situation;
        } else {
            throw new IllegalStateException("No situation set yet!");
        }
    }
}
