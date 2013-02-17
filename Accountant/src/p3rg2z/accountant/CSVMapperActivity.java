package p3rg2z.accountant;

import static android.widget.Toast.LENGTH_LONG;
import static p3rg2z.accountant.FormatUtil.reformatAsCanonicalDateTime;
import static p3rg2z.accountant.FormatUtil.reformatNumberAsCanonical;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import p3rg2z.accountant.Tables.Accounts;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import au.com.bytecode.opencsv.CSVReader;

public class CSVMapperActivity extends Activity {

    private TableLayout table;
    private File csvFile;
    private CheckBox ignoreFirstLineInput;
    private Spinner separator;
    private Spinner quoter;
    private Button importButton;
    private Spinner encoding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.csv_mapper);
        createData();
        initMembers();

        setUpImportButton();
        setUpIgnoreFirstLineInput();
        setUpSeparatorAndQuoter();
        setUpEncoding();
        setUpTable(readData());
    }

    private void setUpEncoding() {
        encoding.setAdapter(new ArrayAdapter<Object>(
                this,
                android.R.layout.simple_spinner_item,
                Charset.availableCharsets().keySet().toArray()) {{
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        }});
        encoding.setOnItemSelectedListener(new OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                setUpTable(readData());
            }

            public void onNothingSelected(AdapterView<?> av) {}
        });
    }

    private void initMembers() {
        importButton = (Button)findViewById(R.id.import_button);
        table = (TableLayout)findViewById(R.id.csv_table);
        ignoreFirstLineInput = (CheckBox)findViewById(R.id.ignore_first_line_input);
        separator = (Spinner)findViewById(R.id.delimiter_input);
        quoter = (Spinner)findViewById(R.id.quoter_input);
        csvFile = csvFileFrom(getIntent());
        encoding = (Spinner)findViewById(R.id.encoding_input);
    }

    @SuppressWarnings("unused")
    private void showLongToast(int stringId, Object... formatArgs) {
        Toast.makeText(this, getString(stringId, formatArgs), LENGTH_LONG).show();
    }

    private void showLongToast(String s ) {
        Toast.makeText(this, s, LENGTH_LONG).show();
    }


    private void setUpImportButton() {
        importButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                String error = cacheSpinnersAndReturnError();
                if (error != null) {
                    showLongToast(error);
                    return;
                }
                List<String[]> data = readData();
                for (String[] line : data) {
                    if (line.length != data.get(0).length) continue; // maybe empty line in CSV
                    try {
                        Data.instance().addNewBooking(reformatNumberAsCanonical(line[colForField.get(1)]),
                                                      validateText(line[colForField.get(2)]),
                                                      validateAccount(line[colForField.get(3)]),
                                                      validateAccount(line[colForField.get(4)]),
                                                      reformatAsCanonicalDateTime(line[colForField.get(5)]));
                    } catch (ParseException e) {
                        showLongToast("Could not read date or amount. Not imported "+ line[colForField.get(2)]);
                    } catch (TextEmptyException e) {
                        showLongToast("Text is empty. Not imported");
                    } catch (AccountInvalidException e) {
                        showLongToast("Account "+ e.getMessage() +" does not exist. Not imported "+ line[colForField.get(2)]);
                    }
                }
                startActivity(new Intent().setClass(getApplicationContext(), BookingsListActivity.class));
            }

            @SuppressWarnings("serial") class TextEmptyException extends Exception {}
            @SuppressWarnings("serial") class AccountInvalidException extends Exception {
                public AccountInvalidException(String string) { super(string); }
            }

            private String validateText(String string) throws TextEmptyException {
                if (string.length() == 0) throw new TextEmptyException();
                else return string;
            }

            private String validateAccount(String string) throws AccountInvalidException {
                for (Cursor c = Data.instance().allAccounts(); c.moveToNext();) {
                    if (c.getString(Accounts.NAME_INDEX).equalsIgnoreCase(string)) {
                        return c.getString(Accounts.NAME_INDEX);
                    }
                }
                throw new AccountInvalidException(string);
            }

            private Map<Integer, Integer> colForField;
            private int[] positionToTextResource = new int[] { R.string.unused,
                    R.string.amount, R.string.booking_text, R.string.source,
                    R.string.destination, R.string.date };

            private String cacheSpinnersAndReturnError() {
                colForField = new HashMap<Integer, Integer>(readData().get(0).length);
                for (int col = 0; col < readData().get(0).length; col++) {
                    if ((spinnerItemPos(col) != 0) && colForField.containsKey(spinnerItemPos(col))) {
                        return getString(R.string.could_not_start_import_specified_more_than_once,
                                getString(positionToTextResource[spinnerItemPos(col)]));
                    }
                    colForField.put(spinnerItemPos(col), col);
                }
                for (int field = 1; field <= 5; field++) {
                    if (!colForField.containsKey(field)) {
                        return getString(R.string.could_not_start_import_missing,
                                getString(positionToTextResource[field]));
                    }
                }
                return null;
            }
            private int spinnerItemPos(int i) {
                return ((Spinner)((TableRow)table.getChildAt(0)).getChildAt(i)).getSelectedItemPosition();
            }
        });
    }

    private void setUpIgnoreFirstLineInput() {
        ignoreFirstLineInput.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setUpTable(readData());
            }
        });
    }

    private void setUpSeparatorAndQuoter() {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, new String[] { ";", ",", ".", "|", "\t"  });
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        separator.setAdapter(adapter);
        separator.setOnItemSelectedListener(new OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                setUpTable(readData());
            }
            public void onNothingSelected(AdapterView<?> arg0) { }
        });
        ArrayAdapter<String> quoterAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, new String[] { "\"", "'" });
        quoterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        quoter.setAdapter(quoterAdapter);
        quoter.setOnItemSelectedListener(new OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                setUpTable(readData());
            }
            public void onNothingSelected(AdapterView<?> arg0) { }
        });

    }

    private void setUpTable(List<String[]> tableData) {
        table.removeAllViews();
        if (tableData == null) {
            importButton.setEnabled(false);
            TableRow row = new TableRow(this);
            TextView content = new TextView(this);
            content.setText("Invalid input.");
            row.addView(content);
            table.addView(row);
        } else if (tableData.isEmpty()) {
            importButton.setEnabled(false);
            TableRow row = new TableRow(this);
            TextView content = new TextView(this);
            content.setText("No records in CSV file.");
            row.addView(content);
            table.addView(row);
        } else {
            importButton.setEnabled(true);
            TableRow headRow = new TableRow(this);
            for (int i = 0; i < tableData.get(0).length; i++) {
                Spinner spinner = new Spinner(this);
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                        android.R.layout.simple_spinner_item, android.R.id.text1, new String[] {
                        getString(R.string.unused),
                        getString(R.string.amount),
                        getString(R.string.booking_text),
                        getString(R.string.source),
                        getString(R.string.destination),
                        getString(R.string.date),
                });
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(adapter);
                headRow.addView(spinner);
                spinner.setSelection(i < 6 ? i : 0);
            }
            table.addView(headRow);
            for (String[] line : tableData) {
                TableRow row = new TableRow(this);
                for (String cell : line) {
                    TextView content = new TextView(this);
                    content.setText(cell);
                    row.addView(content);
                }
                table.addView(row);
            }
        }
    }

    private List<String[]> readData() {
        try {

            return readDataFrom(csvFile,
                    separator.getSelectedItem().toString().charAt(0),
                    quoter.getSelectedItem().toString().charAt(0),
                    ignoreFirstLineInput.isChecked(),
                    encoding.getSelectedItem().toString());
        } catch (FileNotFoundException e) {
            showToastAndFinish("Could not find file "+ csvFile.getAbsolutePath());
            return null;
        } catch (ReadException e) {
            showToastAndFinish("Could not read file "+ csvFile.getAbsolutePath());
            return null;
        }
    }

    private void showToastAndFinish(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG);
        finish();
    }

    @SuppressWarnings("serial")
    private static class ReadException extends Exception {}

    private static List<String[]> readDataFrom(File file, char separator, char quoter, boolean ignoreFirstLine, String charsetName)
            throws ReadException, FileNotFoundException {

        CSVReader reader = new CSVReader(new InputStreamReader(new FileInputStream(file), Charset.forName(charsetName)), separator, quoter, ignoreFirstLine ? 1 : 0);
//        CSVReader reader = new CSVReader(new FileReader(file), separator, quoter, ignoreFirstLine ? 1 : 0);
        try {
            return reader.readAll();
        } catch (IOException e) {
            throw new ReadException();
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                // swallow exception here. What else can we do?
                e.printStackTrace();
            }
        }
    }

    protected File csvFileFrom(Intent intent) {
        return new File(intent.getStringExtra("path"));
    }

    protected void createData() {
    }
}
