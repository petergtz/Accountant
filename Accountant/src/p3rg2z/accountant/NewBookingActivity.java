package p3rg2z.accountant;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import p3rg2z.accountant.AccountancyContentProvider.AccountType;
import p3rg2z.accountant.AccountancyContentProvider.Accounts;
import p3rg2z.accountant.AccountancyContentProvider.Bookings;
import android.app.Activity;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class NewBookingActivity extends Activity {
    
    public static enum BookingType {
        EXPENSE, INCOME, TRANSACTION
    }
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String[] BOOKING_TYPES = new String[] { 
            getString(R.string.booking_type_out), 
            getString(R.string.booking_type_in), 
            getString(R.string.booking_type_transaction)};

        setContentView(R.layout.main);
        
        ContentProviderClient cr = getContentResolver().acquireContentProviderClient(Bookings.CONTENT_URI);
        cr.getClass();
        
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Toast.makeText(getApplicationContext(), "SD card not mounted", Toast.LENGTH_LONG).show();
            return;
        }

        final EditText amountInput = (EditText)findViewById(R.id.amount);
        
        if (isInTestMode()) {
            amountInput.setBackgroundColor(0xffffbb77);
        }
        
        final EditText textInput = (EditText)findViewById(R.id.text_edit);
        final Spinner bookingTypeInput = (Spinner)findViewById(R.id.booking_type_spinner);
        final Spinner sourceInput = (Spinner) findViewById(R.id.source_spinner);
        final Spinner destInput = (Spinner) findViewById(R.id.dest_spinner);
        final Button createBookingButton = (Button)findViewById(R.id.create_booking_button);

        ArrayAdapter<CharSequence> bookingTypeAdapter = new ArrayAdapter<CharSequence>(this, 
                android.R.layout.simple_spinner_item, BOOKING_TYPES);
        bookingTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        
        
        final SimpleCursorAdapter banksAdapter = createAccountAdapter(AccountType.BANK);
        final SimpleCursorAdapter destCategoriesAdapter = createAccountAdapter(AccountType.DEST_CATEGORY);
        final SimpleCursorAdapter incomeSourceAdapter = createAccountAdapter(AccountType.INCOME_SOURCE);
        final SimpleCursorAdapter allAccountsAdapter = createAccountAdapter(null);
        
        bookingTypeInput.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
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

            @Override
            public void onNothingSelected(AdapterView<?> paramAdapterView) {
                throw new UnsupportedOperationException();
            }
        });

        bookingTypeInput.setAdapter(bookingTypeAdapter);
        
        final BookingsRepository bookings;
        if (getContentResolver() != null) {
            bookings = new BookingsRepository(getContentResolver());
        } else {
            bookings = null;
            Toast.makeText(getApplicationContext(), "SD card not mountet", Toast.LENGTH_LONG);
        }
        
        createBookingButton.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View paramView) {
                String amount = amountInput.getText().toString();
                Pattern p = Pattern.compile("(\\d*)[\\.,]?([0-9]{0,2})");
                Matcher m = p.matcher(amount);
                
                
                if (m.matches()) {
                    String first = m.group(1);
                    String second = m.group(2);
                    String amountValidated = first + "." + second;
                    bookings.insert(amountValidated, 
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
    
//    @Override
//    protected void onResume() {
//        super.onResume();
//        final EditText amountInput = (EditText)findViewById(R.id.amount);
//        amountInput.requestFocusFromTouch();
//    }
    
    private SimpleCursorAdapter createAccountAdapter(AccountType type) {
        SimpleCursorAdapter result;
        if (type == null) {
            result = new SimpleCursorAdapter(this, R.layout.accountlistitem, 
                    new AccountsRepository(getContentResolver()).queryAll(),
                    new String[] { Accounts.NAME }, new int[] { R.id.account_name });
        } else {
            result = new SimpleCursorAdapter(this, R.layout.accountlistitem, 
                    new AccountsRepository(getContentResolver()).queryType(type),
                    new String[] { Accounts.NAME }, new int[] { R.id.account_name });
        }
        result.setDropDownViewResource(R.layout.accountdropdownitem);

        return result;
    }
    
    protected boolean isInTestMode() {
        return false;
    }
    
    @Override
    protected void onResume() {
        super.onResume();
    }
    
}