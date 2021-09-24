package at.hajszan.performancerunner.activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import at.hajszan.performancerunner.R;
import at.hajszan.performancerunner.persistence.Database.PRDatabase;
import at.hajszan.performancerunner.persistence.Entity.PerformanceRun;
import at.hajszan.performancerunner.utils.PerformanceRunAdapter;

/**
 * Shows a list of runs taken
 */
public class HistoryActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private ListView performanceRunListView;
    private PerformanceRunAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        performanceRunListView = (ListView) findViewById(R.id.listOfRuns);
        performanceRunListView.setOnItemClickListener(this);
        adapter = new PerformanceRunAdapter(this, new ArrayList<>());
        performanceRunListView.setAdapter(adapter);
    }

    @Override
    protected void onStart() {
        super.onStart();

        class CustomAsyncTask extends AsyncTask<Void, Void, List<PerformanceRun>> {

            @Override
            protected List<PerformanceRun> doInBackground(Void... voids) {
                List<PerformanceRun> runs = getDatabase().getPerformanceRunDao().getAll();
                Collections.reverse(runs);
                adapter.setRuns(runs);
                return runs;
            }

            protected void onPostExecute(List<PerformanceRun> result) {
                adapter.notifyDataSetChanged();
            }
        }

        new CustomAsyncTask().execute(null, null, null);

    }



    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        Intent intent = new Intent();
        intent.setClass(this, PerformanceRunDetailViewActivity.class);
        intent.putExtra("position", position);
        // Or / And
        PerformanceRun perfRun = (PerformanceRun) adapterView.getAdapter().getItem(position);
        intent.putExtra("PerformanceRunId", perfRun.getId());
        //adapterView.getAdapter().getItem(position);
        startActivity(intent);

    }

    public PRDatabase getDatabase() {
        return PRDatabase.getDatabase(this.getApplicationContext());
    }
}