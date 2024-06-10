package ru.theft.obratka.core;

import com.vdurmont.emoji.EmojiParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.theft.obratka.driver.model.Driver;
import ru.theft.obratka.driver.model.TypeCarBody;
import ru.theft.obratka.driver.service.DriverService;

import java.util.ArrayList;
import java.util.List;

import static ru.theft.obratka.util.constant.ConstantMessage.*;
import static ru.theft.obratka.util.constant.Emoji.*;

@Component
@Slf4j
@RequiredArgsConstructor
public class TelegramBotCore extends TelegramLongPollingBot {

    @Value("${bot.name}")
    private String botName;
    @Value("${bot.token}")
    private String botToken;

    private boolean isDriverAuthenticated = false;
    private boolean isRegisterProcessState = false;
    private boolean isPatchProcessState = false;

    private final List<String> telegramIds = new ArrayList<>();
    private final DriverService driverService;

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            log.info("{} [{}]: {}", update.getMessage().getFrom().getUserName(),
                    update.getMessage().getFrom().getId(),
                    update.getMessage().getText());

            if (isNewUser(update.getMessage().getFrom().getId().toString())) {
                try {
                    showFirstMenu(update.getMessage().getChatId(), update.getMessage().getFrom().getFirstName());
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }

            } else {
                switch (update.getMessage().getText()) {
                    default -> {
                        if (isDriverAuthenticated && !isPatchProcessState) {
                            try {
                                showAuthMenu(update.getMessage().getFrom().getId(),
                                        update.getMessage().getFrom().getUserName(), 0);
                            } catch (TelegramApiException e) {
                                throw new RuntimeException(e);
                            }
                        } else if (isRegisterProcessState) {
                            try {
                                Driver driver;
                                driver = createDriverFromString(update.getMessage().getText(),
                                        update.getMessage().getFrom().getId(), update.getMessage().getChatId());
                                if (areAllFieldsFilled(driver)) {
                                    driverService.add(driver);
                                    showAuthMenu(update.getMessage().getChatId(), driver.getFio(), 2);
                                }
                            } catch (ArrayIndexOutOfBoundsException | TelegramApiException e) {
                                setAnswer(update.getMessage().getChatId(), FIELDS_IS_EMPTY);
                            }
                        } else if (isPatchProcessState) {
                            try {
                                Driver driver;
                                driver = createDriverFromString(update.getMessage().getText(),
                                        update.getMessage().getFrom().getId(), update.getMessage().getChatId());
                                if (areAllFieldsFilled(driver)) {
                                    driverService.patch(driver, update.getMessage().getFrom().getId().toString());
                                    showAuthMenu(update.getMessage().getChatId(), driver.getFio(), 3);
                                }
                            } catch (ArrayIndexOutOfBoundsException | TelegramApiException e) {
                                setAnswer(update.getMessage().getChatId(), FIELDS_IS_EMPTY);
                            }
                        } else {
                            setAnswer(update.getMessage().getChatId(), "Сначала пройдите регистрацию! /start");
                        }
                    }
                    case "/start" -> {

                        isRegisterProcessState = false;
                        try {
                            showFirstMenu(update.getMessage().getChatId(), update.getMessage().getFrom().getFirstName());
                        } catch (TelegramApiException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    case "\uD83D\uDC64 Мой профиль" -> {
                        log.info(update.getMessage().getFrom().getUserName() + " clicked own profile.");
                        try {
                            if (driverService.getByTgId(update.getMessage().getFrom().getId().toString()).isPresent()) {
                                showProfile(update.getMessage().getFrom().getId());
                            } else {
                                setAnswer(update.getMessage().getFrom().getId(),
                                        EmojiParser
                                                .parseToUnicode(WARNING_EMOJI)
                                                + " Профиля указанного водителя не существует! " +
                                                "Для начала пройдите регистрацию /start");
                            }
                        } catch (TelegramApiException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    case "\uD83D\uDE9A Поделиться маршрутом" -> {
                        log.info(update.getMessage().getFrom().getUserName() + " clicked share path.");
                        setAnswer(update.getMessage().getChatId(), "Делаем метод < Поделиться маршрутом >...");
                    }
                }
            }
        } else if (update.hasCallbackQuery()) {
            String call_data = update.getCallbackQuery().getData();
            long tgId = update.getCallbackQuery().getFrom().getId();
            long chatId = update.getCallbackQuery().getMessage().getChatId();
            try {
                handleCallbackQuery(call_data, chatId, tgId);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public String getBotUsername() {
        return botName;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    private void handleCallbackQuery(String callData, long chatId, long tgId) throws TelegramApiException {
        switch (callData) {
            case "reg":
                if (driverService.getAll()
                        .stream()
                        .anyMatch(i -> i.getTgId().equals(String.valueOf(tgId)))) {
                    showAuthMenu(chatId, null, 1);
                } else {
                    isDriverAuthenticated = false;
                    isRegisterProcessState = true;
                    setAnswer(chatId, REGISTER_DRIVER);
                }
                break;

            case "edit":
                isPatchProcessState = true;
                setAnswer(chatId, REGISTER_DRIVER);
            default:
                break;
        }
    }

    private InlineKeyboardButton createInlineKeyboardButton(String text, String callbackData) {
        return InlineKeyboardButton.builder()
                .text(text)
                .callbackData(callbackData)
                .build();
    }

    private void showAuthMenu(Long chatId, String tgUserName, int state) throws TelegramApiException {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();

        SendMessage message = new SendMessage();
        message.setReplyMarkup(replyKeyboardMarkup);
        replyKeyboardMarkup.setSelective(false);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);

        // Создаем список строк клавиатуры
        List<KeyboardRow> keyboard = new ArrayList<>();

        switch (state) {
            case 0 -> {
                KeyboardRow keyboardFirstRow = new KeyboardRow();
                keyboardFirstRow.add(new KeyboardButton(EmojiParser.parseToUnicode(BUST_IN_SILHOUETTE_EMOJI) + " Мой профиль"));

                KeyboardRow keyboardSecondRow = new KeyboardRow();
                keyboardSecondRow.add(new KeyboardButton(EmojiParser.parseToUnicode(TRUCK_EMOJI) + " Поделиться маршрутом"));

                keyboard.add(keyboardFirstRow);
                keyboard.add(keyboardSecondRow);

                message.setText(UNKNOWN_MESSAGE);
            }
            case 1 -> {
                KeyboardRow keyboardFirstRow = new KeyboardRow();
                keyboardFirstRow.add(new KeyboardButton(EmojiParser.parseToUnicode(BUST_IN_SILHOUETTE_EMOJI) + " Мой профиль"));

                KeyboardRow keyboardSecondRow = new KeyboardRow();
                keyboardSecondRow.add(new KeyboardButton(EmojiParser.parseToUnicode(TRUCK_EMOJI) + " Поделиться маршрутом"));

                keyboard.add(keyboardFirstRow);
                keyboard.add(keyboardSecondRow);

                isDriverAuthenticated = true;
                isRegisterProcessState = false;

                message.setText(IS_REGISTERED);
            }

            case 2 -> {
                KeyboardRow keyboardFirstRow = new KeyboardRow();
                keyboardFirstRow.add(new KeyboardButton(EmojiParser.parseToUnicode(BUST_IN_SILHOUETTE_EMOJI) + " Мой профиль"));

                KeyboardRow keyboardSecondRow = new KeyboardRow();
                keyboardSecondRow.add(new KeyboardButton(EmojiParser.parseToUnicode(TRUCK_EMOJI) + " Поделиться маршрутом"));

                keyboard.add(keyboardFirstRow);
                keyboard.add(keyboardSecondRow);

                isDriverAuthenticated = true;
                isRegisterProcessState = false;

                message.setText(EmojiParser.parseToUnicode(WHITE_CHECK_MARK_EMOJI) + " " +
                        tgUserName + SUCCESSFUL_REGISTER);
            }

            case 3 -> {
                KeyboardRow keyboardFirstRow = new KeyboardRow();
                keyboardFirstRow.add(new KeyboardButton(EmojiParser.parseToUnicode(BUST_IN_SILHOUETTE_EMOJI) + " Мой профиль"));

                KeyboardRow keyboardSecondRow = new KeyboardRow();
                keyboardSecondRow.add(new KeyboardButton(EmojiParser.parseToUnicode(TRUCK_EMOJI) + " Поделиться маршрутом"));

                keyboard.add(keyboardFirstRow);
                keyboard.add(keyboardSecondRow);

                isPatchProcessState = false;

                message.setText(EmojiParser.parseToUnicode(WHITE_CHECK_MARK_EMOJI) + " " +
                        tgUserName + SUCCESSFUL_PATCH);
            }
            default -> {
                setAnswer(chatId, "Произошла ошибка при статусе Строка: 224");
            }

        }
        message.setChatId(chatId);
        replyKeyboardMarkup.setKeyboard(keyboard);
        message.enableHtml(true);
        message.enableMarkdownV2(true);
        message.enableMarkdown(true);
        execute(message);
    }

    private void showProfile(Long chatId) throws TelegramApiException {
        InlineKeyboardMarkup keyboard;
        var editButton = createInlineKeyboardButton("Изменить данные " + EmojiParser
                .parseToUnicode(WRITING_HAND_EMOJI), "edit");

        keyboard = InlineKeyboardMarkup.builder()
                .keyboardRow(List.of(editButton))
                .build();

        Driver driver = driverService.getByTgId(chatId.toString())
                .orElseThrow(() -> new RuntimeException("Такого водителя не существует!"));

        SendMessage sendMessage = SendMessage.builder().chatId(chatId).parseMode(ParseMode.HTML)
                .text(driver.toString())
                .replyMarkup(keyboard).build();
        sendMessage.enableHtml(true);
        sendMessage.enableMarkdownV2(true);
        sendMessage.enableMarkdown(true);
        execute(sendMessage);
    }

    private void showFirstMenu(Long chatId, String firstName) throws TelegramApiException {
        InlineKeyboardMarkup keyboard;
        var startButton = createInlineKeyboardButton("Пройти регистрацию " + EmojiParser
                .parseToUnicode(PAGE_FACING_UP_EMOJI), "reg");
        keyboard = InlineKeyboardMarkup.builder()
                .keyboardRow(List.of(startButton))
                .build();
        SendMessage message = SendMessage.builder().chatId(chatId).parseMode(ParseMode.HTML)
                .text("Добро пожаловать, _" + firstName + "_ "
                        + EmojiParser.parseToUnicode(WAVE_EMOJI) + "\n\n"
                        + "Перед использованием бота необходимо заполнить данные по водителю и машине. " +
                        "Это первичная процедура, в дальнейшем аутентификация будет работать автоматически.")
                .replyMarkup(keyboard).build();
        message.enableHtml(true);
        message.enableMarkdownV2(true);
        message.enableMarkdown(true);
        execute(message);
    }

    private boolean isNewUser(String tg) {
        if (telegramIds.contains(tg)) {
            return false;
        } else {
            telegramIds.add(String.valueOf(tg));
            return true;
        }
    }

    private Driver createDriverFromString(String input, long tgId, long chatId) {
        String[] lines = input.split("\\n");
        Driver driver = new Driver();

        driver.setTgId(String.valueOf(tgId));
        driver.setFio(lines[0].trim());
        driver.setTelephone(lines[1].trim());
        switch (lines[2].trim()) {
            case "фургон":
                driver.setTypeCarBody(TypeCarBody.VAN);
                break;
            case "тент":
                driver.setTypeCarBody(TypeCarBody.TENT);
                break;
            case "изотерма":
                driver.setTypeCarBody(TypeCarBody.ISOTHERMAL);
                break;
            case "открытый":
                driver.setTypeCarBody(TypeCarBody.OPEN);
                break;
            default:
                setAnswer(chatId, "Такого типа кузова не существует!");
        }
        driver.setDimensions(lines[3].trim());
        driver.setLoadOpacity(Integer.parseInt(lines[4].trim()));

        log.info("Driver has been created from string.");
        return driver;
    }

    private boolean areAllFieldsFilled(Object obj) {
        return ObjectUtils.allNotNull(obj);
    }

    private void setAnswer(Long chatId, String text) {
        SendMessage message = new SendMessage();
        message.enableHtml(true);
        message.enableMarkdownV2(true);
        message.enableMarkdown(true);
        message.setChatId(chatId);
        message.setText(text);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}
