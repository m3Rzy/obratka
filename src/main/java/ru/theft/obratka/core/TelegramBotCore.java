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
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.theft.obratka.driver.model.Driver;
import ru.theft.obratka.driver.model.TypeCarBody;
import ru.theft.obratka.driver.service.DriverService;
import ru.theft.obratka.util.constant.ConstantMessage;

import java.util.ArrayList;
import java.util.List;

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
                        if (isRegisterState) {
                            try {
                                Driver driver;
                                driver = createDriverFromString(update.getMessage().getText(),
                                        update.getMessage().getFrom().getId(), update.getMessage().getChatId());
                                if (areAllFieldsFilled(driver)) {
                                    isRegisterState = false;
                                    driverService.add(driver);
                                    setAnswer(update.getMessage().getChatId(), driver.getFio() + ", вы были успешно зарегестрированы!");
                                }
                            } catch (ArrayIndexOutOfBoundsException e) {
                                setAnswer(update.getMessage().getChatId(), "Не все поля заполнены!");
                            }
                        } else {
                            setAnswer(update.getMessage().getChatId(), "Неизвестная команда!");
                        }
                    }
                    case "/start" -> {
                        if (!isAuthenticated(update.getMessage().getFrom().getId().toString())) {
                            isRegisterState = false;
                            try {
                                showMenu(update.getMessage().getChatId(), update.getMessage().getFrom().getFirstName());
                            } catch (TelegramApiException e) {
                                throw new RuntimeException(e);
                            }
                        } else {
                            setAnswer(update.getMessage().getChatId(),
                                    update.getMessage().getFrom().getFirstName() + ", " +
                                    "вы ведь уже прошли регистрацию! Необходимо поменять данные? Обращайтесь к @hoiboui");
                        }

                    }
                }
            }
        } else if (update.hasCallbackQuery()) {
            String call_data = update.getCallbackQuery().getData();
            long chatId = update.getCallbackQuery().getMessage().getChatId();
            try {
                handleCallbackQuery(call_data, chatId, update);
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

    private void handleCallbackQuery(String callData, long chatId, Update update) throws TelegramApiException {
        switch (callData) {
            case "reg":
                log.info("{} clicked button *reg*.", update.getCallbackQuery().getFrom().getUserName());
                isRegisterState = true;
                setAnswer(chatId, ConstantMessage.REGISTER_DRIVER);
                break;
            default:
                log.info("{} randomly clicked.", update.getCallbackQuery().getFrom().getUserName());
                break;
        }
    }

    private InlineKeyboardButton createInlineKeyboardButton(String text, String callbackData) {
        return InlineKeyboardButton.builder()
                .text(text)
                .callbackData(callbackData)
                .build();
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

    private boolean isAuthenticated(String tg) {
        if (driverService.getByTgId(tg) != null) {
            log.info("User already authenticated.");
            return true;
        } else {
            log.info("User not authenticated.");
            return false;
        }
    }

    private boolean isNewUser(String tg) {
        if (telegramIds.contains(tg)) {
            log.info("Old user.");
            return false;
        } else {
            telegramIds.add(String.valueOf(tg));
            log.info("New user!");
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
