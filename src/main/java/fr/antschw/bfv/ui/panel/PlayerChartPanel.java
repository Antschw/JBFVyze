package fr.antschw.bfv.ui.panel;

import fr.antschw.bfv.domain.model.SessionStats;
import fr.antschw.bfv.application.util.I18nUtils;
import fr.antschw.bfv.domain.model.UserStats;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.Cursor;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Panel displaying charts of player statistics evolution during the session.
 * Avec légende cliquable et redimensionnement automatique du graphique.
 * Modifié pour calculer les statistiques de session basées sur le différentiel avec stats initiales.
 */
public class PlayerChartPanel extends VBox {
    private static final Logger LOGGER = LoggerFactory.getLogger(PlayerChartPanel.class);
    private final ResourceBundle bundle = I18nUtils.getBundle();

    private LineChart<Number, Number> chart;
    private final XYChart.Series<Number, Number> kdSeries = new XYChart.Series<>();
    private final XYChart.Series<Number, Number> kpmSeries = new XYChart.Series<>();
    private final XYChart.Series<Number, Number> hsPercentSeries = new XYChart.Series<>();

    // Composants de légende pour les séries
    private HBox kdLegendItem;
    private HBox kpmLegendItem;
    private HBox hsLegendItem;
    private Circle kdCircle;
    private Circle kpmCircle;
    private Circle hsCircle;

    // Map pour suivre la visibilité des séries
    private final Map<XYChart.Series<Number, Number>, Boolean> seriesVisibility = new HashMap<>();

    private Instant sessionStartTime;
    private UserStats initialStats; // Ajout des stats initiales

    // Couleurs personnalisées pour les séries
    private static final String KD_COLOR = "#4CAF50";  // Vert
    private static final String KPM_COLOR = "#2196F3"; // Bleu
    private static final String HS_COLOR = "#FFC107";  // Jaune/Orange

    // Nombre actuel de points dans le graphique
    private int currentPointCount = 0;

    // Intervalle de fetch en minutes (pour le calcul de l'axe X)
    private static final int FETCH_INTERVAL_MINUTES = 12; // Modifié à 12 minutes

    // Taille initiale de la vue en minutes
    private static final int INITIAL_VIEW_MINUTES = 36; // Modifié pour 3 points à 12 minutes

