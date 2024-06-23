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
            EmojiParser.parseToUnicode(GREY_QUESTION) + " Напишите Ваше отчество (при наличие)";

    public static final String TEXT_REGISTER_PHONE_DRIVER =
            EmojiParser.parseToUnicode(GREY_QUESTION) + " Напишите Ваш номер телефона без символов +7 и 8";

    public static final String SUCCESSFUL_REGISTER = "Вы были успешно добавлены в базу.";


    public static String helloMessage(String userName) {
        return "Добро пожаловать, " + userName + " " + EmojiParser.parseToUnicode(Emoji.WAVE_EMOJI) + "\n\n"
                + "Перед использованием бота необходимо заполнить данные по водителю и машине. " +
                "Это первичная процедура, в дальнейшем аутентификация будет работать автоматически.";
    }

    public static String responseToNotAuthenticatedUser(String userName) {
        return userName + ", чтобы начать пользовать ботом необходимо пройти первичную регистрацию! Жмите /start";
    }


}
