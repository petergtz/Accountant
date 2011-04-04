package p3rg2z.accountant.test;

import p3rg2z.accountant.NewBookingActivity;
import android.test.ActivityInstrumentationTestCase2;
import android.view.KeyEvent;

public class NewBookingActivityTest extends
        ActivityInstrumentationTestCase2<NewBookingActivity> {

    public NewBookingActivityTest() {
        super("Accountant", NewBookingActivity.class);
    }
    
    public void testTest() {
        getActivity().runOnUiThread(new Runnable() {
            
            @Override
            public void run() {
                
            }
        });
        this.sendKeys(KeyEvent.KEYCODE_DPAD_CENTER);
    }
    
}
