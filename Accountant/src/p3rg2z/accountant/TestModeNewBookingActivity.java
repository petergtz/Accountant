package p3rg2z.accountant;

public class TestModeNewBookingActivity extends NewBookingActivity {
    @Override
    protected boolean isInTestMode() {
        return true;
    }
    
    @Override
    protected void createData() {
        Data.instance().initForTesting(getApplicationContext(), getExternalFilesDir(null));
    }
    
    @Override
    protected String getTextChooseActivityName() {
    	return "p3rg2z.accountant.TextChooseActivity";
    }
}
