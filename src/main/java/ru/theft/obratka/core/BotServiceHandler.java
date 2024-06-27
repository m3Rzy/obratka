package ru.theft.obratka.core;

import com.vdurmont.emoji.EmojiParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.theft.obratka.driver.service.DriverService;
import ru.theft.obratka.util.constant.Emoji;
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
    private final ConcurrentMap<Long, UserState> userStates = new ConcurrentHashMap<>();

    public void handleUpdate(Update update, TelegramBotCore bot) throws TelegramApiException {
        if (update.hasMessage() && update.getMessage().hasText()) {
            Long userId = update.getMessage().getFrom().getId();
            userStates.putIfAbsent(userId, new UserState());
            UserState state = userStates.get(userId);
            log.info("{} [{}]: {}", update.getMessage().getFrom().getUserName(),
                    userId,
                    update.getMessage().getText());

            switch (update.getMessage().getText()) {
                case "\uD83D\uDC64 Мой профиль" -> {
//                    todo: ?
                    state.setUserRegistrationInProgress(false);

                    if (driverService.getByTgId(update.getMessage().getFrom().getId().toString()).isPresent()) {
                        bot.execute(botService.showProfile(update.getMessage().getFrom().getId(), update.getMessage().getChatId()));
                    } else {
                        bot.execute(botService.createSendMessage(update.getMessage().getChatId(),
                                EmojiParser.parseToUnicode(Emoji.WARNING_EMOJI)
                                        + " Профиля указанного водителя не существует! " + "Для начала пройдите регистрацию /start"));
                    }
                }
                case "\uD83D\uDE9A Добавить авто" -> {
//                    todo: ?
                    state.setUserRegistrationInProgress(false);
                }

                case "\uD83D\uDEE3 Поделиться маршрутом" -> {
//                    todo: ?
                    state.setUserRegistrationInProgress(false);
                }

                default -> {
                    if ((!state.isUserRegistrationInProgress() && !state.isUserAuthenticated()) || update.getMessage().getText().equals("/start")) {
                        bot.execute(botService.createInlineKeyboardMarkup(
                                helloMessage(update.getMessage().getFrom().getFirstName()),
                                update.getMessage().getChatId(),
                                List.of(botService.createInlineKeyboardButton(TITLE_BUTTON_REGISTER, "register"))));
                    } else if (state.isUserAuthenticated()) {
                        bot.execute(botService.createSendMessage(userId, UNKNOWN_MESSAGE));
                    } else {
                        if (state.isUserRegistrationInProgress()) {
                            try {
                                botService.createDriverFromString(state, update.getMessage().getText(), bot, userId, update.getMessage().getChatId());
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
