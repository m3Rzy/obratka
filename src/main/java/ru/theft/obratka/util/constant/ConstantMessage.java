package ru.theft.obratka.util.constant;

import com.vdurmont.emoji.EmojiParser;

import static ru.theft.obratka.util.constant.Emoji.*;

public class ConstantMessage {
    public static final String REGISTER_DRIVER = """
                 *Ниже необходимо заполнить данные по водителю и авто:*
                
                - ФИО (через пробел)
                - Номер телефона, начиная с 7
                - Тип авто (фургон, тент, изотерма, открытый)
                - Габариты (ДхВхШ)
                - Максимальная грузоподъемность (кг, без пробелов и символов)""";

    public static final String PATCH_DRIVER = """
                 *Ниже необходимо заполнить данные по водителю и авто:*
                
                - ФИО (через пробел)
                - Номер телефона, начиная с 7
                - Тип авто (фургон, тент, изотерма, открытый)
                - Габариты (ДхВхШ)
                - Максимальная грузоподъемность (кг, без пробелов и символов)
                
                Если Вы передумали менять данные, достаточно выбрать /obstop, либо выбрать любую кнопку ниже.""";

    public static final String UNKNOWN_MESSAGE = EmojiParser.parseToUnicode(STOP_SIGN_EMOJI) + " Неизвестная команда! Выберете кнопки ниже.";
    public static final String FIELDS_IS_EMPTY = EmojiParser.parseToUnicode(GRIMACING_EMOJI) + " Некоторые поля не заполнены!";
    public static final String IS_REGISTERED = EmojiParser.parseToUnicode(WHITE_CHECK_MARK_EMOJI) + " Вы уже зарегистрированы. " +
            "Поменять данные можно через _Мой профиль_.";
    public static final String SUCCESSFUL_REGISTER = ", вы были успешно добавлены в базу.";
    public static final String SUCCESSFUL_PATCH = ", Ваш профиль был успешно изменён.";
    public static final String STOP_PATCH = EmojiParser.parseToUnicode(WARNING_EMOJI) + " Вы отменили изменение данных.";
    public static final String STOP_ARRIVAL = EmojiParser.parseToUnicode(WARNING_EMOJI) + " Вы отменили добавление маршрута.";
    public static final String REGISTER_ARRIVAL = """
                 *Ниже необходимо заполнить данные по Вашему текущему маршруту:*
                
                - Откуда
                - Куда
                - Примерная дата прибытия
                
                Если Вы передумали добавлять маршрут, достаточно выбрать /obstop, либо выбрать любую кнопку ниже.""";
    public static final String SHARE_ARRIVAL = EmojiParser.parseToUnicode(WHITE_CHECK_MARK_EMOJI) + " Маршрут отправлен логисту.";

    public static final String TEXT_REGISTER_SURNAME_DRIVER = EmojiParser.parseToUnicode(GREY_QUESTION) + " Напишите Вашу фамилию";
    public static final String VALID_DRIVER_SURNAME_EMPTY = EmojiParser.parseToUnicode(WARNING_EMOJI)
            + " Фамилия не может быть пустой!";
    public static final String VALID_DRIVER_SURNAME_OVER = EmojiParser.parseToUnicode(WARNING_EMOJI)
            + " Фамилия должна содержать не более 255 символов!";

    public static final String TEXT_REGISTER_NAME_DRIVER = EmojiParser.parseToUnicode(GREY_QUESTION) + " Напишите Ваше имя";
    public static final String VALID_DRIVER_NAME_EMPTY = EmojiParser.parseToUnicode(WARNING_EMOJI)
            + " Имя не может быть пустым!";
    public static final String VALID_DRIVER_NAME_OVER = EmojiParser.parseToUnicode(WARNING_EMOJI)
            + " Имя должно содержать не более 255 символов!";

    public static final String TEXT_REGISTER_PATRONYMIC_DRIVER = EmojiParser.parseToUnicode(GREY_QUESTION) + " Напишите Ваше отчество (при наличие)";
    public static final String VALID_DRIVER_PATRONYMIC_OVER = EmojiParser.parseToUnicode(WARNING_EMOJI)
            + " Отчество должно содержать не более 255 символов!";

    public static final String TEXT_REGISTER_PHONE_DRIVER = EmojiParser.parseToUnicode(GREY_QUESTION) + " Напишите Ваш номер телефона без символов +7 и 8";
    public static final String VALID_DRIVER_PHONE_EMPTY = EmojiParser.parseToUnicode(WARNING_EMOJI)
            + " Номер телефона не может быть пустым!";
    public static final String VALID_DRIVER_PHONE_OVER = EmojiParser.parseToUnicode(WARNING_EMOJI)
            + " Номер телефона должен содержать только 10 символов!";
}
