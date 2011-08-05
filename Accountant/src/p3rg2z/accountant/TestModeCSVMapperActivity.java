package p3rg2z.accountant;

import java.io.File;

import android.content.Intent;

public class TestModeCSVMapperActivity extends CSVMapperActivity {
    @Override
    protected void createData() {
        Data.instance().initForTesting(getApplicationContext(), getExternalFilesDir(null));
    }

    @Override
    protected File csvFileFrom(Intent intent) {
        return new File("/mnt/sdcard/Umsaetze.csv");
    }


}
