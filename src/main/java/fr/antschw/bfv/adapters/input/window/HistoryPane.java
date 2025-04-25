package fr.antschw.bfv.adapters.input.window;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;

import java.util.LinkedHashSet;
import java.util.Set;

public class HistoryPane extends FlowPane {
    private final Set<String> seen = new LinkedHashSet<>();

    public HistoryPane() {
        this.setHgap(6);
        this.setVgap(6);
        this.setPadding(new Insets(4, 0, 4, 0));
    }

    /** Ajoute un nouveau badge si pas déjà présent. */
    public void addHistory(String id) {
        if (seen.add(id)) {
            Label badge = new Label("#" + id);
            badge.getStyleClass().add("history-badge");
            this.getChildren().add(badge);
        }
    }
}
