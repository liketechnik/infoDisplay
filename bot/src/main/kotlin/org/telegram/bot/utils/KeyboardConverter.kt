package org.telegram.bot.utils

import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton

/**
 * @author liketechnik
 * @version 1.0
 * @date 21 of Mai 2017
 */

internal fun convertDeleteMediaKeyboard(regexReplacementMap: HashMap<String, Int>, keyboard: List<List<InlineKeyboardButton>>):
        List<List<InlineKeyboardButton>> {
    for (inlineKeyboardButtons in keyboard) {
        for (button in inlineKeyboardButtons) {
            for (regexp in regexReplacementMap.keys) {
                button.callbackData = button.callbackData.replace(regexp, regexReplacementMap[regexp].toString());
            }
        }
    }
    return keyboard;
}
