package p3rg2z.accountant;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class TextChooseActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        
        setContentView(R.layout.textchooser);
        
        Button b = (Button)findViewById(R.id.button1);
        b.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View paramView) {
                setResult(666, new Intent("AAA"));
                finish();
                
            }
        });
    }
}
