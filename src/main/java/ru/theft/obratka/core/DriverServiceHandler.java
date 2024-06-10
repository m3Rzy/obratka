package ru.theft.obratka.core;

import com.vdurmont.emoji.EmojiParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.theft.obratka.driver.model.Driver;
import ru.theft.obratka.driver.model.TypeCarBody;
import ru.theft.obratka.driver.service.DriverService;
import ru.theft.obratka.util.constant.Emoji;

import java.util.ArrayList;
import java.util.List;

import static ru.theft.obratka.util.constant.ConstantMessage.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class DriverServiceHandler {

    public static boolean isDriverAuthenticated = false;
    public static boolean isRegisterProcessState = false;
    public static boolean isPatchProcessState = false;

    private final DriverService driverService;

    private final List<String> telegramIds = new ArrayList<>();
    private final MenuService menuService = new MenuService();

    public void handleUpdate(Update update, TelegramBotCore bot) throws TelegramApiException {
        if (update.hasMessage() && update.getMessage().hasText()) {
            log.info("{} [{}]: {}", update.getMessage().getFrom().getUserName(),
                    update.getMessage().getFrom().getId(),
                    update.getMessage().getText());

            if (isNewUser(update.getMessage().getFrom().getId().toString())) {
                bot.execute(menuService.createFirstMenu(update.getMessage().getChatId(),
                        update.getMessage().getFrom().getFirstName()));
            } else {
                processMessage(update, bot);
            }
        } else if (update.hasCallbackQuery()) {
            handleCallbackQuery(update.getCallbackQuery().getData(),
                    update.getCallbackQuery().getMessage().getChatId(), update.getCallbackQuery().getFrom().getId(), bot);
        }
    }

    private void processMessage(Update update, TelegramBotCore bot) throws TelegramApiException {
        switch (update.getMessage().getText()) {
            case "/start" -> {
                isRegisterProcessState = false;
                bot.execute(menuService.createFirstMenu(update.getMessage().getChatId(),
                        update.getMessage().getFrom().getFirstName()));
            }
            case "/obstop" -> {
                if (isPatchProcessState) {
                    bot.execute(menuService.createAuthMenu(update.getMessage().getChatId(),
                            "", 4));
                }
            }
            case "\uD83D\uDC64 Мой профиль" -> {
                isPatchProcessState = false;
                log.info(update.getMessage().getFrom().getUserName() + " clicked own profile.");
                if (driverService.getByTgId(update.getMessage().getFrom().getId().toString()).isPresent()) {
                    showProfile(update.getMessage().getFrom().getId(), bot);
                } else {
                    bot.execute(createSendMessage(update.getMessage().getChatId(),
                            EmojiParser.parseToUnicode(Emoji.WARNING_EMOJI)
                                    + " Профиля указанного водителя не существует! " + "Для начала пройдите регистрацию /start"));
                }
            }
            case "\uD83D\uDE9A Поделиться маршрутом" -> {
                isPatchProcessState = false;
                log.info(update.getMessage().getFrom().getUserName() + " clicked share path.");
                if (driverService.getByTgId(update.getMessage().getFrom().getId().toString()).isPresent()) {
                    bot.execute(createSendMessage(update.getMessage().getChatId(),
                            "Делаем метод < Поделиться маршрутом >..."));
                } else {
                    bot.execute(createSendMessage(update.getMessage().getChatId(),
                            EmojiParser.parseToUnicode(Emoji.WARNING_EMOJI) +
                                    " Профиля указанного водителя не существует! " + "Для начала пройдите регистрацию /start"));
                }
            }
            default -> handleDefaultMessage(update, bot);
        }
    }

    private void handleDefaultMessage(Update update, TelegramBotCore bot) throws TelegramApiException {
        if (isDriverAuthenticated && !isPatchProcessState) {
            bot.execute(menuService.createAuthMenu(update.getMessage().getChatId(),
                    update.getMessage().getFrom().getUserName(), 0));
        } else if (isRegisterProcessState) {
            try {
                Driver driver = createDriverFromString(update.getMessage().getText(),
                        update.getMessage().getFrom().getId());
                if (areAllFieldsFilled(driver)) {
                    driverService.add(driver);
                    bot.execute(menuService.createAuthMenu(update.getMessage().getChatId(),
                            driver.getFio(), 2));
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                bot.execute(createSendMessage(update.getMessage().getChatId(), FIELDS_IS_EMPTY));
            }
        } else if (isPatchProcessState) {
            try {
                Driver driver = createDriverFromString(update.getMessage().getText(),
                        update.getMessage().getFrom().getId());
                if (areAllFieldsFilled(driver)) {
                    driverService.patch(driver, update.getMessage().getFrom().getId().toString());
                    bot.execute(menuService.createAuthMenu(update.getMessage().getChatId(), driver.getFio(), 3));
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                bot.execute(createSendMessage(update.getMessage().getChatId(), FIELDS_IS_EMPTY));
            }
        } else {
            bot.execute(createSendMessage(update.getMessage().getChatId(), "Сначала пройдите регистрацию! /start"));
        }
    }

    private void handleCallbackQuery(String callData, long chatId, long tgId,
                                     TelegramBotCore bot) throws TelegramApiException {
        switch (callData) {
            case "reg":
                if (driverService.getAll().stream().anyMatch(i -> i.getTgId().equals(String.valueOf(tgId)))) {
                    bot.execute(menuService.createAuthMenu(chatId, null, 1));
                } else {
                    isDriverAuthenticated = false;
                    isRegisterProcessState = true;
                    bot.execute(createSendMessage(chatId, REGISTER_DRIVER));
                }
                break;
            case "edit":
                isPatchProcessState = true;
                bot.execute(createSendMessage(chatId, PATCH_DRIVER));
                break;
            default:
                break;
        }
    }

    private void showProfile(Long chatId, TelegramBotCore bot) throws TelegramApiException {
        InlineKeyboardMarkup keyboard;
        var editButton = menuService.createInlineKeyboardButton("Изменить данные " + EmojiParser
                .parseToUnicode(Emoji.WRITING_HAND_EMOJI), "edit");

        keyboard = InlineKeyboardMarkup.builder().keyboardRow(List.of(editButton)).build();
        Driver driver = driverService.getByTgId(chatId.toString())
                .orElseThrow(() -> new RuntimeException("Такого водителя не существует!"));

        SendMessage message = new SendMessage();
        message.enableHtml(true);
        message.enableMarkdownV2(true);
        message.enableMarkdown(true);
        message.setText(driver.toString());
        message.setChatId(chatId);
        message.setReplyMarkup(keyboard);
        bot.execute(message);
    }

    private Driver createDriverFromString(String input, long tgId) {
        String[] lines = input.split("\\n");
        Driver driver = new Driver();
        driver.setTgId(String.valueOf(tgId));
        driver.setFio(lines[0].trim());
        driver.setTelephone(lines[1].trim());

        switch (lines[2].trim()) {
            case "фургон" -> driver.setTypeCarBody(TypeCarBody.VAN);
            case "тент" -> driver.setTypeCarBody(TypeCarBody.TENT);
            case "изотерма" -> driver.setTypeCarBody(TypeCarBody.ISOTHERMAL);
            case "открытый" -> driver.setTypeCarBody(TypeCarBody.OPEN);
            default -> throw new IllegalArgumentException("Такого типа кузова не существует!");
        }

        driver.setDimensions(lines[3].trim());
        driver.setLoadOpacity(Integer.parseInt(lines[4].trim()));
        log.info("Driver has been created from string.");
        return driver;
    }

    private boolean isNewUser(String tg) {
        if (telegramIds.contains(tg)) {
            return false;
        } else {
            telegramIds.add(tg);
            return true;
        }
    }

    private boolean areAllFieldsFilled(Object obj) {
        return ObjectUtils.allNotNull(obj);
    }

    private SendMessage createSendMessage(Long chatId, String text) {
        SendMessage message = new SendMessage();
        message.enableHtml(true);
        message.enableMarkdownV2(true);
        message.enableMarkdown(true);
        message.setChatId(chatId);
        message.setText(text);
        return message;
    }
}
