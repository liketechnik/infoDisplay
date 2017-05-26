/*
 * Copyright (C) 2016-2017  Florian Warzecha <flowa2000@gmail.com>
 *
 * This file is part of infoDisplay.
 *
 * infoDisplay is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * infoDisplay is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * infoDisplay uses TelegramBots Java API <https://github.com/rubenlagus/TelegramBots> by Ruben Bermudez.
 * TelegramBots API is licensed under GNU General Public License version 3 <https://www.gnu.org/licenses/gpl-3.0.de.html>.
 *
 * infoDisplay uses parts of the Apache Commons project <https://commons.apache.org/>.
 * Apache commons is licensed under the Apache License Version 2.0 <http://www.apache.org/licenses/>.
 *
 * infoDisplay uses vlcj library <http://capricasoftware.co.uk/#/projects/vlcj>.
 * vlcj is licensed under GNU General Public License version 3 <https://www.gnu.org/licenses/gpl-3.0.de.html>.
 *
 * Thanks to all the people who contributed to the projects that make this
 * program possible.
 */

package org.telegram.bot.database;

import org.telegram.bot.DisplayBot;
import org.telegram.bot.api.TelegramLongPollingThreadBot;
import org.telegram.telegrambots.logging.BotLogger;

import java.util.concurrent.TimeUnit;

/**
 * A {@code Thread} that runs every five minutes the save method of the database manager until the bot starts closing.
 * This is done to prevent data loss in case of interruption of the bot.
 * @author Florian Warzecha
 * @version 1.0
 * @date 17 of May 2017
 * @see DatabaseManager#saveBuilders() The save method.
 */
public class SaveThread extends Thread {

    public static final String LOGTAG = "SAVETHREAD";

    private static volatile SaveThread instance;

    private final TelegramLongPollingThreadBot bot;

    /**
     * Initialize as daemon and set to which bot the {@code Thread} belong.
     * @param bot The bot that the {@code Thread} saves data for. Needed to be able to realize that the bot shuts down.
     * @see DisplayBot#onClosing()
     * @see TelegramLongPollingThreadBot#onClosing()
     */
    private SaveThread(TelegramLongPollingThreadBot bot) {
        setDaemon(true);
        this.bot = bot;
    }

    /**
     * Make sure there is only one instance of this saving thread in one program.
     * @param bot Needed to call the constructor.
     * @return An instance of this class.
     * @see #SaveThread(TelegramLongPollingThreadBot)
     */
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

    /**
     * Wait five minutes, call save method, wait again, and so on, and so on....
     * @see DatabaseManager#saveBuilders()
     */
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
