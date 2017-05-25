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


import Config.Bot;
import Config.Keys;
import Config.Paths;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.sync.ReadWriteSynchronizer;
import org.apache.commons.io.FileUtils;
import org.telegram.bot.Main;
import org.telegram.telegrambots.api.methods.GetFile;
import org.telegram.telegrambots.api.objects.File;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.bots.AbsSender;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import org.telegram.telegrambots.logging.BotLogger;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.telegram.bot.Main.mergeExceptions;

/**
 * @author Florian Warzecha
 * @version 1.0.1
 * @date 22 of October of 2016
 * <p>
 * This class accesses the database.
 * It makes sure there is only one {@link #instance} of it at once to pretend IO errors from happening.
 */
public final class DatabaseManager {

    private static final String LOGTAG = "DATABASEMANAGER";

    /**
     * Instance of {@link DatabaseManager DatabaseManager class}.
     *
     * @see #getInstance()
     */
    private static volatile DatabaseManager instance;
    /**
     * Map that saves all builders used by the database, so when stopping all files can be saved. (Not sure if it is a
     * good idea to keep all that builders in memory all the time.)
     */
    private final ConcurrentHashMap<String, FileBasedConfigurationBuilder<FileBasedConfiguration>> builders;

    /**
     * Initialises when {@link #getInstance()} is called.
     * It checks for the existence of paths and creates them when needed.
     *
     * @see #getInstance()
     */
    private DatabaseManager() {
        if (Files.notExists(Config.Paths.BOT_DATABASE)) {
            try {
                Files.createDirectory(Config.Paths.BOT_DATABASE);
            } catch (IOException e) {
                BotLogger.error(LOGTAG, "Error creating bot database directory.", e);
                System.exit(10);
            }
        }
        if (Files.notExists(Config.Paths.USER_DATABASE)) {
            try {
                Files.createDirectory(Config.Paths.USER_DATABASE);
            } catch (IOException e) {
                BotLogger.error(LOGTAG, "Error creating bot database user directory.", e);
                System.exit(10);
            }
        }
        if (Files.notExists(Config.Paths.QUESTION_DATABASE)) {
            try {
                Files.createDirectory(Config.Paths.QUESTION_DATABASE);
            } catch (IOException e) {
                BotLogger.error(LOGTAG, "Error creating bot database question directory.", e);
                System.exit(10);
            }
        }

        this.builders = new ConcurrentHashMap<>();
    }

    /**
     * Returns an instance of {@link DatabaseManager DatabaseManager class}.
     * If there is no instance created yet, it {@link #DatabaseManager() initialises} one.
     *
     * @return Instance of {@link DatabaseManager DatabaseManager class}.
     * @see DatabaseManager
     * @see #DatabaseManager()
     */
    public static DatabaseManager getInstance() {
        final DatabaseManager currentInstance;
        if (instance == null) {
            synchronized (DatabaseManager.class) {
                if (instance == null) {
                    instance = new DatabaseManager();
                }
                currentInstance = instance;
            }
        } else {
            currentInstance = instance;
        }
        return currentInstance;
    }

    public synchronized void saveBuilders() {
        BotLogger.info(LOGTAG, "Saving all builders.");
        for (FileBasedConfigurationBuilder builder : this.builders.values()) {
            try {
                builder.save();
            } catch (ConfigurationException e) {
                BotLogger.error(LOGTAG, e);
            }
        }
    }

    /**
     * User got active or inactive.
     * Saves user state to its configuration file.
     *
     * @param userId     UserID which state changed.
     * @param userActive State that is to be set.
     * @see #getUserState(Integer userId) Look up if user is active or inactive.
     */
    public void setUserState(Integer userId, boolean userActive) throws DatabaseException {
        FileBasedConfiguration configuration;
        try {
            configuration = getConfiguration(userId);
        } catch (IllegalArgumentException e) {
            BotLogger.debug(LOGTAG, e);
            createUser(userId);
            configuration = getConfiguration(userId);
        }

        try {
            configuration.setProperty(Config.Keys.USER_ACTIVE, userActive);
        } catch (NullPointerException e) {
            configuration.addProperty(Config.Keys.USER_ACTIVE, userActive);
        }
    }

    /**
     * Looks up if user is active or not.
     * Reads user state from its configuration file.
     *
     * @param userId UserID of that we want the state to know.
     * @return User state.
     * @see #setUserState(Integer userId, boolean userState) Set if user is active or not.
     */
    public boolean getUserState(Integer userId) throws DatabaseException {
        return getConfiguration(userId).getBoolean(Config.Keys.USER_ACTIVE);
    }

    /**
     * User changed his language preference.
     * Save the language preference for a user in his configuration file.
     *
     * @param userId   UserId of the user who's language preference changed.
     * @param language The language that the user wants to receive his messages in.
     * @see #getUserLanguage(Integer userId) Get the language prefernce of a user.
     */
    public void setUserLanguage(Integer userId, String language) throws DatabaseException {
        if (!validLanguage(language)) {
            throw new IllegalArgumentException("No supported language.");
        }

        FileBasedConfiguration configuration = getConfiguration(userId);

        try {
            configuration.setProperty(Config.Keys.USER_LANGUAGE, language);
        } catch (NullPointerException e) {
            configuration.addProperty(Config.Keys.USER_LANGUAGE, language);
        }
    }

    /**
     * Looks up in which language a user wants to receive his messages.
     *
     * @param userId UserID of the user who's language preference should be looked up.
     * @return The language the messages send to the user should have.
     * @see #setUserLanguage(Integer userId, String language) Set the language preference for a user.
     */
    public String getUserLanguage(Integer userId) throws DatabaseException {
        FileBasedConfiguration userConfiguration = getConfiguration(userId);
        if (userConfiguration.getString(Config.Keys.USER_LANGUAGE).equals(Config.Languages.NONE)) {
            FileBasedConfiguration defaultConfiguration = getConfiguration(Paths.BOT_CONFIG_FILE);
            String language = defaultConfiguration.getString(Keys.DEFAULT_LANGUAGE);
            if (!validLanguage(language)) {
                throw new IllegalArgumentException("No valid default language set.");
            } else {
                return language;
            }
        }
        return userConfiguration.getString(Config.Keys.USER_LANGUAGE);
    }

