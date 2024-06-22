package ru.theft.obratka.core;

import com.vdurmont.emoji.EmojiParser;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import ru.theft.obratka.util.constant.Emoji;
import ru.theft.obratka.util.constant.UserState;

import java.util.ArrayList;
import java.util.List;

import static ru.theft.obratka.util.constant.ConstantMessage.*;

@Service
public class MenuService {

    public SendMessage createFirstMenu(Long chatId, String firstName) {
        InlineKeyboardMarkup keyboard;
        var startButton = createInlineKeyboardButton("Пройти регистрацию "
                + EmojiParser.parseToUnicode(Emoji.PAGE_FACING_UP_EMOJI), "reg");
        keyboard = InlineKeyboardMarkup.builder()
                .keyboardRow(List.of(startButton))
                .build();

        SendMessage message = new SendMessage();
        message.enableHtml(true);
        message.enableMarkdownV2(true);
        message.enableMarkdown(true);
        message.setChatId(chatId);
        message.setText("Добро пожаловать, " + firstName + " " + EmojiParser.parseToUnicode(Emoji.WAVE_EMOJI) + "\n\n"
                + "Перед использованием бота необходимо заполнить данные по водителю и машине. " +
                "Это первичная процедура, в дальнейшем аутентификация будет работать автоматически.");
        message.setReplyMarkup(keyboard);

        return message;
    }

    public SendMessage createAuthMenu(Long chatId, String tgUserName, int state, UserState userState) {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        SendMessage message = new SendMessage();
        message.setReplyMarkup(replyKeyboardMarkup);
        replyKeyboardMarkup.setSelective(false);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);

        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow keyboardFirstRow = new KeyboardRow();
        keyboardFirstRow.add(new KeyboardButton(EmojiParser
                .parseToUnicode(Emoji.TRUCK_EMOJI) + " Добавить авто"));

        KeyboardRow keyboardSecondRow = new KeyboardRow();
        keyboardSecondRow.add(new KeyboardButton(EmojiParser
                .parseToUnicode(Emoji.BUST_IN_SILHOUETTE_EMOJI) + " Мой профиль"));

        KeyboardRow keyboardThirdRow = new KeyboardRow();
        keyboardThirdRow.add(new KeyboardButton(EmojiParser
                .parseToUnicode(Emoji.MOTOR_WAY) + " Поделиться маршрутом"));


        keyboard.add(keyboardFirstRow);
        keyboard.add(keyboardSecondRow);
        keyboard.add(keyboardThirdRow);
        replyKeyboardMarkup.setKeyboard(keyboard);

        switch (state) {
            case 0 -> {
                message.setText(UNKNOWN_MESSAGE);
            }
            case 1 -> {
                userState.setDriverAuthenticated(true);
                userState.setRegisterProcessState(false);
                message.setText(IS_REGISTERED);
            }
            case 2 -> {
                userState.setDriverAuthenticated(true);
                userState.setRegisterProcessState(false);
                message.setText(EmojiParser.parseToUnicode(Emoji.WHITE_CHECK_MARK_EMOJI)
                        + " " + tgUserName + SUCCESSFUL_REGISTER);
            }
            case 3 -> {
                userState.setPatchProcessState(false);
                message.setText(EmojiParser.parseToUnicode(Emoji.WHITE_CHECK_MARK_EMOJI)
                        + " " + tgUserName + SUCCESSFUL_PATCH);
            }
            case 4 -> {
                userState.setPatchProcessState(false);
                message.setText(STOP_PATCH);
            }
            case 5 -> {
                userState.setArrivalProcessState(false);
                message.setText(SHARE_ARRIVAL);
            }
            case 6 -> {
                userState.setArrivalProcessState(false);
                message.setText(STOP_ARRIVAL);
            }
            default -> {
                message.setText("Произошла ошибка при статусе");
            }
        }
        message.enableHtml(true);
        message.enableMarkdownV2(true);
        message.enableMarkdown(true);
        message.setChatId(chatId);
        return message;
    }

    protected InlineKeyboardButton createInlineKeyboardButton(String text, String callbackData) {
        return InlineKeyboardButton.builder()
                .text(text)
                .callbackData(callbackData)
                .build();
    }
}
