package p3rg2z.accountant;

public class TestModeNewBookingActivity extends NewBookingActivity {
    @Override
    protected boolean isInTestMode() {
        return true;
    }
    
    @Override
    protected Data getData() {
        return new Data(getApplicationContext(), getExternalFilesDir(null)+"/" +"test-accountant.db");
    }
}
