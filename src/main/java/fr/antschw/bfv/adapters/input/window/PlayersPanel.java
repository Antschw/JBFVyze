package fr.antschw.bfv.adapters.input.window;

import fr.antschw.bfv.adapters.input.ui.PlayerTableRow;
import fr.antschw.bfv.utils.I18nUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Panel that shows a scrollable table of players, highlights suspicious ones,
 * and displays loading / error placeholders.
 */
public class PlayersPanel extends VBox {

    private final ResourceBundle bundle = I18nUtils.getBundle();
    private final ProgressBar loadingBar = new ProgressBar();
    private final TableView<PlayerTableRow> table = new TableView<>();
    private final ObservableList<PlayerTableRow> data = FXCollections.observableArrayList();
    private final Map<String,PlayerTableRow> rowMap = new HashMap<>();

    public PlayersPanel() {
        this.setSpacing(6);
        this.setPadding(new Insets(10,0,0,0));

        Label header = new Label(bundle.getString("server.result.players"));
        header.getStyleClass().add("header-label");

        // loading bar
        loadingBar.setVisible(false);
        loadingBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);

        // table setup
        table.setItems(data);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label(bundle.getString("server.players.empty")));
        table.getColumns().addAll(
                createColumn("server.column.id","name"),
                createColumn("server.column.rank","rank"),
                createColumn("server.column.kd","kd"),
                createColumn("server.column.kpm","kpm"),
                createColumn("server.column.accuracy","accuracy")
        );
        table.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(PlayerTableRow item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().remove("suspicious-player");
                if (item!=null && item.isSuspicious()) {
                    getStyleClass().add("suspicious-player");
                }
            }
        });

        ScrollPane scroll = new ScrollPane(table);
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(true);

        this.getChildren().addAll(header, loadingBar, scroll);
    }

    private <T> TableColumn<PlayerTableRow, T> createColumn(String key, String prop) {
        TableColumn<PlayerTableRow,T> col = new TableColumn<>(bundle.getString(key));
        col.setCellValueFactory(new PropertyValueFactory<>(prop));
        return col;
    }

    /** Called before fetching begins. */
    public void startLoading() {
        data.clear();
        rowMap.clear();
        loadingBar.setVisible(true);
        table.setPlaceholder(new Label(bundle.getString("server.players.loading")));
    }

    /** Add a new player row when discovered. */
    public void addPlayer(String name) {
        PlayerTableRow row = new PlayerTableRow(name);
        data.add(row);
        rowMap.put(name, row);
    }

    /** Update stats for a given player (or mark error if stats null). */
    public void updatePlayer(String name, Double kd, Double kpm, Integer rank, String accuracy, boolean suspicious) {
        PlayerTableRow row = rowMap.get(name);
        if (row == null) return;

        if (kd==null) {
            row.setError(true);
            row.setAccuracy(bundle.getString("server.players.errorRow"));
        } else {
            row.setKd(kd);
            row.setKpm(kpm);
            row.setRank(rank);
            row.setAccuracy(accuracy);
            row.setSuspicious(suspicious);
        }

        // re-sort: suspicious first, then by name
        data.sort(Comparator
                .comparing(PlayerTableRow::isSuspicious).reversed()
                .thenComparing(PlayerTableRow::getName, String.CASE_INSENSITIVE_ORDER)
        );
    }

    /** Called once fetching completes. */
    public void finishLoading() {
        loadingBar.setVisible(false);
        table.setPlaceholder(new Label(bundle.getString("server.players.empty")));
    }
}
