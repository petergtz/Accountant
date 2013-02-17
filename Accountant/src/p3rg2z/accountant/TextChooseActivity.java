package p3rg2z.accountant;

import android.app.Activity;
import android.app.SearchManager;
import android.app.SearchManager.OnCancelListener;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class TextChooseActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        setContentView(R.layout.textchooser);

        ((SearchManager)getSystemService(Context.SEARCH_SERVICE)).setOnCancelListener(new OnCancelListener() {
            public void onCancel() {
                finish();
            }
        });
        onSearchRequested();
    }

    @Override
    public boolean onSearchRequested () {
        Data.setAmountForSuggestions(getIntent().getStringExtra("amount"));
        startSearch(getIntent().getStringExtra("text"), true, null, false);
        return true;
    }


    @Override
    protected void onNewIntent(Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_SEARCH)) {
            setResult(RESULT_OK, new Intent().putExtra("text", textFrom(intent)));
            finish();
        }
    }

    private static String textFrom(Intent intent) {
        if (intent.getDataString() == null) {
            return intent.getStringExtra(SearchManager.QUERY);
        } else {
            return intent.getDataString();
        }
    }

}