    /**
     * Check if the passed language is registered in the config files (to make sure that a file containing translations
     * exists).
     *
     * @param language The string to check if it represents a valid language.
     * @return true if the language is considered valid, false otherwise
     */
    private boolean validLanguage(String language) {
        try {
            if (language.equals(Config.Languages.ENGLISH) ||
                    language.equals(Config.Languages.GERMAN) ||
                    language.equals(Config.Languages.NONE)) {
                return true;
            } else {
                return false;
            }
        } catch (NullPointerException e) {
            return false;
        }
    }

    /**
     * Gets the configuration for a file on which operations take place.
     * This methods automates getting of the {@link FileBasedConfigurationBuilder builder}.
     *
     * @param configurationFile File from where the configuration is loaded.
     * @return {@link FileBasedConfigurationBuilder<FileBasedConfiguration>} for the file.
     * @see #getConfiguration(Integer userID) set configuration for a user
     * @see #getConfiguration(int questionID) set configuration for a question
     * @see #getConfiguration(String displayFileName) set configuration for a display file
     */
    private FileBasedConfiguration getConfiguration(Path configurationFile) throws DatabaseException {
        try {
            return getBuilder(configurationFile).getConfiguration();
        } catch (ConfigurationException e) {
            throw new DatabaseException(e, configurationFile);
        }
    }

    /**
     * Gets a {@link FileBasedConfiguration configuration} for a user on which operations take place.
     * This method automates the process of getting the file path for a user.
     *
     * @param userID configuration file of the user with this ID.
     * @return {@link FileBasedConfigurationBuilder<FileBasedConfiguration>} of the user.
     * @see #getConfiguration(Path configurationFile) set configuration for a file
     * @see #getConfiguration(int questionID) set configuration for a question
     * @see #getConfiguration(String displayFileName) set configuration for a display file
     */
    private FileBasedConfiguration getConfiguration(Integer userID) throws DatabaseException {
        Path configurationFile = getDatabaseUserPath(userID);
        return getConfiguration(configurationFile);
    }

    /**
     * Gets the {@link FileBasedConfiguration configuration} for a question on which operations take place.
     * This method automates the process of getting the file path for a question.
     *
     * @param questionID configuration file of the question with this ID.
     * @return {@link FileBasedConfigurationBuilder<FileBasedConfiguration>} of the question.
     * @see #getConfiguration(Path configurationFile) set configuration for a file
     * @see #getConfiguration(Integer userID) set configuration for a user
     * @see #getConfiguration(String displayFileName) set configuration for a display file
     */
    private FileBasedConfiguration getConfiguration(int questionID) throws DatabaseException {
        Path configurationFile = getDatabaseQuestionPath(questionID);
        return getConfiguration(configurationFile);
    }

    /**
     * Gets the {@link FileBasedConfiguration configuration} for a display file on which operations take place.
     * This method automates the process of getting the file path for a user.
     *
     * @param displayFile configuration file of the {@link DisplayFile displayFile} with this name.
     * @return {@link FileBasedConfigurationBuilder<FileBasedConfiguration>} of the display file.
     * @see #getConfiguration(Path configurationFile) set configuration for a file
     * @see #getConfiguration(Integer userID) set configuration for a user
     * @see #getConfiguration(int questionID) set configuration for a question
     */
    private FileBasedConfiguration getConfiguration(String displayFile) throws DatabaseException {
        Path configurationFile = getDatabaseDisplayFilePath(displayFile);
        return getConfiguration(configurationFile);
    }

    /**
     * Gets a {@link FileBasedConfigurationBuilder<FileBasedConfiguration> builder} for a configuration file.
     *
     * @param configurationFile File the builder reads from.
     * @return {@link FileBasedConfigurationBuilder<FileBasedConfiguration>} for the file.
     */
    private FileBasedConfigurationBuilder<FileBasedConfiguration> getBuilder(Path configurationFile) {
        if (Files.notExists(configurationFile)) {
            throw new IllegalArgumentException("getConfiguration file not found: " + configurationFile);
        }

        FileBasedConfigurationBuilder builder;

        synchronized (this.builders) {
            builder = this.builders.get(configurationFile.toString());
            if (builder == null) {
                Parameters parameters = new Parameters();
                builder = new FileBasedConfigurationBuilder<FileBasedConfiguration>(PropertiesConfiguration.class)
                        .configure(parameters.properties()
                                .setPath(configurationFile.toString())
                                .setSynchronizer(new ReadWriteSynchronizer()));

                this.builders.put(configurationFile.toString(), builder);
            }
        }

        return builder;
    }

    /**
     * Creates a configuration file.
     *
     * @param file Path of the file to be created.
     * @throws IOException Creation of {@code file} failed.
     */
    public synchronized void createConfigurationFile(Path file) throws DatabaseException {
        try {
            Files.createFile(file);
        } catch (IOException e) {
            throw new DatabaseException(file, e);
        }
    }

    /**
     * Creates a configuration file for a user.
     * The files are created in the users subdirectory of the bot database.
     * {@code userID} is used as name for the file, so a configuration file has a unique name.
     *
     * @param userID The name of the configuration file.
     */
    private void createUser(Integer userID) throws DatabaseException {
        Path file = getDatabaseUserPath(userID);
        createConfigurationFile(file);
        BotLogger.info(LOGTAG, "Created configuration file " + file.toString());
    }

    /**
     * Adds the default prefix for user configuration files and the userID together.
     *
     * @param userID Name of the configuration file the path refers to.
     * @return Path to the configuration file of user with {@code userID}.
     */
    public Path getDatabaseUserPath(Integer userID) {
        return FileSystems.getDefault().getPath(Config.Paths.USER_DATABASE + "/" + userID.toString());
    }

    /**
     * Adds the default prefix for question configuration files and questionID together.
     *
     * @param questionID Name of the configuration file the path refers to.
     * @return Path to the configuration file of question with {@code questionID}.
     */
    public Path getDatabaseQuestionPath(int questionID) {
        return FileSystems.getDefault().getPath(Config.Paths.QUESTION_DATABASE + "/" + questionID);
    }

    /**
     * Adds the default prefix for displayFile configuration files and name together.
     *
     * @param name Name of the configuration file the path refers to.
     * @return Path to the configuration file of displayFile with {@code name}.
     */
    public Path getDatabaseDisplayFilePath(String name) {
        return FileSystems.getDefault().getPath(Config.Paths.DISPLAY_FILES + "/" + name + ".conf");
    }

