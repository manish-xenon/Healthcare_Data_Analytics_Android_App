package edu.uiuc.medical.medicalcorrelation;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.List;

import medical.ARMA;
import medical.GraphData;

/**
 * Created by manish on 5/11/15.
 */
public class GraphActivity extends ActionBarActivity {

    LineChart chart;
    String disease;
    String title;

    boolean arma_graph;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);
        Bundle bundle = getIntent().getExtras();
        chart = (LineChart) findViewById(R.id.chart);

        disease = "cough";
        title = "Plot of " + disease + " occurence per month";

        try {
            disease = bundle.getString("disease");
        } catch (NullPointerException npe) {
            System.out.println("Null Pointer Exception in sending intent");
        }

        arma_graph = false;
        createQuarterWiseGraph();
    }

    private void createQuarterWiseGraph() {
        GraphData graphData = ARMA.getGraphData(disease);

        chart.invalidate();

        LineDataSet dataset = new LineDataSet(graphData.getEntries(), title);

        List<String> labels = graphData.getLabels();

        //LineChart chart = new LineChart(this);
        //setContentView(chart);

        LineData data = new LineData(labels, dataset);
        chart.setData(data);

        chart.setDescription(title);

        dataset.setColors(ColorTemplate.COLORFUL_COLORS);
        chart.animateY(3000);

        //chart.saveToGallery("mychart.jpg", 85);
        arma_graph = false;
    }

    private void createARMAGraph() {
        GraphData graphData1 = ARMA.getGraphData(disease);
        GraphData graphData2 = ARMA.getPredictedData(disease);
        if (graphData2 == null) {
            Toast.makeText(getApplicationContext(), "Data insufficient to predict.", Toast.LENGTH_LONG)
                    .show();
            return;
        }

        chart.invalidate();


        LineDataSet dataset1 = new LineDataSet(graphData1.getEntries(), title);
        dataset1.setColor(Color.RED);
        LineDataSet dataset2 = new LineDataSet(graphData2.getEntries(), "prediction " + title);
        dataset2.setColor(Color.BLUE);

        List<LineDataSet> datasets = new ArrayList<>();
        datasets.add(dataset1);
        datasets.add(dataset2);
        List<String> labels = graphData1.getLabels();
        labels.addAll(graphData2.getLabels());


        //LineChart chart = new LineChart(this);
        //setContentView(chart);

        LineData data = new LineData(labels, datasets);
        chart.setData(data);

        chart.setDescription(title);

        //dataset.setColors(ColorTemplate.COLORFUL_COLORS);
        chart.animateY(3000);

        //chart.saveToGallery("mychart.jpg", 85);
        arma_graph = true;
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_disease, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.arma) {
            if (arma_graph) {
                Toast.makeText(getApplicationContext(), "Already showing ARMA Graph", Toast.LENGTH_LONG)
                        .show();
            } else {
                Toast.makeText(getApplicationContext(), "Showing ARMA Graph", Toast.LENGTH_LONG)
                        .show();
                createARMAGraph();
            }
            return true;
        } else if (id == R.id.quarter_wise) {
            if (!arma_graph) {
                Toast.makeText(getApplicationContext(), "Already Showing quarter wise Graph", Toast.LENGTH_LONG)
                    .show();
            } else {
                Toast.makeText(getApplicationContext(), "Showing quarter wise Graph", Toast.LENGTH_LONG)
                        .show();
                createQuarterWiseGraph();
            }

        } else if (id == R.id.save_image) {
            chart.saveToGallery("medical_chart-" + new Long(System.currentTimeMillis()).intValue() + ".jpg", 85);
        }
        return super.onOptionsItemSelected(item);
    }
}
