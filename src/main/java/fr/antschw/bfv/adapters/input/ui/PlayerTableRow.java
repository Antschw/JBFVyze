package fr.antschw.bfv.adapters.input.ui;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * UI model class for displaying a single player’s stats in a TableView.
 */
public class PlayerTableRow {

    private final StringProperty name = new SimpleStringProperty();
    private final IntegerProperty rank = new SimpleIntegerProperty(0);
    private final DoubleProperty kd = new SimpleDoubleProperty(0.0);
    private final DoubleProperty kpm = new SimpleDoubleProperty(0.0);
    private final StringProperty accuracy = new SimpleStringProperty("");
    private final BooleanProperty suspicious = new SimpleBooleanProperty(false);
    private final BooleanProperty error = new SimpleBooleanProperty(false);

    /** Constructs a row with only the player’s name. Stats will be filled in later. */
    public PlayerTableRow(String name) {
        this.name.set(name);
    }

    // --- name ---
    public String getName() { return name.get(); }
    public StringProperty nameProperty() { return name; }

    // --- rank ---
    public int getRank() { return rank.get(); }
    public void setRank(int rank) { this.rank.set(rank); }
    public IntegerProperty rankProperty() { return rank; }

    // --- kd ---
    public double getKd() { return kd.get(); }
    public void setKd(double kd) { this.kd.set(kd); }
    public DoubleProperty kdProperty() { return kd; }

    // --- kpm ---
    public double getKpm() { return kpm.get(); }
    public void setKpm(double kpm) { this.kpm.set(kpm); }
    public DoubleProperty kpmProperty() { return kpm; }

    // --- accuracy ---
    public String getAccuracy() { return accuracy.get(); }
    public void setAccuracy(String accuracy) { this.accuracy.set(accuracy); }
    public StringProperty accuracyProperty() { return accuracy; }

    // --- suspicious flag ---
    public boolean isSuspicious() { return suspicious.get(); }
    public void setSuspicious(boolean suspicious) { this.suspicious.set(suspicious); }
    public BooleanProperty suspiciousProperty() { return suspicious; }

    // --- error flag ---
    public boolean isError() { return error.get(); }
    public void setError(boolean error) { this.error.set(error); }
    public BooleanProperty errorProperty() { return error; }
}
