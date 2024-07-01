package ru.theft.obratka.core;

import com.vdurmont.emoji.EmojiParser;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.theft.obratka.car.dto.CarDto;
import ru.theft.obratka.car.model.Car;
import ru.theft.obratka.car.service.CarService;
import ru.theft.obratka.driver.dto.DriverDto;
import ru.theft.obratka.driver.model.Driver;
import ru.theft.obratka.driver.service.DriverService;
import ru.theft.obratka.util.constant.Emoji;
import ru.theft.obratka.util.constant.UserState;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ru.theft.obratka.util.constant.ConstantMessage.*;

@Service
@AllArgsConstructor
public class BotService {

    public static HashMap<Long, DriverDto> driverFields = new HashMap<>();
    public static HashMap<Long, CarDto> carFields = new HashMap<>();

    // Регулярное выражение для формата госномера
    private static final String CAR_NUMBER_REGEX = "^[АВЕКМНОРСТУХ]{1}\\d{3}[АВЕКМНОРСТУХ]{2}\\d{2,3}$";
    // Регулярное выражение для валидации фамилии и имени
    private static final String FIO_REGEX = "^[A-ZА-ЯЁ][a-zа-яё\\-']*$";
    // Регулярное выражение для валидации марки грузового авто
    private static final String TRUCK_BRAND_REGEX = "^[A-ZА-ЯЁ][a-zа-яёA-ZА-ЯЁ0-9\\-\\s]*$";
    // Регулярное выражение для валидации номера телефона
    private static final String PHONE_NUMBER_REGEX = "^\\d{10}$";

    private DriverService driverService;
    private CarService carService;

    protected InlineKeyboardButton createInlineKeyboardButton(String text, String callbackData) {
        return InlineKeyboardButton.builder()
                .text(text)
                .callbackData(callbackData)
                .build();
    }

    protected SendMessage createInlineKeyboardMarkup(String text, long chatId, List<InlineKeyboardButton> buttons) {
        InlineKeyboardMarkup keyboardMarkup = InlineKeyboardMarkup
                .builder()
                .keyboardRow(buttons)
                .build();
        SendMessage message = new SendMessage();
        message.enableHtml(true);
        message.enableMarkdownV2(true);
        message.enableMarkdown(true);
        message.setChatId(chatId);
        message.setText(text);
        message.setReplyMarkup(keyboardMarkup);
        return message;
    }

    protected SendMessage createSendMessage(Long chatId, String text) {
        SendMessage message = new SendMessage();
        message.enableHtml(true);
        message.enableMarkdownV2(true);
        message.enableMarkdown(true);
        message.setChatId(chatId);
        message.setText(text);
        return message;
    }

    protected SendMessage createReplyKeyboardMarkup(long chatId, String text) {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        SendMessage message = new SendMessage();
        message.setReplyMarkup(replyKeyboardMarkup);
        replyKeyboardMarkup.setSelective(false);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);

        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow keyboardFirstRow = new KeyboardRow();
        keyboardFirstRow.add(new KeyboardButton(EmojiParser
                .parseToUnicode(Emoji.BUST_IN_SILHOUETTE_EMOJI) + " Мой профиль"));
        keyboardFirstRow.add(new KeyboardButton(EmojiParser
                .parseToUnicode(Emoji.TRUCK_EMOJI) + " Список авто"));

        KeyboardRow keyboardSecondRow = new KeyboardRow();
        keyboardSecondRow.add(new KeyboardButton(EmojiParser
                .parseToUnicode(Emoji.MOTOR_WAY) + " Поделиться маршрутом"));
        keyboardSecondRow.add(new KeyboardButton(EmojiParser
                .parseToUnicode(Emoji.NEW_EMOJI) + " Добавить авто"));

        keyboard.add(keyboardFirstRow);
        keyboard.add(keyboardSecondRow);

