package stats;

import org.jfree.chart.*;
import org.jfree.chart.plot.*;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.chart.axis.CategoryLabelPositions;

import java.awt.*;
import java.util.Map;

public class ChartFactoryUtil {

    public static JFreeChart createBarChart(
            String title, String xLabel, String yLabel, Map<String, Integer> data) {

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (Map.Entry<String, Integer> e : data.entrySet()) {
            dataset.addValue(e.getValue(), "Valeurs", e.getKey());
        }

        JFreeChart chart = ChartFactory.createBarChart(
                title, xLabel, yLabel, dataset,
                PlotOrientation.VERTICAL, false, true, false
        );

        // Améliorations visuelles
        chart.setBackgroundPaint(Color.WHITE);
        chart.getTitle().setFont(new Font("Segoe UI", Font.BOLD, 16));
        
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(new Color(200, 200, 200));
        
        // Rotation des labels pour éviter les chevauchements
        if (data.size() > 5) {
            plot.getDomainAxis().setCategoryLabelPositions(
                CategoryLabelPositions.createUpRotationLabelPositions(Math.PI / 6.0)
            );
        }
        
        // Couleur des barres
        plot.getRenderer().setSeriesPaint(0, new Color(52, 152, 219));

        return chart;
    }

    public static JFreeChart createPieChart(String title, Map<String, Integer> data) {

        DefaultPieDataset dataset = new DefaultPieDataset();

        for (Map.Entry<String, Integer> e : data.entrySet()) {
            dataset.setValue(e.getKey(), e.getValue());
        }

        JFreeChart chart = ChartFactory.createPieChart(title, dataset, true, true, false);
        
        // Améliorations visuelles
        chart.setBackgroundPaint(Color.WHITE);
        chart.getTitle().setFont(new Font("Segoe UI", Font.BOLD, 16));
        
        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setBackgroundPaint(Color.WHITE);
        
        return chart;
    }
}