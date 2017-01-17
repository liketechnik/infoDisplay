[![Stories in Ready](https://badge.waffle.io/liketechnik/infoDisplay.png?label=ready&title=Ready)](http://waffle.io/liketechnik/infoDisplay)
## README ##

This is a telegram bot based on Ruben Bermudez Telegram Bots API. 
The purpose of this program is to have a virtual display which displays
information (pictures or videos) at, for example, schools.
The program is needed, because everyone in charge of it, should be able
to upload things to it from home too with a nice looking GUI and 
without the need to set up a server on ones own.

## Use ##

### Prerequisites ###

Create a bot inside Telegram with @BotFather. Remember the bot's name and token.

### Build and Execution ###

Download the source code and edit Config.java (fill in the missing Strings with your corresponding text). Then execute the gradle tasks createBotJarArchive
and createDisplayJarArchive. The output .jar-Archives represent the 
two parts of the program: The bot for receiving the media, and the display
which display the information. You can find the output in the org.build/libs
directory.

## Contribute ##

Please contact me if you want to contribute to this project.

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

