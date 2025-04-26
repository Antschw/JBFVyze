package fr.antschw.bfv.ui.panel;

import fr.antschw.bfv.ui.control.table.PlayerNameLinkCell;
import fr.antschw.bfv.ui.control.table.PlayerTableRow;
import fr.antschw.bfv.application.util.I18nUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.beans.value.ObservableValue;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.List;

/**
 * Panel that shows a scrollable table of players, highlights suspicious ones,
 * and displays loading / error placeholders.
 */
public class PlayersPanel extends VBox {

    private final ResourceBundle bundle = I18nUtils.getBundle();
    private final TableView<PlayerTableRow> table = new TableView<>();
    private final ObservableList<PlayerTableRow> data = FXCollections.observableArrayList();
    private final Map<String,PlayerTableRow> rowMap = new HashMap<>();

    // Compteur de joueurs suspects
    private final Label countLabel = new Label("0");
    private final ProgressIndicator countSpinner = new ProgressIndicator();
    
    // Label pour le temps (référence externe)
    private Label timeLabel;

    public PlayersPanel() {
        this.setSpacing(4);  // Reduced spacing
        this.getStyleClass().add("players-panel");
        this.setPadding(new Insets(2, 0, 0, 0));  // Reduced padding

        // Créer l'en-tête avec le titre et le compteur
        Label headerLabel = new Label(bundle.getString("server.result.players"));
        headerLabel.getStyleClass().add("header-label");

        // Configuration du spinner
        countSpinner.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
        countSpinner.setPrefSize(16, 16);
        countSpinner.setVisible(false);

        // Configuration du compteur
        countLabel.getStyleClass().add("count-label");
        
        // Spacer pour pousser le temps à droite
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Mise en page horizontale pour l'en-tête avec temps à droite
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getChildren().addAll(headerLabel, countLabel, countSpinner, spacer);
        
        // Le timeLabel sera injecté plus tard via setTimeLabel()

        // Table setup
        table.setItems(data);
        table.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

        table.setPlaceholder(new Label(bundle.getString("server.players.empty")));

        // --- ID column (clickable link) ---
        TableColumn<PlayerTableRow,String> idCol = new TableColumn<>(bundle.getString("server.column.id"));
        idCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        idCol.setCellFactory(col -> new PlayerNameLinkCell());
        idCol.setPrefWidth(200);  // Initial preferred width for ID column

        // --- Rank column with bold if rank triggered ---
        TableColumn<PlayerTableRow,Number> rankCol = new TableColumn<>(bundle.getString("server.column.rank"));
        rankCol.setCellValueFactory(new PropertyValueFactory<>("rank"));
        rankCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item==null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item.toString());
                    PlayerTableRow row = getTableRow().getItem();
                    // bold if metrics list contains a "Rank =" entry
                    if (row!=null && row.getMetrics().stream().anyMatch(m -> m.startsWith("Rank"))) {
                        setStyle("-fx-font-weight:bold");
                    } else {
                        setStyle("");
                    }
                }
            }
        });
        rankCol.setPrefWidth(75);

        // --- K/D column with bold on K/D suspicious ---
        TableColumn<PlayerTableRow,Number> kdCol = new TableColumn<>(bundle.getString("server.column.kd"));
        kdCol.setCellValueFactory(new PropertyValueFactory<>("kd"));
        kdCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item==null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(String.format("%.2f", item.doubleValue()));
                    PlayerTableRow row = getTableRow().getItem();
                    if (row!=null && row.getMetrics().stream().anyMatch(m -> m.startsWith("K/D"))) {
                        setStyle("-fx-font-weight:bold");
                    } else {
                        setStyle("");
                    }
                }
            }
        });
        kdCol.setPrefWidth(75);

        // --- KPM column with bold on KPM suspicious ---
        TableColumn<PlayerTableRow,Number> kpmCol = new TableColumn<>(bundle.getString("server.column.kpm"));
        kpmCol.setCellValueFactory(new PropertyValueFactory<>("kpm"));
        kpmCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item==null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(String.format("%.2f", item.doubleValue()));
                    PlayerTableRow row = getTableRow().getItem();
                    if (row!=null && row.getMetrics().stream().anyMatch(m -> m.startsWith("KPM"))) {
                        setStyle("-fx-font-weight:bold");
                    } else {
                        setStyle("");
                    }
                }
            }
        });
        kpmCol.setPrefWidth(75);

        // --- Accuracy column with bold on Accuracy suspicious ---
        TableColumn<PlayerTableRow,String> accCol = new TableColumn<>(bundle.getString("server.column.accuracy"));
        accCol.setCellValueFactory(new PropertyValueFactory<>("accuracy"));
        accCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item==null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    PlayerTableRow row = getTableRow().getItem();
                    if (row!=null && row.getMetrics().stream().anyMatch(m -> m.startsWith("Accuracy"))) {
                        setStyle("-fx-font-weight:bold");
                    } else {
                        setStyle("");
                    }
                }
            }
        });
        accCol.setPrefWidth(75);

        table.getColumns().addAll(List.of(idCol, rankCol, kdCol, kpmCol, accCol));

        // Configure column resizing behavior
        table.widthProperty().addListener((ObservableValue<? extends Number> source, Number oldWidth, Number newWidth) -> {
            // Calculer l'espace disponible après avoir attribué à idCol ce dont elle a besoin
            double remainingSpace = newWidth.doubleValue() - idCol.getWidth() - 20; // 20px pour le scrollbar
            
            // Diviser l'espace restant équitablement entre les autres colonnes
            int remainingColumns = table.getColumns().size() - 1; // sans la colonne ID
            if (remainingColumns > 0 && remainingSpace > 0) {
                double columnWidth = remainingSpace / remainingColumns;
                
                rankCol.setPrefWidth(columnWidth);
                kdCol.setPrefWidth(columnWidth);
                kpmCol.setPrefWidth(columnWidth);
                accCol.setPrefWidth(columnWidth);
            }
        });

        // Ajuster la colonne ID en fonction du contenu le plus long
        idCol.widthProperty().addListener((obs, oldWidth, newWidth) -> {
            // Cette logique sera déclenchée à chaque changement de taille de la colonne
            // Vérifier si la colonne a besoin d'être élargie pour afficher tout le contenu
            if (data.size() > 0) {
                double requiredWidth = calculateRequiredIdWidth();
                if (requiredWidth > newWidth.doubleValue()) {
                    idCol.setPrefWidth(requiredWidth);
                }
            }
        });

        // Highlight entire row for any suspicious
        table.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(PlayerTableRow item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().removeAll("suspicious-player");
                if (item!=null && item.isSuspicious()) {
                    getStyleClass().add("suspicious-player");
                }
            }
        });

        VBox.setVgrow(table, Priority.ALWAYS);
        this.getChildren().addAll(header, table);
    }
    
    /**
     * Définit le label pour l'affichage du temps et l'ajoute à l'en-tête
     * 
     * @param timeLabel le label pour l'affichage du temps
     */
    public void setTimeLabel(Label timeLabel) {
        this.timeLabel = timeLabel;
        
        // Récupérer la HBox d'en-tête
        HBox header = (HBox) getChildren().get(0);
        
        // Ajouter le timeLabel à l'en-tête s'il n'y est pas déjà
        if (!header.getChildren().contains(timeLabel)) {
            header.getChildren().add(timeLabel);
        }
    }

    /**
     * Calcule la largeur requise pour la colonne ID basée sur le contenu le plus long.
     * 
     * @return la largeur recommandée en pixels
     */
    private double calculateRequiredIdWidth() {
        double maxWidth = 150; // Largeur minimale
        
        // Utilise une heuristique simple: 9 pixels par caractère + marge de 20px
        for (PlayerTableRow row : data) {
            String name = row.getName();
            if (name != null) {
                double requiredWidth = name.length() * 9 + 20;
                maxWidth = Math.max(maxWidth, requiredWidth);
            }
        }
        
        // Limiter à une taille maximale raisonnable
        return Math.min(maxWidth, 300);
    }

    /**
     * Appliquer les dimensions optimales aux colonnes
     */
    public void optimizeColumnWidths() {
        if (data.isEmpty()) return;
        
        // Pour la colonne ID
        double idWidth = calculateRequiredIdWidth();
        TableColumn<PlayerTableRow, ?> idColumn = table.getColumns().get(0);
        idColumn.setPrefWidth(idWidth);
        
        // Déclencher un recalcul des autres colonnes
        double width = table.getWidth();
        double remainingSpace = width - idWidth - 20; // 20px pour le scrollbar
        int remainingColumns = table.getColumns().size() - 1;
        
        if (remainingColumns > 0 && remainingSpace > 0) {
            double columnWidth = remainingSpace / remainingColumns;
            
            for (int i = 1; i < table.getColumns().size(); i++) {
                table.getColumns().get(i).setPrefWidth(columnWidth);
            }
        }
    }

    /**
     * Call before fetching begins to clear and show a bar in the *table area*.
     */
    public void startLoading() {
        data.clear();
        rowMap.clear();

        // Réinitialiser le compteur et afficher le spinner
        countLabel.setText("0");
        countSpinner.setVisible(true);

        // show an indeterminate ProgressBar *inside* the table area
        ProgressBar waiter = new ProgressBar(ProgressBar.INDETERMINATE_PROGRESS);
        waiter.setPrefWidth(200);
        table.setPlaceholder(waiter);
    }

    /**
     * Add a player row as soon as discovered.
     */
    public void addPlayer(String name) {
        PlayerTableRow row = new PlayerTableRow(name);
        data.add(row);
        rowMap.put(name, row);
    }

    /**
     * Add a player row with ID as soon as discovered.
     *
     * @param name the player's name
     * @param playerId the player's unique ID
     */
    public void addPlayer(String name, long playerId) {
        PlayerTableRow row = new PlayerTableRow(name, playerId);
        data.add(row);
        rowMap.put(name, row);
    }

    /**
     * Update a player's stats — or mark error if kd==null.
     * Also records the list of interesting metrics for per-cell highlighting.
     */
    public void updatePlayer(String name,
                             Double kd, Double kpm,
                             Integer rank, String accuracy,
                             List<String> metrics) {
        PlayerTableRow row = rowMap.get(name);
        if (row==null) return;

        if (kd==null) {
            row.setError(true);
            row.setAccuracy(bundle.getString("server.players.errorRow"));
        } else {
            row.setKd(kd);
            row.setKpm(kpm);
            row.setRank(rank);
            row.setAccuracy(accuracy);
            row.setMetrics(metrics);
            row.setSuspicious(!metrics.isEmpty());
        }

        // re-sort: suspicious first, then alpha
        data.sort(Comparator
                .comparing(PlayerTableRow::isSuspicious).reversed()
                .thenComparing(PlayerTableRow::getName, String.CASE_INSENSITIVE_ORDER)
        );

        // Mettre à jour le compteur de joueurs suspects
        long suspiciousCount = data.stream()
                .filter(PlayerTableRow::isSuspicious)
                .count();

        countLabel.setText(String.valueOf(suspiciousCount));
        
        // Optimiser les largeurs des colonnes quand tous les joueurs sont chargés
        optimizeColumnWidths();
    }

    /**
     * Update a player's ID after the initial row creation.
     *
     * @param name the player's name (key)
     * @param playerId the player's unique ID
     */
    public void updatePlayerId(String name, long playerId) {
        PlayerTableRow row = rowMap.get(name);
        if (row != null) {
            row.setPlayerId(playerId);
        }
    }

    /** Called when all fetching is done. */
    public void finishLoading() {
        table.setPlaceholder(new Label(bundle.getString("server.players.empty")));
        countSpinner.setVisible(false);
        
        // Optimiser les colonnes après le chargement complet
        optimizeColumnWidths();
    }
}