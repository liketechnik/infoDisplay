package org.telegram.bot.commands.setLanguageCommand;

import Config.Bot;
import Config.CallbackData;
import Config.Languages;
import jdk.nashorn.internal.codegen.CompilerConstants;
import org.apache.commons.configuration2.XMLConfiguration;
import org.telegram.bot.commands.SendOnErrorOccurred;
import org.telegram.bot.database.DatabaseManager;
import org.telegram.bot.messages.ContentMessage;
import org.telegram.bot.messages.Message;
import org.telegram.bot.messages.SituationalContentMessage;
import org.telegram.bot.messages.SituationalMessage;
import org.telegram.telegrambots.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.api.objects.Chat;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.bots.AbsSender;
import org.telegram.telegrambots.bots.commands.BotCommand;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import org.telegram.telegrambots.logging.BotLogger;

import java.util.HashMap;

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

        EditMessageText answer = new EditMessageText();
        String additionalContent;

        try {

            DatabaseManager databaseManager = DatabaseManager.getInstance();

            SituationalMessage message = new SituationalMessage(this.getCommandIdentifier() + "_command");
            message.setMessageName(this.getClass().getPackage().getName()
                            .replaceAll("org.telegram.bot.commands.", ""), this.getCommandIdentifier() + "_command",
                    "language");

            if (arguments[0].equals(CallbackData.SET_LANGUAGE_ENGLISH)) {
                databaseManager.setUserLanguage(user.getId(), Languages.ENGLISH);
            } else if (arguments[0].equals(CallbackData.SET_LANGUAGE_GERMAN)) {
                databaseManager.setUserLanguage(user.getId(), Languages.GERMAN);
            }

            if (arguments[0].equals(CallbackData.SET_LANGUAGE_DEFAULT)) {
                databaseManager.setUserLanguage(user.getId(), Languages.NONE);

                message.setMessageName(this.getClass().getPackage().getName()
                                .replaceAll("org.telegram.bot.commands.", ""), this.getCommandIdentifier() + "_command",
                        "default");

            }

            databaseManager.setUserCommandState(user.getId(), Bot.NO_COMMAND);

            answer.setChatId(arguments[2]);
            answer.setMessageId(Integer.valueOf(arguments[1]));
            answer.setText(message.getContent(user.getId(), true));
        } catch (Exception e) {
            BotLogger.error(LOGTAG, e);

            new SendOnErrorOccurred().execute(absSender, user, chat, new String[]{LOGTAG, arguments[2]});

            return;
        }

        try {
            absSender.editMessageText(answer);
        } catch (TelegramApiException e) {
            BotLogger.error(LOGTAG, e);
        }
    }
}
