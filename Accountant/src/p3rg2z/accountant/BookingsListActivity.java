package p3rg2z.accountant;

import static p3rg2z.accountant.FormatUtil.formatAsLocalCurrency;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

import p3rg2z.accountant.Tables.Bookings;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.CursorWrapper;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class BookingsListActivity extends Activity {
    private Button newBookingButton;
    private ListView listview;
    protected long tmpId;

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        setContentView(R.layout.bookings_list);

        createData();

        newBookingButton = (Button)findViewById(R.id.new_booking_button);
        listview = (ListView)findViewById(R.id.bookings_list);

        newBookingButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_MAIN).
                        setComponent(new ComponentName(getApplicationContext(), newBookingActivityClass())));
            }
        });
        CursorWrapper bookings = new CursorWrapper(Data.instance().allBookings()) {
            public String getString(int columnIndex) {
                String result = super.getString(columnIndex);
                if (columnIndex == getColumnIndex(Bookings.AMOUNT)) {
                    try {
                        return formatAsLocalCurrency(NumberFormat.getNumberInstance(Locale.US).parse(result));
                    } catch (ParseException e) {
                        return "Invalid";
                    }
                } else {
                    return result;
                }
            }
        };

        SimpleCursorAdapter bookingsAdapter = new SimpleCursorAdapter(getApplicationContext(),
                R.layout.bookings_list_entry, bookings,
                new String[] {
                    Bookings.TEXT,
                    Bookings.DATE,
                    Bookings.AMOUNT,
                    Bookings.SOURCE,
                    Bookings.DEST },
                new int[] {
                    R.id.bookings_list_entry_text_label,
                    R.id.bookings_list_entry_date,
                    R.id.bookings_list_entry_amount,
                    R.id.bookings_list_entry_source,
                    R.id.bookings_list_entry_dest });
        listview.setAdapter(bookingsAdapter);

        listview.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> av, View v, int position, long id) {
                editBooking(id);
            }
        });

        listview.setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> av, View v, int position, long id) {
                tmpId = id;
                showDialog(-1);
                return true;
            }
        });

        if (Data.instance().isTestData()) {
            setTitleColor(0xffff0000);
            setTitle(getTitle() + " TEST MODE");
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        // should actually reset the cursor again for the adapter instead of calling requery.
        ((SimpleCursorAdapter)listview.getAdapter()).getCursor().requery();
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        final CharSequence[] items = {"Edit Booking", "Delete Booking"};

        return new AlertDialog.Builder(this).
            setTitle("Choose").
            setItems(items, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int item) {
                    switch (item) {
                    case 0:
                        editBooking(tmpId);
                        break;
                    case 1:
                        deleteBooking(tmpId);
                        ((SimpleCursorAdapter)listview.getAdapter()).getCursor().requery();
                        break;
                    default:
                        throw new IllegalStateException();
                    }
                }

            }).create();
    }

    private void editBooking(long id) {
        startActivity(new Intent(Intent.ACTION_EDIT,
                ContentUris.withAppendedId(Uri.parse("booking://p3rg2z.accountant/booking"), id),
                BookingsListActivity.this, newBookingActivityClass()));
    }

    private void deleteBooking(long id) {
        Data.instance().deleteBooking(String.valueOf(id));
    }

    protected void createData() {
        Data.instance().init(getApplicationContext(), getExternalFilesDir(null));
    }

    protected Class<?> newBookingActivityClass() {
        return NewBookingActivity.class;
    }

}
