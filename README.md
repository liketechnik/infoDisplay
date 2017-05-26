[![Stories in Ready](https://badge.waffle.io/liketechnik/infoDisplay.png?label=ready&title=Ready)](http://waffle.io/liketechnik/infoDisplay)
stable: [![Build Status](https://travis-ci.org/liketechnik/infoDisplay.svg?branch=stable)](https://travis-ci.org/liketechnik/infoDisplay)
[![CodeFactor](https://www.codefactor.io/repository/github/liketechnik/infodisplay/badge)](https://www.codefactor.io/repository/github/liketechnik/infodisplay)
development: [![Build Status](https://travis-ci.org/liketechnik/infoDisplay.svg?branch=development)](https://travis-ci.org/liketechnik/infoDisplay)
[![CodeFactor](https://www.codefactor.io/repository/github/liketechnik/infodisplay/badge/development)](https://www.codefactor.io/repository/github/liketechnik/infodisplay/overview/development)

## README ##

This is a telegram bot based on Ruben Bermudez Telegram Bots API. 
The purpose of this program is to have a virtual display which displays
information (pictures or videos) at, for example, schools.
I decided to develop it, because everyone in charge of it should be able
to upload things to it from home too with a nice looking GUI and 
without the need to set up a server on ones own.

[Changelog (latest release)](https://github.com/liketechnik/infoDisplay/blob/stable/CHANGELOG.md)

[Changelog (development)](https://github.com/liketechnik/infoDisplay/blob/development/CHANGELOG.md)

## Use ##

### Prerequisites ###

Create a bot inside Telegram with @BotFather. Remember the bot's name and token. 
Then create the file bot.conf in ```<your_home_directory>/.infoDisplay```. Edit the file 
and add the following lines:
```botUsername = <bot's name>```
```botToken = <bot's token>```
```defaultLanguage = <language you prefer for users of the bot (you can choose between any of the supported languages)* >```

\* Currently supported languages are 'english' or 'german'.

### Build ###

Execute gradle task ":zipDist" (for zip archive) or ":tarDist" (for tar archive) for the 
projects bot and display. Copy the archives from ```build/distributions/<name>-<version>.zip``` to
anywhere you want them. Then follow the instructions under execution.

### Execution ###
#### Bot ####
Download or build the zip / tar archive for the bot, extract it and run the bot executable for your OS.
#### Display ####
Download or build the zip / tar archive for the display, extract it and run the bot executable for your OS.

### Administration ###
#### In Telegram ####
If you do not know your Telegram userID yet, go to Telegram and send the command /id to the
 bot. Remember your userID, go to directory `<your_home_directory>/.infoDisplay/` and open the file
 ```bot.conf```. Add the lines:
  ```botAdminUserId = <your_user_id>```
  ```botAdminChatId = <your_chat_id_with_bot>```
  to it, restart the bot. Now you can administrate the bot via 
  Telegram. 
#### Allow new users to use the bot ####
If any new user of the bot wants to upload new media to the board, he/she needs to be registered. If
  a user wants to register him, he can do that via a command that sends a message with his/her userId to the
  administrator (see above). To complete the registration the administrator needs to edit the file 
  ```<your_home_directory/.infoDisplay/bot_database/users/<user_id>``` and set ```userRegistered``` to ```true```.

## Contribute ##

Fork the project and / or create pull requests. The project is build and run with gradle
tasks.

## License ##

 Copyright (C) 2016-2017  Florian Warzecha <flowa2000@gmail.com>
 
This file is part of infoDisplay.

infoDisplay is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

infoDisplay is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.

infoDisplay uses TelegramBots Java API <https://github.com/rubenlagus/TelegramBots> by Ruben Bermudez.
TelegramBots API is licensed under MIT License <https://opensource.org/licenses/mit-license.php>.

infoDisplay uses parts of the Apache Commons project <https://commons.apache.org/>.
Apache commons is licensed under the Apache License Version 2.0 <http://www.apache.org/licenses/>.

infoDisplay uses vlcj library <http://capricasoftware.co.uk/#/projects/vlcj>.
vlcj is licensed under GNU General Public License version 3 <https://www.gnu.org/licenses/gpl-3.0.de.html>.

## Thanks ##

Thanks to all the people who contributed to the projects that make this
program possible.

