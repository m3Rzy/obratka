package ru.theft.obratka.core;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.theft.obratka.car.model.Car;
import ru.theft.obratka.car.service.CarService;
import ru.theft.obratka.driver.model.Driver;
import ru.theft.obratka.driver.service.DriverService;
import ru.theft.obratka.util.constant.UserState;

import java.util.List;
import java.util.concurrent.ConcurrentMap;

import static ru.theft.obratka.car.model.TypeCarBody.*;
import static ru.theft.obratka.util.constant.ConstantMessage.*;

@Service
@Slf4j
@AllArgsConstructor
public class BotHandleCallback {

    private DriverService driverService;
    private BotService botService;
    private CarService carService;

    protected void handleCallbackQuery(String callData,
                                       long chatId,
                                       long tgId,
                                       ConcurrentMap<Long, UserState> userStates,
                                       TelegramBotCore bot) throws TelegramApiException {

        if (userStates.get(tgId) == null) {
            userStates.putIfAbsent(tgId, new UserState());
        }
        UserState userState = userStates.get(tgId);
        try {
            switch (callData) {
                case "register" -> {
                        if (!userState.isUserAuthenticated()) {
                            log.info("User {} not authenticated, he started registration state.", tgId);
                            userState.setUserRegistrationInProgress(true);
                            userState.setUserSurnameRegistrationState(true);
                            botService.clearDriverFields(tgId);
                            bot.execute(botService.createSendMessage(chatId, TEXT_REGISTER_SURNAME_DRIVER));
                        }
                        if (userState.isUserAuthenticated()) {
                            if (driverService.getAll().stream().anyMatch(i -> i.getTgId().equals(String.valueOf(tgId)))) {
                                bot.execute(botService.createInlineKeyboardMarkup(
                                        "Вы уже зарегестрированы!",
                                        chatId,
                                        List.of(
                                                botService.createInlineKeyboardButton(MAIN_PROFILE, "profile")
                                        )
                                ));
                            }
                        }
                }
                case "car-van" -> {
                    if (userState == null) {
                        throw new RuntimeException(NOT_AUTHENTICATED_USER);
                    }
                    if (userState.isCarRegistrationInProgress() && userState.isCarTypeBodyRegistrationState() &&
                            userState.isUserAuthenticated()) {
                        BotService.carFields.get(tgId).setCarType(VAN);
                        userState.setCarTypeBodyRegistrationState(false);
                        userState.setCarBodyLengthRegistrationState(true);
                        bot.execute(botService.createSendMessage(chatId, "Укажите длину кузова в метрах (цифра после запятой обозначется <.>)"));
                    } else {
                        bot.execute(botService.createSendMessage(chatId, NOT_ALLOWED_ACCESS));
                    }
                }

                case "car-tent" -> {
                    if (userState == null) {
                        throw new RuntimeException(NOT_AUTHENTICATED_USER);
                    }
                    if (userState.isCarRegistrationInProgress() && userState.isCarTypeBodyRegistrationState() &&
                            userState.isUserAuthenticated()) {
                        BotService.carFields.get(tgId).setCarType(TENT);
                        userState.setCarTypeBodyRegistrationState(false);
                        userState.setCarBodyLengthRegistrationState(true);
                        bot.execute(botService.createSendMessage(chatId, "Укажите длину кузова в метрах (цифра после запятой обозначется <.>)"));
                    } else {
                        bot.execute(botService.createSendMessage(chatId, NOT_ALLOWED_ACCESS));
                    }
                }

                case "car-isothermal" -> {
                    if (userState == null) {
                        throw new RuntimeException(NOT_AUTHENTICATED_USER);
                    }
                    if (userState.isCarRegistrationInProgress() && userState.isCarTypeBodyRegistrationState() &&
                            userState.isUserAuthenticated()) {
                        BotService.carFields.get(tgId).setCarType(ISOTHERMAL);
                        userState.setCarTypeBodyRegistrationState(false);
                        userState.setCarBodyLengthRegistrationState(true);
                        bot.execute(botService.createSendMessage(chatId, "Укажите длину кузова в метрах (цифра после запятой обозначется <.>)"));
                    } else {
                        bot.execute(botService.createSendMessage(chatId, NOT_ALLOWED_ACCESS));
                    }
                }

                case "car-open" -> {
                    if (userState == null) {
                        throw new RuntimeException(NOT_AUTHENTICATED_USER);
                    }
                    if (userState.isCarRegistrationInProgress() && userState.isCarTypeBodyRegistrationState() &&
                            userState.isUserAuthenticated()) {
                        BotService.carFields.get(tgId).setCarType(OPEN);
                        userState.setCarTypeBodyRegistrationState(false);
                        userState.setCarBodyLengthRegistrationState(true);
                        bot.execute(botService.createSendMessage(chatId, "Укажите длину кузова в метрах (цифра после запятой обозначется <.>)"));
                    } else {
                        bot.execute(botService.createSendMessage(chatId, NOT_ALLOWED_ACCESS));
                    }
                }

                default -> {
                    if (carService.getCarByCarNumber(callData).isPresent()) {
                        Car car = carService.getCarByCarNumber(callData).get();
                        Driver driver = driverService.getByTgId(String.valueOf(tgId)).get();
                        if (!car.getDriver().equals(driver)) {
                            log.info("{}", car);
                            log.info("{}", driver);
                            throw new RuntimeException(NOT_ALLOWED_ACCESS);
                        }
                        bot.execute(botService.createSendMessage(chatId, car.toTerminal()));
                    } else {
                        throw new RuntimeException(UNKNOWN_MESSAGE);
                    }
                }
            }
        } catch (Exception e) {
            bot.execute(botService.createSendMessage(chatId, e.getMessage()));
        }
    }
}
