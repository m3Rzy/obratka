package ru.theft.obratka.core;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.theft.obratka.driver.service.DriverService;
import ru.theft.obratka.util.constant.UserState;

import java.util.List;
import java.util.concurrent.ConcurrentMap;

import static ru.theft.obratka.util.constant.ConstantMessage.*;

@Service
@Slf4j
@AllArgsConstructor
public class BotHandleCallback {

    private DriverService driverService;
    private BotService botService;

    protected void handleCallbackQuery(String callData,
                                       long chatId,
                                       long tgId,
                                       ConcurrentMap<Long, UserState> userStates,
                                       TelegramBotCore bot) throws TelegramApiException {

        UserState userState = userStates.get(tgId);
        switch (callData) {
            case "register" -> {
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
                if (!userState.isUserAuthenticated()) {
                    log.info("User {} not authenticated, he started registration state.", tgId);
                    userState.setUserRegistrationInProgress(true);
                    userState.setUserSurnameRegistrationState(true);
                    botService.clearDriverFields();
                    bot.execute(botService.createSendMessage(chatId, TEXT_REGISTER_SURNAME_DRIVER));
                }
            }
        }
    }
}
