package org.telegram.bot.messages;

import jdk.nashorn.internal.ir.annotations.Ignore;
import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.http.annotation.Obsolete;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.logging.BotLogger;

import java.util.ArrayList;
import java.util.List;

/**
 * @author liketechnik
 * @version ${VERSION}
 * @date 24 of März 2017
 */
public class  InlineKeyboard extends Message {

    private List<List<InlineKeyboardButton>> keyboard;

    public InlineKeyboard(String command) {
        super(command);
        super.messageName = command + "_reply_keyboard";
        super.xmlQuarry = "command_message[@command='" + command + "']/reply_keyboard/keyboard_button";
    }

    @Override
    public void setMessageName(String command) {
        super.messageName = command + "_description";
        super.xmlQuarry = "command_message[@command='" + command + "']/reply_keyboard/keyboard_button";
        this.keyboard = null; // force reload
    }

    @Override
    public void setMessageName(String commandPackage, String command) {
        super.messageName = command + "_reply_keyboard";
        super.xmlQuarry = "command_package[@command='" + commandPackage + "']/command_message[@command='" + command +
                "']/reply_keyboard/keyboard_button";
        this.keyboard = null; // force reload
    }

    @Override
    @Deprecated
    public String getContent(int userId, boolean addHelp) {
        return "No content available!";
    }

    public List<List<InlineKeyboardButton>> getKeyboard(int userId) {

        if (super.xmlQuarry == null) {
            BotLogger.error(LOGTAG, "Can't load message text without setting " +
                    "message name");
            return null;
        }

        if (this.keyboard == null || this.keyboard.isEmpty()) {
            XMLConfiguration config = super.getXmlConfiguration(userId);
            this.keyboard = new ArrayList<>();

            int row = 1;

            String rowQuarry = super.xmlQuarry + "[@row='" + row + "']";

            while (config.containsKey(rowQuarry)) {
                     //   @columns + columns
               // language/command_package[5]/command_message/reply_keyboard/keyboard_button[1]/@callback_data
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
}
