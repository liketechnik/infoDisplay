package org.telegram.bot.messages;

import org.telegram.telegrambots.logging.BotLogger;

/**
 * @author liketechnik
 * @version 1.2.1
 * @date 09 of February 2017
 */
public class SituationalContentMessage extends ContentMessage {
    private String situation;

    /**
     * Initialize new Message object.
     *
     * @param command Command the message gets requested for.
     */
    public SituationalContentMessage(String command) {
        super(command);
    }

    public void setMessageName(String command, String situation) {
        super.messageName =  command;
        this.situation = situation;

        super.xmlQuarry = "command_message[@command='" + command + "']/case[@case='" +
                situation +  "']/command_content";
    }

    public void setMessageName(String commandPackage, String command, String situation) {
        super.messageName =  command;
        this.situation = situation;

        super.xmlQuarry = "command_package[@command='" +  commandPackage +
                "']/command_message[@command='" + command + "']/case[@case='" +
                situation + "']/command_content";
    }

    public String getSituation() {
        if (this.situation != null) {
            return this.situation;
        } else {
            BotLogger.warn(LOGTAG, "No situation set yet!");
            return "No situation set.";
        }
    }
}
