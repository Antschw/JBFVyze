package fr.antschw.bfv.ui.panel;

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
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;

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

    public PlayersPanel() {
        this.setSpacing(6);
        this.setPadding(new Insets(10,0,0,0));

        // Créer l'en-tête avec le titre et le compteur
        Label headerLabel = new Label(bundle.getString("server.result.players"));
        headerLabel.getStyleClass().add("header-label");

        // Configuration du spinner
        countSpinner.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
        countSpinner.setPrefSize(16, 16);
        countSpinner.setVisible(false);

        // Configuration du compteur
        countLabel.getStyleClass().add("count-label");

        // Mise en page horizontale pour l'en-tête
        HBox header = new HBox(10, headerLabel, countLabel, countSpinner);
        header.setAlignment(Pos.CENTER_LEFT);

        // Table setup
        table.setItems(data);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        table.setPlaceholder(new Label(bundle.getString("server.players.empty")));

        // --- ID column (simple) ---
        TableColumn<PlayerTableRow,String> idCol = new TableColumn<>(bundle.getString("server.column.id"));
        idCol.setCellValueFactory(new PropertyValueFactory<>("name"));

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

        table.getColumns().addAll(idCol, rankCol, kdCol, kpmCol, accCol);

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
    }

    /** Called when all fetching is done. */
    public void finishLoading() {
        table.setPlaceholder(new Label(bundle.getString("server.players.empty")));
        countSpinner.setVisible(false);
    }
}
