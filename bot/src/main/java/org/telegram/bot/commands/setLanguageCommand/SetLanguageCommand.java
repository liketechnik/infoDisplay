package org.telegram.bot.commands.setLanguageCommand;

import org.telegram.bot.commands.SendOnErrorOccurred;
import org.telegram.bot.database.DatabaseManager;
import org.telegram.bot.messages.InlineKeyboard;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Chat;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.bots.AbsSender;
import org.telegram.telegrambots.bots.commands.BotCommand;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import org.telegram.telegrambots.logging.BotLogger;

/**
 * @author liketechnik
 * @version 1.0
 * @date 24 of March 2017
 */
public class SetLanguageCommand extends BotCommand {

    public static final String LOGTAG = "SETLANGUAGECOMMAND";

    public SetLanguageCommand() {
        super("set_language", "Choose the language the bot uses to respond.");
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {

        SendMessage answer = new SendMessage();

        try {

            DatabaseManager databaseManager = DatabaseManager.getInstance();

            org.telegram.bot.messages.Message message = new org.telegram.bot.messages.Message(this.getCommandIdentifier() +
                "_command");
            message.setMessageName(this.getClass().getPackage().getName()
                    .replaceAll("org.telegram.bot.commands.", ""), this.getCommandIdentifier() + "_command");

            InlineKeyboard inlineKeyboard = new InlineKeyboard(this.getCommandIdentifier() + "_command");
            inlineKeyboard.setMessageName(this.getClass().getPackage().getName()
                    .replaceAll("org.telegram.bot.commands.", ""), this.getCommandIdentifier() + "_command");

            InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
            inlineKeyboardMarkup.setKeyboard(inlineKeyboard.getKeyboard(user.getId()));

            databaseManager.setUserCommandState(user.getId(), Config.Bot.SET_LANGUAGE_COMMAND);

            answer.setReplyMarkup(inlineKeyboardMarkup);
            answer.setChatId(chat.getId().toString());
            answer.setText(message.getContent(user.getId(), false));
        } catch (Exception e) {
            BotLogger.error(LOGTAG, e);

            new SendOnErrorOccurred().execute(absSender, user, chat, new String[]{LOGTAG});

            return;
        }

        try {
            absSender.sendMessage(answer);
        } catch (TelegramApiException e) {
            BotLogger.error(LOGTAG, e);
        }
    }
}