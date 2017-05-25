package org.telegram.bot.messages;

/**
 * @author liketechnik
 * @version 1.0
 * @date 18 of Mai 2017
 */
public class SituationalInlineKeyboard extends InlineKeyboard {

    private String situation;

    public SituationalInlineKeyboard(String command, String situation) {
        super(command);
        this.situation = situation;
    }


    public void setMessageName(String command, String situation) {
        super.messageName = command + "_reply_keyboard";
        this.situation = situation;

        super.xmlQuarry = "command_message[@command='" + command + "']/reply_keyboard/keyboard_button";
        super.keyboard = null; // force reload
    }

    public void setMessageName(String commandPackage, String command, String situation) {
        super.messageName = command + "_reply_keyboard";
        this.situation = situation;

        super.xmlQuarry = "command_package[@command='" + commandPackage  + "']/command_message[@command='" + command +
                "']/case[@case='" + situation + "']/reply_keyboard/keyboard_button";

        super.keyboard = null; // force reload
    }

    public String getSituation() {
        if (this.situation != null) {
            return this.situation;
        } else {
            throw new IllegalStateException("No situation set yet!");
        }
    }
}
