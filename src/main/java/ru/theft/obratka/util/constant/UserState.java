package ru.theft.obratka.util.constant;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserState {
    private boolean isUserAuthenticated = false;
    private boolean isUserRegistrationInProgress = false;
    private boolean isCarRegistrationInProgress = false;

    private boolean isUserSurnameRegistrationState = false;
    private boolean isUserNameRegistrationState = false;
    private boolean isUserPatronymicRegistrationState = false;
    private boolean isUserTelephoneNumberRegistrationState = false;

    private boolean isUserSurnamePatchState = false;
    private boolean isUserNamePatchState = false;
    private boolean isUserPatronymicPatchState = false;
    private boolean isUserTelephoneNumberPatchState = false;

    private boolean isCarStampRegistrationState = false;
    private boolean isCarNumberRegistrationState = false;
    private boolean isCarTypeBodyRegistrationState = false;
    private boolean isCarBodyLengthRegistrationState = false;
    private boolean isCarBodyWidthRegistrationState = false;
    private boolean isCarBodyHeightRegistrationState = false;
    private boolean isCarLoadOpacityRegistrationState = false;
}
