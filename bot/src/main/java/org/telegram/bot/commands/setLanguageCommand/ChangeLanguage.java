package org.telegram.bot.commands.setLanguageCommand;

import Config.CallbackData;
import Config.Languages;
import org.telegram.bot.api.SendMessages;
import org.telegram.bot.commands.SendOnErrorOccurred;
import org.telegram.bot.database.DatabaseException;
import org.telegram.bot.database.DatabaseManager;
import org.telegram.bot.messages.SituationalMessage;
import org.telegram.telegrambots.api.objects.Chat;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.bots.AbsSender;
import org.telegram.telegrambots.bots.commands.BotCommand;
import org.telegram.telegrambots.logging.BotLogger;

import java.util.Optional;

/**
 * @author liketechnik
 * @version 1.0
 * @date 25 of March 2017
 */
public class ChangeLanguage extends BotCommand {

    public final String LOGTAG = "CHANGELANGUAGE";

    public ChangeLanguage() {
        super("change_language", "Change the language of a user corresponding to the callback data.");
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {

        try {

            DatabaseManager databaseManager = DatabaseManager.getInstance();

            SituationalMessage message = new SituationalMessage(this.getCommandIdentifier() + "_command");
            message.setMessageName(this.getClass().getPackage().getName()
                            .replaceAll("org.telegram.bot.commands.", ""), this.getCommandIdentifier() + "_command",
                    "language");

            if (arguments[0].equals(CallbackData.SET_LANGUAGE.set_language_english.name())) {
                databaseManager.setUserLanguage(user.getId(), Languages.ENGLISH);
            } else if (arguments[0].equals(CallbackData.SET_LANGUAGE.set_language_german.name())) {
                databaseManager.setUserLanguage(user.getId(), Languages.GERMAN);
            }

            if (arguments[0].equals(CallbackData.SET_LANGUAGE.set_language_default.name())) {
                databaseManager.setUserLanguage(user.getId(), Languages.NONE);

                message.setMessageName(this.getClass().getPackage().getName()
                                .replaceAll("org.telegram.bot.commands.", ""), this.getCommandIdentifier() + "_command",
                        "default");

            }

            String messageText = message.getContent(user.getId(), true);
            SendMessages.getInstance().addEditMessage(message.calculateHash(), messageText, chat.getId().toString(),
                    absSender, Integer.valueOf(arguments[1]), Optional.empty(), Optional.empty());
        } catch (DatabaseException | InterruptedException e) {
            BotLogger.error(LOGTAG, e);

            new SendOnErrorOccurred().execute(absSender, user, chat, new String[]{LOGTAG, arguments[2]});

            return;
        }
    }
}
