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

/**
 * @author Florian Warzecha
 * @version 1.0.1
 * @date 24 of September of 2016
 *
 * This class represents a file that gets displayed on the virtual info display.
 */
public class DisplayFile {

    public static final int TYPE_IMAGE = 0;
    public static final int TYPE_VIDEO = 1;

    private int displayDuration;
    private String type;
    private String fileName;

    /**
     * Create a new display file.
     * @param displayDuration Hwo long the picture should be displayed.
     * @param type The type of the file (e. g. image or video). Use the DISPLAY_FILE_TYPE strings from
     * {@link Config.Bot}.
     * @param fileName The path to the file. This should NOT be a relative path.
     * @throws IllegalArgumentException The type passed in is invalid.
     */
    public DisplayFile(int displayDuration, String type, String fileName) throws IllegalArgumentException {
        if (!type.equals(Config.Bot.DISPLAY_FILE_TYPE_IMAGE)) {
            throw new IllegalArgumentException("No known type: " + type);
        }

        this.displayDuration = displayDuration;
        this.type = type;
        this.fileName = fileName;
    }

    public int getDisplayDuration() {
        return displayDuration;
    }

    public void setDisplayDuration(int displayDuration) {
        this.displayDuration = displayDuration;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) throws IllegalArgumentException {
        if (type.equals(Config.Bot.DISPLAY_FILE_TYPE_IMAGE)) {
            throw new IllegalArgumentException("No known type: " + type);
        }
        this.type = type;
    }

    public String getFileName() {
        return fileName;
    }

    public void setName(String fileName) {
        this.fileName = fileName;
    }
}
