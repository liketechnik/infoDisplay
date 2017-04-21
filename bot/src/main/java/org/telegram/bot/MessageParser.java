package org.telegram.bot;

import Config.Bot;
import org.telegram.bot.api.TelegramLongPollingThreadBot;
import org.telegram.bot.api.Parser;
import org.telegram.bot.commands.CancelCommand;
import org.telegram.bot.commands.answerCommand.ChooseNumber;
import org.telegram.bot.commands.answerCommand.WriteAnswer;
import org.telegram.bot.commands.askCommand.WriteQuestion;
import org.telegram.bot.commands.pinPictureCommand.SendDescription;
import org.telegram.bot.commands.pinPictureCommand.SendDuration;
import org.telegram.bot.commands.pinPictureCommand.SendPicture;
import org.telegram.bot.commands.pinPictureCommand.SendTitle;
import org.telegram.bot.commands.pinVideoCommand.SendVideo;
import org.telegram.bot.database.DatabaseManager;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.logging.BotLogger;

import java.lang.reflect.Constructor;
import java.util.Arrays;

/**
 * @author Florian Warzecha
 * @version 1.0
 * @date 14 of April 2017
 */
public class MessageParser extends Parser {

    public final String LOGTAG = "MESSAGEPARSER";

    public MessageParser(Update update, TelegramLongPollingThreadBot bot) {
        super(update, bot);
    }

    @Override
    protected boolean parse() {
        /** Parsing taken from {@link org.telegram.telegrambots.bots.commands.CommandRegistry.ececuteCommand} **/
        Message message = this.update.getMessage();
        this.user = message.getFrom();
        this.chat = message.getChat();

        String text = message.getText();
        // Check if the text message calls a registered command
        if (text.startsWith("/")) {
            String command = text.substring(1);
            String[] commandSplit = command.split(" ");

            try {
                // check if either the user sent a new, known command or he sent the cancel command
                if ((this.bot.getCommandsMap().containsKey(commandSplit[0]) &&
                        DatabaseManager.getInstance().getUserCommandState(this.user.getId()).equals(Bot.NO_COMMAND))
                        || commandSplit[0].equals(CancelCommand.class.getConstructor().newInstance().getCommandIdentifier())) {
                    this.arguments = Arrays.copyOfRange(commandSplit, 1, commandSplit.length);
                    this.commandConstructor = this.bot.getRegisteredCommand(commandSplit[0]);
                    return true;
                }
            } catch (Exception e) {
                BotLogger.error(LOGTAG, e);
            }
            return false;
        // Check if the user executed a command and the message is a response
        } else {
            try {
                String userCommandState = DatabaseManager.getInstance().getUserCommandState(this.user.getId());
                if (userCommandState.equals(Bot.NO_COMMAND)) {
                    return false;
                }

                this.arguments = new String[]{text};

                if (userCommandState.equals(Bot.ASK_COMMAND_WRITE_QUESTION)) {
                    this.commandConstructor = (Constructor) WriteQuestion.class.getConstructor();
                } else if (userCommandState.equals(Bot.ANSWER_COMMAND_CHOOSE_NUMBER)) {
                    this.commandConstructor = (Constructor) ChooseNumber.class.getConstructor();
                } else if (userCommandState.equals(Bot.ANSWER_COMMAND_WRITE_ANSWER)) {
                    this.commandConstructor = (Constructor) WriteAnswer.class.getConstructor();
                } else if (userCommandState.equals(Bot.PIN_PICTURE_COMMAND_SEND_TITLE)) {
                    this.commandConstructor = (Constructor) SendTitle.class.getConstructor();
                } else if (userCommandState.equals(Bot.PIN_PICTURE_COMMAND_SEND_DESCRIPTION)) {
                    this.commandConstructor = (Constructor) SendDescription.class.getConstructor();
                } else if (userCommandState.equals(Bot.PIN_PICTURE_COMMAND_SEND_DURATION)) {
                    this.commandConstructor = (Constructor) SendDuration.class.getConstructor();
                } else if (userCommandState.equals(Bot.PIN_PICTURE_COMMAND_SEND_PICTURE)) {
                    this.arguments = new String[]{Bot.HAS_NO_PHOTO};
                    this.commandConstructor = (Constructor) SendPicture.class.getConstructor();
                } else if (userCommandState.equals(Bot.PIN_VIDEO_COMMAND_SEND_VIDEO)) {
                    this.arguments = new String[]{Bot.HAS_NO_VIDEO};
                    this.commandConstructor = (Constructor) SendVideo.class.getConstructor();
                } else if (userCommandState.equals(Bot.PIN_VIDEO_COMMAND_SEND_DESCRIPTION)) {
                    this.commandConstructor = (Constructor) org.telegram.bot.commands.pinVideoCommand.
                            SendDescription.class.getConstructor();
                } else if (userCommandState.equals(Bot.PIN_VIDEO_COMMAND_SEND_TITLE)) {
                    this.commandConstructor = (Constructor) org.telegram.bot.commands.pinVideoCommand.
                            SendTitle.class.getConstructor();
                }

                return true;
            } catch (Exception e) {
                BotLogger.error(LOGTAG, e);
                return false;
            }
        }
    }
}
