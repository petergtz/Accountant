package p3rg2z.accountant;

import com.xtremelabs.robolectric.util.Implementation;
import com.xtremelabs.robolectric.util.Implements;
import com.xtremelabs.robolectric.util.RealObject;

import android.os.Environment;

@Implements(Environment.class)
public class ShadowEnvironment {
	@RealObject 
	protected Environment realEnv;
	
	static String externalStorageState; 
	
	public static void setExternalStorageState(String state) {
		externalStorageState = state;
	}
	
	@Implementation
	public static String getExternalStorageState() {
		return externalStorageState;
	}

}
