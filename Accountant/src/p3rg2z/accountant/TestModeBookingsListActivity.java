package p3rg2z.accountant;

public class TestModeBookingsListActivity extends BookingsListActivity {

    @Override
    protected void createData() {
        Data.instance().initForTesting(getApplicationContext(), getExternalFilesDir(null));
    }

    @Override
    protected Class<?> newBookingActivityClass() {
        return TestModeNewBookingActivity.class;
    }
}
