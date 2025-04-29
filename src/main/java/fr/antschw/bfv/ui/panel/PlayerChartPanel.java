package fr.antschw.bfv.ui.panel;

import fr.antschw.bfv.domain.model.SessionStats;
import fr.antschw.bfv.application.util.I18nUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Panel displaying charts of player statistics evolution during the session.
 * Improved with better legend and larger height, avec correction des couleurs.
 */
public class PlayerChartPanel extends VBox {
    private static final Logger LOGGER = LoggerFactory.getLogger(PlayerChartPanel.class);
    private final ResourceBundle bundle = I18nUtils.getBundle();

    // Modifié pour ne plus être final
    private LineChart<Number, Number> chart;
    private final XYChart.Series<Number, Number> kdSeries = new XYChart.Series<>();
    private final XYChart.Series<Number, Number> kpmSeries = new XYChart.Series<>();
    private final XYChart.Series<Number, Number> hsPercentSeries = new XYChart.Series<>();

    private final CheckBox kdCheckbox = new CheckBox();
    private final CheckBox kpmCheckbox = new CheckBox();
    private final CheckBox hsCheckbox = new CheckBox();

    private Instant sessionStartTime;

    // Couleurs personnalisées pour les séries
    private static final String KD_COLOR = "#4CAF50";  // Vert
    private static final String KPM_COLOR = "#2196F3"; // Bleu
    private static final String HS_COLOR = "#FFC107";  // Jaune/Orange

    // Nombre actuel de points dans le graphique
    private int currentPointCount = 0;

    public PlayerChartPanel() {
        LOGGER.info("Initializing PlayerChartPanel");
        this.setSpacing(5);
        this.setPadding(new Insets(5, 0, 5, 0));
        this.getStyleClass().add("stats-chart-panel");

        try {
            // Header label
            Label headerLabel = new Label(bundle.getString("stats.chart.title"));
            headerLabel.getStyleClass().add("header-label");

            // Set up the chart
            NumberAxis xAxis = new NumberAxis(0, 60, 10); // Commence à 0
            xAxis.setLabel(bundle.getString("stats.chart.x_axis"));
            xAxis.setTickLabelRotation(0); // Éviter rotation

            NumberAxis yAxis = new NumberAxis();
            yAxis.setLabel(bundle.getString("stats.chart.y_axis"));

            chart = new LineChart<>(xAxis, yAxis);
            chart.setTitle(null); // We use the header label instead
            chart.setAnimated(false);
            chart.setCreateSymbols(true);
            chart.setLegendVisible(false); // We'll create our own legend
            chart.setMinHeight(300); // Plus grand
            chart.setPrefHeight(300);

            // Initialize series with custom names
            kdSeries.setName(bundle.getString("stats.chart.kd"));
            kpmSeries.setName(bundle.getString("stats.chart.kpm"));
            hsPercentSeries.setName(bundle.getString("stats.chart.headshots"));

            // Créer une légende personnalisée
            HBox legend = createLegend();

            // Set up checkboxes to toggle series visibility
            kdCheckbox.setText(bundle.getString("stats.chart.kd"));
            kdCheckbox.setSelected(true);
            kdCheckbox.setOnAction(e -> updateSeriesVisibility());

            kpmCheckbox.setText(bundle.getString("stats.chart.kpm"));
            kpmCheckbox.setSelected(true);
            kpmCheckbox.setOnAction(e -> updateSeriesVisibility());

            hsCheckbox.setText(bundle.getString("stats.chart.headshots"));
            hsCheckbox.setSelected(true);
            hsCheckbox.setOnAction(e -> updateSeriesVisibility());

            // Add checkboxes to a horizontal box
            HBox controls = new HBox(15);
            controls.setPadding(new Insets(5, 0, 0, 10));
            controls.getChildren().addAll(kdCheckbox, kpmCheckbox, hsCheckbox);

            // Add all elements to the panel
            VBox.setVgrow(chart, Priority.ALWAYS);
            this.getChildren().addAll(headerLabel, legend, chart, controls);

            LOGGER.info("PlayerChartPanel initialized successfully");
        } catch (Exception e) {
            LOGGER.error("Error initializing PlayerChartPanel", e);

            // En cas d'échec d'initialisation du graphique, créer un graphique vide comme secours
            if (chart == null) {
                NumberAxis fallbackXAxis = new NumberAxis();
                NumberAxis fallbackYAxis = new NumberAxis();
                chart = new LineChart<>(fallbackXAxis, fallbackYAxis);
                chart.setTitle("Chart initialization failed");
            }

            // Nettoyer et afficher un message d'erreur
            this.getChildren().clear();
            this.getChildren().addAll(
                    new Label("Chart could not be initialized: " + e.getMessage()),
                    chart  // Ajouter quand même le graphique, même s'il est vide
            );
        }
    }

