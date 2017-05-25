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

import org.apache.commons.configuration2.ex.ConfigurationException;
import org.telegram.telegrambots.api.methods.GetFile;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;

/**
 * @author liketechnik
 * @version 1.0
 * @date 15 of Mai 2017
 */
public class DatabaseException extends Exception {
    DatabaseException() {

    }
    DatabaseException(String error) {
        super(error);
    }
    DatabaseException(java.io.File file) {
        super("Error accessing: " + file.toString());
    }
    DatabaseException(Path path, IOException e) {
        super("Error accessing: " + path.toString(), e);
    }
    DatabaseException(ConfigurationException e, Path path) {
        super("Error getting configuration of file: " + path.toString(), e);
    }
    DatabaseException(Path[] files, Exception exception) {
        super("Error accessing files " + files, exception);
    }

    DatabaseException(String urlParams, MalformedURLException e) {
        super("Error creating url from: " + urlParams, e);
    }

    DatabaseException(GetFile getFileRequest, TelegramApiException e) {
        super("Error getting file request: " + getFileRequest.toString(), e);
    }

    DatabaseException(URL fileUrl, Path displayFile, IOException e) {
        super("Error saving media from '" + fileUrl.toString() + "' to file " + displayFile.toString(), e);
    }
}
