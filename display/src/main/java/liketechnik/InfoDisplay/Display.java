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

package liketechnik.InfoDisplay;


import Config.Bot;
import Config.Keys;
import ch.qos.logback.classic.Level;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.slf4j.LoggerFactory;
import uk.co.caprica.vlcj.component.EmbeddedMediaPlayerComponent;
import uk.co.caprica.vlcj.discovery.NativeDiscovery;
import uk.co.caprica.vlcj.player.embedded.DefaultAdaptiveRuntimeFullScreenStrategy;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import DisplayFile.DisplayFile;

/**
 * This class represents the display file viewer.
 * @author Florian Warzecha
 * @version 1.0.1
 * @date 24 of September of 2016
 */
public class Display {

    private final JFrame frame;
    private final EmbeddedMediaPlayerComponent mediaPlayerComponent;

    private final static ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(
            Display.class);

    public static void main (String args[]) {
        logger.setLevel(Level.DEBUG);

        logger.info("Starting initialization process...");

        // Check if app directory exists
        if (Files.notExists(Config.Paths.APP_HOME)) {
            logger.info("App directory does not exist yet, creating directory '" + Config.Paths.APP_HOME);

            try {
                Files.createDirectory(Config.Paths.APP_HOME);
            } catch (IOException e) {
                logger.error("Creating of application's home directory failed. The error was: {}", e);
                System.exit(2);
            }
        }

        // Can we find the native vlc library?
        logger.debug("Searching for native vlc library.");
        if (!new NativeDiscovery().discover()) {
            logger.error("No native vlc library found. Terminating.");
            System.exit(1);
        }
        else {
            logger.debug("Found native vlc library.");
        }

        // Start window thread
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new Display();
            }
        });
    }

    private Display() {
        logger.setLevel(Level.DEBUG);

        // Create new JFrame
        logger.info("Initializing JFrame.");

        logger.debug("Creating new JFrame with title {}", Config.Window.windowTitle);
        frame = new JFrame(Config.Window.windowTitle);

        logger.trace("Setting window bounds.");
        frame.setBounds(Config.Window.posX, Config.Window.posY, Config.Window.width, Config.Window.height);

        logger.trace("Setting default close operation.");
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        // Add a window listener for the closing operation
        logger.debug("Adding window listener");
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                logger.info("User closed window. Exiting now...");
                logger.trace("Freeing media player component resources.");
                mediaPlayerComponent.release();
                logger.trace("Calling System.exit");
                System.exit(0);
            }
        });


        // create the media player component and make the window visible
        logger.debug("Creating media player component.");
        mediaPlayerComponent = new EmbeddedMediaPlayerComponent() {
            @Override
            public void keyPressed(KeyEvent event) {
                if (event.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    mediaPlayerComponent.getMediaPlayer().toggleFullScreen();
                    logger.info("Toggled fullscreen mode to: {}", mediaPlayerComponent.getMediaPlayer().isFullScreen());
                }
            }
        };
        mediaPlayerComponent.getMediaPlayer().setFullScreenStrategy(new DefaultAdaptiveRuntimeFullScreenStrategy(frame));

        logger.debug("Adding media player component to window.");
        frame.setContentPane(mediaPlayerComponent);

        logger.info("Making media player window visible.");
        frame.setVisible(true);

        logger.info("Initialization finished.");
        logger.info("Starting to show files now...");

        new showFiles().start(mediaPlayerComponent, loadDisplayFiles());
    }

    static DisplayFile[] loadDisplayFiles() {
        Configuration displayFileConfig = null;
        Configuration config = null;
        String[] displayFilesToLoad;
        int processedFiles = 0;

        logger.setLevel(Level.INFO);

        logger.debug("(Re)loading display files.");

        // Check if the file containing the display file names exists
        if (Files.notExists(Config.Paths.DISPLAY_FILES_CONFIG_FILE)) {
            logger.error("Did not find config file for displayFiles.");
            System.exit(5);
        }

        // create builder to get all display files from the configuration file
        Parameters params = new Parameters();
        FileBasedConfigurationBuilder<FileBasedConfiguration> builder =
                new FileBasedConfigurationBuilder<FileBasedConfiguration>(PropertiesConfiguration.class)
                        .configure(params.properties()
                                .setPath(Config.Paths.DISPLAY_FILES_CONFIG_FILE.toString()));

        // get the configuration from the builder
        try {
            config = builder.getConfiguration();
        } catch (ConfigurationException e) {
            logger.error("Error while getting configuration for displayFiles. The error was: {}", e);
            System.exit(3);
        }

        // check if the configuration contains display file entries.
        if (!config.containsKey(Config.Keys.DISPLAY_FILES_KEY)) {
            logger.error("Config file does not contain any information about media.");
            System.exit(4);
        }

        // load the names of all display files
        displayFilesToLoad = config.getStringArray(Config.Keys.DISPLAY_FILES_KEY);

        DisplayFile[] displayFiles = new DisplayFile[displayFilesToLoad.length];

        for (String displayFile : displayFilesToLoad) {
            // Check if configuration file for the display file exists
            if (Files.notExists(FileSystems.getDefault().getPath(Config.Paths.DISPLAY_FILES + "/" + displayFile + ".conf"))) {
                logger.error("Did not find config file for displayFile {}.conf in directory {}.", displayFile,
                        Config.Paths.DISPLAY_FILES.toString());
                continue;
            }

            // create builder to get all properties for the display file
            FileBasedConfigurationBuilder<FileBasedConfiguration> displayFileBuilder  =
                    new FileBasedConfigurationBuilder<FileBasedConfiguration>(PropertiesConfiguration.class)
                            .configure(params.properties()
                                    .setPath(Config.Paths.DISPLAY_FILES.toString() + "/" + displayFile + ".conf"));

            // get the configuration from the builder
            try {
                displayFileConfig = displayFileBuilder.getConfiguration();
            } catch (ConfigurationException e) {
                logger.error("Error while getting configuration for displayFile {}. The error was: {}", displayFile, e);
                continue;
            }

            // check if all necessary information is present in the configuration for the display file
//            if (!displayFileConfig.containsKey(Config.Keys.DISPLAY_FILE_DURATION_KEY) ||
//                    !displayFileConfig.containsKey(Config.Keys.DISPLAY_FILE_TYPE_KEY)) {
//                logger.error("Config file for displayFile {} seems corrupted.", displayFile);
//                continue;
//            }

            if (!displayFileConfig.containsKey(Config.Keys.DISPLAY_FILE_TYPE_KEY)) {
                logger.error("Config file for displayFile {} seems corrupted.", displayFile);
                continue;
            }

            // create a new displayFile and add it to the array containing all display files
            if (displayFileConfig.getString(Keys.DISPLAY_FILE_TYPE_KEY).equals(Bot.DISPLAY_FILE_TYPE_IMAGE)) {
                displayFiles[processedFiles] = new DisplayFile(
                        displayFileConfig.getInt(Config.Keys.DISPLAY_FILE_DURATION_KEY),
                        displayFileConfig.getString(Config.Keys.DISPLAY_FILE_TYPE_KEY),
                        Config.Paths.DISPLAY_FILES.toString() + "/" + displayFile);
            } else if (displayFileConfig.getString(Keys.DISPLAY_FILE_TYPE_KEY).equals(Bot.DISPLAY_FILE_TYPE_VIDEO)) {
                displayFiles[processedFiles] = new DisplayFile(
                        displayFileConfig.getString(Config.Keys.DISPLAY_FILE_TYPE_KEY),
                        Config.Paths.DISPLAY_FILES.toString() + "/" + displayFile);
            }

            processedFiles++;
        }

        return displayFiles;
    }
}