    /**
     * Saves the registration state of a user in its configuration file.
     *
     * @param userID     UserID of the user who's file is edited.
     * @param registered Registration state to be set.
     * @see #getUserRegistrationState(Integer userID) Look up the registration state.
     */
    public void setUserRegistrationState(Integer userID, boolean registered)
            throws DatabaseException {

        FileBasedConfiguration configuration = getConfiguration(userID);

        try {
            configuration.setProperty(Config.Keys.USER_REGISTERED, registered);
        } catch (NullPointerException e) {
            configuration.addProperty(Config.Keys.USER_REGISTERED, registered);
        }
    }

    /**
     * Looks up if the user is marked as registered in its configuration file.
     *
     * @param userID UserID of the user which config file is used to look up.
     * @return Registration state of the user.
     * @see #setUserRegistrationState(Integer userID, boolean registered) Save the registration state.
     */
    public boolean getUserRegistrationState(Integer userID) throws DatabaseException {
        return getConfiguration(userID).getBoolean(Config.Keys.USER_REGISTERED);
    }


    /**
     * Saves the registration state of a user in its configuration file.
     *
     * @param userID            UserID of the user who's file is edited.
     * @param wantsRegistration Wants registration state to be set.
     * @see #getUserWantsRegistrationState(Integer userID)  Look up the wantsRegistration state.
     */
    public void setUserWantsRegistrationState(Integer userID, boolean wantsRegistration)
            throws DatabaseException {

        FileBasedConfiguration configuration = getConfiguration(userID);

        try {
            configuration.setProperty(Config.Keys.USER_WANTS_REGISTRATION, wantsRegistration);
        } catch (NullPointerException e) {
            configuration.addProperty(Config.Keys.USER_WANTS_REGISTRATION, wantsRegistration);
        }
    }

    /**
     * Looks up if the user is marked as registered in its configuration file.
     *
     * @param userID UserID of the user which config file is used to look up.
     * @return WantsRegistration state of the user.
     * @see #setUserWantsRegistrationState(Integer userID, boolean wantsRegistration) Save the wantsRegistration state.
     */
    public boolean getUserWantsRegistrationState(Integer userID) throws DatabaseException {
        return getConfiguration(userID).getBoolean(Config.Keys.USER_WANTS_REGISTRATION);
    }

    /**
     * Saves at which step of a command loop a user is.
     *
     * @param userID UserID of the user which's state is saved.
     * @param state  The step the user is. Can have the value of one of the '*COMMAND*' Strings in {@link Config.Bot Config.Bot}.
     * @see Config.Bot#ASK_COMMAND_WRITE_QUESTION
     * @see Config.Bot#NO_COMMAND
     * @see Config.Bot#ANSWER_COMMAND_CHOOSE_NUMBER
     * @see Config.Bot#ANSWER_COMMAND_WRITE_ANSWER
     * @see Config.Bot#PIN_PICTURE_COMMAND_SEND_DESCRIPTION
     * @see Config.Bot#PIN_PICTURE_COMMAND_SEND_TITLE
     * @see Config.Bot#PIN_PICTURE_COMMAND_SEND_PICTURE
     * @see Config.Bot#PIN_PICTURE_COMMAND_SEND_DURATION
     * @see #getUserCommandState(Integer userID) Look up the command state.
     */
    public void setUserCommandState(Integer userID, String state) throws DatabaseException {
        if (!state.equals(Config.Bot.ASK_COMMAND_WRITE_QUESTION)
                && !state.equals(Config.Bot.NO_COMMAND)
                && !state.equals(Config.Bot.ANSWER_COMMAND_CHOOSE_NUMBER)
                && !state.equals(Config.Bot.ANSWER_COMMAND_WRITE_ANSWER)
                && !state.equals(Config.Bot.PIN_PICTURE_COMMAND_SEND_DESCRIPTION)
                && !state.equals(Config.Bot.PIN_PICTURE_COMMAND_SEND_TITLE)
                && !state.equals(Config.Bot.PIN_PICTURE_COMMAND_SEND_PICTURE)
                && !state.equals(Config.Bot.PIN_PICTURE_COMMAND_SEND_DURATION)
                && !state.equals(Bot.PIN_VIDEO_COMMAND_SEND_VIDEO)
                && !state.equals(Bot.PIN_VIDEO_COMMAND_SEND_DESCRIPTION)
                && !state.equals(Bot.PIN_VIDEO_COMMAND_SEND_TITLE)) {
            throw new IllegalArgumentException("No known state: " + state);
        }

        FileBasedConfiguration configuration = getConfiguration(userID);

        try {
            configuration.setProperty(Config.Keys.USER_COMMAND_STATE, state);
        } catch (NullPointerException e) {
            configuration.addProperty(Config.Keys.USER_COMMAND_STATE, state);
        }
    }

    /**
     * Look up the step of a command loop a user is.
     *
     * @param userID UserID of the user who's step is looked up.
     * @return The step of a command loop the user is.
     * @see #setUserCommandState(Integer userID, String state) Save the command state of a user.
     */
    public String getUserCommandState(Integer userID) throws DatabaseException {
        return getConfiguration(userID).getString(Config.Keys.USER_COMMAND_STATE, Bot.NO_COMMAND);
    }

    /**
     * Reads a question from the questions subdirectory.
     *
     * @param questionID The ID of the question.
     * @return The question.
     * @see #getQuestions() Read all questions from questions subdirectory.
     * @see #createQuestion(String question, Long chatID) Create a new question.
     * @see #getQuestionChatID(int questionID) Get the chatID of a question.
     * @see #getQuestionChatIDs() Get all chatID's of questions.
     * @see #getNumberOfQuestions() Get the number of all existing questions.
     * @see #deleteQuestion(int questionID) Delete a question.
     * @see #deleteQuestions() Deletes all questions.
     */
    public String getQuestion(int questionID) throws DatabaseException {
        return getConfiguration(questionID).getString(Config.Keys.QUESTION);
    }

