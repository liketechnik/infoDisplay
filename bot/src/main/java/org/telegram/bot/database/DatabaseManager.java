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
import Config.Paths;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.io.FileUtils;
import org.telegram.telegrambots.api.methods.GetFile;
import org.telegram.telegrambots.api.objects.File;
import org.telegram.telegrambots.bots.AbsSender;
import org.telegram.telegrambots.logging.BotLogger;

import java.io.IOException;
import java.net.URL;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Florian Warzecha
 * @version 1.0.1
 * @date 22 of October of 2016
 *
 * This class accesses the database.
 * It makes sure there is only one {@link #instance} of it at once to pretend IO errors from happening.
 */
public class DatabaseManager {

    private static final String LOGTAG ="DATABASEMANAGER";

    /**
     * Instance of {@link DatabaseManager DatabaseManager class}.
     * @see #getInstance()
     * */
    private static volatile DatabaseManager instance;


    /**
     * Initialises when {@link #getInstance()} is called.
     * It checks for the existence of paths and creates them when needed.
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
        if(Files.notExists(Config.Paths.QUESTION_DATABASE)) {
            try {
                Files.createDirectory(Config.Paths.QUESTION_DATABASE);
            } catch (IOException e) {
                BotLogger.error(LOGTAG, "Error creating bot database question directory.", e);
                System.exit(10);
            }
        }
    }

    /**
     * Returns an instance of {@link DatabaseManager DatabaseManager class}.
     * If there is no instance created yet, it {@link #DatabaseManager() initialises} one.
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

    /** Configuration on which operations take place. */
    private static Configuration currentConfiguration;
    /** Configuration builder on which operations take place. */
    private static FileBasedConfigurationBuilder<FileBasedConfiguration> currentBuilder;

    /**
     * User got active or inactive.
     * Saves user state to its configuration file.
     * @param userID UserID which state changed.
     * @param userActive State that is to be set.
     * @see #getUserState(Integer userID) Look up if user is active or inactive.
     */
    public void setUserState(Integer userID, boolean userActive)
            throws Exception
    {
        try {
            setCurrentConfiguration(userID);
        } catch (IllegalArgumentException e){
            BotLogger.info(LOGTAG, e);
            createUser(userID);
            setCurrentConfiguration(userID);
        }

        try {
            currentConfiguration.setProperty(Config.Keys.USER_ACTIVE, userActive);
        } catch (NullPointerException e) {
            currentConfiguration.addProperty(Config.Keys.USER_ACTIVE, userActive);
        }


        currentBuilder.save();
    }

    /**
     * Looks up if user is active or not.
     * Reads user state from its configuration file.
     * @param userID UserID of that we want the state to know.
     * @return User state.
     * @see #setUserState(Integer userID, boolean userState) Set if user is active or not.
     */
    public boolean getUserState(Integer userID) throws Exception {
        setCurrentConfiguration(userID);
        return currentConfiguration.getBoolean(Config.Keys.USER_ACTIVE);
    }

    /**
     * Sets the configuration (from {@link #currentBuilder}) on which operations take place.
     * Before this method can be called, the {@link #currentBuilder builder} needs to be set.
     * @see #setCurrentBuilder(Path configurationFile) set builder
     * @see #setCurrentConfiguration(Path configurationFile) set configuration for a file
     * @see #setCurrentConfiguration(Integer userID) set configuration for a user
     * @see #setCurrentConfiguration(int questionID) set configuration for a question
     * @see #setCurrentConfiguration(String displayFileName) set configuration for a display file
     */
    private void setCurrentConfiguration() throws Exception {
        currentConfiguration = currentBuilder.getConfiguration();
    }

    /**
     * User changed his language preference.
     * Save the language preference for a user in his configuration file.
     * @param userId UserId of the user who's language preference changed.
     * @param language The language that the user wants to receive his messages in.
     * @see #getUserLanguage(Integer userId) Get the language prefernce of a user.
     */
    public void setUserLanguage(Integer userId, String language) throws Exception {
        if (!language.equals(Config.Languages.ENGLISH) &&
                !language.equals(Config.Languages.GERMAN) &&
                !language.equals(Config.Languages.NONE)) {
            throw new IllegalArgumentException("No supported language.");
        }

        setCurrentConfiguration(userId);

        try {
            currentConfiguration.setProperty(Config.Keys.USER_LANGUAGE, language);
        } catch (NullPointerException e) {
            currentConfiguration.addProperty(Config.Keys.USER_LANGUAGE, language);
        }

        currentBuilder.save();
    }

    /**
     * Looks up in which language a user wants to receive his messages.
     * @param userId UserID of the user who's language preference should be looked up.
     * @return The language the messages send to the user should have.
     * @see #setUserLanguage(Integer userId, String language) Set the language preference for a user.
     */
    public String getUserLanguage(Integer userId) throws Exception {
        setCurrentConfiguration(userId);
        if (currentConfiguration.getString(Config.Keys.USER_LANGUAGE).equals(Config.Languages.NONE)) {
            throw new IllegalArgumentException("No language preference set for this user.");
        }
        return currentConfiguration.getString(Config.Keys.USER_LANGUAGE);
    }

    /**
     * Sets the configuration for a file on which operations take place.
     * This methods automates setting of the {@link #currentBuilder builder}.
     * @param configurationFile File from where the configuration is loaded.
     * @see #setCurrentConfiguration() set configuration for the {@link #currentBuilder builder}
     * @see #setCurrentConfiguration(Integer userID) set configuration for a user
     * @see #setCurrentConfiguration(int questionID) set configuration for a question
     * @see #setCurrentConfiguration(String displayFileName) set configuration for a display file
     */
    private void setCurrentConfiguration(Path configurationFile) throws Exception {
        setCurrentBuilder(configurationFile);
        setCurrentConfiguration();
    }

    /**
     * Sets the {@link #currentConfiguration configuration} for a user on which operations take place.
     * This method automates the process of getting the file path for a user.
     * @param userID configuration file of the user with this ID.
     * @see #setCurrentConfiguration(Path configurationFile) set configuration for a file
     * @see #setCurrentConfiguration() set configuration for the {@link #currentBuilder builder}
     * @see #setCurrentConfiguration(int questionID) set configuration for a question
     * @see #setCurrentConfiguration(String displayFileName) set configuration for a display file
     */
    private void setCurrentConfiguration(Integer userID) throws Exception {
        Path configurationFile = getDatabaseUserPath(userID);
        setCurrentConfiguration(configurationFile);
    }

    /**
     * Sets the {@link #currentConfiguration configuration} for a user on which operations take place.
     * This method automates the process of getting the file path for a user.
     * @param questionID configuration file of the question with this ID.
     * @see #setCurrentConfiguration(Path configurationFile) set configuration for a file
     * @see #setCurrentConfiguration(Integer userID) set configuration for a user
     * @see #setCurrentConfiguration() set configuration for the {@link #currentBuilder builder}
     * @see #setCurrentConfiguration(String displayFileName) set configuration for a display file
     */
    private void setCurrentConfiguration(int questionID) throws Exception {
        Path configurationFile = getDatabaseQuestionPath(questionID);
        setCurrentConfiguration(configurationFile);
    }

    /**
     * Sets the {@link #currentConfiguration configuration} for a user on which operations take place.
     * This method automates the process of getting the file path for a user.
     * @param displayFile configuration file of the {@link org.telegram.bot.DisplayFile displayFile} with this name.
     * @see #setCurrentConfiguration(Path configurationFile) set configuration for a file
     * @see #setCurrentConfiguration(Integer userID) set configuration for a user
     * @see #setCurrentConfiguration(int questionID) set configuration for a question
     * @see #setCurrentConfiguration() set configuration for the builder
     */
    private void setCurrentConfiguration(String displayFile) throws Exception {
        Path configurationFile = getDatabaseDisplayFilePath(displayFile);
        setCurrentConfiguration(configurationFile);
    }

    /**
     * Sets the {@link #currentBuilder builder} for a configuration file.
     * @param configurationFile File the builder reads from.
     */
    private void setCurrentBuilder(Path configurationFile) throws Exception {
        if (Files.notExists(configurationFile)) {
            throw new IllegalArgumentException("Configuration file not found: " + configurationFile);
        }

        Parameters parameters = new Parameters();
        this.currentBuilder = new FileBasedConfigurationBuilder<FileBasedConfiguration>(PropertiesConfiguration.class)
                .configure(parameters.properties()
                        .setPath(configurationFile.toString()));
    }

    /**
     * Creates a configuration file.
     * @param file Path of the file to be created.
     * @throws IOException Creation of {@code file} failed.
     */
    public void createConfigurationFile(Path file) throws IOException {
        Files.createFile(file);
    }

    /**
     * Creates a configuration file for a user.
     * The files are created in the users subdirectory of the bot database.
     * {@code userID} is used as name for the file, so a configuration file has a unique name.
     * @param userID The name of the configuration file.
     */
    private void createUser(Integer userID) throws Exception {
        Path file = getDatabaseUserPath(userID);
        createConfigurationFile(file);
        BotLogger.info(LOGTAG, "Created configuration file " + file.toString());
    }

    /**
     * Adds the default prefix for user configuration files and the userID together.
     * @param userID Name of the configuration file the path refers to.
     * @return Path to the configuration file of user with {@code userID}.
     */
    public Path getDatabaseUserPath(Integer userID) {
        return FileSystems.getDefault().getPath(Config.Paths.USER_DATABASE + "/" + userID.toString());
    }

    /**
     * Adds the default prefix for question configuration files and questionID together.
     * @param questionID Name of the configuration file the path refers to.
     * @return Path to the configuration file of question with {@code questionID}.
     */
    public Path getDatabaseQuestionPath(int questionID) {
        return FileSystems.getDefault().getPath(Config.Paths.QUESTION_DATABASE + "/" + questionID);
    }

    /**
     * Adds the default prefix for displayFile configuration files and name together.
     * @param name Name of the configuration file the path refers to.
     * @return Path to the configuration file of displayFile with {@code name}.
     */
    public Path getDatabaseDisplayFilePath(String name) {
        return FileSystems.getDefault().getPath(Config.Paths.DISPLAY_FILES + "/" + name + ".conf");
    }

    /**
     * Saves the registration state of a user in its configuration file.
     * @param userID UserID of the user who's file is edited.
     * @param registered Registration state to be set.
     * @see #getUserRegistrationState(Integer userID) Look up the registration state.
     */
    public void setUserRegistrationState(Integer userID, boolean registered)
            throws Exception {

        setCurrentConfiguration(userID);

        try {
            currentConfiguration.setProperty(Config.Keys.USER_REGISTERED, registered);
        } catch (NullPointerException e) {
            currentConfiguration.addProperty(Config.Keys.USER_REGISTERED, registered);
        }

        currentBuilder.save();
    }

    /**
     * Looks up if the user is marked as registered in its configuration file.
     * @param userID UserID of the user which config file is used to look up.
     * @return Registration state of the user.
     * @see #setUserRegistrationState(Integer userID, boolean registered) Save the registration state.
     */
    public boolean getUserRegistrationState(Integer userID) throws Exception {
        setCurrentConfiguration(userID);
        return currentConfiguration.getBoolean(Config.Keys.USER_REGISTERED);
    }


    /**
     * Saves the registration state of a user in its configuration file.
     * @param userID UserID of the user who's file is edited.
     * @param wantsRegistration Wants registration state to be set.
     * @see #getUserWantsRegistrationState(Integer userID)  Look up the wantsRegistration state.
     */
    public void setUserWantsRegistrationState(Integer userID, boolean wantsRegistration)
            throws Exception {

        setCurrentConfiguration(userID);

        try {
            currentConfiguration.setProperty(Config.Keys.USER_WANTS_REGISTRATION, wantsRegistration);
        } catch (NullPointerException e) {
            currentConfiguration.addProperty(Config.Keys.USER_WANTS_REGISTRATION, wantsRegistration);
        }

        currentBuilder.save();
    }

    /**
     * Looks up if the user is marked as registered in its configuration file.
     * @param userID UserID of the user which config file is used to look up.
     * @return WantsRegistration state of the user.
     * @see #setUserWantsRegistrationState(Integer userID, boolean wantsRegistration) Save the wantsRegistration state.
     */
    public boolean getUserWantsRegistrationState(Integer userID) throws Exception {
        setCurrentConfiguration(userID);
        return currentConfiguration.getBoolean(Config.Keys.USER_WANTS_REGISTRATION);
    }

    /**
     * Saves at which step of a command loop a user is.
     * @param userID UserID of the user which's state is saved.
     * @param state The step the user is. Can have the value of one of the '*COMMAND*' Strings in {@link org.telegram.bot.Config.Bot Config.Bot}.
     * @see org.telegram.bot.Config.Bot#ASK_COMMAND_WRITE_QUESTION
     * @see org.telegram.bot.Config.Bot#NO_COMMAND
     * @see org.telegram.bot.Config.Bot#ANSWER_COMMAND_CHOOSE_NUMBER
     * @see org.telegram.bot.Config.Bot#ANSWER_COMMAND_WRITE_ANSWER
     * @see org.telegram.bot.Config.Bot#PIN_PICTURE_COMMAND_SEND_DESCRIPTION
     * @see org.telegram.bot.Config.Bot#PIN_PICTURE_COMMAND_SEND_TITLE
     * @see org.telegram.bot.Config.Bot#PIN_PICTURE_COMMAND_SEND_PICTURE
     * @see org.telegram.bot.Config.Bot#PIN_PICTURE_COMMAND_SEND_DURATION
     * @see #getUserCommandState(Integer userID) Look up the command state.
     */
    public void setUserCommandState(Integer userID, String state) throws Exception {
        if(!state.equals(Config.Bot.ASK_COMMAND_WRITE_QUESTION)
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

        setCurrentConfiguration(userID);

        try {
            currentConfiguration.setProperty(Config.Keys.USER_COMMAND_STATE, state);
        } catch (NullPointerException e) {
            currentConfiguration.addProperty(Config.Keys.USER_COMMAND_STATE, state);
        }

        currentBuilder.save();
    }

    /**
     * Look up the step of a command loop a user is.
     * @param userID UserID of the user who's step is looked up.
     * @return The step of a command loop the user is.
     * @see #setUserCommandState(Integer userID, String state) Save the command state of a user.
     */
    public String getUserCommandState(Integer userID) throws Exception {
        setCurrentConfiguration(userID);

        return  currentConfiguration.getString(Config.Keys.USER_COMMAND_STATE);
    }

    /**
     * Reads a question from the questions subdirectory.
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
    public String getQuestion(int questionID) throws Exception {
        setCurrentConfiguration(questionID);
        return currentConfiguration.getString(Config.Keys.QUESTION);
    }

    /**
     * Reads the chatID of the chat where the question was asked.
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
    public Long getQuestionChatID(int questionID) throws Exception {
        setCurrentConfiguration(questionID);
        return  currentConfiguration.getLong(Config.Keys.CHAT_ID, this.getAdminChatId());
    }

    /**
     * Reads all questions in the questions subdirectory.
     * @return A ListArray of type String containing the text of all questions.
     * @see #getQuestion(int questionID) Get a question's text.
     * @see #createQuestion(String question, Long chatID) Create a new question.
     * @see #getQuestionChatID(int questionID) Get the chatID of a question.
     * @see #getQuestionChatIDs() Get all chatID's of questions.
     * @see #getNumberOfQuestions() Get the number of all existing questions.
     * @see #deleteQuestion(int questionID) Delete a question.
     * @see #deleteQuestions() Deletes all questions.
     */
    public String[] getQuestions() throws Exception {
        List<String> questions = new ArrayList<String>();
        boolean gotAllFiles = false;
        int filesScanned = 0;
        Path currentFile;

        while (gotAllFiles == false) {
            currentFile = getDatabaseQuestionPath(filesScanned);

            if(Files.notExists(currentFile)) {
                gotAllFiles = true;
            } else {
                questions.add(getQuestion(filesScanned));
                filesScanned++;
            }
        }

        return  questions.toArray(new String[0]);
    }

    /**
     * Get all chatID's from questions in the questions subdirectory.
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
    public Long[] getQuestionChatIDs() throws Exception {
        List<Long> chatIDs = new ArrayList<Long>();
        boolean gotAllFiles = false;
        int filesScanned = 0;
        Path currentFile;

        while (gotAllFiles == false) {
            currentFile = getDatabaseQuestionPath(filesScanned);

            if(Files.notExists(currentFile)) {
                gotAllFiles = true;
            } else {
                chatIDs.add(getQuestionChatID(filesScanned));
                filesScanned++;
            }
        }

        return chatIDs.toArray(new Long[0]);
    }

    /**
     * Count the number of all questions in the questions subdirectory.
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

        while (gotAllFiles == false) {
            currentFile = getDatabaseQuestionPath(filesScanned);

            if(Files.notExists(currentFile)) {
                gotAllFiles = true;
            } else {
                filesScanned++;
            }
        }

        return filesScanned;
    }

    /**
     * Creates a new question.
     * The question's ID is one bigger than the number of existing files.
     * @param question The text of the question.
     * @param chatID The chatID of the chat in which the question was asked.
     * @see #getQuestion(int questionID) Get a question's text.
     * @see #getQuestions() Read all questions from questions subdirectory.
     * @see #getQuestionChatID(int questionID) Get the chatID of a question.
     * @see #getQuestionChatIDs() Get all chatID's of questions.
     * @see #getNumberOfQuestions() Get the number of all existing questions.
     * @see #deleteQuestion(int questionID) Delete a question.
     * @see #deleteQuestions() Deletes all questions.
     */
    public void createQuestion(String question, Long chatID) throws Exception {
        Path questionFile = getDatabaseQuestionPath(getNumberOfQuestions());

        createConfigurationFile(questionFile);
        setCurrentConfiguration(questionFile);

        currentConfiguration.addProperty(Config.Keys.QUESTION, question);
        currentConfiguration.addProperty(Config.Keys.CHAT_ID, chatID);

        currentBuilder.save();
    }

    /**
     * Delete a question and adjust the IDs of the other questions.
     * @param questionID The question that will be deleted.
     * @see #getQuestion(int questionID) Get a question's text.
     * @see #getQuestions() Read all questions from questions subdirectory.
     * @see #createQuestion(String question, Long chatID) Create a new question.
     * @see #getQuestionChatID(int questionID) Get the chatID of a question.
     * @see #getQuestionChatIDs() Get all chatID's of questions.
     * @see #getNumberOfQuestions() Get the number of all existing questions.
     * @see #deleteQuestions() Deletes all questions.
     */
    public void deleteQuestion(int questionID) throws Exception {
        Long[] questionChatIDsArray = getQuestionChatIDs();
        String[] questionsArray = getQuestions();

        List<Long> questionChatIDs = new ArrayList<Long>();
        List<String> questions = new ArrayList<String>();

        for (Long questionChatID : questionChatIDsArray) {
            questionChatIDs.add(questionChatID);
        }
        for (String question : questionsArray) {
            questions.add(question);
        }

        deleteQuestions();

        questionChatIDs.remove(questionID);
        questions.remove(questionID);

        for (int x = 0; x < questions.size(); x++) {
            createQuestion(questions.get(x), questionChatIDs.get(x));
        }
    }

    /**
     * Deletes all existing questions in questions subdirectory.
     * @throws IOException Error deleting a file.
     * @see #getQuestion(int questionID) Get a question's text.
     * @see #getQuestions() Read all questions from questions subdirectory.
     * @see #createQuestion(String question, Long chatID) Create a new question.
     * @see #getQuestionChatID(int questionID) Get the chatID of a question.
     * @see #getQuestionChatIDs() Get all chatID's of questions.
     * @see #getNumberOfQuestions() Get the number of all existing questions.
     * @see #deleteQuestion(int questionID) Delete a question.
     */
    public void deleteQuestions() throws IOException {

        int filesDeleted = 0;
        boolean processedAllFiles = false;

        Path currentFile;

        while (processedAllFiles == false) {
            currentFile = getDatabaseQuestionPath(filesDeleted);

            if (Files.notExists(currentFile)) {
                processedAllFiles = true;
            } else {
                Files.deleteIfExists(currentFile);
                filesDeleted++;
            }
        }

    }

    /**
     * Set's the description of a {@link org.telegram.bot.DisplayFile DisplayFile}.
     * @param displayFileName The name of the displayFile to set the description.
     * @param description The description text.
     * @see #addDisplayFile(String displayFileName) Create a new displayFile.
     * @see #setDisplayFileDuration(String displayFileName, int duration) Save the duration for a displayFile.
     * @see #setDisplayFileType(String displayFileName, String type) Save the type of a displayFile.
     */
    public void setDisplayFileDescription(String displayFileName, String description)
            throws Exception {

        setCurrentConfiguration(displayFileName);
        try {
            currentConfiguration.setProperty(Config.Keys.DISPLAY_FILE_DESCRIPTION, description);
        } catch (NullPointerException e) {
            currentConfiguration.addProperty(Config.Keys.DISPLAY_FILE_DESCRIPTION, description);
        }

        currentBuilder.save();
    }

    /**
     * Set's the display duration of a {@link org.telegram.bot.DisplayFile DisplayFile}.
     * @param displayFileName The name of the displayFile to set the duration.
     * @param duration The duration in seconds.
     * @see #addDisplayFile(String displayFileName) Create a new displayFile.
     * @see #setDisplayFileDescription(String displayFileName, String description) Set the description of a displayFile.
     * @see #setDisplayFileType(String displayFileName, String type) Save the type of a displayFile.
     */
    public void setDisplayFileDuration(String displayFileName, int duration) throws Exception {

        setCurrentConfiguration(displayFileName);
        try {
            currentConfiguration.setProperty(Config.Keys.DISPLAY_FILE_DURATION_KEY, duration);
        } catch (NullPointerException e) {
            currentConfiguration.addProperty(Config.Keys.DISPLAY_FILE_DURATION_KEY, duration);
        }

        currentBuilder.save();
    }

    /**
     * Set's the type of a {@link org.telegram.bot.DisplayFile DisplayFile}.
     * @param displayFileName The name of the displayFile to set the type.
     * @param type The type of the displayFile. Can be one of the 'DISPLAY_FILE_TYPE_*' Strings in
     * {@link org.telegram.bot.Config.Bot Config.Bot}.
     * @see org.telegram.bot.Config.Bot#DISPLAY_FILE_TYPE_IMAGE
     * @see #addDisplayFile(String displayFileName) Create a new displayFile.
     * @see #setDisplayFileDuration(String displayFileName, int duration) Save the duration for a displayFile.
     * @see #setDisplayFileDescription(String displayFileName, String description) Set the description of a displayFile.
     */
    public void setDisplayFileType(String displayFileName, String type) throws Exception {
        if (!type.equals(Config.Bot.DISPLAY_FILE_TYPE_IMAGE)
                && !type.equals(Bot.DISPLAY_FILE_TYPE_VIDEO)) {
            throw new IllegalArgumentException("No known type: " + type);
        }


        setCurrentConfiguration(displayFileName);
        try {
            currentConfiguration.setProperty(Config.Keys.DISPLAY_FILE_TYPE_KEY, type);
        } catch (NullPointerException e) {
            currentConfiguration.addProperty(Config.Keys.DISPLAY_FILE_TYPE_KEY, type);
        }

        currentBuilder.save();
    }

    /**
     * Adds a new displayFile to the configuration file.
     * @param displayFileName The name of the displayFile to be added.
     * @see #setDisplayFileDuration(String displayFileName, int duration) Save the duration for a displayFile.
     * @see #setDisplayFileDescription(String displayFileName, String description) Set the description of a displayFile.
     * @see #setDisplayFileType(String displayFileName, String type) Save the type of a displayFile.
     */
    public void addDisplayFile(String displayFileName) throws Exception {

        List<String> displayFiles = getDisplayFiles();
        displayFiles.add(displayFileName);

        setCurrentConfiguration(Config.Paths.DISPLAY_FILES_CONFIG_FILE);
        try {
            currentConfiguration.setProperty(Config.Keys.DISPLAY_FILES_KEY, displayFiles);
        } catch (NullPointerException e) {
            currentConfiguration.addProperty(Config.Keys.DISPLAY_FILES_KEY, displayFiles);
        }

        currentBuilder.save();
    }

    public List<String> getDisplayFiles() throws Exception {
        setCurrentConfiguration(Config.Paths.DISPLAY_FILES_CONFIG_FILE);
        return currentConfiguration.getList(String.class, Config.Keys.DISPLAY_FILES_KEY, new ArrayList<String>());
    }

    /**
     * Saves selectedQuestion value in the configuration file of a user.
     * @param userID User for who the value is saved.
     * @param selectedQuestion Value that should be saved for selectedQuestion.
     * @see #getSelectedQuestion(Integer userID) Read the value of selectedQuestion.
     */
    public void setSelectedQuestion(Integer userID, int selectedQuestion) throws Exception {
        setCurrentConfiguration(userID);
        try {
            currentConfiguration.setProperty(Config.Keys.SELECTED_QUESTION, selectedQuestion);
        } catch (NullPointerException e) {
            currentConfiguration.addProperty(Config.Keys.SELECTED_QUESTION, selectedQuestion);
        }

        currentBuilder.save();
    }

    /**
     * Reads selectedQuestion value from the configuration file of a user.
     * @param userID User of who the value is read.
     * @return The value of selectedQuestion.
     * @see #setSelectedQuestion(Integer userID, int selectedQuestion) Save the value of selectedQuestion.
     */
    public int getSelectedQuestion(Integer userID) throws Exception {
        setCurrentConfiguration(userID);
        return currentConfiguration.getInt(Config.Keys.SELECTED_QUESTION);
    }

    /**
     * Saves currentPictureTitle in the configuration file of a user.
     * @param userID User for who the value is saved.
     * @param title The title that is to be saved.
     * @see #getCurrentPictureTitle(Integer userID) Read the currentPictureTitle.
     */
    public void setCurrentPictureTitle(Integer userID, String title) throws Exception {
        if (Files.exists(this.getDatabaseDisplayFilePath(title))) {
            throw new FileAlreadyExistsException("There already is one file with this name.");
        }

        setCurrentConfiguration(userID);
        try {
            currentConfiguration.setProperty(Config.Keys.CURRENT_PICTURE_TITLE, title);
        } catch (NullPointerException e) {
            currentConfiguration.addProperty(Config.Keys.CURRENT_PICTURE_TITLE, title);
        }

        currentBuilder.save();
    }

    /**
     * Reads currentPictureTile from the configuration file of a user.
     * @param userID User of who the value is read.
     * @return The currentPictureTitle for the specified user.
     * @see #setCurrentPictureTitle(Integer userID, String title) Save the currentPicture title.
     */
    public String getCurrentPictureTitle(Integer userID) throws Exception {
        setCurrentConfiguration(userID);
        return  currentConfiguration.getString(Config.Keys.CURRENT_PICTURE_TITLE);
    }

    /**
     * Saves currentPictureDescription in the configuration file of a user.
     * @param userId User for who the value is saved.
     * @param description The description to be changed.
     * @see #getCurrentPictureDescription(Integer userId) Read the currentPictureDescription.
     */
    public void setCurrentPictureDescription(Integer userId, String description) throws Exception {
        setCurrentConfiguration(userId);
        try {
            currentConfiguration.setProperty(Config.Keys.CURRENT_PICTURE_DESCRIPTION, description);
        } catch (NullPointerException e) {
            currentConfiguration.addProperty(Config.Keys.CURRENT_PICTURE_DESCRIPTION, description);
        }

        currentBuilder.save();
    }

    /**
     * Reads currentPictureDescription from the configuration file of a user.
     * @param userId User of who the value is read.
     * @return The currentPictureDescription for the specified user.
     * @see #setCurrentPictureDescription(Integer userId, String description) Save the currentPictureDescription.
     */
    public String getCurrentPictureDescription(Integer userId) throws Exception {
        setCurrentConfiguration(userId);
        return currentConfiguration.getString(Config.Keys.CURRENT_PICTURE_DESCRIPTION);
    }

    /**
     * Saves currentPictureDuration in the configuration file of a user.
     * @param userId User for who the value is saved.
     * @param duration The new value for the duration.
     * @see #getCurrentPictureDuration(Integer userId) Read the currentPictureDuration.
     */
    public void setCurrentPictureDuration(Integer userId, int duration) throws Exception {
        setCurrentConfiguration(userId);
        try {
            currentConfiguration.setProperty(Config.Keys.CURRENT_PICTURE_DURATION, duration);
        } catch (NullPointerException e) {
            currentConfiguration.addProperty(Config.Keys.CURRENT_PICTURE_DURATION, duration);
        }

        currentBuilder.save();
    }

    /**
     * Reads currentPictureDuration from the configuration file of a user.
     * @param userId User of who the value is read.
     * @return The currentPictureDuration for the specified user.
     * @see #setCurrentPictureDuration(Integer userId, int duration) Save the currentPictureDuration.
     */
    public int getCurrentPictureDuration(Integer userId) throws Exception {
        setCurrentConfiguration(userId);
        return currentConfiguration.getInt(Config.Keys.CURRENT_PICTURE_DURATION, 0);
    }

    /**
     * Saves currentVideoTitle in the configuration file of a user.
     * @param userID User for who the value is saved.
     * @param title The title that is to be saved.
     * @see #getCurrentVideoTitle(Integer userID) Read the currentPictureTitle.
     */
    public void setCurrentVideoTitle(Integer userID, String title) throws Exception {
        if (Files.exists(this.getDatabaseDisplayFilePath(title))) {
            throw new FileAlreadyExistsException("There already is one file with this name.");
        }

        setCurrentConfiguration(userID);
        try {
            currentConfiguration.setProperty(Config.Keys.CURRENT_VIDEO_TITLE, title);
        } catch (NullPointerException e) {
            currentConfiguration.addProperty(Config.Keys.CURRENT_VIDEO_TITLE, title);
        }

        currentBuilder.save();
    }

    /**
     * Reads currentVideoTitle from the configuration file of a user.
     * @param userID User of who the value is read.
     * @return The currentPictureTitle for the specified user.
     * @see #setCurrentVideoTitle(Integer userID, String title) Save the currentVideo title.
     */
    public String getCurrentVideoTitle(Integer userID) throws Exception {
        setCurrentConfiguration(userID);
        return  currentConfiguration.getString(Config.Keys.CURRENT_VIDEO_TITLE);
    }

    /**
     * Saves currentVideoDescription in the configuration file of a user.
     * @param userId User for who the value is saved.
     * @param description The description to be changed.
     * @see #getCurrentVideoDescription(Integer userId) Read the currentVideoDescription.
     */
    public void setCurrentVideoDescription(Integer userId, String description) throws Exception {
        setCurrentConfiguration(userId);
        try {
            currentConfiguration.setProperty(Config.Keys.CURRENT_VIDEO_DESCRIPTION, description);
        } catch (NullPointerException e) {
            currentConfiguration.addProperty(Config.Keys.CURRENT_VIDEO_DESCRIPTION, description);
        }

        currentBuilder.save();
    }

    /**
     * Reads currentVideoDescription from the configuration file of a user.
     * @param userId User of who the value is read.
     * @return The currentVideoDescription for the specified user.
     * @see #setCurrentVideoDescription(Integer userId, String description) Save the currentVideoDescription.
     */
    public String getCurrentVideoDescription(Integer userId) throws Exception {
        setCurrentConfiguration(userId);
        return currentConfiguration.getString(Config.Keys.CURRENT_VIDEO_DESCRIPTION);
    }

    /**
     * Download the file to display and add it together with its properties to the configuration files.
     * @param absSender
     * @param userId
     * @param fileId
     * @param type
     */
    public void createNewDisplayFile(AbsSender absSender, Integer userId, String fileId, String type) throws Exception {
        if (type.equals(Config.Bot.DISPLAY_FILE_TYPE_IMAGE)) {
            String pictureTitle = this.getCurrentPictureTitle(userId);

            GetFile getFileRequest = new GetFile();
            getFileRequest.setFileId(fileId);

            File file = absSender.getFile(getFileRequest);
            URL fileUrl = new URL(
                    "https://api.telegram.org/file/bot" +
                            DatabaseManager.getInstance().getBotToken() + "/" + file.getFilePath());
            Path image = FileSystems.getDefault().getPath(Config.Paths.DISPLAY_FILES + "/" + pictureTitle);

            FileUtils.copyURLToFile(fileUrl, image.toFile());

            this.createConfigurationFile(this.getDatabaseDisplayFilePath(pictureTitle));
            this.setDisplayFileType(pictureTitle, Config.Bot.DISPLAY_FILE_TYPE_IMAGE);
            this.setDisplayFileDescription(pictureTitle, this.getCurrentPictureDescription(userId));
            this.setDisplayFileDuration(pictureTitle, this.getCurrentPictureDuration(userId));
            this.addDisplayFile(pictureTitle);
        } else if (type.equals(Config.Bot.DISPLAY_FILE_TYPE_VIDEO)) {
            String videoTitle = this.getCurrentVideoTitle(userId);

            GetFile getFileRequest = new GetFile();
            getFileRequest.setFileId(fileId);

            File file = absSender.getFile(getFileRequest);
            URL fileUrl = new URL(
                    "https://api.telegram.org/file/bot" + DatabaseManager.getInstance().getBotToken() + "/" +
                            file.getFilePath());
            Path image = FileSystems.getDefault().getPath(Paths.DISPLAY_FILES + "/" + videoTitle);

            FileUtils.copyURLToFile(fileUrl, image.toFile());

            this.createConfigurationFile(this.getDatabaseDisplayFilePath(videoTitle));
            this.setDisplayFileType(videoTitle, Bot.DISPLAY_FILE_TYPE_VIDEO);
            this.setDisplayFileDescription(videoTitle, this.getCurrentVideoDescription(userId));
            this.addDisplayFile(videoTitle);
        } else {
            throw new IllegalArgumentException("No known type.");
        }
    }

    /**
     * Reads the name for the bot from the configuration file
     * @return the username for the bot
     * @throws Exception
     */
    public String getBotUsername() throws Exception {
        setCurrentConfiguration(Config.Paths.BOT_CONFIG_FILE);
        return currentConfiguration.getString(Config.Keys.BOT_USERNAME_KEY);
    }

    /**
     * Reads the token of the bot from the configuration file
     * @return the token for the bot
     * @throws Exception
     */
    public String getBotToken() throws Exception {
        setCurrentConfiguration(Config.Paths.BOT_CONFIG_FILE);
        return currentConfiguration.getString(Config.Keys.BOT_TOKEN_KEY);
    }

    /**
     * Reads the admin's user id from the configuration file
     * @return the user id of the bot's admin
     * @throws Exception
     */
    public Integer getAdminUserId() throws Exception {
        setCurrentConfiguration(Config.Paths.BOT_CONFIG_FILE);
        return currentConfiguration.getInteger(Config.Keys.BOT_ADMIN_USER_ID_KEY, 0);
    }

    /**
     * Reads the admin's chat id from the configuration file
     * @return the chat id of the chat between bot and admin
     * @throws Exception
     */
    public Integer getAdminChatId() throws Exception {
        setCurrentConfiguration(Config.Paths.BOT_CONFIG_FILE);
        return currentConfiguration.getInteger(Config.Keys.BOT_ADMIN_CHAT_ID_KEY, 0);
    }
}