/**
 * This {@code Thread} iterates over the display files and shows them in the window.
 */
class showFiles extends Thread {

    EmbeddedMediaPlayerComponent mediaPlayerComponent;
    final static ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(
            showFiles.class);

    DisplayFile[] displayFiles;

    public void start(EmbeddedMediaPlayerComponent mediaPlayerComponent, DisplayFile[] displayFiles) {
        this.mediaPlayerComponent  = mediaPlayerComponent;
        this.displayFiles = displayFiles;

        logger.setLevel(Level.DEBUG);

        super.start();
    }

    /**
     * Display all display files, reload them and start again...
     */
    public void run() {
        while (true) {
            for (DisplayFile displayFile : this.displayFiles ) {
                this.logger.debug("Adding media to window.");

                String location = displayFile.getFileName();

                if (displayFile.getType().equals(Bot.DISPLAY_FILE_TYPE_IMAGE)) {
                    this.mediaPlayerComponent.getMediaPlayer().playMedia(location, ":image-duration=-1");

                    while (this.mediaPlayerComponent.getMediaPlayer().getTime() < displayFile.getDisplayDuration() * 1000) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            this.logger.error("Encountered interruption during sleep time of the thread. " +
                                    "Exception was: {}", e);
                        }
                    }
                } else if (displayFile.getType().equals(Bot.DISPLAY_FILE_TYPE_VIDEO)) {
                    this.mediaPlayerComponent.getMediaPlayer().playMedia(location);

                    while (this.mediaPlayerComponent.getMediaPlayer().getPosition() >= 0) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            this.logger.error("Encountered interruption during sleep time of the thread. " +
                                    "Exception was: {}", e);
                        }
                        logger.debug(String.valueOf(this.mediaPlayerComponent.getMediaPlayer().getPosition()));
                    }
                }
            }

            // reload the display files
            this.displayFiles = Display.loadDisplayFiles();
        }
    }
}
