package org.telegram.bot.messages;

import org.apache.commons.configuration2.XMLConfiguration;


import java.util.*;

/**
 * @author liketechnik
 * @version 1.2.1
 * @date 09 of Februar 2017
 */
public class ContentMessage extends Message {
    private HashMap<String, String> additionalContent;

    /**
     * Initialize new Message object.
     *
     * @param command Command the message gets requested for.
     */
    public ContentMessage(String command) {
        super(command);
    }

    public void setAdditionalContent(HashMap<String, String> addition) {
        this.additionalContent = addition;
    }

    public Map<String, String> getAdditionalContent() {
        if (this.additionalContent != null) {
            return this.additionalContent;
        } else {
            throw new IllegalStateException("No additional content added yet!");
        }
    }

    public String getContent(int userId, boolean addHelp) {
        if (super.xmlQuarry == null) {
            throw new IllegalStateException("No xml quarry set yet!");
        }

        if (super.message == null) {
            XMLConfiguration config = super.getXmlConfiguration(userId);
//            super.message = config.getString(super.xmlQuarry).replaceAll("/n>", "\n")
//                .replaceAll("/additionalContent>", this.additionalContent);
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
