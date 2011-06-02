package p3rg2z.accountant;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class BookingsListActivity extends Activity {
    private ListView lv;

    protected boolean isInTestMode() {
        return false;
    }
    
    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        
        setContentView(R.layout.bookings_list);
        
        lv = (ListView)findViewById(R.id.bookings_list);
        
        lv.setAdapter(new SimpleCursorAdapter(getApplicationContext(), R.layout.bookings_list_entry,
                Data.instance().queryAllBookings(), new String[] { Tables.Bookings.TEXT }, 
                new int[] { R.id.bookings_list_entry_text_label }));

        if (isInTestMode()) {
            setTitleColor(0xffff0000);
            setTitle(getTitle() + " TEST MODE");
        }
        
    }
}
