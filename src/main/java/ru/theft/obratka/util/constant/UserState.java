package ru.theft.obratka.util.constant;

public class UserState {
    private boolean isDriverAuthenticated = false;
    private boolean isRegisterProcessState = false;
    private boolean isPatchProcessState = false;
    private boolean isArrivalProcessState = false;

    public boolean isDriverAuthenticated() {
        return isDriverAuthenticated;
    }

    public void setDriverAuthenticated(boolean driverAuthenticated) {
        isDriverAuthenticated = driverAuthenticated;
    }

    public boolean isRegisterProcessState() {
        return isRegisterProcessState;
    }

    public void setRegisterProcessState(boolean registerProcessState) {
        isRegisterProcessState = registerProcessState;
    }

    public boolean isPatchProcessState() {
        return isPatchProcessState;
    }

    public void setPatchProcessState(boolean patchProcessState) {
        isPatchProcessState = patchProcessState;
    }

    public boolean isArrivalProcessState() {
        return isArrivalProcessState;
    }

    public void setArrivalProcessState(boolean arrivalProcessState) {
        isArrivalProcessState = arrivalProcessState;
    }
}
