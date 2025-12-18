package com.potato.desktop.component;

import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;

public class NumberLineChart {
    private ChartType chartType;
    private LineChart<Number, Number> chart;
    private Double yUpperBound;  // null for varying
    private Double yLowerBound;
    private Double xUpperBound;
    private Double xLowerBound;  // null for varying
    private Double xUpperBoundMax;
    private NumberAxis xAxis;
    private NumberAxis yAxis;
    private double yUpperBoundMax;

    public NumberLineChart(ChartType chartType, LineChart lineChart, Double xUpperBound, Double xLowerBound, Double yUpperBound, Double yLowerBound) {
        this.chartType = chartType;
        this.chart = lineChart;
        this.xUpperBound = xUpperBound;
        this.xLowerBound = xLowerBound;
        this.xUpperBoundMax = xUpperBound;
        this.yUpperBound = yUpperBound;
        this.yLowerBound = yLowerBound;
        this.yUpperBoundMax = yUpperBound != null ? yUpperBound : yLowerBound;
        this.xAxis = (NumberAxis) chart.getXAxis();
        this.yAxis = (NumberAxis) chart.getYAxis();

        configureChart();
    }

    private void configureChart() {
        NumberAxis x = (NumberAxis) chart.getXAxis();
        x.setAutoRanging(false);
        if (xUpperBound != null) {
            x.setUpperBound(xUpperBound);
        }
        if (xLowerBound != null) {
            x.setLowerBound(xLowerBound);
        }
        x.setTickUnit(5);
//        x.setTickLabelsVisible(false);
//        x.setTickMarkVisible(false);

        NumberAxis y = (NumberAxis) chart.getYAxis();
        y.setAutoRanging(false);
        if (yLowerBound != null) {
            y.setLowerBound(yLowerBound);
        }
        if (yUpperBound != null) {
            y.setUpperBound(yUpperBound);
        }

        chart.setCreateSymbols(false);
        chart.setLegendVisible(true);
        chart.setAnimated(false);
    }

    public void setyUpperBoundByRadio(double yValue, double radio) {
        yUpperBoundMax = Math.max(yUpperBoundMax, yValue * radio);
        setyUpperBound(yUpperBoundMax);
    }

    public void setxAxisInRange(double xUpperBound) {
        if (xUpperBound > xUpperBoundMax) {
            xLowerBound = Math.max(0, 0 + xUpperBound - xUpperBoundMax);
            this.xAxis.setLowerBound(xLowerBound);
        }
        setxUpperBound(Math.max(xUpperBound, xUpperBoundMax));
    }

    public ChartType getChartType() {
        return chartType;
    }

    public LineChart<Number, Number> getChart() {
        return chart;
    }

    public Double getxLowerBound() {
        return xLowerBound;
    }

    public void setxLowerBound(Double xLowerBound) {
        this.xLowerBound = xLowerBound;
        this.xAxis.setLowerBound(xLowerBound);
    }

    public Double getxUpperBound() {
        return xUpperBound;
    }

    public void setxUpperBound(Double xUpperBound) {
        this.xUpperBound = xUpperBound;
        this.xAxis.setUpperBound(xUpperBound);
    }

    public Double getyLowerBound() {
        return yLowerBound;
    }

    public void setyLowerBound(Double yLowerBound) {
        this.yLowerBound = yLowerBound;
        this.yAxis.setLowerBound(yLowerBound);
    }

    public Double getyUpperBound() {
        return yUpperBound;
    }

    public void setyUpperBound(Double yUpperBound) {
        this.yLowerBound = yUpperBound;
        this.yAxis.setUpperBound(yUpperBound);
    }
}
