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

    private boolean editSurnameState = false;
    private boolean editNameState = false;
    private boolean editPatronymicState = false;
    private boolean editPhoneState = false;
    private boolean editDriverFinal = false;

    private boolean registerNewCarState = false;
    private boolean registerCarStampState = false;
    private boolean registerCarNumberState = false;
    private boolean registerCarTypeState = false;
    private boolean registerCarLengthState = false;
    private boolean registerCarWeightState = false;
    private boolean registerCarHeightState = false;
    private boolean registerCarVolumeState = false;
    private boolean registerCarTypeOfLoadingState = false;
    private boolean registerCarLoadOpacityState = false;
    private boolean registerCarFinal = false;

}
