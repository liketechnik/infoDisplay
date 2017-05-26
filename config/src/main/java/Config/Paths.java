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

package Config;

import java.nio.file.FileSystems;
import java.nio.file.Path;

/**
 * This class contains {@code Path}s that are constant and needed in many places of the program.
 * @author Florian Warzecha
 * @version 1.0
 * @date 11 of January 2017
 */
public final class Paths {
    public static final String USER_HOME = System.getProperty("user.home");
    public static final Path APP_HOME = FileSystems.getDefault().getPath(USER_HOME + "/.infoDisplay");

    public static final Path DISPLAY_FILES_CONFIG_FILE = FileSystems.getDefault().getPath(APP_HOME + "/displayFiles.conf");
    public static final Path DISPLAY_FILES = FileSystems.getDefault().getPath(APP_HOME + "/displayFiles");

    public static final Path BOT_DATABASE = FileSystems.getDefault().getPath(APP_HOME + "/bot_database");
    public static final Path USER_DATABASE = FileSystems.getDefault().getPath(BOT_DATABASE + "/users");
    public static final Path QUESTION_DATABASE = FileSystems.getDefault().getPath(BOT_DATABASE + "/questions");

    public static final Path BOT_CONFIG_FILE = FileSystems.getDefault().getPath(APP_HOME + "/bot.conf");
}
