package ru.theft.obratka.core;

import com.vdurmont.emoji.EmojiParser;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.theft.obratka.driver.model.Driver;
import ru.theft.obratka.driver.service.DriverService;
import ru.theft.obratka.util.constant.Emoji;
import ru.theft.obratka.util.constant.UserState;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static ru.theft.obratka.util.constant.ConstantMessage.*;

@Service
@AllArgsConstructor
public class BotService {

    private final List<String> driverFields = new ArrayList<>();
    private DriverService driverService;

    protected InlineKeyboardButton createInlineKeyboardButton(String text, String callbackData) {
        return InlineKeyboardButton.builder()
                .text(text)
                .callbackData(callbackData)
                .build();
    }

    protected SendMessage createInlineKeyboardMarkup(String text, long chatId, List<InlineKeyboardButton> buttons) {
        InlineKeyboardMarkup keyboardMarkup = InlineKeyboardMarkup
                .builder()
                .keyboardRow(buttons)
                .build();
        SendMessage message = new SendMessage();
        message.enableHtml(true);
        message.enableMarkdownV2(true);
        message.enableMarkdown(true);
        message.setChatId(chatId);
        message.setText(text);
        message.setReplyMarkup(keyboardMarkup);
        return message;
    }

    protected SendMessage createSendMessage(Long chatId, String text) {
        SendMessage message = new SendMessage();
        message.enableHtml(true);
        message.enableMarkdownV2(true);
        message.enableMarkdown(true);
        message.setChatId(chatId);
        message.setText(text);
        return message;
    }

    protected SendMessage createReplyKeyboardMarkup(long chatId, String text) {
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
        message.enableHtml(true);
        message.enableMarkdownV2(true);
        message.enableMarkdown(true);
        message.setChatId(chatId);
        message.setText(text);
        return message;
    }

    protected void createDriverFromString(UserState state, String text, TelegramBotCore bot, long userId, long chatId) throws TelegramApiException {
        if (state.isUserSurnameRegistrationState()) {
            if (text.isEmpty()) {
                throw new RuntimeException("Фамилия не может быть пустой!");
            }
            if (text.length() > 80) {
                throw new RuntimeException("Фамилия может содержать не более 80 символов!");
            }

            driverFields.add(text);
            state.setUserSurnameRegistrationState(false);
            state.setUserNameRegistrationState(true);
            bot.execute(createSendMessage(userId, TEXT_REGISTER_NAME_DRIVER));
            return;
        }

        if (state.isUserNameRegistrationState()) {
            if (text.isEmpty()) {
                throw new RuntimeException("Имя не может быть пустым!");
            }
            if (text.length() > 80) {
                throw new RuntimeException("Имя может содержать не более 80 символов!");
            }
            driverFields.add(text);
            state.setUserNameRegistrationState(false);
            state.setUserPatronymicRegistrationState(true);
            bot.execute(createSendMessage(userId, TEXT_REGISTER_PATRONYMIC_DRIVER));
            return;
        }

        if (state.isUserPatronymicRegistrationState()) {
            if (text.length() > 80) {
                throw new RuntimeException("Отчество может содержать не более 80 символов!");
            }
            driverFields.add(text);
            state.setUserPatronymicRegistrationState(false);
            state.setUserTelephoneNumberRegistrationState(true);
            bot.execute(createSendMessage(userId, TEXT_REGISTER_PHONE_DRIVER));
            return;
        }

        if (state.isUserTelephoneNumberRegistrationState()) {
            if (text.isEmpty()) {
                throw new RuntimeException("Номер телефона не может быть пустым!");
            }
            if (text.length() != 10) {
                throw new RuntimeException("Номер телефона должен содержать только 10 символов!");
            }
            if (driverService.getAll()
                    .stream()
                    .anyMatch(f -> f.getTelephone().equals("7" + text))) {
                throw new RuntimeException("Пользователь с таким номером телефона уже зарегестрирован! Укажите другой номер.");
            }

            driverFields.add("7" + text);
            state.setUserTelephoneNumberRegistrationState(false);
            driverService.add(
                    Driver.builder()
                            .tgId(String.valueOf(userId))
                            .fio(driverFields.get(0) + " " + driverFields.get(1) + " " + driverFields.get(2))
                            .telephone(driverFields.get(3))
                            .createdAt(LocalDateTime.now())
                            .build()
            );
            state.setUserAuthenticated(true);
            state.setUserRegistrationInProgress(false);
            bot.execute(createReplyKeyboardMarkup(chatId, SUCCESSFUL_REGISTER));
            clearDriverFields();
        }
    }

    protected SendMessage showProfile(Long tgId, Long chatId) {
        Driver driver = driverService.getByTgId(tgId.toString())
                .orElseThrow(() -> new RuntimeException("Такого водителя не существует!"));

        return createInlineKeyboardMarkup(
                driver.toString(),
                chatId,
                List.of(createInlineKeyboardButton("Изменить данные " + EmojiParser
                        .parseToUnicode(Emoji.WRITING_HAND_EMOJI), "edit")));
    }

    protected void clearDriverFields() {
        driverFields.clear();
    }
}
