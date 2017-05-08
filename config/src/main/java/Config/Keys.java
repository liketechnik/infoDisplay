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

/**
 * @author liketechnik
 * @version 1.0
 * @date 11 of Januar 2017
 */
public final class Keys {
    public static final String DISPLAY_FILES_KEY = "displayFiles";
    public static final String DISPLAY_FILE_DURATION_KEY = "displayDuration";
    public static final String DISPLAY_FILE_TYPE_KEY = "type";
    public static final String DISPLAY_FILE_DESCRIPTION = "description";

    public static final String USER_ACTIVE = "userActive";
    public static final String USER_REGISTERED = "userRegistered";
    public static final String USER_WANTS_REGISTRATION = "userWantsregistration";
    public static final String USER_COMMAND_STATE = "userState";
    public static final String USER_LANGUAGE = "userLanguage";

    public static final String DEFAULT_LANGUAGE = "defaultLanguage";

    public final static String QUESTION = "question";

    public static final String CHAT_ID = "chatID";

    public static final String SELECTED_QUESTION = "selectedQuestion";

    public static final String CURRENT_PICTURE_TITLE = "currentPictureTitle";
    public static final String CURRENT_PICTURE_DESCRIPTION = "currentPictureDescription";
    public static final String CURRENT_PICTURE_DURATION = "currentPictureDuration";

    public static final String CURRENT_VIDEO_TITLE = "currentVideoTitle";
    public static final String CURRENT_VIDEO_DESCRIPTION = "currentVideoDescription";

    public static final String BOT_USERNAME_KEY = "botUsername";
    public static final String BOT_TOKEN_KEY = "botToken";

    public static final String BOT_ADMIN_USER_ID_KEY = "botAdminUserId";
    public static final String BOT_ADMIN_CHAT_ID_KEY = "botAdminChatId";
}
