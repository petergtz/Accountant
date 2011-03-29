package p3rg2z.accountant;

import p3rg2z.accountant.AccountancyContentProvider.Accounts;
import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

public class NewBookingActivity extends Activity {
    
    public static enum BookingType {
        EXPENSE, INCOME, TRANSACTION
    }
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Spinner bookingTypeSpinner = (Spinner)findViewById(R.id.booking_type_spinner);
        ArrayAdapter<CharSequence> adapter = /*ArrayAdapter.createFromResource(
                this, R.array.booking_type, android.R.layout.simple_spinner_item);*/
            new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item, 
                    new String[] { 
                        getString(R.string.booking_type_out), 
                        getString(R.string.booking_type_in), 
                        getString(R.string.booking_type_transaction)});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        bookingTypeSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> paramAdapterView,
                    View paramView, int index, long paramLong) {

                if (index == BookingType.EXPENSE.ordinal()) {
                    ((TextView) findViewById(R.id.source_label)).setText(R.string.bank);
                } else if (index == BookingType.INCOME.ordinal()) {
                    ((TextView) findViewById(R.id.source_label)).setText(R.string.income_source);
                } else if (index == BookingType.TRANSACTION.ordinal()) {
                    ((TextView) findViewById(R.id.source_label)).setText(R.string.source);
                } else {
                    throw new RuntimeException();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> paramAdapterView) {
                throw new UnsupportedOperationException();
            }
        });

        bookingTypeSpinner.setAdapter(adapter);
        
        final EditText amountEdit = (EditText) findViewById(R.id.amount);
        final Spinner source = (Spinner) findViewById(R.id.source_spinner);
        Cursor c = getContentResolver().query(Accounts.CONTENT_URI, new String[] { Accounts._ID, Accounts.NAME }, null, null, null);
        int count = c.getCount();
        source.setAdapter(new SimpleCursorAdapter(this, R.layout.accountlistitem, 
                c, new String[] { Accounts.NAME }, new int[] { R.id.account_name }));
        final Spinner dest = (Spinner) findViewById(R.id.dest_spinner);
        
        final BookingsRepository repo = new BookingsRepository(getContentResolver());
        
        Button button = (Button)findViewById(R.id.create_booking_button);
        button.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View paramView) {
//                repo.insert(Integer.valueOf(amountEdit.getText().toString()), 
//                        source.getSelectedView().get
//                        text, bank, category, datetime);
//                getContentResolver().insert(AccountancyContentProvider.Bookings.CONTENT_URI, values)
            }
        });
    }
}