        replyKeyboardMarkup.setKeyboard(keyboard);
        message.enableHtml(true);
        message.enableMarkdownV2(true);
        message.enableMarkdown(true);
        message.setChatId(chatId);
        message.setText(text);
        return message;
    }

    protected void createCarFromString(UserState state, String text, TelegramBotCore bot, long userId, long chatId)
            throws TelegramApiException {

        if (state.isCarStampRegistrationState()) {
            if (!isValidTruckBrand(text)) {
                throw new RuntimeException("Проблема с валидацией марки авто. \nМарка должна начинаться с заглавной буквы" +
                        " (латинской или кириллической) и может содержать буквы, цифры, дефисы и пробелы.");
            }
            CarDto carDto = new CarDto();
            carFields.put(userId, carDto);
            carFields.get(userId).setCarStamp(text);
            state.setCarStampRegistrationState(false);
            state.setCarNumberRegistrationState(true);
            bot.execute(createSendMessage(chatId, "Укажите госномер авто с регионом"));
            return;
        }

        if (state.isCarNumberRegistrationState()) {
            if (!isValidCarNumber(text)) {
                throw new RuntimeException("Проблема с валидацией госномера. \nНомер авто должен соотвествовать " +
                        "российскому формату госномера: одна буква, три цифры, две буквы и две или три цифры.");
            }
            if (carService.getCars().stream().anyMatch(f -> f.getCarNumber().equals(text))) {
                throw new RuntimeException(ERROR_CAR_NUMBER_ALREADY_HAS);
            }
            carFields.get(userId).setCarNumber(text);
            state.setCarNumberRegistrationState(false);
            state.setCarTypeBodyRegistrationState(true);
            bot.execute(createInlineKeyboardMarkup("Выберете тип кузова", chatId, List.of(
                    createInlineKeyboardButton("Фургон", "car-van"),
                    createInlineKeyboardButton("Тент", "car-tent"),
                    createInlineKeyboardButton("Изотермический", "car-isothermal"),
                    createInlineKeyboardButton("Открытый", "car-open")
            )));
            return;
        }

        if (state.isCarBodyLengthRegistrationState()) {
            if (!isValidCarBody(text, 10.0)) {
                throw new RuntimeException("Проблема с валидацией длины кузова. " +
                        "\nДлина должна быть числом и не превышать 10 м.");
            }
            carFields.get(userId).setCarLength(String.valueOf(Double.parseDouble(text)));
            state.setCarBodyLengthRegistrationState(false);
            state.setCarBodyWidthRegistrationState(true);
            bot.execute(createSendMessage(chatId, "Укажите ширину кузова в метрах (цифра после запятой обозначется <.>)"));
            return;
        }

        if (state.isCarBodyWidthRegistrationState()) {
            if (!isValidCarBody(text, 3.0)) {
                throw new RuntimeException("Проблема с валидацией ширины кузова. " +
                        "\nШирина должна быть числом и не превышать 3 м.");
            }
            carFields.get(userId).setCarWidth(String.valueOf(Double.parseDouble(text)));
            state.setCarBodyWidthRegistrationState(false);
            state.setCarBodyHeightRegistrationState(true);
            bot.execute(createSendMessage(chatId, "Укажите высоту кузова в метрах (цифра после запятой обозначется <.>)"));
            return;
        }

        if (state.isCarBodyHeightRegistrationState()) {
            if (!isValidCarBody(text, 3.0)) {
                throw new RuntimeException("Проблема с валидацией высоты кузова. " +
                        "\nВысота должна быть числом и не превышать 3 м.");
            }
            carFields.get(userId).setCarHeight(String.valueOf(Double.parseDouble(text)));
            state.setCarBodyHeightRegistrationState(false);
            double volume = Double.parseDouble(carFields.get(userId).getCarLength()) *
                    Double.parseDouble(carFields.get(userId).getCarWidth()) *
                    Double.parseDouble(carFields.get(userId).getCarHeight());
            BigDecimal bd = new BigDecimal(volume).setScale(1, RoundingMode.FLOOR);
            carFields.get(userId).setCarVolume(bd.toString());
            state.setCarLoadOpacityRegistrationState(true);
            bot.execute(createSendMessage(chatId, "Укажите грузоподъемность в кг."));
            return;
        }

        if (state.isCarLoadOpacityRegistrationState()) {
            if (!isValidCarBody(text, 20000.0)) {
                throw new RuntimeException("Проблема с валидацией грузоподъемности авто. " +
                        "\nГрузоподъемность должна быть числом и не превышать 20 т.");
            }
            carFields.get(userId).setCarLoadOpacity(text);
            state.setCarLoadOpacityRegistrationState(false);
            carService.add(
                    Car.builder()
                            .carStamp(carFields.get(userId).getCarStamp())
                            .carNumber(carFields.get(userId).getCarNumber())
                            .typeCarBody(carFields.get(userId).getCarType())
                            .carLength(carFields.get(userId).getCarLength())
                            .carWidth(carFields.get(userId).getCarWidth())
                            .carHeight(carFields.get(userId).getCarHeight())
                            .carVolume(carFields.get(userId).getCarVolume())
                            .carLoadOpacity(carFields.get(userId).getCarLoadOpacity())
                            .driver(driverService.getByTgId(String.valueOf(userId)).get())
                            .createdAt(LocalDateTime.now())
                            .build()
            );
            state.setCarRegistrationInProgress(false);
            state.setCarLoadOpacityRegistrationState(false);
            bot.execute(createReplyKeyboardMarkup(chatId, SUCCESSFUL_CAR_REGISTER));
            clearCarFields(userId);
        }
    }

    protected void createDriverFromString(UserState state, String text, TelegramBotCore bot, long userId, long chatId)
            throws TelegramApiException {

        if (state.isUserSurnameRegistrationState()) {
            if (!isValidFio(text)) {
                throw new RuntimeException("Проблема с валидацией фамилии. \nФамилия должна начинаться с заглавной буквы" +
                        " (латинской или кириллической), а затем может содержать строчные буквы, дефисы и апострофы.");
            }
            if (text.length() > 80) {
                throw new RuntimeException("Фамилия не может превышать 80 символов!");
            }
            DriverDto driverDto = new DriverDto();
            driverFields.put(userId, driverDto);
            driverFields.get(userId).setSurname(text);
            state.setUserSurnameRegistrationState(false);
            state.setUserNameRegistrationState(true);
            bot.execute(createSendMessage(userId, TEXT_REGISTER_NAME_DRIVER));
            return;
        }

        if (state.isUserNameRegistrationState()) {
            if (!isValidFio(text)) {
                throw new RuntimeException("Проблема с валидацией имени. \nИмя должно начинаться с заглавной буквы" +
                        " (латинской или кириллической), а затем может содержать строчные буквы, дефисы и апострофы.");
            }
            if (text.length() > 80) {
                throw new RuntimeException("Имя не может превышать 80 символов!");
            }
            driverFields.get(userId).setName(text);
            state.setUserNameRegistrationState(false);
            state.setUserPatronymicRegistrationState(true);
            bot.execute(createSendMessage(userId, TEXT_REGISTER_PATRONYMIC_DRIVER));
            return;
        }

        if (state.isUserPatronymicRegistrationState()) {
            if (!isValidFio(text)) {
                throw new RuntimeException("Проблема с валидацией отчества. \nОтчество должно начинаться с заглавной буквы" +
                        " (латинской или кириллической), а затем может содержать строчные буквы, дефисы и апострофы.");
            }
            if (text.length() > 80) {
                throw new RuntimeException("Отчество не может превышать 80 символов!");
            }
            driverFields.get(userId).setPatronymic(text);
            state.setUserPatronymicRegistrationState(false);
            state.setUserTelephoneNumberRegistrationState(true);
            bot.execute(createSendMessage(userId, TEXT_REGISTER_PHONE_DRIVER));
            return;
        }

        if (state.isUserTelephoneNumberRegistrationState()) {
            if (!isValidPhoneNumber(text)) {
                throw new RuntimeException("Проблема с валидацией номера телефона. " +
                        "\nНомер телефона должен состоять ровно из 10 цифр без +7 и 8 и т.д.");
            }
            if (driverService.getAll()
                    .stream()
                    .anyMatch(f -> f.getTelephone().equals("7" + text))) {
                throw new RuntimeException("Пользователь с таким номером телефона уже зарегестрирован! Укажите другой номер.");
            }

            driverFields.get(userId).setTelephone("7" + text);
            state.setUserTelephoneNumberRegistrationState(false);
            driverService.add(
                    Driver.builder()
                            .tgId(String.valueOf(userId))
                            .fio(driverFields.get(userId).getSurname() + " " + driverFields.get(userId).getName()
                                    + " " + driverFields.get(userId).getPatronymic())
                            .telephone(driverFields.get(userId).getTelephone())
                            .createdAt(LocalDateTime.now())
                            .build()
            );
            state.setUserAuthenticated(true);
            state.setUserRegistrationInProgress(false);
            bot.execute(createReplyKeyboardMarkup(chatId, SUCCESSFUL_DRIVER_REGISTER));
            clearDriverFields(userId);
        }
    }

    protected SendMessage showProfile(Long tgId, Long chatId) {
        Driver driver = driverService.getByTgId(tgId.toString())
                .orElseThrow(() -> new RuntimeException("Такого водителя не существует!"));

        return createInlineKeyboardMarkup(
                driver.toTerminal(),
                chatId,
                List.of(createInlineKeyboardButton("Изменить данные " + EmojiParser
                        .parseToUnicode(Emoji.WRITING_HAND_EMOJI), "edit")));
    }

    protected SendMessage showCars(Long tgId, Long chatId) {
        Driver driver = driverService.getByTgId(tgId.toString())
                .orElseThrow(() -> new RuntimeException("Такого водителя не существует!"));
        List<InlineKeyboardButton> buttons = new ArrayList<>();
        for (Car car : carService.getCarsByDriverId(tgId)) {
            buttons.add(createInlineKeyboardButton(car.getCarNumber(), car.getCarNumber()));
        }
        if (buttons.isEmpty()) {
            throw new RuntimeException(LIST_OF_CARS_EMPTY);
        }
        return createInlineKeyboardMarkup(
                "Парк водителя " + driver.getFio(),
                chatId,
                buttons);
    }

    protected void clearDriverFields(long userId) {
        driverFields.remove(userId);
    }

    protected void clearCarFields(long userId) {
        carFields.remove(userId);
    }

    // Метод для валидации госномера
    private boolean isValidCarNumber(String carNumber) {
        if (carNumber == null || carNumber.isEmpty()) {
            return false;
        }

        Pattern pattern = Pattern.compile(CAR_NUMBER_REGEX);
        Matcher matcher = pattern.matcher(carNumber);

        return matcher.matches();
    }

    // Метод для валидации фамилии
    private boolean isValidFio(String fio) {
        if (fio == null || fio.isEmpty()) {
            return false;
        }

        Pattern pattern = Pattern.compile(FIO_REGEX);
        Matcher matcher = pattern.matcher(fio);

        return matcher.matches();
    }

    // Метод для валидации номера телефона
    private boolean isValidPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            return false;
        }

        Pattern pattern = Pattern.compile(PHONE_NUMBER_REGEX);
        Matcher matcher = pattern.matcher(phoneNumber);

        return matcher.matches();
    }

    // Метод для валидации марки грузового авто
    private boolean isValidTruckBrand(String brand) {
        if (brand == null || brand.isEmpty()) {
            return false;
        }

        Pattern pattern = Pattern.compile(TRUCK_BRAND_REGEX);
        Matcher matcher = pattern.matcher(brand);

        return matcher.matches();
    }

    // Метод для валидации кузова
    private boolean isValidCarBody(String bodyLength, double maxValue) {
        if (bodyLength == null || bodyLength.isEmpty()) {
            return false;
        }
        try {
            // Преобразование строки в число с плавающей точкой
            double length = Double.parseDouble(bodyLength);

            // Проверка, что длина находится в пределах от 0 до 10 метров включительно
            return length >= 0.0 && length <= maxValue;
        } catch (NumberFormatException e) {
            // Если строка не может быть преобразована в число, возвращаем false
            return false;
        }
    }
}
