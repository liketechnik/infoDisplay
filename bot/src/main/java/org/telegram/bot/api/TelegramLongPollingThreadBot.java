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

package org.telegram.bot.api;

import org.telegram.bot.database.DatabaseManager;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.bots.commands.BotCommand;
import org.telegram.telegrambots.logging.BotLogger;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * This class implements a long polling bot that works on a command base.
 *
 * The difference to the standard {@link org.telegram.telegrambots.bots.TelegramLongPollingCommandBot} is that
 *  for every update a new thread is started that parses the message text, callback data or document.
 *
 *  Command registration is made after {@link org.telegram.telegrambots.bots.commands.CommandRegistry} of TelegramBots API
 *  by Ruben Bermudez (see README).
 *
 * @author Florian Warzecha
 * @version 1.0
 * @date 13 of April 2017
 * @see org.telegram.telegrambots.bots.TelegramLongPollingCommandBot
 * @see org.telegram.telegrambots.bots.commands.CommandRegistry
 * @see Parser
 */
public abstract class TelegramLongPollingThreadBot extends TelegramLongPollingBot {

    String LOGTAG;
    private HashMap<String, Constructor<BotCommand>> commandsMap = new HashMap<>();
    private ExecutorService executor = Executors.newCachedThreadPool();

    private Constructor<Parser> documentParser;
    private Constructor<Parser> messageParser;
    private Constructor<Parser> callbackParser;

    private boolean closing = false;

    /**
     * Registers a new command that can be executed by users.
     * @param commandClass The class that represents the command. It must extend {@link BotCommand}.
     * @return If it was possible to register the passed command. {@code true} on success, {@code false} otherwise.
     * @see BotCommand
     * @see #deregisterCommand(Class) Used to deregister commands.
     * @see #getRegisteredCommands()
     * @see #getRegisteredCommand(String) Get a command via its identifier.
     */
    public final boolean registerCommand(Class<?> commandClass) {
        try {
            Class<BotCommand> botCommandClass = (Class<BotCommand>) commandClass;
            Constructor<BotCommand> botCommandConstructor = this.getCommandConstructor(botCommandClass);

            String commandIdentifier = botCommandConstructor.newInstance().getCommandIdentifier();
            if (commandsMap.containsKey(commandIdentifier)) {
                return false;
            }
            commandsMap.put(commandIdentifier, botCommandConstructor);
            return true;
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException | NoSuchMethodException e) {
            BotLogger.error(LOGTAG, e);
            return false;
        }
    }

    /**
     * Deregister a command so it cannot be executed by users anymore.
     * @param botCommandClass The class that represent the command
     * @return If it was possible to deregister the passed command. {@code true} on success, {@code false} otherwise.
     * @see BotCommand
     * @see #registerCommand(Class) Used to register commands.
     * @see #getRegisteredCommands()
     * @see #getRegisteredCommand(String) Get a command via its identifier.
     */
    public final boolean deregisterCommand(Class<BotCommand> botCommandClass) {
        try {
            String commandIdentifier = this.getCommandConstructor(botCommandClass).newInstance().getCommandIdentifier();
            if (commandsMap.containsKey(commandIdentifier)) {
                commandsMap.remove(commandIdentifier);
                return true;
            }
            return false;
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException | NoSuchMethodException e) {
            BotLogger.error(LOGTAG, e);
            return false;
        }
    }

    /**
     * Get all registered commands.
     * @return A {@link Collection} of the {@link Constructor}s of the classes representing the commands.
     */
    public final Collection<Constructor<BotCommand>> getRegisteredCommands() {
        return commandsMap.values();
    }

    /**
     * Get a command via its command identifier.
     * @param commandIdentifier The command identifier, e. g. the name of the command that is used to execute in Telegram chat with the bot.
     * @return The {@link Constructor} of the class representing the command.
     */
    public final Constructor<BotCommand> getRegisteredCommand(String commandIdentifier) {
        return commandsMap.get(commandIdentifier);
    }