    /**
     * Reads the chatID of the chat where the question was asked.
     *
     * @param questionID The ID of the question of what we want to know the chatID.
     * @return The chatID from where the question was asked.
     * @see #getQuestion(int questionID) Get a question's text.
     * @see #getQuestions() Read all questions from questions subdirectory.
     * @see #getQuestionChatIDs() Get all chatID's of questions.
     * @see #createQuestion(String question, Long chatID) Create a new question.
     * @see #getNumberOfQuestions() Get the number of all existing questions.
     * @see #deleteQuestion(int questionID) Delete a question.
     * @see #deleteQuestions() Deletes all questions.
     */
    public Long getQuestionChatID(int questionID) throws DatabaseException {
        return getConfiguration(questionID).getLong(Config.Keys.CHAT_ID, this.getAdminChatId());
    }

    /**
     * Reads all questions in the questions subdirectory.
     *
     * @return A ListArray of type String containing the text of all questions.
     * @see #getQuestion(int questionID) Get a question's text.
     * @see #createQuestion(String question, Long chatID) Create a new question.
     * @see #getQuestionChatID(int questionID) Get the chatID of a question.
     * @see #getQuestionChatIDs() Get all chatID's of questions.
     * @see #getNumberOfQuestions() Get the number of all existing questions.
     * @see #deleteQuestion(int questionID) Delete a question.
     * @see #deleteQuestions() Deletes all questions.
     */
    public String[] getQuestions() throws DatabaseException {
        List<String> questions = new ArrayList<>();
        boolean gotAllFiles = false;
        int filesScanned = 0;
        Path currentFile;

        synchronized (this) {
            while (!gotAllFiles) {
                currentFile = getDatabaseQuestionPath(filesScanned);


                if (Files.notExists(currentFile)) {
                    gotAllFiles = true;
                } else {
                    questions.add(getQuestion(filesScanned));
                    filesScanned++;
                }
            }
        }

        return questions.toArray(new String[0]);
    }

    /**
     * Get all chatID's from questions in the questions subdirectory.
     *
     * @return A ListArray of type Long containing all the chatID's of all questions.
     * @see #getQuestion(int questionID) Get a question's text.
     * @see #getQuestions() Read all questions from questions subdirectory.
     * @see #createQuestion(String question, Long chatID) Create a new question.
     * @see #getQuestionChatID(int questionID) Get the chatID of a question.
     * @see #getQuestionChatIDs() Get all chatID's of questions.
     * @see #getNumberOfQuestions() Get the number of all existing questions.
     * @see #deleteQuestion(int questionID) Delete a question.
     * @see #deleteQuestions() Deletes all questions.
     */
    public Long[] getQuestionChatIDs() throws DatabaseException {
        List<Long> chatIDs = new ArrayList<>();
        boolean gotAllFiles = false;
        int filesScanned = 0;
        Path currentFile;

        synchronized (this) {
            while (!gotAllFiles) {
                currentFile = getDatabaseQuestionPath(filesScanned);

                if (Files.notExists(currentFile)) {
                    gotAllFiles = true;
                } else {
                    chatIDs.add(getQuestionChatID(filesScanned));
                    filesScanned++;
                }
            }
        }

        return chatIDs.toArray(new Long[0]);
    }

    /**
     * Count the number of all questions in the questions subdirectory.
     *
     * @return Number of all questions.
     * @see #getQuestion(int questionID) Get a question's text.
     * @see #getQuestions() Read all questions from questions subdirectory.
     * @see #createQuestion(String question, Long chatID) Create a new question.
     * @see #getQuestionChatID(int questionID) Get the chatID of a question.
     * @see #getQuestionChatIDs() Get all chatID's of questions.
     * @see #deleteQuestion(int questionID) Delete a question.
     * @see #deleteQuestions() Deletes all questions.
     */
    public int getNumberOfQuestions() {
        boolean gotAllFiles = false;
        int filesScanned = 0;
        Path currentFile;

        synchronized (this) {
            while (!gotAllFiles) {
                currentFile = getDatabaseQuestionPath(filesScanned);


                if (Files.notExists(currentFile)) {
                    gotAllFiles = true;
                } else {
                    filesScanned++;
                }
            }
        }

        return filesScanned;
    }

    /**
     * Creates a new question.
     * The question's ID is one bigger than the number of existing files.
     *
     * @param question The text of the question.
     * @param chatID   The chatID of the chat in which the question was asked.
     * @see #getQuestion(int questionID) Get a question's text.
     * @see #getQuestions() Read all questions from questions subdirectory.
     * @see #getQuestionChatID(int questionID) Get the chatID of a question.
     * @see #getQuestionChatIDs() Get all chatID's of questions.
     * @see #getNumberOfQuestions() Get the number of all existing questions.
     * @see #deleteQuestion(int questionID) Delete a question.
     * @see #deleteQuestions() Deletes all questions.
     */
    public void createQuestion(String question, Long chatID) throws DatabaseException {
        Path questionFile = getDatabaseQuestionPath(getNumberOfQuestions());

        createConfigurationFile(questionFile);

        FileBasedConfiguration configuration = getConfiguration(questionFile);

        configuration.addProperty(Config.Keys.QUESTION, question);
        configuration.addProperty(Config.Keys.CHAT_ID, chatID);
    }

    /**
     * Delete a question and adjust the IDs of the other questions.
     *
     * @param questionID The question that will be deleted.
     * @see #getQuestion(int questionID) Get a question's text.
     * @see #getQuestions() Read all questions from questions subdirectory.
     * @see #createQuestion(String question, Long chatID) Create a new question.
     * @see #getQuestionChatID(int questionID) Get the chatID of a question.
     * @see #getQuestionChatIDs() Get all chatID's of questions.
     * @see #getNumberOfQuestions() Get the number of all existing questions.
     * @see #deleteQuestions() Deletes all questions.
     */
    public void deleteQuestion(int questionID) throws DatabaseException {
        Long[] questionChatIDsArray = getQuestionChatIDs();
        String[] questionsArray = getQuestions();

        List<Long> questionChatIDs = new ArrayList<>();
        List<String> questions = new ArrayList<>();

        synchronized (this) {
            Collections.addAll(questionChatIDs, questionChatIDsArray);
            Collections.addAll(questions, questionsArray);

            deleteQuestions();

            questionChatIDs.remove(questionID);
            questions.remove(questionID);

            for (int x = 0; x < questions.size(); x++) {
                createQuestion(questions.get(x), questionChatIDs.get(x));
            }
        }
    }

