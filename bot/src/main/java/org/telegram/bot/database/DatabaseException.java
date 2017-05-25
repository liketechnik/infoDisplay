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