    public PlayerChartPanel() {
        LOGGER.info("Initializing PlayerChartPanel");
        this.setSpacing(5);
        this.setPadding(new Insets(5, 0, 5, 0));
        this.getStyleClass().add("stats-chart-panel");

        try {
            // Header label
            Label headerLabel = new Label(bundle.getString("stats.chart.title"));
            headerLabel.getStyleClass().add("header-label");

            // Set up the chart with fixed initial scale
            NumberAxis xAxis = new NumberAxis(0, INITIAL_VIEW_MINUTES, 12); // Modifié pour échelle de 12 minutes
            xAxis.setLabel(bundle.getString("stats.chart.x_axis"));
            xAxis.setTickLabelRotation(0);
            xAxis.setAnimated(false);

            NumberAxis yAxis = new NumberAxis();
            yAxis.setLabel(bundle.getString("stats.chart.y_axis"));
            yAxis.setAnimated(false);

            chart = new LineChart<>(xAxis, yAxis);
            chart.setTitle(null); // We use the header label instead
            chart.setAnimated(false);
            chart.setCreateSymbols(true);
            chart.setLegendVisible(false); // We'll create our own legend
            chart.setMinHeight(300);
            chart.setPrefHeight(300);

            // Initialize series with custom names
            kdSeries.setName(bundle.getString("stats.chart.kd"));
            kpmSeries.setName(bundle.getString("stats.chart.kpm"));
            hsPercentSeries.setName(bundle.getString("stats.chart.headshots"));

            // Initialiser map de visibilité
            seriesVisibility.put(kdSeries, true);
            seriesVisibility.put(kpmSeries, true);
            seriesVisibility.put(hsPercentSeries, true);

            // Créer une légende cliquable
            HBox legend = createLegend();

            // Add all elements to the panel
            VBox.setVgrow(chart, Priority.ALWAYS);
            this.getChildren().addAll(headerLabel, legend, chart);

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
                    chart
            );
        }
    }

    /**
     * Creates a custom legend for the chart with clickable items.
     */
    private HBox createLegend() {
        HBox legend = new HBox(20);
        legend.setPadding(new Insets(5, 0, 0, 10));
        legend.setAlignment(Pos.CENTER);

        // K/D legend item
        kdCircle = new Circle(6);
        kdCircle.setFill(Color.web(KD_COLOR));
        kdCircle.setStroke(Color.web(KD_COLOR).darker());
        kdCircle.setStrokeWidth(1);
        Label kdLabel = new Label(bundle.getString("stats.chart.kd"));
        kdLegendItem = new HBox(5, kdCircle, kdLabel);
        kdLegendItem.getStyleClass().add("legend-item");
        kdLegendItem.setCursor(Cursor.HAND);
        kdLegendItem.setOnMouseClicked(e -> toggleSeries(kdSeries, kdLegendItem, kdCircle));

        // KPM legend item
        kpmCircle = new Circle(6);
        kpmCircle.setFill(Color.web(KPM_COLOR));
        kpmCircle.setStroke(Color.web(KPM_COLOR).darker());
        kpmCircle.setStrokeWidth(1);
        Label kpmLabel = new Label(bundle.getString("stats.chart.kpm"));
        kpmLegendItem = new HBox(5, kpmCircle, kpmLabel);
        kpmLegendItem.getStyleClass().add("legend-item");
        kpmLegendItem.setCursor(Cursor.HAND);
        kpmLegendItem.setOnMouseClicked(e -> toggleSeries(kpmSeries, kpmLegendItem, kpmCircle));

        // Headshots legend item
        hsCircle = new Circle(6);
        hsCircle.setFill(Color.web(HS_COLOR));
        hsCircle.setStroke(Color.web(HS_COLOR).darker());
        hsCircle.setStrokeWidth(1);
        Label hsLabel = new Label(bundle.getString("stats.chart.headshots"));
        hsLegendItem = new HBox(5, hsCircle, hsLabel);
        hsLegendItem.getStyleClass().add("legend-item");
        hsLegendItem.setCursor(Cursor.HAND);
        hsLegendItem.setOnMouseClicked(e -> toggleSeries(hsPercentSeries, hsLegendItem, hsCircle));

        legend.getChildren().addAll(kdLegendItem, kpmLegendItem, hsLegendItem);

        return legend;
    }

    /**
     * Toggle visibility of a series by clicking on legend item.
     */
    private void toggleSeries(XYChart.Series<Number, Number> series, HBox legendItem, Circle legendCircle) {
        boolean visible = seriesVisibility.get(series);
        visible = !visible;
        seriesVisibility.put(series, visible);

        if (visible) {
            // Show series
            legendItem.setOpacity(1.0);
            legendCircle.setOpacity(1.0);
        } else {
            // Hide series
            legendItem.setOpacity(0.5);
            legendCircle.setOpacity(0.5);
        }

        // Update chart data based on visibility
        updateSeriesVisibility();
    }

    /**
     * Sets the start time of the session.
     */
    public void setSessionStartTime(Instant startTime) {
        this.sessionStartTime = startTime;
    }

    /**
     * Définit les statistiques initiales (de début de session).
     */
    public void setInitialStats(UserStats stats) {
        this.initialStats = stats;
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
     * Modifié pour calculer les métriques de session basées sur les stats initiales.
     */
    public void updateChart(List<SessionStats> history) {
        try {
            if (chart == null || history == null || history.isEmpty() || sessionStartTime == null || initialStats == null) {
                LOGGER.debug("Cannot update chart - missing session data");
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

            // Ajouter les données en utilisant les formules pour les calculs de session
            for (int i = 0; i < history.size(); i++) {
                SessionStats stats = history.get(i);

                // Position en X basée sur l'intervalle fixe entre les échantillons
                double minutesSinceStart = i * FETCH_INTERVAL_MINUTES;

                // Calculer le K/D de session (absolue de la différence pour éviter les valeurs négatives)
                int sessionKills = Math.abs(stats.kills() - initialStats.kills());
                int sessionDeaths = Math.abs(stats.deaths() - initialStats.deaths());
                double sessionKd = sessionDeaths > 0 ? (double) sessionKills / sessionDeaths : sessionKills;

                // Calculer le KPM basé sur le temps réel écoulé
                Duration timeSinceStart = Duration.between(sessionStartTime, stats.timestamp());
                double minutesElapsed = timeSinceStart.toSeconds() / 60.0;
                double sessionKpm = minutesElapsed > 0 ? (double) sessionKills / minutesElapsed : 0;

                // Calculer le % de headshots
                // Note: comme nous n'avons pas directement le nombre de headshots, on utilise le % global
                double headshots = parsePercentage(stats.headshots());

                // Ajouter les points au graphique
                kdSeries.getData().add(new XYChart.Data<>(minutesSinceStart, sessionKd));
                kpmSeries.getData().add(new XYChart.Data<>(minutesSinceStart, sessionKpm));
                hsPercentSeries.getData().add(new XYChart.Data<>(minutesSinceStart, headshots));
            }

            // Adapter l'axe des X en fonction de la durée de session
            adjustXAxisRange(history.size());

            // Mise à jour de l'affichage en fonction de la visibilité des séries
            updateSeriesVisibility();

            // Appliquer le style et les tooltips
            applySeriesStyles();
        } catch (Exception e) {
            LOGGER.error("Error updating chart", e);
        }
    }

    /**
     * Adjusts the X-axis range based on the number of data points.
     * Initially shows 36 minutes (3 points at 12min each), then expands as needed.
     */
    private void adjustXAxisRange(int dataPointCount) {
        if (chart == null) return;

        NumberAxis xAxis = (NumberAxis) chart.getXAxis();

        // Calculate minutes represented by all data points
        double totalMinutes = (dataPointCount - 1) * FETCH_INTERVAL_MINUTES;

        // Keep initial view of 36 minutes until we exceed that
        if (totalMinutes <= INITIAL_VIEW_MINUTES) {
            xAxis.setUpperBound(INITIAL_VIEW_MINUTES);
        } else {
            // Extend the view to show all data
            // Round up to next multiple of interval for cleaner display
            double upperBound = Math.ceil(totalMinutes / FETCH_INTERVAL_MINUTES) * FETCH_INTERVAL_MINUTES;
            xAxis.setUpperBound(upperBound);
        }
    }

    /**
     * Updates the visibility of chart series based on seriesVisibility map.
     */
    private void updateSeriesVisibility() {
        try {
            if (chart == null) {
                return;
            }

            chart.getData().clear();

            if (seriesVisibility.get(kdSeries)) {
                chart.getData().add(kdSeries);
            }

            if (seriesVisibility.get(kpmSeries)) {
                chart.getData().add(kpmSeries);
            }

            if (seriesVisibility.get(hsPercentSeries)) {
                chart.getData().add(hsPercentSeries);
            }
        } catch (Exception e) {
            LOGGER.error("Error updating series visibility", e);
        }
    }

    /**
     * Applies styles and tooltips to all series in the chart.
     */
    private void applySeriesStyles() {
        try {
            for (XYChart.Series<Number, Number> series : chart.getData()) {
                if (series == null || series.getNode() == null) continue;

                // Apply style to the series line
                if (series == kdSeries) {
                    series.getNode().setStyle("-fx-stroke: " + KD_COLOR + "; -fx-stroke-width: 2px;");
                } else if (series == kpmSeries) {
                    series.getNode().setStyle("-fx-stroke: " + KPM_COLOR + "; -fx-stroke-width: 2px;");
                } else if (series == hsPercentSeries) {
                    series.getNode().setStyle("-fx-stroke: " + HS_COLOR + "; -fx-stroke-width: 2px;");
                }

                // Apply style to each data point and add tooltips
                for (XYChart.Data<Number, Number> data : series.getData()) {
                    if (data.getNode() == null) continue;

                    String fillColor;
                    String seriesName;

                    if (series == kdSeries) {
                        fillColor = KD_COLOR;
                        seriesName = bundle.getString("stats.chart.kd");
                    } else if (series == kpmSeries) {
                        fillColor = KPM_COLOR;
                        seriesName = bundle.getString("stats.chart.kpm");
                    } else if (series == hsPercentSeries) {
                        fillColor = HS_COLOR;
                        seriesName = bundle.getString("stats.chart.headshots");
                    } else {
                        continue;
                    }

                    // Style the data point
                    data.getNode().setStyle(
                            "-fx-background-color: " + fillColor + ", white;" +
                                    "-fx-background-radius: 5px;" +
                                    "-fx-padding: 5px;"
                    );

                    // Add tooltip
                    double time = data.getXValue().doubleValue();
                    double value = data.getYValue().doubleValue();
                    String tooltip = String.format("%s: %.2f\n%s: %.1f %s",
                            seriesName, value,
                            bundle.getString("stats.chart.x_axis"), time,
                            bundle.getString("stats.session.minutes"));

                    javafx.scene.control.Tooltip.install(data.getNode(),
                            new javafx.scene.control.Tooltip(tooltip));
                }
            }
        } catch (Exception e) {
            LOGGER.debug("Error applying series styles", e);
        }
    }
}