    /**
     * Deletes all existing questions in questions subdirectory.
     *
     * @throws IOException Error deleting a file.
     * @see #getQuestion(int questionID) Get a question's text.
     * @see #getQuestions() Read all questions from questions subdirectory.
     * @see #createQuestion(String question, Long chatID) Create a new question.
     * @see #getQuestionChatID(int questionID) Get the chatID of a question.
     * @see #getQuestionChatIDs() Get all chatID's of questions.
     * @see #getNumberOfQuestions() Get the number of all existing questions.
     * @see #deleteQuestion(int questionID) Delete a question.
     */
    public void deleteQuestions() throws DatabaseException {

        int filesDeleted = 0;
        boolean processedAllFiles = false;

        Path currentFile;

        synchronized (this) {
            HashMap<Path, IOException> exceptions = new HashMap<>();
            while (!processedAllFiles) {
                currentFile = getDatabaseQuestionPath(filesDeleted);

                if (Files.notExists(currentFile)) {
                    processedAllFiles = true;
                } else {
                    //TODO use the boolean value returned by deleteIfExists WHY? I dont remember why I should use it here o_o
                    try {
                        Files.deleteIfExists(currentFile);
                    } catch (IOException e) {
                        exceptions.put(currentFile, e);
                    }
                    filesDeleted++;
                }
            }
            if (!exceptions.isEmpty()) {
                throw new DatabaseException(exceptions.keySet().toArray(new Path[]{}), mergeExceptions(exceptions.values().toArray(new Exception[]{})));
            }
        }

    }

    /**
     * Set's the description of a {@link DisplayFile DisplayFile}.
     *
     * @param displayFileName The name of the displayFile to set the description.
     * @param description     The description text.
     * @see #addDisplayFile(String displayFileName, User user) Create a new displayFile.
     * @see #setDisplayFileDuration(String displayFileName, int duration) Save the duration for a displayFile.
     * @see #setDisplayFileType(String displayFileName, String type) Save the type of a displayFile.
     */
    public void setDisplayFileDescription(String displayFileName, String description)
            throws DatabaseException {

        FileBasedConfiguration configuration = getConfiguration(displayFileName);
        try {
            configuration.setProperty(Config.Keys.DISPLAY_FILE_DESCRIPTION, description);
        } catch (NullPointerException e) {
            configuration.addProperty(Config.Keys.DISPLAY_FILE_DESCRIPTION, description);
        }
    }

    public String getDisplayFileDescription(String displayFileName) throws DatabaseException {
        return this.getConfiguration(displayFileName).getString(Keys.DISPLAY_FILE_DESCRIPTION);
    }

    /**
     * Set's the display duration of a {@link DisplayFile DisplayFile}.
     *
     * @param displayFileName The name of the displayFile to set the duration.
     * @param duration        The duration in seconds.
     * @see #addDisplayFile(String displayFileName, User user) Create a new displayFile.
     * @see #setDisplayFileDescription(String displayFileName, String description) Set the description of a displayFile.
     * @see #setDisplayFileType(String displayFileName, String type) Save the type of a displayFile.
     */
    public void setDisplayFileDuration(String displayFileName, int duration) throws DatabaseException {

        FileBasedConfiguration configuration = getConfiguration(displayFileName);
        try {
            configuration.setProperty(Config.Keys.DISPLAY_FILE_DURATION_KEY, duration);
        } catch (NullPointerException e) {
            configuration.addProperty(Config.Keys.DISPLAY_FILE_DURATION_KEY, duration);
        }
    }

    public int getDisplayFileDuration(String name) throws DatabaseException {
        return this.getConfiguration(name).getInt(Keys.DISPLAY_FILE_DURATION_KEY, -1);
    }

    /**
     * Set's the type of a {@link DisplayFile DisplayFile}.
     *
     * @param displayFileName The name of the displayFile to set the type.
     * @param type            The type of the displayFile. Can be one of the 'DISPLAY_FILE_TYPE_*' Strings in
     *                        {@link Config.Bot Config.Bot}.
     * @see Config.Bot#DISPLAY_FILE_TYPE_IMAGE
     * @see #addDisplayFile(String displayFileName, User user) Create a new displayFile.
     * @see #setDisplayFileDuration(String displayFileName, int duration) Save the duration for a displayFile.
     * @see #setDisplayFileDescription(String displayFileName, String description) Set the description of a displayFile.
     */
    public void setDisplayFileType(String displayFileName, String type) throws DatabaseException {
        if (!type.equals(Config.Bot.DISPLAY_FILE_TYPE_IMAGE)
                && !type.equals(Bot.DISPLAY_FILE_TYPE_VIDEO)) {
            throw new IllegalArgumentException("No known type: " + type);
        }

        FileBasedConfiguration configuration = getConfiguration(displayFileName);
        try {
            configuration.setProperty(Config.Keys.DISPLAY_FILE_TYPE_KEY, type);
        } catch (NullPointerException e) {
            configuration.addProperty(Config.Keys.DISPLAY_FILE_TYPE_KEY, type);
        }
    }

    /**
     * Adds a new displayFile to the configuration file.
     *
     * @param displayFileName The name of the displayFile to be added.
     * @see #setDisplayFileDuration(String displayFileName, int duration) Save the duration for a displayFile.
     * @see #setDisplayFileDescription(String displayFileName, String description) Set the description of a displayFile.
     * @see #setDisplayFileType(String displayFileName, String type) Save the type of a displayFile.
     */
    public void addDisplayFile(String displayFileName, User user) throws DatabaseException {

        this.setDisplayFileUploadInfo(displayFileName, user);

        // add image to global image files 'database'
        List<String> displayFiles = getDisplayFiles();
        displayFiles.add(displayFileName);

        this.setDisplayFiles(displayFiles);

        // add image to users image files
        displayFiles = getDisplayFiles(user.getId());
        displayFiles.add(displayFileName);

        this.setDisplayFiles(displayFiles, user.getId());
    }

    /**
     * Get all display files that are registered in the main database.
     *
     * @return A {@link List<String>} containing the names of the display files.
     * @see #getDisplayFiles(Integer) Get the display files registered for a user.
     * @see #setDisplayFiles(List) Set the display files in the main database.
     */
    public List<String> getDisplayFiles() throws DatabaseException {
        return getConfiguration(Config.Paths.DISPLAY_FILES_CONFIG_FILE).getList(String.class,
                Config.Keys.DISPLAY_FILES_KEY, new ArrayList<>());
    }

