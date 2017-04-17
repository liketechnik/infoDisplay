package org.telegram.bot;

import Config.Bot;
import org.telegram.bot.api.TelegramLongPollingThreadBot;
import org.telegram.bot.api.Parser;
import org.telegram.bot.database.DatabaseManager;
import org.telegram.telegrambots.api.objects.CallbackQuery;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.logging.BotLogger;

/**
 * @author liketechnik
 * @version 1.0
 * @date 14 of April 2017
 */
public class CallbackParser extends Parser {

    public final String LOGTAG = "CALLBACKPARSER";

    public CallbackParser(Update update, TelegramLongPollingThreadBot bot) {
        super(update, bot);
    }

    @Override
    protected boolean parse() {
        CallbackQuery callbackQuery = this.update.getCallbackQuery();
        this.user = callbackQuery.getFrom();
        this.chat = callbackQuery.getMessage().getChat();

        try {
            String userCommandState = DatabaseManager.getInstance().getUserCommandState(this.user.getId());

            if (userCommandState.equals(Bot.NO_COMMAND)) {
                return false;
            }

//            if (userCommandState.equals(Bot.SET_LANGUAGE_COMMAND)) {
//                if (callbackQuery.getData().equals(CallbackData.SET_LANGUAGE_DEFAULT)
//                        || callbackQuery.getData().equals(CallbackData.SET_LANGUAGE_GERMAN)
//                        || callbackQuery.getData().equals(CallbackData.SET_LANGUAGE_ENGLISH)) {
//                    this.arguments = new String[] {callbackQuery.getData(),
//                            callbackQuery.getMessage().getMessageId().toString()};
//                    this.commandConstructor = (Constructor) ChangeLanguage.class.getConstructor();
//                }
//                return false;
//            } else {
//                return false;
//            }
//            return true;
            return false;
        } catch (Exception e) {
            BotLogger.error(LOGTAG, e);
            return false;
        }
    }
}
