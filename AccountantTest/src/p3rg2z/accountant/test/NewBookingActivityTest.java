package p3rg2z.accountant.test;

import p3rg2z.accountant.NewBookingActivity;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.EditText;

public class NewBookingActivityTest extends
        ActivityInstrumentationTestCase2<NewBookingActivity> {

    private NewBookingActivity activity;
    private EditText text_edit;

    public NewBookingActivityTest() {
        super("Accountant", NewBookingActivity.class);
    }
    protected void setUp() throws Exception {
        setActivityInitialTouchMode(false);
        activity = getActivity();
        text_edit = ((EditText)activity.findViewById(p3rg2z.accountant.R.id.text_edit));
    }
    
    public void testTest() {
//        text_edit.setText("Hello");
        getActivity().runOnUiThread(new Runnable() {
            @Override 
            public void run() {
                text_edit.setText("Hello");
            }
        });
//        this.sendKeys(KeyEvent.KEYCODE_DPAD_CENTER);
    }
    
}
