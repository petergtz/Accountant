package p3rg2z.accountant;

public class TestModeNewBookingActivity extends NewBookingActivity {
    @Override
    protected boolean isInTestMode() {
        return true;
    }
    
    @Override
    protected Data getData() {
        return Data.createForTesting(getApplicationContext(), getExternalFilesDir(null));
    }
    
    @Override
    protected String getTextChooseActivityName() {
    	return "p3rg2z.accountant.TextChooseActivity";
    }
}
