package ru.theft.obratka.util.constant;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserState {
    private boolean isDriverAuthenticated = false;
    private boolean isRegisterProcessState = false;
    private boolean isPatchProcessState = false;
    private boolean isArrivalProcessState = false;

    private boolean registerSurnameState = false;
    private boolean registerNameState = false;
    private boolean registerPatronymicState = false;
    private boolean registerPhoneState = false;
    private boolean registerDriverFinal = false;
}
