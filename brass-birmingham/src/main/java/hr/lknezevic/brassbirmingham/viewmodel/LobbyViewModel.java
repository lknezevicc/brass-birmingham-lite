package hr.lknezevic.brassbirmingham.viewmodel;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public final class LobbyViewModel {

    private final StringProperty statusMessage = new SimpleStringProperty("");
    private final StringProperty roomCode = new SimpleStringProperty("---");
    private final BooleanProperty buttonsDisabled = new SimpleBooleanProperty(false);

    public StringProperty statusMessageProperty() { return statusMessage; }
    public StringProperty roomCodeProperty() { return roomCode; }
    public BooleanProperty buttonsDisabledProperty() { return buttonsDisabled; }

    public void setStatus(String msg) { statusMessage.set(msg != null ? msg : ""); }
    public void setRoomCode(String code) { roomCode.set(code != null ? code : "---"); }
    public void setButtonsDisabled(boolean disabled) { buttonsDisabled.set(disabled); }
}