    /**
     * Creates a custom legend for the chart.
     */
    private HBox createLegend() {
        HBox legend = new HBox(20);
        legend.setPadding(new Insets(5, 0, 0, 10));
        legend.setAlignment(Pos.CENTER);

        // K/D legend item
        Circle kdCircle = new Circle(6);
        kdCircle.setFill(Color.web(KD_COLOR));
        kdCircle.setStroke(Color.web(KD_COLOR).darker());
        kdCircle.setStrokeWidth(1);
        HBox kdItem = new HBox(5, kdCircle, new Label(bundle.getString("stats.chart.kd")));

        // KPM legend item
        Circle kpmCircle = new Circle(6);
        kpmCircle.setFill(Color.web(KPM_COLOR));
        kpmCircle.setStroke(Color.web(KPM_COLOR).darker());
        kpmCircle.setStrokeWidth(1);
        HBox kpmItem = new HBox(5, kpmCircle, new Label(bundle.getString("stats.chart.kpm")));

        // Headshots legend item
        Circle hsCircle = new Circle(6);
        hsCircle.setFill(Color.web(HS_COLOR));
        hsCircle.setStroke(Color.web(HS_COLOR).darker());
        hsCircle.setStrokeWidth(1);
        HBox hsItem = new HBox(5, hsCircle, new Label(bundle.getString("stats.chart.headshots")));

        legend.getChildren().addAll(kdItem, kpmItem, hsItem);
        return legend;
    }

    /**
     * Sets the start time of the session.
     */
    public void setSessionStartTime(Instant startTime) {
        this.sessionStartTime = startTime;
    }

    /**
     * Formats a headshot percentage string (e.g., "20.5%") as a double.
     */
    private double parsePercentage(String percentage) {
        try {
            // Remove % symbol and parse as double
            return Double.parseDouble(percentage.replace("%", ""));
        } catch (NumberFormatException | NullPointerException e) {
            LOGGER.debug("Error parsing percentage: {}", percentage, e);
            return 0.0;
        }
    }

    /**
     * Updates the chart with session history data.
     * Optimisé pour ne redessiner que si le nombre de points a changé.
     */
    public void updateChart(List<SessionStats> history) {
        try {
            if (chart == null || history == null || history.isEmpty() || sessionStartTime == null) {
                return;
            }

            // Vérifier si le nombre de points a changé
            if (history.size() == currentPointCount) {
                LOGGER.debug("No new data points, skipping chart update");
                return;
            }

            LOGGER.debug("Updating chart with {} data points (was {})", history.size(), currentPointCount);
            currentPointCount = history.size();

            // Clear existing data
            kdSeries.getData().clear();
            kpmSeries.getData().clear();
            hsPercentSeries.getData().clear();

            // Add data points - uniquement les données de session
            // Pour ne pas montrer les statistiques globales
            if (history.size() >= 2) {
                SessionStats first = history.get(0);

                for (int i = 0; i < history.size(); i++) {
                    SessionStats stats = history.get(i);

                    // Calculate minutes since session start - commence à 0
                    double minutesSinceStart = (stats.timestamp().toEpochMilli() - sessionStartTime.toEpochMilli()) / 60000.0;

                    // Calculer les valeurs de session, pas les globales
                    double sessionKD = 0;
                    double sessionKPM = 0;
                    double sessionHSPercent = 0;

                    int sessionKills = stats.kills() - first.kills();
                    int sessionDeaths = stats.deaths() - first.deaths();

                    // K/D de session
                    sessionKD = sessionDeaths > 0 ? (double) sessionKills / sessionDeaths : sessionKills;

                    // KPM de session basé sur le temps de jeu réel
                    long sessionSeconds = stats.secondsPlayed() - first.secondsPlayed();
                    sessionKPM = sessionSeconds > 0 ? (double) sessionKills / (sessionSeconds / 60.0) : 0;

                    // Calculer le pourcentage de headshots de session si possible
                    // Pour l'instant on garde le global car nous n'avons pas cette donnée par session
                    sessionHSPercent = parsePercentage(stats.headshots());

                    // Add data to series
                    kdSeries.getData().add(new XYChart.Data<>(minutesSinceStart, sessionKD));
                    kpmSeries.getData().add(new XYChart.Data<>(minutesSinceStart, sessionKPM));
                    hsPercentSeries.getData().add(new XYChart.Data<>(minutesSinceStart, sessionHSPercent));
                }
            } else {
                // S'il n'y a qu'un seul point, on montre les stats globales à 0 minutes
                SessionStats stats = history.get(0);
                double minutesSinceStart = 0;

                kdSeries.getData().add(new XYChart.Data<>(minutesSinceStart, stats.killDeath()));
                kpmSeries.getData().add(new XYChart.Data<>(minutesSinceStart, stats.killsPerMinute()));
                hsPercentSeries.getData().add(new XYChart.Data<>(minutesSinceStart, parsePercentage(stats.headshots())));
            }

            updateSeriesVisibility();
        } catch (Exception e) {
            LOGGER.error("Error updating chart", e);
        }
    }

