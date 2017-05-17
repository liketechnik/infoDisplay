package org.telegram.bot.api;

import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.telegram.bot.database.DatabaseException;
import org.telegram.bot.database.DatabaseManager;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.bots.commands.BotCommand;
import org.telegram.telegrambots.logging.BotLogger;

import javax.xml.crypto.Data;
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
 */
public abstract class TelegramLongPollingThreadBot extends TelegramLongPollingBot {

    String LOGTAG;
    private HashMap<String, Constructor<BotCommand>> commandsMap = new HashMap<>();
    private ExecutorService executor = Executors.newCachedThreadPool();

    private Constructor<Parser> documentParser;
    private Constructor<Parser> messageParser;
    private Constructor<Parser> callbackParser;

    private boolean closing = false;

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

    public final Collection<Constructor<BotCommand>> getRegisteredCommands() {
        return commandsMap.values();
    }

    public final Constructor<BotCommand> getRegisteredCommand(String commandIdentifier) {
        return commandsMap.get(commandIdentifier);
    }

    private final Constructor<BotCommand> getCommandConstructor(Class<BotCommand> botCommandClass)
            throws NoSuchMethodException {
        return botCommandClass.getConstructor();
    }

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

    public abstract Constructor<Parser> getDocumentParser();

    public abstract Constructor<Parser> getMessageParser();

    public abstract Constructor<Parser> getCallbackParser();

    public boolean isClosing() {
        return closing;
    }

}
