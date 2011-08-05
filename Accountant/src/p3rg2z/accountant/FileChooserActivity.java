package p3rg2z.accountant;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class FileChooserActivity extends Activity {

    private String currentDir = "/mnt/sdcard";
    private ListView listView;


    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        listView = new ListView(this);
        setContentView(listView);
        goTo(currentDir);

        listView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> av, View v, int position, long id) {
                String basename = listView.getItemAtPosition(position).toString();
                if (basename.equals("..")) {
                    goTo(new File(currentDir).getParent());
                } else if (new File(currentDir + File.separator + basename).isDirectory()) {
                    goTo(currentDir + File.separator + basename);
                } else {
                    startActivity(new Intent().setClass(getApplicationContext(), CSVMapperActivity.class));
//                    setResult(RESULT_OK, new Intent().putExtra("path", currentDir + File.separator + basename));
//                    finish();
                }
            }
        });
    };

    private void goTo(String path) {
        currentDir = path;
        setTitle(path);
        listView.setAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, entries(path)));
    }

    private List<String> entries(String path) {
        List<String> entries = new ArrayList<String>();
        entries.add("..");
        for (File file : new File(path).listFiles()) {
            entries.add(file.getName());
        }
        return entries;
    };
};
