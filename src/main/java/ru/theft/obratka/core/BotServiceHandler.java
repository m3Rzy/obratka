package ru.theft.obratka.core;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.theft.obratka.car.service.CarService;
import ru.theft.obratka.driver.service.DriverService;
import ru.theft.obratka.util.constant.UserState;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static ru.theft.obratka.util.constant.ConstantMessage.*;


@Service
@Slf4j
@RequiredArgsConstructor
public class BotServiceHandler {

    private final BotService botService;
    private final BotHandleCallback botHandleCallback;
    private final DriverService driverService;
    private final CarService carService;
    private final ConcurrentMap<Long, UserState> userStates = new ConcurrentHashMap<>();

    public void handleUpdate(Update update, TelegramBotCore bot) throws TelegramApiException {
        if (update.hasMessage() && update.getMessage().hasText()) {
            Long userId = update.getMessage().getFrom().getId();

            if (userStates.get(userId) == null) {
                userStates.putIfAbsent(userId, new UserState());
            }

            UserState state = userStates.get(userId);
            log.info("{} [{}]: {}", update.getMessage().getFrom().getUserName(),
                    userId,
                    update.getMessage().getText());

            switch (update.getMessage().getText()) {
                case "\uD83D\uDC64 Мой профиль" -> {
                    state.setUserRegistrationInProgress(false);
                    state.setCarRegistrationInProgress(false);
                    state.setCarStampRegistrationState(false);
                    if (driverService.getByTgId(update.getMessage().getFrom().getId().toString()).isPresent()) {
                        bot.execute(botService.showProfile(update.getMessage().getFrom().getId(), update.getMessage().getChatId()));
                    } else {
                        bot.execute(botService.createSendMessage(update.getMessage().getChatId(),
                                NOT_AUTHENTICATED_USER));
                    }
                }
                case "\uD83C\uDD95 Добавить авто" -> {
                    try {
                        if (driverService.getByTgId(userId.toString()).isPresent()) {
                            state.setUserRegistrationInProgress(false);
                            state.setCarRegistrationInProgress(true);

                            state.setCarStampRegistrationState(true);
                            bot.execute(botService.createSendMessage(update.getMessage().getChatId(), "Укажите марку авто"));
                        } else {
                            throw new RuntimeException(NOT_AUTHENTICATED_USER);
                        }
                    } catch (Exception e) {
                        bot.execute(botService.createSendMessage(update.getMessage().getChatId(), e.getMessage()));
                    }
                }
                case "\uD83D\uDE9A Список авто" -> {
                    try {
                        state.setUserRegistrationInProgress(false);
                        state.setCarRegistrationInProgress(false);
                        state.setCarStampRegistrationState(false);
                        if (driverService.getByTgId(update.getMessage().getFrom().getId().toString()).isPresent()) {
                            bot.execute(botService.showCars(update.getMessage().getFrom().getId(), update.getMessage().getChatId()));
                        } else {
                            bot.execute(botService.createSendMessage(update.getMessage().getChatId(),
                                    NOT_AUTHENTICATED_USER));
                        }
                    } catch (Exception e) {
                        bot.execute(botService.createSendMessage(userId, e.getMessage()));
                    }

                }
                case "\uD83D\uDEE3 Поделиться маршрутом" -> {
                    try {
                        if (driverService.getByTgId(userId.toString()).isPresent()) {
                            if (!carService.getCarsByDriverId(update.getMessage().getFrom().getId()).isEmpty()) {
                                state.setUserRegistrationInProgress(false);
                            } else {
                                throw new RuntimeException(NOT_ALLOWED_ACCESS_WITHOUT_CAR);
                            }
                        } else {
                            throw new RuntimeException(NOT_AUTHENTICATED_USER);
                        }
                    } catch (Exception e) {
                        bot.execute(botService.createSendMessage(update.getMessage().getChatId(), e.getMessage()));
                    }
                }

                default -> {
                    if ((!state.isUserRegistrationInProgress() && !state.isUserAuthenticated()) || update.getMessage()
                            .getText().equals("/start")) {
                        bot.execute(botService.createInlineKeyboardMarkup(
                                helloMessage(update.getMessage().getFrom().getFirstName()),
                                update.getMessage().getChatId(),
                                List.of(botService.createInlineKeyboardButton(TITLE_BUTTON_REGISTER, "register"))));
                    } else if (state.isUserAuthenticated()) {
                        if (state.isCarRegistrationInProgress()) {
                            try {
                                botService.createCarFromString(state, update.getMessage().getText(), bot, userId,
                                        update.getMessage().getChatId());
                            } catch (Exception e) {
                                bot.execute(botService.createSendMessage(userId, e.getMessage()));
                            }
                        } else {
                            bot.execute(botService.createSendMessage(userId, UNKNOWN_MESSAGE_AUTHENTICATED));
                        }
                    } else {
                        if (state.isUserRegistrationInProgress()) {
                            try {
                                botService.createDriverFromString(state, update.getMessage().getText(), bot, userId,
                                        update.getMessage().getChatId());
                            } catch (Exception e) {
                                bot.execute(botService.createSendMessage(userId, e.getMessage()));
                            }
                        }
                    }
                }
            }
        } else if (update.hasCallbackQuery()) {
            botHandleCallback.handleCallbackQuery(
                    update.getCallbackQuery().getData(),
                    update.getCallbackQuery().getMessage().getChatId(),
                    update.getCallbackQuery().getFrom().getId(),
                    userStates,
                    bot);
        }
    }

}
