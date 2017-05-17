package org.telegram.bot.api;

import org.telegram.bot.api.TelegramLongPollingThreadBot;
import org.telegram.bot.commands.HelpCommand;
import org.telegram.bot.database.DatabaseException;
import org.telegram.bot.messages.ContentMessage;
import org.telegram.bot.messages.Message;
import org.telegram.telegrambots.api.methods.send.SendChatAction;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Chat;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.bots.AbsSender;
import org.telegram.telegrambots.bots.commands.BotCommand;
import org.telegram.telegrambots.logging.BotLogger;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * @author Florian Warzecha
 * @version 1.0
 * @date 13 of April 2017
 */
public abstract class Parser implements Runnable {

    public final String LOGTAG = "PARSER";

    protected Update update;
    protected TelegramLongPollingThreadBot bot;

    protected Constructor<BotCommand> commandConstructor;

    protected User user;
    protected Chat chat;
    protected String[] arguments;

    public Parser(Update update, TelegramLongPollingThreadBot bot) {
        this.update = update;
        this.bot = bot;
    }

    /**
     * Create a new instance of a BotCommand and run it.
     */
    protected void executeCommand() {
        try {
            BotCommand command = this.commandConstructor.newInstance();
            command.execute(this.bot, this.user, this.chat, this.arguments); // every bot is an absSender
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
            BotLogger.error(LOGTAG, e);
        }
    }

    /**
     * Parse a new update.
     *
     * Parses a new update for information on how to set parameters of a command and the choice of the command.
     * @return If the message was successfully parsed.
     */
    protected abstract boolean parse();


    public void run() {
        if (!this.parse()) {
            Message message = new Message("unknown");
            String messageText = message.getContent(user.getId(), false);
            try {
                SendMessages.getInstance().addMessage(message.calculateHash(), messageText, chat.getId().toString(), this.bot);
            } catch (InterruptedException e) {
                BotLogger.error(LOGTAG, e);
            }

            this.arguments = new String[]{};
            try {
                this.commandConstructor = (Constructor) HelpCommand.class.getConstructor();
            } catch (NoSuchMethodException e) {
                BotLogger.error(LOGTAG, e);
            }
        }
        this.executeCommand();
    }

}
