package org.telegram.bot.commands.deleteMediaCommand;

import Config.CallbackData;
import org.telegram.bot.api.SendMessages;
import org.telegram.bot.commands.SendOnErrorOccurred;
import org.telegram.bot.database.DatabaseException;
import org.telegram.bot.database.DatabaseManager;
import org.telegram.bot.messages.SituationalContentMessage;
import org.telegram.bot.messages.SituationalInlineKeyboard;
import org.telegram.telegrambots.api.objects.Chat;
import org.telegram.telegrambots.api.objects.User;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.bots.AbsSender;
import org.telegram.telegrambots.bots.commands.BotCommand;
import org.telegram.telegrambots.logging.BotLogger;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static org.telegram.bot.commands.deleteMediaCommand.UtilsKt.*;
import static org.telegram.bot.utils.KeyboardConverterKt.convertDeleteMediaKeyboard;

/**
 * @author liketechnik
 * @version 1.0
 * @date 17 of Mai 2017
 */
public class DeleteMediaCommand extends BotCommand {

    public static final String LOGTAG = "DELETEMEDIACOMMAND";
    private final String packageName;
    private final String commandName;
//    final String currentIndexRegex = "/current_index>";
//    final String lastIndexRegex = "/last_index>";
//    final String nextIndexRegex = "/next_index>";

    private enum SITUATIONS {
        information, keyboard
    }

    public DeleteMediaCommand() {
        super("delete_media", "Delete a media file you uploaded to the board.");
        packageName = this.getClass().getPackage().getName().replace("org.telegram.bot.commands.", "");
        commandName = this.getCommandIdentifier() + "_command";
    }

    public void execute(AbsSender absSender, User user, Chat chat, String[] arguments) {
        try {
            DatabaseManager databaseManager = DatabaseManager.getInstance();

            SituationalContentMessage message = new SituationalContentMessage(commandName);
            String messageText;

            // send info message
            if (arguments.length < 2) {
                message.setMessageName(packageName, commandName, SITUATIONS.information.name());

                messageText = message.getContent(user.getId(), false);
                SendMessages.getInstance().addMessage(message.calculateHash(), messageText, chat.getId().toString(), absSender, Optional.empty(), Optional.empty());
            } else {
                SendMessages.getInstance().addDeleteMessage(chat.getId().toString(), Integer.valueOf(arguments[1]), absSender);
            }


            // set indexes
            List<String> displayFiles = DatabaseManager.getInstance().getAdminUserId().equals(user.getId()) ?
                    DatabaseManager.getInstance().getDisplayFiles() : DatabaseManager.getInstance().getDisplayFiles(user.getId());

            int currentIndex;
            String[] argumentsSplit;
            try {
                argumentsSplit = arguments[0].split("_");
                currentIndex = argumentsSplit[argumentsSplit.length - 1] != null ? Integer.valueOf(argumentsSplit[2]) : 0; //check last index
            } catch (NumberFormatException e) {
                if (arguments[0].equals(CallbackData.DELETE_MEDIA.delete_media_first.name())) {
                    currentIndex = 0;
                } else if (arguments[0].equals(CallbackData.DELETE_MEDIA.delete_media_last.name())) {
                    currentIndex = displayFiles.size() - 1;
                } else if (arguments[0].equals(CallbackData.DELETE_MEDIA.delete_media_middle.name())) {
                    currentIndex = displayFiles.size() / 2;
                } else {
                    currentIndex = 0;
                }
            } catch (NullPointerException | ArrayIndexOutOfBoundsException e) {
                currentIndex = 0;
            }

            int lastIndex = currentIndex - 1 < 0 ? displayFiles.size() - 1 : currentIndex - 1; // check that we don't get negative indexes
            int nextIndex = currentIndex + 1 >= displayFiles.size() ? 0 : currentIndex + 1; // check that we don't exceed index range, e. g. if we have 20 pictures biggest index may be 19

            // send choose message with keyboard
            SituationalInlineKeyboard inlineKeyboard = new SituationalInlineKeyboard(commandName, SITUATIONS.keyboard.name());
            inlineKeyboard.setMessageName(packageName, commandName, SITUATIONS.keyboard.name());

            //add the index to the callbacks where it is needed
            List<List<InlineKeyboardButton>> keyboardList = inlineKeyboard.getKeyboard(user.getId());
            HashMap<String, Integer> regexReplacementMap = new HashMap<>();
            regexReplacementMap.put(getCurrentIndexRegex(), currentIndex);
            regexReplacementMap.put(getLastIndexRegex(), lastIndex);
            regexReplacementMap.put(getNextIndexRegex(), nextIndex);
            keyboardList = convertDeleteMediaKeyboard(regexReplacementMap, keyboardList);

            InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
            keyboard.setKeyboard(keyboardList);


            message.setMessageName(packageName, commandName, SITUATIONS.keyboard.name());

            message.setAdditionalContent(getAdditionalContent(displayFiles.get(currentIndex)));
            messageText = message.getContent(user.getId(), false);

            String fileId = databaseManager.getDisplayFileId(displayFiles.get(currentIndex));

            String displayFileTgType = databaseManager.getDisplayFileTgType(displayFiles.get(currentIndex));
            sendMediaMessage(displayFileTgType, message.calculateHash(), messageText, chat.getId().toString(), absSender, fileId, Optional.of(keyboard));
        } catch (InterruptedException | DatabaseException e) {
            BotLogger.error(LOGTAG, e);

            new SendOnErrorOccurred().execute(absSender, user, chat, new String[]{LOGTAG});

            return;
        }

    }
}
