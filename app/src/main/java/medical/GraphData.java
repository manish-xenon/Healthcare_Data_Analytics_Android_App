package medical;

import com.github.mikephil.charting.data.Entry;

import java.util.List;

/**
 * Created by manish on 5/11/15.
 */
public class GraphData {

    List<String> labels;
    List<Entry> entries;

    public GraphData(List<String> labels, List<Entry> entries) {
        this.labels = labels;
        this.entries = entries;
    }

    public List<String> getLabels() {
        return labels;
    }

    public void setLabels(List<String> labels) {
        this.labels = labels;
    }

    public List<Entry> getEntries() {
        return entries;
    }

    public void setEntries(List<Entry> entries) {
        this.entries = entries;
    }
}
