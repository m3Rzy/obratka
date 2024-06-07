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
import ru.theft.obratka.util.constant.ConstantMessage;

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

    private boolean isRegisterState = false;
    private boolean isAuthState = false;
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
                    showMenu(update.getMessage().getChatId(), update.getMessage().getFrom().getFirstName());
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
            } else {
                switch (update.getMessage().getText()) {
                    default -> {
                        if (isAuthState) {
                            try {
                                showAuthMenu(update.getMessage().getFrom().getId());
                            } catch (TelegramApiException e) {
                                throw new RuntimeException(e);
                            }
                        }
                        if (isRegisterState) {
                            try {
                                Driver driver;
                                driver = createDriverFromString(update.getMessage().getText(),
                                        update.getMessage().getFrom().getId(), update.getMessage().getChatId());
                                if (areAllFieldsFilled(driver)) {
                                    isRegisterState = false;
                                    isAuthState = true;
                                    driverService.add(driver);
                                    setAnswer(update.getMessage().getChatId(),
                                            EmojiParser.parseToUnicode(WHITE_CHECK_MARK_EMOJI) + " " +
                                                    driver.getFio() + SUCCESSFUL_REGISTER);
                                }
                            } catch (ArrayIndexOutOfBoundsException e) {
                                setAnswer(update.getMessage().getChatId(), FIELDS_IS_EMPTY);
                            }
                        }
                    }
                    case "/start" -> {
                        isRegisterState = false;
                        try {
                            showMenu(update.getMessage().getChatId(), update.getMessage().getFrom().getFirstName());
                        } catch (TelegramApiException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    case "\uD83D\uDC64 Мой профиль" -> {
                        log.info(update.getMessage().getFrom().getUserName() + " clicked own profile.");
                        Driver driver = driverService.getByTgId(update.getMessage().getFrom().getId().toString());
                        setAnswer(update.getMessage().getChatId(), driver.toString());
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
                    setAnswer(chatId, IS_REGISTERED);
                    isAuthState = true;
                } else {
                    isAuthState = false;
                    isRegisterState = true;
                    setAnswer(chatId, ConstantMessage.REGISTER_DRIVER);
                }
                break;
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

    private void showAuthMenu(Long chatId) throws TelegramApiException {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();

        SendMessage message = new SendMessage();

        message.setReplyMarkup(replyKeyboardMarkup);
        replyKeyboardMarkup.setSelective(false);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);

        // Создаем список строк клавиатуры
        List<KeyboardRow> keyboard = new ArrayList<>();

        // Первая строчка клавиатуры
        KeyboardRow keyboardFirstRow = new KeyboardRow();
        // Добавляем кнопки в первую строчку клавиатуры
        keyboardFirstRow.add(new KeyboardButton(EmojiParser.parseToUnicode(BUST_IN_SILHOUETTE_EMOJI) + " Мой профиль"));

        // Вторая строчка клавиатуры
        KeyboardRow keyboardSecondRow = new KeyboardRow();
        // Добавляем кнопки во вторую строчку клавиатуры
        keyboardSecondRow.add(new KeyboardButton( EmojiParser.parseToUnicode(TRUCK_EMOJI) + " Поделиться маршрутом"));

        // Добавляем все строчки клавиатуры в список
        keyboard.add(keyboardFirstRow);
        keyboard.add(keyboardSecondRow);
        // и устанваливаем этот список нашей клавиатуре
        replyKeyboardMarkup.setKeyboard(keyboard);

        message.setChatId(chatId);
        message.setText("Выберете нужную кнопку ниже...");
        message.enableHtml(true);
        message.enableMarkdownV2(true);
        message.enableMarkdown(true);
        execute(message);
    }

    private void showMenu(Long chatId, String firstName) throws TelegramApiException {
        InlineKeyboardMarkup keyboard;

//        var helpButton = createInlineKeyboardButton("О приложении " + EmojiParser
//                .parseToUnicode(INFORMATION_SOURCE_EMOJI), "info");
        var startButton = createInlineKeyboardButton("Пройти регистрацию " + EmojiParser
                .parseToUnicode(PAGE_FACING_UP_EMOJI), "reg");

        keyboard = InlineKeyboardMarkup.builder()
//                .keyboardRow(List.of(helpButton))
                .keyboardRow(List.of(startButton))
                .build();

        SendMessage message = SendMessage.builder().chatId(chatId).parseMode(ParseMode.HTML)
                .text("Добро пожаловать, _" + firstName + "_ "
                        + EmojiParser.parseToUnicode(WAVE_EMOJI) + "\n\n"
                        + "Перед использованием бота необходимо заполнить данные по водителю и машине. " +
                        "Это первичная процедура, в дальнейшем аутентификация будет работать автоматически. \n\n"
                        + EmojiParser.parseToUnicode(ZAP_EMOJI) + " Нашел ошибку или есть предложения " +
                        "по улучшению? @hoiboui")
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
