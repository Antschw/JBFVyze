package fr.antschw.bfv.ui.control.table;

import fr.antschw.bfv.application.util.BrowserUtils;
import javafx.css.PseudoClass;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableRow;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Custom TableCell that renders a player name as a clickable link.
 */
public class PlayerNameLinkCell extends TableCell<PlayerTableRow, String> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PlayerNameLinkCell.class);
    private static final PseudoClass LINK_PSEUDO_CLASS = PseudoClass.getPseudoClass("link");
    
    public PlayerNameLinkCell() {
        super();
        
        // Apply link styling
        getStyleClass().add("player-link-cell");
        
        // Handle click events
        setOnMouseClicked(this::handleMouseClick);
        
        // Handle hover effect
        setOnMouseEntered(e -> pseudoClassStateChanged(LINK_PSEUDO_CLASS, true));
        setOnMouseExited(e -> pseudoClassStateChanged(LINK_PSEUDO_CLASS, false));
    }
    
    @Override
    protected void updateItem(String name, boolean empty) {
        super.updateItem(name, empty);
        
        if (empty || name == null) {
            setText(null);
            setGraphic(null);
            pseudoClassStateChanged(LINK_PSEUDO_CLASS, false);
        } else {
            setText(name);
            setCursor(javafx.scene.Cursor.HAND);
            
            // La couleur est définie par les styles CSS et hérite du thème
            // Aucune coloration spécifique n'est appliquée ici pour préserver
            // la cohérence avec le thème (mode clair ou sombre)
        }
    }
    
    private void handleMouseClick(MouseEvent event) {
        if (event.getButton() == MouseButton.PRIMARY) {
            TableRow<PlayerTableRow> row = getTableRow();
            if (row != null && row.getItem() != null) {
                PlayerTableRow playerRow = row.getItem();
                long playerId = playerRow.getPlayerId();
                
                if (playerId > 0) {
                    LOGGER.info("Opening BFVHackers profile for player: {} (ID: {})", 
                            playerRow.getName(), playerId);
                    BrowserUtils.openPlayerProfile(playerId);
                } else {
                    LOGGER.warn("Cannot open profile for player {} - invalid ID: {}", 
                            playerRow.getName(), playerId);
                }
            }
        }
    }
}