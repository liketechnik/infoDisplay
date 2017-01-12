/*
 * Copyright (C) 2017  Florian Warzecha <flowa2000@gmail.com>
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

/**
 * @author liketechnik
 * @version 1.0
 * @date 11 of Januar 2017
 */
public final class Bot {

    public static final String DISPLAY_FILE_TYPE_IMAGE = "image";
    public static final String HAS_PHOTO = "hasPhoto";
    public static final String HAS_NO_PHOTO = "hasNoPhoto";

    public final static String ASK_COMMAND_WRITE_QUESTION = "askCommand";
    public final static String NO_COMMAND = "none";
    public final static String ANSWER_COMMAND_CHOOSE_NUMBER = "answerCommandChooseNumber";
    public final static String ANSWER_COMMAND_WRITE_ANSWER = "answerCommandWriteAnswer";
    public final static String PIN_PICTURE_COMMAND_SEND_DESCRIPTION =
            "pinPictureCommandSendDescription";
    public static final String PIN_PICTURE_COMMAND_SEND_TITLE =
            "pinPictureCommandSendTitle";
    public final static String PIN_PICTURE_COMMAND_SEND_PICTURE = "pinPictureCommandSendPicture";
    public static final String PIN_PICTURE_COMMAND_SEND_DURATION = "pinPictureCommandSendDuration";
}
