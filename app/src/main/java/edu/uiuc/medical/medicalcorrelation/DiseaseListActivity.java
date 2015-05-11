package edu.uiuc.medical.medicalcorrelation;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import medical.ARMA;

/**
 * Created by manshu on 5/11/15.
 */
public class DiseaseListActivity extends Activity implements ListView.OnItemClickListener {

    ListView listView;
    String[] diseases;
    String file_name = "table2_ed";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_disease_list);
        Bundle bundle = getIntent().getExtras();
        file_name = bundle.getString("file");

        listView = (ListView) findViewById(R.id.listView);
        ReadCSVTask task = new ReadCSVTask();
        task.execute();
//        try {
//            task.execute().get();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        } catch (ExecutionException e) {
//            e.printStackTrace();
//        }
//
        listView.setOnItemClickListener(this);
    }

    private void initList() {
        diseases = new String[ARMA.disease_date_count_map.size()];
        List<String> disease_list = new ArrayList<>(ARMA.
                disease_date_count_map.keySet());
        Collections.sort(disease_list);
        int i = 0;
        for (String disease : disease_list) {
            diseases[i++] = disease;
        }

        listView.setAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, diseases));//new String[]{"abhinav", "saikat", "shyam"}));

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(this, GraphActivity.class);
        intent.putExtra("disease", diseases[position]);
        startActivity(intent);
    }

    private class ReadCSVTask extends AsyncTask <Void, Void, Void> {
        private ProgressDialog dialog;

        public ReadCSVTask() {
            dialog = new ProgressDialog(DiseaseListActivity.this);
        }


        @Override
        protected void onPreExecute() {
            dialog.setMessage("Reading disease data from file");
            dialog.show();
        }

        @Override
        protected void onPostExecute(Void result) {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
            initList();

        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                InputStream inputStream = getResources().openRawResource(getResources().
                        getIdentifier("raw/" + file_name, "raw", getPackageName()));
                ARMA.csvRead(inputStream, "Chief Complaint", "Presentation Date");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

    }
}