    /**
     * Get the constructor of a class that represents a {@link BotCommand}.
     * @param botCommandClass The class that represents the command.
     * @return The {@link Constructor} of the class passed in.
     * @throws NoSuchMethodException Thrown if the passed {@link BotCommand}'s constructor needs arguments.
     */
    private final Constructor<BotCommand> getCommandConstructor(Class<BotCommand> botCommandClass)
            throws NoSuchMethodException {
        return botCommandClass.getConstructor();
    }

    /**
     * Get the commands {@link HashMap} containing the identifiers of the commands linked to their classes {@link Constructor}s.
     * @return A {@link HashMap} that contains mappings of command identifiers to their {@link Constructor}s.
     */
    public final HashMap<String, Constructor<BotCommand>> getCommandsMap() {
        return this.commandsMap;
    }

    @Override
    public final void onUpdateReceived(Update update) {
        if (closing) {
            return;
        }
        Parser parser;
        try {
            if (update.hasCallbackQuery()) {
                parser = getCallbackParser().newInstance(update, this);
            } else if (update.hasMessage()) {
                if (update.getMessage().hasDocument() || update.getMessage().hasPhoto() || update.getMessage().getVideo() != null) {
                    parser = getDocumentParser().newInstance(update, this);
                } else {
                    parser = getMessageParser().newInstance(update, this);
                }
            } else {
                BotLogger.error(LOGTAG, "Can't handle update without message or callback.\n" + update.toString());
                return;
            }
            this.executor.execute(parser);
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
            BotLogger.error(LOGTAG, e);
        }
    }

    /**
     * Called when the bot session is closed.
     *
     * Logs that the shutdown process is initiated, waits some time to let the commands shut down. Then lets the {@link SendMessages} thread do its work.
     * During the whole process it logs the current status, so the user knows how long it will take till the bot shuts down completely.
     */
    @Override
    public void onClosing() {
        closing = true;
        BotLogger.info(LOGTAG, "Shutting down bot...");
        if (!this.executor.isShutdown()) {
            this.executor.shutdown();
            BotLogger.info(LOGTAG, "Waiting for commands to finish execution.");
            try {
                if (this.executor.awaitTermination(1, TimeUnit.MINUTES)) {
                    BotLogger.info(LOGTAG, "All commands finished!");
                } else {
                    BotLogger.info(LOGTAG, "Not all commands finished, stopping remaining ones.");
                    BotLogger.info(LOGTAG, this.executor.shutdownNow().toString());
                }
            } catch (InterruptedException e) {
                BotLogger.error(LOGTAG, e);
            }
        }

        long messageSendTime = SendMessages.getInstance().getMessages() * (long)  1.5; // one second between each message + some spare time
        if (messageSendTime != 0) {
            BotLogger.info(LOGTAG, "Sending remaining messages, this will take " + messageSendTime + " minutes.");
            try {
                TimeUnit.MINUTES.sleep(messageSendTime);
            } catch (InterruptedException e) {
                BotLogger.info(LOGTAG, "Could not send all remaining messages.", e);
            }
        }

        DatabaseManager.getInstance().saveBuilders();
    }

    /**
     * Get the {@link Constructor} of the parser for documents.
     * @return {@link Constructor} of a parser specialized for documents.
     */
    public abstract Constructor<Parser> getDocumentParser();

    /**
     * Get the {@link Constructor} of the parser for messages.
     * @return {@link Constructor} of  a parser specialized for messages.
     */
    public abstract Constructor<Parser> getMessageParser();

    /**
     * Get the {@link Constructor} of the parser for callbacks.
     * @return {@link Constructor} of  a parser specialized for callbacks.
     */
    public abstract Constructor<Parser> getCallbackParser();

    /**
     * Returns the status of the bot.
     * @return {@code true} if the bot is closing right now, false if it is running
     */
    public boolean isClosing() {
        return closing;
    }

}
