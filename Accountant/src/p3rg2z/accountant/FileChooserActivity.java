/*
   Copyright 2013 Peter Goetz

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

package p3rg2z.accountant;

import static java.io.File.separator;

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
                } else if (new File(currentDir + separator + basename).isDirectory()) {
                    goTo(currentDir + separator + basename);
                } else {
                    startActivity(new Intent().
                                      setClass(getApplicationContext(), CSVMapperActivity.class).
                                      putExtra("path", currentDir + separator + basename));
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