    /**
     * Updates the visibility of chart series based on checkbox state.
     */
    private void updateSeriesVisibility() {
        try {
            if (chart == null) {
                return;
            }

            chart.getData().clear();

            if (kdCheckbox.isSelected()) {
                chart.getData().add(kdSeries);
            }

            if (kpmCheckbox.isSelected()) {
                chart.getData().add(kpmSeries);
            }

            if (hsCheckbox.isSelected()) {
                chart.getData().add(hsPercentSeries);
            }

            // Apply styling
            applySeriesTooltips();
            applySeriesStyling();
        } catch (Exception e) {
            LOGGER.error("Error updating series visibility", e);
        }
    }

    /**
     * Applies tooltips to data points.
     */
    private void applySeriesTooltips() {
        try {
            if (chart == null) {
                return;
            }

            // Apply tooltips to each data point
            for (XYChart.Series<Number, Number> series : chart.getData()) {
                if (series == null) continue;

                String seriesName = series.getName();

                for (XYChart.Data<Number, Number> data : series.getData()) {
                    if (data == null || data.getNode() == null) continue;

                    double time = data.getXValue().doubleValue();
                    double value = data.getYValue().doubleValue();

                    String tooltip = String.format("%s: %.2f\nTime: %.1f min", seriesName, value, time);
                    javafx.scene.control.Tooltip.install(data.getNode(), new javafx.scene.control.Tooltip(tooltip));
                }
            }
        } catch (Exception e) {
            LOGGER.debug("Error applying tooltips", e);
        }
    }

    /**
     * Applies CSS styling to series.
     */
    private void applySeriesStyling() {
        try {
            if (chart == null) {
                return;
            }

            for (XYChart.Series<Number, Number> series : chart.getData()) {
                if (series == null || series.getNode() == null) continue;

                // Appliquer style direct plutôt que classes CSS
                if (series == kdSeries) {
                    series.getNode().setStyle("-fx-stroke: " + KD_COLOR + "; -fx-stroke-width: 2px;");
                } else if (series == kpmSeries) {
                    series.getNode().setStyle("-fx-stroke: " + KPM_COLOR + "; -fx-stroke-width: 2px;");
                } else if (series == hsPercentSeries) {
                    series.getNode().setStyle("-fx-stroke: " + HS_COLOR + "; -fx-stroke-width: 2px;");
                }

                // Appliquer le style aux points de données
                for (XYChart.Data<Number, Number> data : series.getData()) {
                    if (data.getNode() != null) {
                        String fillColor;

                        if (series == kdSeries) {
                            fillColor = KD_COLOR;
                        } else if (series == kpmSeries) {
                            fillColor = KPM_COLOR;
                        } else if (series == hsPercentSeries) {
                            fillColor = HS_COLOR;
                        } else {
                            continue;
                        }

                        // Appliquer le style directement
                        data.getNode().setStyle(
                                "-fx-background-color: " + fillColor + ", white;" +
                                        "-fx-background-radius: 5px;" +
                                        "-fx-padding: 5px;"
                        );
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.debug("Error applying series styling", e);
        }
    }
}