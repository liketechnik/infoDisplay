package org.telegram.bot.database;

import org.telegram.bot.api.TelegramLongPollingThreadBot;
import org.telegram.telegrambots.logging.BotLogger;

import java.util.concurrent.TimeUnit;

/**
 * @author liketechnik
 * @version 1.0
 * @date 17 of Mai 2017
 */
public class SaveThread extends Thread {

    public static final String LOGTAG = "SAVETHREAD";

    private static volatile SaveThread instance;

    private final TelegramLongPollingThreadBot bot;

    private SaveThread(TelegramLongPollingThreadBot bot) {
        setDaemon(true);
        this.bot = bot;
    }

    public static SaveThread getInstance(TelegramLongPollingThreadBot bot) {
        final SaveThread currentInstance;
        if (instance == null) {
            synchronized (SaveThread.class) {
                if (instance == null) {
                    instance = new SaveThread(bot);
                }
                currentInstance = instance;
            }
        } else {
            currentInstance = instance;
        }
        return currentInstance;
    }

    @Override
    public void run() {
        while (!bot.isClosing()) {
            try {
                TimeUnit.MINUTES.sleep(5);
            } catch (InterruptedException e) {
                BotLogger.error(LOGTAG, e);
            }
            DatabaseManager.getInstance().saveBuilders();
        }
    }
}