    /**
     * Set the display files in the main database.
     *
     * @param displayFiles A {@link List<String>} containing the names of the display files.
     * @see #setDisplayFiles(List, Integer) Set the display files for a user.
     * @see #getDisplayFiles() Get the display files from the main database.
     */
    public void setDisplayFiles(List<String> displayFiles) throws DatabaseException {
        FileBasedConfiguration configuration = getConfiguration(Paths.DISPLAY_FILES_CONFIG_FILE);
        try {
            configuration.setProperty(Keys.DISPLAY_FILES_KEY, displayFiles);
        } catch (NullPointerException e) {
            configuration.addProperty(Keys.DISPLAY_FILES_KEY, displayFiles);
        }
    }

    /**
     * Get all display files that are registered for a user.
     *
     * @return A {@link List<String>} containing the names of the display files.
     * @see #setDisplayFiles(List, Integer) Set the display files for a user.
     * @see #getDisplayFiles() Get the display files registered in the main database.
     */
    public List<String> getDisplayFiles(Integer userId) throws DatabaseException {
        return getConfiguration(userId).getList(String.class, Keys.DISPLAY_FILES_KEY, new ArrayList<>());
    }

    /**
     * Set the display files in the main database.
     *
     * @param displayFiles A {@link List<String>} containing the names of the display files.
     * @param userId       The id of the user the display files belong to.
     * @see #setDisplayFiles(List) Set the display files of the main database.
     * @see #getDisplayFiles(Integer) Get the display files for a user.
     */
    public void setDisplayFiles(List<String> displayFiles, Integer userId) throws DatabaseException {
        FileBasedConfiguration configuration = getConfiguration(userId);
        try {
            configuration.setProperty(Keys.DISPLAY_FILES_KEY, displayFiles);
        } catch (NullPointerException e) {
            configuration.addProperty(Keys.DISPLAY_FILES_KEY, displayFiles);
        }
    }

    /**
     * Unregisters a display file by deleting it from the main database and the user's database it came from.
     *
     * @param displayFileName The name of the displayFile to be deleted.
     * @param userId          The id of the user who uploaded the displayFile.
     */
    public synchronized void removeDisplayFile(String displayFileName, Integer userId) throws DatabaseException {
        List<String> displayFiles = getDisplayFiles();
        if (!displayFiles.remove(displayFileName)) {
            throw new IllegalArgumentException("This display file is not registered in global database, not removing: " + displayFileName);
        }

        this.setDisplayFiles(displayFiles);

        displayFiles = getDisplayFiles(userId);
        if (!displayFiles.remove(displayFileName)) {
            throw new IllegalArgumentException("This display file is not registered in user's database, not removing: " + displayFileName);
        }

        this.setDisplayFiles(displayFiles, userId);
    }

    /**
     * Saves selectedQuestion value in the configuration file of a user.
     *
     * @param userID           User for who the value is saved.
     * @param selectedQuestion Value that should be saved for selectedQuestion.
     * @see #getSelectedQuestion(Integer userID) Read the value of selectedQuestion.
     */
    public void setSelectedQuestion(Integer userID, int selectedQuestion) throws DatabaseException {
        FileBasedConfiguration configuration = getConfiguration(userID);
        try {
            configuration.setProperty(Config.Keys.SELECTED_QUESTION, selectedQuestion);
        } catch (NullPointerException e) {
            configuration.addProperty(Config.Keys.SELECTED_QUESTION, selectedQuestion);
        }
    }

    /**
     * Reads selectedQuestion value from the configuration file of a user.
     *
     * @param userID User of who the value is read.
     * @return The value of selectedQuestion.
     * @see #setSelectedQuestion(Integer userID, int selectedQuestion) Save the value of selectedQuestion.
     */
    public int getSelectedQuestion(Integer userID) throws DatabaseException {
        return getConfiguration(userID).getInt(Config.Keys.SELECTED_QUESTION);
    }

    /**
     * Saves currentPictureTitle in the configuration file of a user.
     *
     * @param userID User for who the value is saved.
     * @param title  The title that is to be saved.
     * @see #getCurrentPictureTitle(Integer userID) Read the currentPictureTitle.
     */
    public void setCurrentPictureTitle(Integer userID, String title) throws DatabaseException, FileAlreadyExistsException {
        if (Files.exists(this.getDatabaseDisplayFilePath(title))) {
            throw new FileAlreadyExistsException("There already is one file with this name.");
        }

        FileBasedConfiguration configuration = getConfiguration(userID);
        try {
            configuration.setProperty(Config.Keys.CURRENT_PICTURE_TITLE, title);
        } catch (NullPointerException e) {
            configuration.addProperty(Config.Keys.CURRENT_PICTURE_TITLE, title);
        }
    }

    /**
     * Reads currentPictureTile from the configuration file of a user.
     *
     * @param userID User of who the value is read.
     * @return The currentPictureTitle for the specified user.
     * @see #setCurrentPictureTitle(Integer userID, String title) Save the currentPicture title.
     */
    public String getCurrentPictureTitle(Integer userID) throws DatabaseException {
        return getConfiguration(userID).getString(Config.Keys.CURRENT_PICTURE_TITLE);
    }

    /**
     * Saves currentPictureDescription in the configuration file of a user.
     *
     * @param userId      User for who the value is saved.
     * @param description The description to be changed.
     * @see #getCurrentPictureDescription(Integer userId) Read the currentPictureDescription.
     */
    public void setCurrentPictureDescription(Integer userId, String description) throws DatabaseException {
        FileBasedConfiguration configuration = getConfiguration(userId);
        try {
            configuration.setProperty(Config.Keys.CURRENT_PICTURE_DESCRIPTION, description);
        } catch (NullPointerException e) {
            configuration.addProperty(Config.Keys.CURRENT_PICTURE_DESCRIPTION, description);
        }
    }

    /**
     * Reads currentPictureDescription from the configuration file of a user.
     *
     * @param userId User of who the value is read.
     * @return The currentPictureDescription for the specified user.
     * @see #setCurrentPictureDescription(Integer userId, String description) Save the currentPictureDescription.
     */
    public String getCurrentPictureDescription(Integer userId) throws DatabaseException {
        return getConfiguration(userId).getString(Config.Keys.CURRENT_PICTURE_DESCRIPTION);
    }

