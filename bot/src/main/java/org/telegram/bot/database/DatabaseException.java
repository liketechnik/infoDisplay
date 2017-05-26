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
 * A wrapper for common {@link Exception}s that may occur in the {@link DatabaseManager} to allow easy catching of exceptions.
 * @author Florian Warzecha
 * @version 1.0
 * @date 15 of May 2017
 */
public class DatabaseException extends Exception {
    /**
     * Create a new empty {@code DatabaseException}.
     */
    DatabaseException() {
    }

    /**
     * Create a new {@code DatabaseException} with a message.
     * @param error A message explaining why this error occured.
     */
    DatabaseException(String error) {
        super(error);
    }

    /**
     * Create a new {@code DatabaseException} stating that access to a file failed.
     * @param file The file that was not accessible. Its name is used in the error message.
     */
    DatabaseException(java.io.File file) {
        super("Error accessing: " + file.toString());
    }

    /**
     * Create a new {@code DatabaseException} stating that access to a file failed.
     * @param path The path of the file that was not accessible. The {@code String} representation of the path is used in the error message.
     * @param e The original {@link IOException} thrown while trying to access the {@code path}.
     */
    DatabaseException(Path path, IOException e) {
        super("Error accessing: " + path.toString(), e);
    }

    /**
     * Create a new {@code DatabaseException} stating that there is a problem getting the content of a file with {@link org.apache.commons.configuration2.Configuration}.
     * @param e The original {@code ConfigurationException} thrown.
     * @param path The path of the file that was accessed.
     */
    DatabaseException(ConfigurationException e, Path path) {
        super("Error getting configuration of file: " + path.toString(), e);
    }

    /**
     * Create a new {@code DatabaseException} stating that there was a problem accessing multiple files.
     * @param files The paths of the files, added as String representation to the end of the error message.
     * @param exception The exception that was originally thrown.
     */
    DatabaseException(Path[] files, Exception exception) {
        super("Error accessing files " + files, exception);
    }

    /**
     * Creates a new {@code DatabaseException} stating there was a problem transforming a {@code String} into an {@code URL}.
     * @param urlParams The {@code String} passed to the {@code new URL(String)} method.
     * @param e The original {@code MalformedURLException} from the url creation.
     */
    DatabaseException(String urlParams, MalformedURLException e) {
        super("Error creating url from: " + urlParams, e);
    }

    /**
     * Create a new {@code DatabaseException} stating there was a problem with a {@link GetFile}.
     * @param getFileRequest The {@link GetFile} there was a problem with.
     * @param e The original {@code TelegramApiException} from the url creation.
     */
    DatabaseException(GetFile getFileRequest, TelegramApiException e) {
        super("Error getting file request: " + getFileRequest.toString(), e);
    }

    /**
     * Create a new {@code DatabaseException} stating there was a problem saving from an {@code URL} to a file.
     * @param fileUrl The {@code URL} where the media comes from.
     * @param displayFile The {@code Path} the media should be saved to.
     * @param e The original {@code IOException} from the saving try.
     */
    DatabaseException(URL fileUrl, Path displayFile, IOException e) {
        super("Error saving media from '" + fileUrl.toString() + "' to file " + displayFile.toString(), e);
    }
}
