package ru.theft.obratka.util.constant;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserState {
    private boolean isUserAuthenticated = false;
    private boolean isUserRegistrationInProgress = false;

    private boolean isUserSurnameRegistrationState = false;
    private boolean isUserNameRegistrationState = false;
    private boolean isUserPatronymicRegistrationState = false;
    private boolean isUserTelephoneNumberRegistrationState = false;

    private boolean isUserSurnamePatchState = false;
    private boolean isUserNamePatchState = false;
    private boolean isUserPatronymicPatchState = false;
    private boolean isUserTelephoneNumberPatchState = false;
}