    /**
     * Saves currentPictureDuration in the configuration file of a user.
     *
     * @param userId   User for who the value is saved.
     * @param duration The new value for the duration.
     * @see #getCurrentPictureDuration(Integer userId) Read the currentPictureDuration.
     */
    public void setCurrentPictureDuration(Integer userId, int duration) throws DatabaseException {
        FileBasedConfiguration configuration =  getConfiguration(userId);
        try {
            configuration.setProperty(Config.Keys.CURRENT_PICTURE_DURATION, duration);
        } catch (NullPointerException e) {
            configuration.addProperty(Config.Keys.CURRENT_PICTURE_DURATION, duration);
        }
    }

    /**
     * Reads currentPictureDuration from the configuration file of a user.
     *
     * @param userId User of who the value is read.
     * @return The currentPictureDuration for the specified user.
     * @see #setCurrentPictureDuration(Integer userId, int duration) Save the currentPictureDuration.
     */
    public int getCurrentPictureDuration(Integer userId) throws DatabaseException {
        return getConfiguration(userId).getInt(Config.Keys.CURRENT_PICTURE_DURATION, 0);
    }

    /**
     * Saves currentVideoTitle in the configuration file of a user.
     *
     * @param userId User for who the value is saved.
     * @param title  The title that is to be saved.
     * @see #getCurrentVideoTitle(Integer userID) Read the currentPictureTitle.
     */
    public void setCurrentVideoTitle(Integer userId, String title) throws DatabaseException, FileAlreadyExistsException {
        if (Files.exists(this.getDatabaseDisplayFilePath(title))) {
            throw new FileAlreadyExistsException("There already is one file with this name.");
        }

        FileBasedConfiguration configuration = getConfiguration(userId);
        try {
            configuration.setProperty(Config.Keys.CURRENT_VIDEO_TITLE, title);
        } catch (NullPointerException e) {
            configuration.addProperty(Config.Keys.CURRENT_VIDEO_TITLE, title);
        }
    }

    /**
     * Reads currentVideoTitle from the configuration file of a user.
     *
     * @param userId User of who the value is read.
     * @return The currentPictureTitle for the specified user.
     * @see #setCurrentVideoTitle(Integer userID, String title) Save the currentVideo title.
     */
    public String getCurrentVideoTitle(Integer userId) throws DatabaseException {
        return getConfiguration(userId).getString(Config.Keys.CURRENT_VIDEO_TITLE);
    }

    /**
     * Saves currentVideoDescription in the configuration file of a user.
     *
     * @param userId      User for who the value is saved.
     * @param description The description to be changed.
     * @see #getCurrentVideoDescription(Integer userId) Read the currentVideoDescription.
     */
    public void setCurrentVideoDescription(Integer userId, String description) throws DatabaseException {
        FileBasedConfiguration configuration = getConfiguration(userId);
        try {
            configuration.setProperty(Config.Keys.CURRENT_VIDEO_DESCRIPTION, description);
        } catch (NullPointerException e) {
            configuration.addProperty(Config.Keys.CURRENT_VIDEO_DESCRIPTION, description);
        }
    }

    /**
     * Reads currentVideoDescription from the configuration file of a user.
     *
     * @param userId User of who the value is read.
     * @return The currentVideoDescription for the specified user.
     * @see #setCurrentVideoDescription(Integer userId, String description) Save the currentVideoDescription.
     */
    public String getCurrentVideoDescription(Integer userId) throws DatabaseException {
        return getConfiguration(userId).getString(Config.Keys.CURRENT_VIDEO_DESCRIPTION);
    }

    public void setDisplayFileUploadInfo(String title, User user) throws DatabaseException {
        FileBasedConfiguration configuration = getConfiguration(this.getDatabaseDisplayFilePath(title));
        String userName = Main.getSpecialFilteredUsername(user);
        Integer userId = user.getId();
        try {
            configuration.setProperty(Config.Keys.DISPLAY_FILE_UPLOAD_INFO_NAME, userName);
            configuration.setProperty(Keys.DISPLAY_FILE_UPLOAD_INFO_ID, userId);
        } catch (NullPointerException e) {
            configuration.addProperty(Keys.DISPLAY_FILE_UPLOAD_INFO_NAME, userName);
            configuration.addProperty(Keys.DISPLAY_FILE_UPLOAD_INFO_ID, userId);
        }
    }

    public String getDisplayFileUploadInfoName(String title) throws DatabaseException {
        return this.getConfiguration(this.getDatabaseDisplayFilePath(title))
                .getString(Keys.DISPLAY_FILE_UPLOAD_INFO_NAME);
    }

    public Integer getDisplayFileUploadInfoId(String title) throws DatabaseException {
        Integer defaultVal = this.getAdminUserId();
        return this.getConfiguration(this.getDatabaseDisplayFilePath(title))
                .getInteger(Keys.DISPLAY_FILE_UPLOAD_INFO_ID, defaultVal);
    }

    public void setDisplayFileId(String title, String fileId) throws DatabaseException {
        FileBasedConfiguration configuration = this.getConfiguration(title);
        try {
            configuration.setProperty(Config.Keys.DISPLAY_FILE_ID, fileId);
        } catch (NullPointerException e) {
            configuration.addProperty(Keys.DISPLAY_FILE_ID, fileId);
        }
    }

    public String getDisplayFileId(String title) throws DatabaseException {
        return this.getConfiguration(title).getString(Keys.DISPLAY_FILE_ID);
    }

    public void setDisplayFileTgType(String displayFileName, String tgType) throws DatabaseException {
        if (!tgType.equals(Config.Bot.DISPLAY_FILE_TG_TYPE_IMAGE)
                && !tgType.equals(Bot.DISPLAY_FILE_TG_TYPE_VIDEO)
                && !tgType.equals(Bot.DISPLAY_FILE_TG_TYPE_AS_DOCUMENT)) {
            throw new IllegalArgumentException("No known type: " + tgType);
        }

        FileBasedConfiguration configuration = getConfiguration(displayFileName);
        try {
            configuration.setProperty(Config.Keys.DISPLAY_FILE_TG_TYPE_KEY, tgType);
        } catch (NullPointerException e) {
            configuration.addProperty(Config.Keys.DISPLAY_FILE_TG_TYPE_KEY, tgType);
        }
    }

