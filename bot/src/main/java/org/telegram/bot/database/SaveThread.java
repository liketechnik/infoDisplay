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
