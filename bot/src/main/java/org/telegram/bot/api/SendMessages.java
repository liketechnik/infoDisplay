package org.telegram.bot.api;

import org.telegram.bot.database.DatabaseException;
import org.telegram.bot.messages.InlineKeyboard;
import org.telegram.telegrambots.api.methods.send.SendDocument;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.send.SendPhoto;
import org.telegram.telegrambots.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.bots.AbsSender;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import org.telegram.telegrambots.logging.BotLogger;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * @author liketechnik
 * @version 1.0
 * @date 17 of April 2017
 */
public class SendMessages extends Thread {

    public static final String LOGTAG = "SENDMESSAGES";

    private static volatile SendMessages instance;

    public enum types {
        send_message, edit_message, document_message
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
        fileIds = new ConcurrentHashMap<>();
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

    private final LinkedBlockingQueue<Integer> messageHashes;
    private final ConcurrentHashMap<Integer, String> messageTypes;


    private final ConcurrentHashMap<Integer, AbsSender> absSender;
    private final ConcurrentHashMap<Integer, String> chatIds;

    private final ConcurrentHashMap<Integer, String> messages;
    private final ConcurrentHashMap<Integer, Boolean> enableMarkdown;

    private final ConcurrentHashMap<Integer, InlineKeyboardMarkup> inlineKeyboards;

    private final ConcurrentHashMap<Integer, Integer> messageIds;

    private final ConcurrentHashMap<Integer, String> fileIds;

    private final boolean enableMarkdownDefault = false;

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
                    boolean currentEnableMarkdown = this.enableMarkdown.remove(currentMessageHash);

                    try {
                        message.setReplyMarkup(this.inlineKeyboards.remove(currentMessageHash));
                    } catch (NullPointerException ignored) {

                    }

                    message.enableMarkdown(currentEnableMarkdown);
                    message.setText(currentMessage);
                    message.setMessageId(currentMessageId);
                    message.setChatId(currentChatId);
                    currentAbsSender.editMessageText(message);
                } else if (currentType.equals(types.document_message.name())) {
                    SendDocument message = new SendDocument();
                    String currentMessage = this.messages.remove(currentMessageHash);
                    String currentChatId = this.chatIds.remove(currentMessageHash);
                    AbsSender currentAbsSender = this.absSender.remove(currentMessageHash);
                    String currentFileId = this.fileIds.remove(currentMessageHash);

                    try {
                        message.setReplyMarkup(this.inlineKeyboards.remove(currentMessageHash));
                    } catch (NullPointerException ignored) {

                    }

                    message.setCaption(currentMessage);
                    message.setChatId(currentChatId);
                    message.setDocument(currentFileId);
                    currentAbsSender.sendDocument(message);
                } else {
                    BotLogger.error(LOGTAG, "Registered message without known type.");
                }
                TimeUnit.SECONDS.sleep(1); // sleep one second yo avoid sending to many messages in a short interval
            } catch (InterruptedException | TelegramApiException e) {
                BotLogger.error(LOGTAG, e);
            } finally {
                this.messages.remove(removalMessageHash);
                this.absSender.remove(removalMessageHash);
                this.chatIds.remove(removalMessageHash);
                this.enableMarkdown.remove(removalMessageHash);
                this.inlineKeyboards.remove(removalMessageHash);
                this.messageTypes.remove(removalMessageHash);
                this.messageIds.remove(removalMessageHash);
                this.fileIds.remove(removalMessageHash);
            }
        }
    }

    public void addMessage(Integer messageHash, String message, String chatId, AbsSender absSender, Optional<Boolean> enableMarkdown, Optional<InlineKeyboardMarkup> inlineKeyboard) {
        this.messages.putIfAbsent(messageHash, message);
        this.chatIds.putIfAbsent(messageHash, chatId);
        this.absSender.putIfAbsent(messageHash, absSender);
        this.enableMarkdown.putIfAbsent(messageHash, enableMarkdown.orElse(this.enableMarkdownDefault));
        inlineKeyboard.ifPresent(inlineKeyboardMarkup -> {
            this.inlineKeyboards.putIfAbsent(messageHash, inlineKeyboardMarkup);
        });
        this.messageTypes.putIfAbsent(messageHash, types.send_message.name());
        this.messageHashes.add(messageHash);
    }

    public void addEditMessage(Integer messageHash, String message, String chatId, AbsSender absSender, Integer messageID, Optional<Boolean> enableMarkdown, Optional<InlineKeyboardMarkup> inlineKeyboard) {
        this.messages.putIfAbsent(messageHash, message);
        this.chatIds.putIfAbsent(messageHash, chatId);
        this.absSender.putIfAbsent(messageHash, absSender);
        this.enableMarkdown.putIfAbsent(messageHash, enableMarkdown.orElse(this.enableMarkdownDefault));
        this.messageIds.putIfAbsent(messageHash, messageID);
        inlineKeyboard.ifPresent(inlineKeyboardMarkup -> {
            this.inlineKeyboards.putIfAbsent(messageHash, inlineKeyboardMarkup);
        });
        this.messageTypes.putIfAbsent(messageHash, types.edit_message.name());
        this.messageHashes.add(messageHash);
    }

    public void addDocumentMessage(Integer messageHash, String caption, String chatId, AbsSender absSender, String fileId, Optional<InlineKeyboardMarkup> inlineKeyboard) {
        this.messages.putIfAbsent(messageHash, caption);
        this.chatIds.putIfAbsent(messageHash, chatId);
        this.absSender.putIfAbsent(messageHash, absSender);
        this.fileIds.putIfAbsent(messageHash, fileId);
        inlineKeyboard.ifPresent(inlineKeyboardMarkup -> {
            this.inlineKeyboards.putIfAbsent(messageHash, inlineKeyboardMarkup);
        });
        this.messageTypes.putIfAbsent(messageHash, types.document_message.name());
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
