package org.telegram.bot.api;

import org.telegram.bot.database.DatabaseManager;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.bots.AbsSender;
import org.telegram.telegrambots.logging.BotLogger;

import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;
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

    private SendMessages() {
        setDaemon(true);
        messageHashes = new LinkedBlockingQueue<>();
        messages = new ConcurrentHashMap<>();
        absSender = new ConcurrentHashMap<>();
        chatIds = new ConcurrentHashMap<>();
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
    private ConcurrentHashMap<Integer, String> messages;
    private ConcurrentHashMap<Integer, AbsSender> absSender;
    private ConcurrentHashMap<Integer, String> chatIds;

    @Override
    public void run() {
        while (true) {
            try {
                SendMessage message = new SendMessage();

                Integer currentMessageHash = this.messageHashes.take();
                String currentMessage = this.messages.remove(currentMessageHash);
                String currentChatId = this.chatIds.remove(currentMessageHash);
                AbsSender currentAbsSender = this.absSender.remove(currentMessageHash);

                message.setText(currentMessage);
                message.setChatId(currentChatId);
                currentAbsSender.sendMessage(message);
                TimeUnit.SECONDS.sleep(1); // sleep one second yo avoid sending to many messages in a short interval
            } catch (Exception e) {
                BotLogger.error(LOGTAG, e);
            }
        }
    }

    public void addMessage(Integer messageHash, String message, String chatId, AbsSender absSender) {
        this.messageHashes.add(messageHash);
        this.messages.putIfAbsent(messageHash, message);
        this.chatIds.putIfAbsent(messageHash, chatId);
        this.absSender.putIfAbsent(messageHash, absSender);
    }
}
