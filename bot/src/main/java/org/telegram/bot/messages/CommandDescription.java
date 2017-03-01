package org.telegram.bot.messages;

/**
 * @author liketechnik
 * @version ${VERSION}
 * @date 24 of Februar 2017
 */
public class CommandDescription extends Message {

    public CommandDescription(String command) {
        super(command);
        super.messageName = command + "_description";
        super.xmlQuarry = "command_message[@command='" + command + "']/command_description";
    }

    @Override
    public void setMessageName(String command) {
        super.messageName = command + "_description";
        super.xmlQuarry = "command_message[@command='" + command + "']/command_description";
    }

    @Override
    public void setMessageName(String commandPackage, String command) {
        super.messageName = command + "_description";
        super.xmlQuarry = "command_package[@command='" + commandPackage + "']/command_message[@command='" + command +
                "']/command_description";
    }
}
