package p3rg2z.accountant;

import java.text.DateFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static p3rg2z.accountant.Tables.*;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class NewBookingActivity extends Activity {
    
    private EditText textInput;
    private Spinner bookingTypeInput;
    private Spinner sourceInput;
    private Spinner destInput;
    private Button createBookingButton;
    private EditText amountInput;

    String[] BOOKING_TYPES;
    private Data data;
    private Button textChooserButton;
    private Button dateInput;

    public static enum BookingType {
        EXPENSE, INCOME, TRANSACTION
    }
    
    protected Data getData() {
        return Data.create(getApplicationContext(), getExternalFilesDir(null));
    }
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.newbooking);
        Toast.makeText(getApplicationContext(), "onCreate", Toast.LENGTH_LONG).show(); 

        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Toast.makeText(getApplicationContext(), "SD card not mounted", Toast.LENGTH_LONG).show();
            return;
        }

        initMembers();

        if (isInTestMode()) {
            setTitleColor(0xffff0000);
            setTitle(getTitle() + " TEST MODE");
        }
        
        setUpBookingTypeButton();
        setUpBookingButton();
        setUpTextChooserButton();
        setUpDateInput();
    }

    private void initMembers() {
        amountInput = (EditText)findViewById(R.id.amount);
        textInput = (EditText)findViewById(R.id.text_edit);
        bookingTypeInput = (Spinner)findViewById(R.id.booking_type_spinner);
        sourceInput = (Spinner) findViewById(R.id.source_spinner);
        destInput = (Spinner) findViewById(R.id.dest_spinner);
        createBookingButton = (Button)findViewById(R.id.create_booking_button);
        textChooserButton = (Button)findViewById(R.id.text_chooser_button);
        dateInput = (Button)findViewById(R.id.date_input);

        BOOKING_TYPES = new String[] { 
                getString(R.string.booking_type_out), 
                getString(R.string.booking_type_in), 
                getString(R.string.booking_type_transaction)};
        
        data = getData();
    }
    
    private void setUpBookingTypeButton() {
        ArrayAdapter<CharSequence> bookingTypeAdapter = new ArrayAdapter<CharSequence>(this, 
                android.R.layout.simple_spinner_item, BOOKING_TYPES);
        bookingTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        
        final SimpleCursorAdapter banksAdapter = createAccountAdapter(AccountType.BANK);
        final SimpleCursorAdapter destCategoriesAdapter = createAccountAdapter(AccountType.DEST_CATEGORY);
        final SimpleCursorAdapter incomeSourceAdapter = createAccountAdapter(AccountType.INCOME_SOURCE);
        final SimpleCursorAdapter allAccountsAdapter = createAccountAdapter(null);
        
        bookingTypeInput.setOnItemSelectedListener(new OnItemSelectedListener() {

            public void onItemSelected(AdapterView<?> paramAdapterView,
                    View paramView, int index, long paramLong) {

                if (index == BookingType.EXPENSE.ordinal()) {
                    ((TextView) findViewById(R.id.source_label)).setText(R.string.bank);
                    ((TextView) findViewById(R.id.dest_label)).setText(R.string.category);
                    sourceInput.setAdapter(banksAdapter);
                    destInput.setAdapter(destCategoriesAdapter);
                } else if (index == BookingType.INCOME.ordinal()) {
                    ((TextView) findViewById(R.id.source_label)).setText(R.string.income_source);
                    ((TextView) findViewById(R.id.dest_label)).setText(R.string.bank);
                    sourceInput.setAdapter(incomeSourceAdapter);
                    destInput.setAdapter(banksAdapter);
                } else if (index == BookingType.TRANSACTION.ordinal()) {
                    ((TextView) findViewById(R.id.source_label)).setText(R.string.source);
                    ((TextView) findViewById(R.id.dest_label)).setText(R.string.destination);
                    sourceInput.setAdapter(allAccountsAdapter);
                    destInput.setAdapter(allAccountsAdapter);
                } else {
                    throw new UnsupportedOperationException();
                }
            }

            public void onNothingSelected(AdapterView<?> paramAdapterView) {
                throw new UnsupportedOperationException();
            }
        });

        bookingTypeInput.setAdapter(bookingTypeAdapter);
    }

    private SimpleCursorAdapter createAccountAdapter(AccountType type) {
        SimpleCursorAdapter result;
        if (type == null) {
            result = new SimpleCursorAdapter(this, R.layout.accountlistitem, 
                    data.queryAllAccounts(),
                    new String[] { Accounts.NAME }, new int[] { R.id.account_name });
        } else {
            result = new SimpleCursorAdapter(this, R.layout.accountlistitem, 
                    data.queryType(type),
                    new String[] { Accounts.NAME }, new int[] { R.id.account_name });
        }
        result.setDropDownViewResource(R.layout.accountdropdownitem);

        return result;
    }
    
    private void setUpBookingButton() {
        createBookingButton.setOnClickListener(new OnClickListener() {
            public void onClick(View paramView) {
                String amount = amountInput.getText().toString();
                Pattern p = Pattern.compile("(\\d*)[\\.,]?([0-9]{0,2})");
                Matcher m = p.matcher(amount);
                
                if (m.matches()) {
                    String first = m.group(1);
                    String second = m.group(2);
                    String amountValidated = first + "." + second;
                    data.insert(amountValidated, 
                            textInput.getText().toString(),
                            ((Cursor)sourceInput.getSelectedItem()).getString(Accounts.NAME_INDEX), 
                            ((Cursor)destInput.getSelectedItem()).getString(Accounts.NAME_INDEX),
                            java.util.Calendar.getInstance().getTime().toLocaleString());

                    Toast.makeText(getApplicationContext(), String.format("Saved new booking \"%s\": %s", 
                            textInput.getText().toString(), amountValidated), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), "INVALID", Toast.LENGTH_LONG).show();
                }
                
            }
        });
    }
    
    private void setUpTextChooserButton() {
        textChooserButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                startActivityForResult(new Intent("skjdhfkjdshfkjs").
                    setComponent(new ComponentName("p3rg2z.accountant", "p3rg2z.accountant.TextChooseActivity")), 
                    3);
            }
        });
    }


    void setUpDateInput() {
        dateInput.setText(DateFormat.getDateInstance(DateFormat.LONG).format(
                Calendar.getInstance().getTime()));
        dateInput.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                showDialog(0);
            }
        });
    }
    
    @Override
    protected Dialog onCreateDialog(int id) {
        return new DatePickerDialog(this, new OnDateSetListener() {
            public void onDateSet(DatePicker paramDatePicker, int year, int month, int day) {
                dateInput.setText(DateFormat.getDateInstance(DateFormat.LONG).format(
                        new GregorianCalendar(year, month, day).getTime()));
            }
        }, 
        Calendar.getInstance().get(Calendar.YEAR),
        Calendar.getInstance().get(Calendar.MONTH),
        Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
    }

    protected boolean isInTestMode() {
        return false;
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        textInput.setText(data.getAction());
        Toast.makeText(getApplicationContext(), ""+ requestCode + " " + resultCode + " " + data, Toast.LENGTH_LONG).show();
    }
      
    
}