    public String getDisplayFileTgType(String displayFileName) throws DatabaseException {
        return this.getConfiguration(displayFileName).getString(Keys.DISPLAY_FILE_TG_TYPE_KEY);
    }

    /**
     * Download the file to display and add it together with its properties to the configuration files.
     *
     * @param absSender Used to get the file to download.
     * @param user Used to identify which title, etc. should be set.
     * @param fileId Used to get the file.
     * @param type Used to differentiate between the download methods.
     * @param tgType Differentiate between videos, images and images sent as documents.
     */
    public void createNewDisplayFile(AbsSender absSender, User user, String fileId, String type, String tgType) throws DatabaseException {
        String urlParams = null;
        GetFile getFileRequest = new GetFile();
        URL fileUrl = null;
        Path displayFile = null;
        try {
            switch (type) {
                case Bot.DISPLAY_FILE_TYPE_IMAGE: {
                    String pictureTitle = this.getCurrentPictureTitle(user.getId());

                    getFileRequest.setFileId(fileId);

                    File file = absSender.getFile(getFileRequest); //api

                    urlParams = "https://api.telegram.org/file/bot" + DatabaseManager.getInstance().getBotToken() + "/"
                            + file.getFilePath();
                    fileUrl = new URL(urlParams);
                    displayFile = FileSystems.getDefault().getPath(Paths.DISPLAY_FILES + "/" + pictureTitle);

                    FileUtils.copyURLToFile(fileUrl, displayFile.toFile()); //io

                    this.createConfigurationFile(this.getDatabaseDisplayFilePath(pictureTitle)); //io
                    this.setDisplayFileType(pictureTitle, Bot.DISPLAY_FILE_TYPE_IMAGE);
                    this.setDisplayFileTgType(pictureTitle, tgType);
                    this.setDisplayFileDescription(pictureTitle, this.getCurrentPictureDescription(user.getId()));
                    this.setDisplayFileDuration(pictureTitle, this.getCurrentPictureDuration(user.getId()));
                    this.setDisplayFileId(pictureTitle, fileId);
                    this.addDisplayFile(pictureTitle, user);
                    break;
                }
                case Bot.DISPLAY_FILE_TYPE_VIDEO: {
                    String videoTitle = this.getCurrentVideoTitle(user.getId());

                    getFileRequest.setFileId(fileId);

                    File file = absSender.getFile(getFileRequest); //api

                    urlParams = "https://api.telegram.org/file/bot" + DatabaseManager.getInstance().getBotToken() + "/" +
                            file.getFilePath();
                    fileUrl = new URL(urlParams);
                    displayFile = FileSystems.getDefault().getPath(Paths.DISPLAY_FILES + "/" + videoTitle);

                    FileUtils.copyURLToFile(fileUrl, displayFile.toFile()); //io

                    this.createConfigurationFile(this.getDatabaseDisplayFilePath(videoTitle));
                    this.setDisplayFileType(videoTitle, Bot.DISPLAY_FILE_TYPE_VIDEO);
                    this.setDisplayFileTgType(videoTitle, tgType);
                    this.setDisplayFileDescription(videoTitle, this.getCurrentVideoDescription(user.getId()));
                    this.setDisplayFileId(videoTitle, fileId);
                    this.addDisplayFile(videoTitle, user);
                    break;
                }
                default:
                    throw new IllegalArgumentException("No known type.");
            }
        } catch (MalformedURLException e) {
            throw new DatabaseException(urlParams, e);
        } catch (TelegramApiException e) {
            throw new DatabaseException(getFileRequest, e);
        } catch (IOException e) {
            throw new DatabaseException(fileUrl, displayFile, e);
        }
        this.saveBuilders();
    }

    /**
     * Wrapper for deleting display files that have not been uploaded by the admin so the userId is unknown for the command.
     * @param name The name of the file that should be deleted.
     * @see #deleteDisplayFile(Integer, String) The method that is deleting the display files.
     */
    public void deleteDisplayFile(String name) throws DatabaseException {
        this.deleteDisplayFile(this.getDisplayFileUploadInfoId(name), name);
    }

    /**
     * Delete a display file by removing it from the databases for display files and deleting the files.
     * @param userId The user id of the user who uploaded the file.
     * @param name The name of the file that is going to be deleted.
     * @see #deleteDisplayFile(String) A wrapper for this method when the user id is unknown.
     * @see #removeDisplayFile(String, Integer) Remove display files from the databases.
     */
    public void deleteDisplayFile(Integer userId, String name) throws DatabaseException {
        java.io.File file = FileSystems.getDefault().getPath(Paths.DISPLAY_FILES + "/" + name).toFile();
        if (!file.delete()) {
            throw new DatabaseException(file);
        }
        this.removeDisplayFile(name, userId);
        file = this.getDatabaseDisplayFilePath(name).toFile();
        if (!file.delete()) {
            throw new DatabaseException(file);
        }
        this.saveBuilders();
    }

    /**
     * Reads the name for the bot from the configuration file
     *
     * @return the username for the bot
     */
    public String getBotUsername() throws DatabaseException {
        return getConfiguration(Config.Paths.BOT_CONFIG_FILE).getString(Config.Keys.BOT_USERNAME_KEY);
    }

    /**
     * Reads the token of the bot from the configuration file
     *
     * @return the token for the bot
     */
    public String getBotToken() throws DatabaseException {
        return getConfiguration(Config.Paths.BOT_CONFIG_FILE).getString(Config.Keys.BOT_TOKEN_KEY);
    }

    /**
     * Reads the admin's user id from the configuration file
     *
     * @return the user id of the bot's admin
     */
    public Integer getAdminUserId() throws DatabaseException {
        getConfiguration(Config.Paths.BOT_CONFIG_FILE);
        return getConfiguration(Config.Paths.BOT_CONFIG_FILE).getInteger(Config.Keys.BOT_ADMIN_USER_ID_KEY, 0);
    }

    /**
     * Reads the admin's chat id from the configuration file
     *
     * @return the chat id of the chat between bot and admin
     */
    public Integer getAdminChatId() throws DatabaseException {
        return getConfiguration(Config.Paths.BOT_CONFIG_FILE).getInteger(Config.Keys.BOT_ADMIN_CHAT_ID_KEY, 0);
    }
}
