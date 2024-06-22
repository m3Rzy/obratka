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
import ru.theft.obratka.destination.model.Destination;
import ru.theft.obratka.destination.service.DestinationService;
import ru.theft.obratka.driver.model.Driver;
import ru.theft.obratka.driver.service.DriverService;
import ru.theft.obratka.util.constant.Emoji;
import ru.theft.obratka.util.constant.UserState;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static ru.theft.obratka.util.constant.ConstantMessage.*;
import static ru.theft.obratka.util.constant.Emoji.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class DriverServiceHandler {

    private final ConcurrentMap<Long, UserState> userStates = new ConcurrentHashMap<>();

    private final DriverService driverService;
    private final DestinationService destinationService;

    private final List<String> telegramIds = new ArrayList<>();
    private final MenuService menuService = new MenuService();

    private final List<String> driverFields = new ArrayList<>();

    public void handleUpdate(Update update, TelegramBotCore bot) throws TelegramApiException {
        if (update.hasMessage() && update.getMessage().hasText()) {
            Long userId = update.getMessage().getFrom().getId();

            userStates.putIfAbsent(userId, new UserState());
            log.info("{} [{}]: {}", update.getMessage().getFrom().getUserName(),
                    userId,
                    update.getMessage().getText());

            if (isNewUser(userId.toString())) {
                resetUserState(userId);
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
        Long userId = update.getMessage().getFrom().getId();
        UserState userState = userStates.get(userId);

        switch (update.getMessage().getText()) {
            case "/start" -> {
                userState.setRegisterSurnameState(false);
                userState.setRegisterNameState(false);
                userState.setRegisterPatronymicState(false);
                userState.setRegisterPhoneState(false);
                userState.setRegisterProcessState(false);
                driverFields.clear();
                bot.execute(menuService.createFirstMenu(update.getMessage().getChatId(),
                        update.getMessage().getFrom().getFirstName()));
            }
            case "/obstop" -> {
                if (userState.isPatchProcessState()) {
                    bot.execute(menuService.createAuthMenu(update.getMessage().getChatId(),
                            "", 4, userState));
                }
                if (userState.isArrivalProcessState()) {
                    bot.execute(menuService.createAuthMenu(update.getMessage().getChatId(),
                            "", 6, userState));
                }
            }
            case "\uD83D\uDC64 Мой профиль" -> {
                userState.setPatchProcessState(false);
                userState.setArrivalProcessState(false);
                log.info(update.getMessage().getFrom().getUserName() + " clicked own profile.");
                if (driverService.getByTgId(update.getMessage().getFrom().getId().toString()).isPresent()) {
                    showProfile(update.getMessage().getFrom().getId(), bot);
                } else {
                    bot.execute(createSendMessage(update.getMessage().getChatId(),
                            EmojiParser.parseToUnicode(Emoji.WARNING_EMOJI)
                                    + " Профиля указанного водителя не существует! " + "Для начала пройдите регистрацию /start"));
                }
            }
            case "\uD83D\uDEE3 Поделиться маршрутом" -> {
                userState.setPatchProcessState(false);
                log.info(update.getMessage().getFrom().getUserName() + " clicked share path.");
                if (driverService.getByTgId(update.getMessage().getFrom().getId().toString()).isPresent()) {
                    userState.setArrivalProcessState(true);
                    bot.execute(createSendMessage(update.getMessage().getChatId(),
                            REGISTER_ARRIVAL));
                } else {
                    bot.execute(createSendMessage(update.getMessage().getChatId(),
                            EmojiParser.parseToUnicode(Emoji.WARNING_EMOJI) +
                                    " Профиля указанного водителя не существует! " + "Для начала пройдите регистрацию /start"));
                }
            }

            case "\uD83D\uDE9A Добавить авто" -> {
                userState.setPatchProcessState(false);
                userState.setArrivalProcessState(false);
                log.info(update.getMessage().getFrom().getUserName() + " clicked added new car.");
                if (driverService.getByTgId(update.getMessage().getFrom().getId().toString()).isPresent()) {
                    userState.setRegisterNewCarState(true);
                } else {
                    bot.execute(createSendMessage(update.getMessage().getChatId(),
                            EmojiParser.parseToUnicode(Emoji.WARNING_EMOJI)
                                    + " Профиля указанного водителя не существует! " + "Для начала пройдите регистрацию /start"));
                }
            }
            default -> handleDefaultMessage(update, bot);
        }
    }

    private void handleDefaultMessage(Update update, TelegramBotCore bot) throws TelegramApiException {
        Long userId = update.getMessage().getFrom().getId();
        UserState userState = userStates.get(userId);

        if (userState.isDriverAuthenticated() && !userState.isPatchProcessState() && !userState.isArrivalProcessState()) {
            bot.execute(menuService.createAuthMenu(update.getMessage().getChatId(),
                    update.getMessage().getFrom().getUserName(), 0, userState));
        } else if (userState.isRegisterProcessState()) {
            try {
                String text = update.getMessage().getText();
                if (userState.isRegisterSurnameState()) {
                    if (text.isEmpty()) {
                        throw new RuntimeException(VALID_DRIVER_SURNAME_EMPTY);
                    }
                    if (text.length() > 80) {
                        throw new RuntimeException(VALID_DRIVER_SURNAME_OVER);
                    }
                    driverFields.add(text);
                    userState.setRegisterSurnameState(false);
                    userState.setRegisterNameState(true);
                    bot.execute(createSendMessage(userId, TEXT_REGISTER_NAME_DRIVER));
                    return;

                }

                if (userState.isRegisterNameState()) {
                    if (text.isEmpty()) {
                        throw new RuntimeException(VALID_DRIVER_NAME_EMPTY);
                    }
                    if (text.length() > 80) {
                        throw new RuntimeException(VALID_DRIVER_NAME_OVER);
                    }
                    driverFields.add(text);
                    userState.setRegisterNameState(false);
                    userState.setRegisterPatronymicState(true);
                    bot.execute(createSendMessage(userId, TEXT_REGISTER_PATRONYMIC_DRIVER));
                    return;

                }

                if (userState.isRegisterPatronymicState()) {
                    if (text.length() > 80) {
                        throw new RuntimeException(VALID_DRIVER_PATRONYMIC_OVER);
                    }
                    driverFields.add(text);
                    userState.setRegisterPatronymicState(false);
                    userState.setRegisterPhoneState(true);
                    bot.execute(createSendMessage(userId, TEXT_REGISTER_PHONE_DRIVER));
                    return;
                }

                if (userState.isRegisterPhoneState()) {
                    if (text.isEmpty()) {
                        throw new RuntimeException(VALID_DRIVER_PHONE_EMPTY);
                    }
                    if (text.length() != 10) {
                        throw new RuntimeException(VALID_DRIVER_PHONE_OVER);
                    }
                    driverFields.add("7" + text);
                    userState.setRegisterPhoneState(false);
                    userState.setRegisterDriverFinal(true);
                }

                if (userState.isRegisterDriverFinal()) {
                    userState.setRegisterDriverFinal(false);
                    driverService.add(Driver
                            .builder()
                            .tgId(userId.toString())
                            .fio(driverFields.get(0) + " " + driverFields.get(1) + " " + driverFields.get(2))
                            .telephone(driverFields.get(3))
                            .createdAt(LocalDateTime.now()).build());

                    bot.execute(menuService.createAuthMenu(update.getMessage().getChatId(),
                            driverFields.get(1), 2, userState));
                    driverFields.clear();
                }
            } catch (Exception e) {
                bot.execute(createSendMessage(userId, e.getMessage()));
            }

        } else if (userState.isPatchProcessState()) {
            // todo: сделать редактирование водилы
            try {
                String text = update.getMessage().getText();
                if (userState.isEditSurnameState()) {
                    if (text.isEmpty()) {
                        throw new RuntimeException(VALID_DRIVER_SURNAME_EMPTY);
                    }
                    if (text.length() > 255) {
                        throw new RuntimeException(VALID_DRIVER_SURNAME_OVER);
                    }
                    driverFields.add(text);
                    userState.setEditSurnameState(false);
                    userState.setEditNameState(true);
                    bot.execute(createSendMessage(userId, TEXT_REGISTER_NAME_DRIVER));
                    return;

                }

                if (userState.isEditNameState()) {
                    if (text.isEmpty()) {
                        throw new RuntimeException(VALID_DRIVER_NAME_EMPTY);
                    }
                    if (text.length() > 255) {
                        throw new RuntimeException(VALID_DRIVER_NAME_OVER);
                    }
                    driverFields.add(text);
                    userState.setEditNameState(false);
                    userState.setEditPatronymicState(true);
                    bot.execute(createSendMessage(userId, TEXT_REGISTER_PATRONYMIC_DRIVER));
                    return;
                }

                if (userState.isEditPatronymicState()) {
                    if (text.length() > 255) {
                        throw new RuntimeException(VALID_DRIVER_PATRONYMIC_OVER);
                    }
                    driverFields.add(text);
                    userState.setEditPatronymicState(false);
                    userState.setEditPhoneState(true);
                    bot.execute(createSendMessage(userId, TEXT_REGISTER_PHONE_DRIVER));
                    return;
                }

                if (userState.isEditPhoneState()) {
                    if (text.isEmpty()) {
                        throw new RuntimeException(VALID_DRIVER_PHONE_EMPTY);
                    }
                    if (text.length() != 10) {
                        throw new RuntimeException(VALID_DRIVER_PHONE_OVER);
                    }
                    driverFields.add("7" + text);
                    userState.setEditPhoneState(false);
                    userState.setEditDriverFinal(true);
                }

                if (userState.isEditDriverFinal()) {
                    userState.setEditDriverFinal(false);
                    driverService.patch(Driver
                            .builder()
                            .tgId(userId.toString())
                            .fio(driverFields.get(0) + " " + driverFields.get(1) + " " + driverFields.get(2))
                            .telephone(driverFields.get(3))
                            .createdAt(LocalDateTime.now()).build(), userId.toString());

                    bot.execute(menuService.createAuthMenu(update.getMessage().getChatId(), driverFields.get(1),
                            3, userState));
                    driverFields.clear();
                }
            } catch (Exception e) {
                bot.execute(createSendMessage(userId, e.getMessage()));
            }
        } else if (userState.isArrivalProcessState()) {
            try {
                Destination destination = createDestinationFromString(update.getMessage().getText(),
                        update.getMessage().getChatId(), bot);
                if (areAllFieldsFilled(destination)) {
                    destinationService.add(destination, String.valueOf(update.getMessage().getFrom().getId()));
                    bot.execute(menuService.createAuthMenu(update.getMessage().getChatId(),
                            update.getMessage().getFrom().getUserName(), 5, userState));
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                bot.execute(createSendMessage(update.getMessage().getChatId(), FIELDS_IS_EMPTY));
            }
        } else if (userState.isRegisterNewCarState()) {
//            todo: зарегать новое авто
        }
        else {
            bot.execute(createSendMessage(update.getMessage().getChatId(), "Сначала пройдите регистрацию! /start"));
        }
    }

    private void handleCallbackQuery(String callData, long chatId, long tgId,
                                     TelegramBotCore bot) throws TelegramApiException {
        UserState userState = userStates.get(tgId);

        switch (callData) {
            case "reg":
                if (driverService.getAll().stream().anyMatch(i -> i.getTgId().equals(String.valueOf(tgId)))) {
                    bot.execute(menuService.createAuthMenu(chatId, null, 1, userState));
                } else {
                    userState.setDriverAuthenticated(false);
                    userState.setRegisterProcessState(true);

                    userState.setRegisterSurnameState(true);
                    userState.setRegisterNameState(false);
                    userState.setRegisterPatronymicState(false);
                    userState.setRegisterPhoneState(false);

                    userState.setRegisterNewCarState(false);
                    bot.execute(createSendMessage(chatId, TEXT_REGISTER_SURNAME_DRIVER));
                }
                break;
            case "edit":
                userState.setPatchProcessState(true);
                userState.setEditSurnameState(true);
                bot.execute(createSendMessage(chatId, TEXT_REGISTER_SURNAME_DRIVER));
                break;
            default:
                break;
        }
    }

    private void resetUserState(Long userId) {
        UserState userState = userStates.get(userId);
        userState.setDriverAuthenticated(false);
        userState.setRegisterProcessState(false);
        userState.setPatchProcessState(false);
        userState.setArrivalProcessState(false);
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

    private Destination createDestinationFromString(String input, long chatId,
                                                    TelegramBotCore bot) throws TelegramApiException {
        String[] lines = input.split("\\n");
        Destination destination = new Destination();
        if (lines[0].trim().isEmpty() || lines[0].trim().length() < 3) {
            bot.execute(createSendMessage(chatId,
                    EmojiParser.parseToUnicode(WARNING_EMOJI)
                            + " Необходимо указать изначальную точку маршрута!"
                            + EmojiParser.parseToUnicode(BULB_EMOJI)
                            + " Пример: _Барнаул_"));
            throw new RuntimeException("Необходимо указать изначальную точку маршрута!");
        }
        destination.setFromRoute(lines[0].trim());

        if (lines[1].trim().isEmpty() || lines[1].trim().length() < 3) {
            bot.execute(createSendMessage(chatId,
                    EmojiParser.parseToUnicode(WARNING_EMOJI)
                            + " Необходимо указать конечную точку маршрута!"
                            + EmojiParser.parseToUnicode(BULB_EMOJI)
                            + " Пример: _Санкт-Петербург_"));
            throw new RuntimeException("Необходимо указать конечную точку маршрута!");
        }
        destination.setToRoute(lines[1].trim());

        if (lines[2].trim().isEmpty() || lines[2].trim().length() < 3) {
            bot.execute(createSendMessage(chatId,
                    EmojiParser.parseToUnicode(WARNING_EMOJI)
                            + " Необходимо указать примерную дату прибытия на конечную точку!"
                            + EmojiParser.parseToUnicode(BULB_EMOJI)
                            + " Пример: завтра"));
            throw new RuntimeException("Необходимо указать примерную дату прибытия на конечную точку!");
        }
        destination.setDateOfArrival(lines[2].trim());
        destination.setCreatedAt(LocalDateTime.now());
        log.info("Destination has been created from string.");
        return destination;
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
