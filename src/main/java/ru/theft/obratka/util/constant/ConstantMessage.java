package ru.theft.obratka.util.constant;

import com.vdurmont.emoji.EmojiParser;

import static ru.theft.obratka.util.constant.Emoji.*;

public class ConstantMessage {

    public static final String TITLE_BUTTON_REGISTER = "Пройти регистрацию "
            + EmojiParser.parseToUnicode(Emoji.PAGE_FACING_UP_EMOJI);

    public static final String MAIN_PROFILE = EmojiParser
            .parseToUnicode(Emoji.BUST_IN_SILHOUETTE_EMOJI) + " Мой профиль";

    public static final String TEXT_REGISTER_SURNAME_DRIVER =
            EmojiParser.parseToUnicode(GREY_QUESTION) + " Напишите Вашу фамилию";

    public static final String TEXT_REGISTER_NAME_DRIVER =
            EmojiParser.parseToUnicode(GREY_QUESTION) + " Напишите Ваше имя";

    public static final String TEXT_REGISTER_PATRONYMIC_DRIVER =
            EmojiParser.parseToUnicode(GREY_QUESTION) + " Напишите Ваше отчество";

    public static final String TEXT_REGISTER_PHONE_DRIVER =
            EmojiParser.parseToUnicode(GREY_QUESTION) + " Напишите Ваш номер телефона без символов +7 и 8";

    public static final String SUCCESSFUL_DRIVER_REGISTER = EmojiParser.parseToUnicode(Emoji.WHITE_CHECK_MARK_EMOJI)
            + " Вы были успешно добавлены в базу!";

    public static final String UNKNOWN_MESSAGE_AUTHENTICATED =
            EmojiParser.parseToUnicode(STOP_SIGN_EMOJI) + " Неизвестная команда! Выберете кнопки ниже.";

    public static final String UNKNOWN_MESSAGE =
            EmojiParser.parseToUnicode(STOP_SIGN_EMOJI) + " Неизвестная команда!";

    public static final String NOT_ALLOWED_ACCESS_WITHOUT_CAR = EmojiParser.parseToUnicode(Emoji.WARNING_EMOJI)
            + " Для начала необходимо добавить авто!";

    public static final String LIST_OF_CARS_EMPTY = EmojiParser.parseToUnicode(Emoji.INFORMATION_SOURCE_EMOJI)
            + " Ваш парк пуст.";

    public static final String SUCCESSFUL_CAR_REGISTER = EmojiParser.parseToUnicode(Emoji.WHITE_CHECK_MARK_EMOJI)
            + " Вы успешно добавили авто к себе в парк!";

    public static final String NOT_AUTHENTICATED_USER = EmojiParser.parseToUnicode(Emoji.WARNING_EMOJI)
            + " Профиля указанного водителя не существует! " + "Для начала пройдите регистрацию /start";

    public static final String NOT_ALLOWED_ACCESS = EmojiParser.parseToUnicode(Emoji.WARNING_EMOJI)
            + " Нет доступа к просмотру!";

    public static final String ERROR_CAR_NUMBER_ALREADY_HAS = EmojiParser.parseToUnicode(Emoji.WARNING_EMOJI) +
            " Такой номер авто уже занят!";

    public static String helloMessage(String userName) {
        return "Добро пожаловать, " + userName + " " + EmojiParser.parseToUnicode(Emoji.WAVE_EMOJI) + "\n\n"
                + "Перед использованием бота необходимо заполнить данные по водителю и машине. " +
                "Это первичная процедура, в дальнейшем аутентификация будет работать автоматически.";
    }

    public static String responseToNotAuthenticatedUser(String userName) {
        return userName + ", чтобы начать пользовать ботом необходимо пройти первичную регистрацию! Жмите /start";
    }
}
