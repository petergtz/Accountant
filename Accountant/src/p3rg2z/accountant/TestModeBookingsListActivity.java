package p3rg2z.accountant;

public class TestModeBookingsListActivity extends BookingsListActivity {

    @Override
    protected boolean isInTestMode() {
        return true;
    }

    @Override
    protected Data getData() {
        return Data.createForTesting(getApplicationContext(), getExternalFilesDir(null));
    }
    

}
