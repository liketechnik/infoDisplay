package org.telegram.bot.api;

import org.telegram.bot.database.DatabaseException;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.bots.AbsSender;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import org.telegram.telegrambots.logging.BotLogger;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author liketechnik
 * @version 1.0
 * @date 17 of April 2017
 */
public class SendMessages extends Thread {

    public static final String LOGTAG = "SENDMESSAGES";

    private static volatile SendMessages instance;

    public enum types {
        send_message, edit_message
    }

    private SendMessages() {
        setDaemon(true);
        messageHashes = new LinkedBlockingQueue<>();
        messages = new ConcurrentHashMap<>();
        absSender = new ConcurrentHashMap<>();
        chatIds = new ConcurrentHashMap<>();
        enableMarkdown = new ConcurrentHashMap<>();
        inlineKeyboards = new ConcurrentHashMap<>();
        messageTypes = new ConcurrentHashMap<>();
        messageIds = new ConcurrentHashMap<>();
    }

    public static SendMessages getInstance() {
        final SendMessages currentInstance;
        if (instance == null) {
            synchronized (SendMessages.class) {
                if (instance == null) {
                    instance = new SendMessages();
                }
                currentInstance = instance;
            }
        } else {
            currentInstance = instance;
        }
        return currentInstance;
    }

    private LinkedBlockingQueue<Integer> messageHashes;
    private ConcurrentHashMap<Integer, String> messageTypes;


    private ConcurrentHashMap<Integer, AbsSender> absSender;
    private ConcurrentHashMap<Integer, String> chatIds;

    private ConcurrentHashMap<Integer, String> messages;
    private ConcurrentHashMap<Integer, Boolean> enableMarkdown;
    private ConcurrentHashMap<Integer, InlineKeyboardMarkup> inlineKeyboards;

    private ConcurrentHashMap<Integer, Integer> messageIds;

    @Override
    public void run() {
        while (true) {
            Integer removalMessageHash = null;
            try {
                Integer currentMessageHash = this.messageHashes.take();
                removalMessageHash = currentMessageHash;
                String currentType = this.messageTypes.remove(currentMessageHash);
                if (currentType.equals(types.send_message.name())) {
                    SendMessage message = new SendMessage();
                    String currentMessage = this.messages.remove(currentMessageHash);
                    String currentChatId = this.chatIds.remove(currentMessageHash);
                    AbsSender currentAbsSender = this.absSender.remove(currentMessageHash);
                    boolean currentEnableMarkdown = this.enableMarkdown.remove(currentMessageHash);

                    try {
                        message.setReplyMarkup(this.inlineKeyboards.remove(currentMessageHash));
                    } catch (NullPointerException ignored) {

                    }

                    message.enableMarkdown(currentEnableMarkdown);
                    message.setText(currentMessage);
                    message.setChatId(currentChatId);
                    currentAbsSender.sendMessage(message);
                } else if (currentType.equals(types.edit_message.name())) {
                    EditMessageText message = new EditMessageText();
                    String currentMessage = this.messages.remove(currentMessageHash);
                    String currentChatId = this.chatIds.remove(currentMessageHash);
                    AbsSender currentAbsSender = this.absSender.remove(currentMessageHash);
                    Integer currentMessageId = this.messageIds.remove(currentMessageHash);

                    message.setText(currentMessage);
                    message.setMessageId(currentMessageId);
                    message.setChatId(currentChatId);
                    currentAbsSender.editMessageText(message);
                } else {
                    BotLogger.error(LOGTAG, "Registered message without known type.");
                }
                TimeUnit.SECONDS.sleep(1); // sleep one second yo avoid sending to many messages in a short interval
            } catch (InterruptedException | TelegramApiException e) {
                BotLogger.error(LOGTAG, e);
            } finally {
                try {
                    this.messages.remove(removalMessageHash);
                } catch (NullPointerException ignored) {

                } try {
                    this.absSender.remove(removalMessageHash);
                } catch (NullPointerException ignored) {

                } try {
                    this.chatIds.remove(removalMessageHash);
                } catch (NullPointerException ignored) {

                } try {
                    this.enableMarkdown.remove(removalMessageHash);
                } catch (NullPointerException ignored) {

                } try {
                    this.inlineKeyboards.remove(removalMessageHash);
                } catch (NullPointerException ignored) {

                } try {
                    this.messageTypes.remove(removalMessageHash);
                } catch (NullPointerException ignored) {

                } try {
                    this.messageIds.remove(removalMessageHash);
                } catch (NullPointerException ignored) {

                }
            }
        }
    }

    public void addMessage(Integer messageHash, String message, String chatId, AbsSender absSender) {
        this.messages.putIfAbsent(messageHash, message);
        this.chatIds.putIfAbsent(messageHash, chatId);
        this.absSender.putIfAbsent(messageHash, absSender);
        this.enableMarkdown.putIfAbsent(messageHash, false);
        this.messageTypes.putIfAbsent(messageHash, types.send_message.name());
        this.messageHashes.add(messageHash);
    }

    public void addMessage(Integer messageHash, String message, String chatId, AbsSender absSender, boolean enableMarkdown) {
        this.messages.putIfAbsent(messageHash, message);
        this.chatIds.putIfAbsent(messageHash, chatId);
        this.absSender.putIfAbsent(messageHash, absSender);
        this.enableMarkdown.putIfAbsent(messageHash, enableMarkdown);
        this.messageTypes.putIfAbsent(messageHash, types.send_message.name());
        this.messageHashes.add(messageHash);
    }

    public void addMessage(Integer messageHash, String message, String chatId, AbsSender absSender, InlineKeyboardMarkup inlineKeyboard) {
        this.messages.putIfAbsent(messageHash, message);
        this.chatIds.putIfAbsent(messageHash, chatId);
        this.absSender.putIfAbsent(messageHash, absSender);
        this.enableMarkdown.putIfAbsent(messageHash, false);
        this.inlineKeyboards.putIfAbsent(messageHash, inlineKeyboard);
        this.messageTypes.putIfAbsent(messageHash, types.send_message.name());
        this.messageHashes.add(messageHash);
    }

    public void addEditMessage(Integer messageHash, String message, String chatId, AbsSender absSender, Integer messageID) {
        this.messages.putIfAbsent(messageHash, message);
        this.chatIds.putIfAbsent(messageHash, chatId);
        this.absSender.putIfAbsent(messageHash, absSender);
        this.messageIds.putIfAbsent(messageHash, messageID);
        this.messageTypes.putIfAbsent(messageHash, types.edit_message.name());
        this.messageHashes.add(messageHash);
    }

    /**
     * Get the number of messages that need to be send.
     * @return an integer that represents the number of not yet send messages.
     */
    public int getMessages() {
        return  this.messages.size();
    }
}
