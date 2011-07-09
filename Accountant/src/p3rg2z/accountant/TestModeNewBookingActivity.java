package p3rg2z.accountant;

import android.widget.Toast;

public class TestModeNewBookingActivity extends NewBookingActivity {
    @Override
    protected void runTestModeOperations() {
        Toast.makeText(this, "onCreate", Toast.LENGTH_LONG).show();
        getWindow().setBackgroundDrawableResource(R.color.red);
    }

    @Override
    protected void createData() {
        Data.instance().initForTesting(getApplicationContext(), getExternalFilesDir(null));
    }

    @Override
    protected Class<?> bookingsListActivity() {
        return TestModeBookingsListActivity.class;
    }


}
