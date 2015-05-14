package medical;

import com.github.mikephil.charting.data.Entry;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.*;
import java.util.List;

/**
 * Created by manish on 5/9/15.
 */

public class ARMA {

    public static Map<String, Map<Integer, Map<Integer, Integer>>> disease_date_count_map;

    private enum QuarterMap {
        Q1,
        Q2,
        Q3,
        Q4
    }

    public static void csvRead(InputStream inputStream, String disease_column, String date_column) throws IOException {
        disease_date_count_map = new LinkedHashMap<String, Map<Integer, Map<Integer, Integer>>>();

        Reader in = new InputStreamReader(inputStream);
        Iterable<CSVRecord> records = CSVFormat.EXCEL.parse(in);
        Iterator<CSVRecord> recordIterator = records.iterator();

        if (!recordIterator.hasNext()) return;

        Map<String, Integer> headersMap = new HashMap<String, Integer>();
        int header_index = 0;
        for (String header : recordIterator.next()) {
            headersMap.put(header, header_index++);
        }

        while (recordIterator.hasNext()) {
            CSVRecord record = recordIterator.next();
            String disease_type = record.get(headersMap.get(disease_column));
            String time = record.get(headersMap.get(date_column));
            DateTime dateTime = DateTimeFormat.forPattern("MM/dd/yyyy").parseDateTime(time);
            //System.out.println("time " + dateTime.getMonthOfYear() + " disease " + disease_type);
            int year = dateTime.getYear();
            int month = dateTime.getMonthOfYear();
            int date = dateTime.getDayOfMonth();
            int quarter = ((month - 1) / 3) + 1;

            if (!disease_date_count_map.containsKey(disease_type)) {
                disease_date_count_map.put(disease_type, new LinkedHashMap<Integer, Map<Integer, Integer>>());
            }
            if (!disease_date_count_map.get(disease_type).containsKey(year)) {
                disease_date_count_map.get(disease_type).put(year, new LinkedHashMap<Integer, Integer>());
            }
            if (!disease_date_count_map.get(disease_type).get(year).containsKey(quarter)) {
                disease_date_count_map.get(disease_type).get(year).put(quarter, 0);
            }
            int count = disease_date_count_map.get(disease_type).get(year).get(quarter);
            disease_date_count_map.get(disease_type).get(year).put(quarter, count + 1);
        }

        for (String disease : disease_date_count_map.keySet()) {
            if (!disease.toLowerCase().contains("cough")) continue;
            System.out.println("Plotting graph for " + disease);
        }
        in.close();
        inputStream.close();
    }

    public static GraphData getGraphData(String disease) {
        Map<Integer, Map<Integer, Integer>> year_month_count_map = disease_date_count_map.get(disease);
        int plot_count = 0;
        for (Integer year : year_month_count_map.keySet()) {
            for (Integer quarter : year_month_count_map.get(year).keySet()) {
                plot_count++;
            }
        }
        List<String> labels = new ArrayList<String>();
        List<Entry> entries = new ArrayList<Entry>();

        int i = 0;
        List<Integer> ylist = new ArrayList<Integer>(year_month_count_map.keySet());
        Collections.sort(ylist);
        for (Integer year : ylist) {
            List<Integer> mlist = new ArrayList<Integer>(year_month_count_map.get(year).keySet());
            Collections.sort(mlist);
            for (Integer quarter : mlist) {
                QuarterMap q1 = QuarterMap.values()[quarter - 1];
                String time = q1.toString() + "/" + year.toString();
                labels.add(time);
                entries.add(new Entry(year_month_count_map.get(year).get(quarter), i));
                i++;
            }
        }

        return new GraphData(labels, entries);
    }

