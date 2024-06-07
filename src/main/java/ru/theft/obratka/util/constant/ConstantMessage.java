package ru.theft.obratka.util.constant;

import com.vdurmont.emoji.EmojiParser;

import static ru.theft.obratka.util.constant.Emoji.*;

public class ConstantMessage {
    public static final String REGISTER_DRIVER = """
                 *Ниже необходимо заполнить данные по водителю и авто:*
                
                - ФИО (через пробел)
                - Номер телефона (7**********)
                - Тип авто [фургон, тент, изотерма, открытый]
                - Габариты (Д,В,Ш)
                - Максимальная грузоподъемность""";

    public static final String UNKNOWN_MESSAGE = EmojiParser.parseToUnicode(STOP_SIGN_EMOJI) + " Неизвестная команда!";
    public static final String FIELDS_IS_EMPTY = EmojiParser.parseToUnicode(GRIMACING_EMOJI) + " Некоторые поля не заполнены!";
    public static final String IS_REGISTERED = EmojiParser.parseToUnicode(WHITE_CHECK_MARK_EMOJI) + " Вы уже зарегистрированы. " +
            "Если планируете сменить профиль, обращайтесь к @hoiboui.";
    public static final String SUCCESSFUL_REGISTER = ", вы были успешно добавлены в базу.";
    public static final String SUCCESSFUL_PATCH = ", Ваш профиль был успешно изменён.";
}
