package org.telegram.bot;

import Config.CallbackData;
import org.telegram.bot.api.TelegramLongPollingThreadBot;
import org.telegram.bot.api.Parser;
import org.telegram.bot.commands.deleteMediaCommand.*;
import org.telegram.bot.commands.setLanguageCommand.ChangeLanguage;
import org.telegram.telegrambots.api.objects.CallbackQuery;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.logging.BotLogger;

import java.lang.reflect.Constructor;

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
            String text = callbackQuery.getData();

            for (CallbackData.SET_LANGUAGE language : CallbackData.SET_LANGUAGE.values()) {
                if (text.equals(language.toString())) {
                    this.arguments = new String[]{text, callbackQuery.getMessage().getMessageId().toString()};
                    this.commandConstructor = (Constructor) ChangeLanguage.class.getConstructor();
                    return true;
                }
            }
            if (text.matches(CallbackData.DELETE_MEDIA.delete_media_delete_.name() + "\\d")) {
                this.arguments = new String[]{text, callbackQuery.getMessage().getMessageId().toString()};
                this.commandConstructor = (Constructor) ConfirmDeleteMediaCommand.class.getConstructor();
                return true;
            }
            else if (text.equals(CallbackData.DELETE_MEDIA.delete_media_first.name()) ||
                    text.equals(CallbackData.DELETE_MEDIA.delete_media_last.name()) ||
                    text.equals(CallbackData.DELETE_MEDIA.delete_media_middle.name()) ||
                    text.matches(CallbackData.DELETE_MEDIA.delete_media_.name() + "\\d") ||
                    text.matches(CallbackData.CONFIRM_DELETE_MEDIA.confirm_delete_media_back_.name()+ "\\d")) {
                this.arguments = new String[]{text, callbackQuery.getMessage().getMessageId().toString()};
                this.commandConstructor = (Constructor) DeleteMediaCommand.class.getConstructor();
                return true;
            } else if (text.equals(CallbackData.CONFIRM_DELETE_MEDIA.confirm_delete_media_cancel.name())) {
                this.arguments = new String[]{FUNCTIONS.cancel.name(), callbackQuery.getMessage().getMessageId().toString()};
                this.commandConstructor = (Constructor) DeletedMediaCommand.class.getConstructor();
                return true;
            } else if (text.matches(CallbackData.CONFIRM_DELETE_MEDIA.confirm_delete_media_yes_.name() + "\\d+_\\d")) {
                this.arguments = new String[]{FUNCTIONS.delete.name(), text, callbackQuery.getMessage().getMessageId().toString()};
                this.commandConstructor = (Constructor) DeletedMediaCommand.class.getConstructor();
                return true;
            }


            return false;
        } catch (NoSuchMethodException e) {
            BotLogger.error(LOGTAG, e);
            return false;
        }
    }
}