    public static GraphData getPredictedData(String disease) {
        Map<Integer, Map<Integer, Integer>> year_quarter_count_map =
                disease_date_count_map.get(disease);
        int count = 0;
        for (int year : disease_date_count_map.get(disease).keySet()) {
            count += disease_date_count_map.get(disease).get(year).size();
        }

        Map<Integer, Map<Integer, Integer>> predicted_data = new LinkedHashMap<>();

        if (count <= 4) {
            System.out.println("Count is less than 4. Can't predict future");
            return null;
        }

        int original_count = count;

        int[] quarters = new int[count];
        int[] quarterNumbers = new int[count];
        double[] quarterInformation = new double[count];
        double[] periodMovingAverage = new double[count - 3];
        double[] centralMovingAverage = new double[count - 4];
        double[] seasonalIndex = new double[count - 4];

        List<Integer> ylist = new ArrayList<Integer>(year_quarter_count_map.keySet());
        Collections.sort(ylist);
        int endyear = ylist.get(ylist.size() - 1);
        count = 0;
        for (Integer year : ylist) {
            List<Integer> mlist = new ArrayList<Integer>(year_quarter_count_map.get(year).keySet());
            Collections.sort(mlist);
            for (Integer quarter : mlist) {
                quarterNumbers[count] = count + 1;
                QuarterMap q1 = QuarterMap.values()[quarter - 1];
                quarters[count] = quarter;
                quarterInformation[count] = year_quarter_count_map.get(year).get(quarter);
                count++;
            }
        }

        for (int i = 0; i < (quarterInformation.length - 3); i++) {
            int sum = 0;
            for (int j = i; j < i + 4; j++) sum += quarterInformation[j];
            periodMovingAverage[i] = sum / 4.0;
            //System.out.println("Period Moving Average " + i + " " + periodMovingAverage[i]);
        }

        for (int i = 0; i < periodMovingAverage.length - 1; i++) {
            centralMovingAverage[i] = (periodMovingAverage[i] + periodMovingAverage[i + 1]) / 2;
            seasonalIndex[i] = quarterInformation[i + 2] / centralMovingAverage[i];
//            System.out.println("Central Moving Average = " + centralMovingAverage[i]);
//            System.out.println("Seasonal Index " + i + " " + quarterInformation[i + 2] + " " + seasonalIndex[i]);
        }

        double[] seasonal_indexes = new double[4];
        double[] quarter_count = new double[4];
        for (int i = 0; i < seasonalIndex.length; i++) {
            quarter_count[quarters[i + 2] - 1]++;
            seasonal_indexes[quarters[i + 2] - 1] += seasonalIndex[i];
        }
        double seasonal_index_sum = 0.0;
        for (int i = 0; i < 4; i++) {
            seasonal_indexes[i] /= quarter_count[i];
            seasonal_index_sum += seasonal_indexes[i];
        }
        for (int i = 0; i < 4; i++) seasonal_indexes[i] /= (seasonal_index_sum / 4.0);

        int n = original_count - 4;
        double sigma_xy = 0.0, sigma_x = 0.0, sigma_y = 0.0, sigma_xx = 0.0;
        for (int i = 2; i < original_count - 2; i++) {
            int x = quarterNumbers[i];
            double y = centralMovingAverage[i - 2];
            sigma_x += x;
            sigma_y += y;
            sigma_xx += x * x;
            sigma_xy += x * y;
        }

        double slope_alpha = ((n * sigma_xy) - (sigma_x * sigma_y)) / ((n * sigma_xx) - (sigma_x * sigma_x));
        double offset_beta = (sigma_y - (slope_alpha * sigma_x)) / n;

        double[] predicted_quarter_number = new double[4];
        int ending_quarter = quarters[quarters.length - 1];
        double[] predicted_result = new double[4];

        if (!predicted_data.containsKey(endyear)) {
            predicted_data.put(endyear, new LinkedHashMap<Integer, Integer>());
        }
        predicted_data.get(endyear).put(ending_quarter,
                year_quarter_count_map.get(endyear).get(ending_quarter));

        for (int i = 0; i < 4; i++) {
            predicted_quarter_number[i] = quarterNumbers[quarterNumbers.length - 1] + i + 1;
            int quarter = (ending_quarter % 4) + 1;
            ending_quarter++;
            if (quarter == 1) {
                endyear++;
            }
            double si = seasonal_indexes[quarter - 1];
            double y = slope_alpha * predicted_quarter_number[i] + offset_beta;
            predicted_result[i] = y * si;
            System.out.println(predicted_result[i]);

            if (!predicted_data.containsKey(endyear)) {
                predicted_data.put(endyear, new LinkedHashMap<Integer, Integer>());
            }
            predicted_data.get(endyear).put(quarter, (int) predicted_result[i]);
        }

        int plot_count = 0;
        for (Integer year : predicted_data.keySet()) {
            for (Integer quarter : predicted_data.get(year).keySet()) {
                plot_count++;
            }
        }

        List<String> labels = new ArrayList<String>();
        List<Entry> entries = new ArrayList<Entry>();

        int i = 0;
        List<Integer> ylist2 = new ArrayList<Integer>(predicted_data.keySet());
        Collections.sort(ylist2);
        for (Integer year : ylist2) {
            List<Integer> mlist2 = new ArrayList<Integer>(predicted_data.get(year).keySet());
            Collections.sort(mlist2);
            for (Integer quarter : mlist2) {
                QuarterMap q1 = QuarterMap.values()[quarter - 1];
                String time = q1.toString() + "/" + year.toString();
                labels.add(time);
                entries.add(new Entry(predicted_data.get(year).get(quarter), i + original_count - 1));
                i++;
            }
        }

        return new GraphData(labels, entries);
    }

    public static void main(String[] args) throws IOException {
        //csvRead("Table2_ED.csv", "Chief Complaint", "Presentation Date");
    }


}
