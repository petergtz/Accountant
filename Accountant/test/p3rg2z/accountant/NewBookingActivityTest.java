package p3rg2z.accountant;

import org.junit.Test;
import org.junit.runner.RunWith;
//import static org.hamcrest.CoreMatchers.equalTo;
//import static org.hamcrest.CoreMatchers.is;
//import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import android.os.Environment;

import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.RobolectricTestRunner;
import com.xtremelabs.robolectric.shadows.ShadowToast;

@RunWith(RobolectricTestRunner.class)
public class NewBookingActivityTest {

	@Test
	public void showsWhenSDCardNotMounted() {
		ShadowEnvironment.setExternalStorageState(Environment.MEDIA_UNMOUNTED);
		Robolectric.bindShadowClass(ShadowEnvironment.class);
		NewBookingActivity act = new NewBookingActivity();
		act.onCreate(null);
		assertTrue(ShadowToast.showedToast("SD card not mounted"));
	}
}
