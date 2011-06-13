package p3rg2z.accountant;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import p3rg2z.accountant.Data.SourceAndDest;
import p3rg2z.accountant.Tables.AccountType;
import p3rg2z.accountant.Tables.Accounts;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.os.Bundle;
import android.os.Environment;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class NewBookingActivity extends Activity {
    
    private static final NumberFormat CURRENCY_FORMATTER = NumberFormat.getCurrencyInstance();
    private static final NumberFormat NUMBER_FORMATTER = NumberFormat.getNumberInstance();
    private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    private static final int CHOOSE_TEXT_REQUEST_CODE = 3;

    private static final int ADD_SOURCE_DIALOG = 1;
    private static final int DATE_PICKER_DIALOG = 2;
    private static final int ADD_DEST_DIALOG = 3;


    private EditText textInput;
    private Spinner bookingTypeInput;
    private Spinner sourceInput;
    private Spinner destInput;
    private Button createBookingButton;
    private EditText amountInput;

    String[] BOOKING_MODES;
    private Data data;
    private Button textChooserButton;
    private Button dateInput;
    private Calendar cal;
    private DateFormat dateFormatter;
    
    private SimpleCursorAdapter banksAdapter;
    private SimpleCursorAdapter destCategoriesAdapter;
    private SimpleCursorAdapter incomeSourceAdapter;
    private SimpleCursorAdapter allAccountsAdapter;
    
    BookingModeDependent bookingMode;

    private TextView sourceLabel;
    private TextView destLabel;
    private static final MatrixCursor ADDITION_ENTRY = new MatrixCursor(new String[] { Accounts._ID, Accounts.NAME });
    static {
        ADDITION_ENTRY.addRow(new Object[] {-1, "Add new ..." });
    }

    public static enum BookingMode {
        EXPENSE, INCOME, TRANSACTION
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.newbooking);
        createData();
        if (isInTestMode()) {
            showToast("onCreate"); 
            setTitleColor(0xffff0000);
        }

        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            showToast("SD card not mounted");
            return;
        }

        initMembers();

        setUpBookingTypeButton();
        setUpSourceAndDestInput();
        setUpTextInput();
        setUpBookingButton();
        setUpTextChooserButton();
        setUpDateInput();
    }

    private void initMembers() {
        amountInput = (EditText)findViewById(R.id.amount);
        textInput = (EditText)findViewById(R.id.text_edit);
        bookingTypeInput = (Spinner)findViewById(R.id.booking_type_spinner);
        sourceLabel = (TextView) findViewById(R.id.source_label);
        sourceInput = (Spinner) findViewById(R.id.source_spinner);
        destLabel = (TextView) findViewById(R.id.dest_label);
        destInput = (Spinner) findViewById(R.id.dest_spinner);
        createBookingButton = (Button)findViewById(R.id.create_booking_button);
        textChooserButton = (Button)findViewById(R.id.text_chooser_button);
        dateInput = (Button)findViewById(R.id.date_input);

        BOOKING_MODES = new String[] { 
                getString(R.string.booking_type_out), 
                getString(R.string.booking_type_in), 
                getString(R.string.booking_type_transaction)};
        
        data = Data.instance();

        banksAdapter = createAccountAdapter(AccountType.BANK);
        destCategoriesAdapter = createAccountAdapter(AccountType.DEST_CATEGORY);
        incomeSourceAdapter = createAccountAdapter(AccountType.INCOME_SOURCE);
        allAccountsAdapter = createAccountAdapter(null);
    }
    
    private void setUpSourceAndDestInput() {
        class SelectListener implements OnItemSelectedListener {
            int id;
            int oldPosition;

            public SelectListener(int id) { this.id = id; }

            public void onItemSelected(AdapterView<?> av, View v, int index, long l) {
                if (!(bookingMode instanceof TransactionSpecific) && index == av.getCount() - 1) {
                    av.setSelection(oldPosition);
                    showDialog(id);
                } else {
                    oldPosition = index;
                }
            }

            public void onNothingSelected(AdapterView<?> av) {
                throw new UnsupportedOperationException();
            }
        };        
        sourceInput.setOnItemSelectedListener(new SelectListener(ADD_SOURCE_DIALOG));
        destInput.setOnItemSelectedListener(new SelectListener(ADD_DEST_DIALOG));
    }
    
    private void setUpTextInput() {
        textInput.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View arg0, MotionEvent arg1) {
                onSearchRequested();
                return true;
            }
        });
    }
    
    @Override
    protected void onNewIntent(Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_SEARCH)) {
            String text = textFrom(intent);
            textInput.setText(text);
            adjustSourceAndDestFor(text);
        }
    }

    private static String textFrom(Intent intent) {
        if (intent.getDataString() == null) {
            return intent.getStringExtra(SearchManager.QUERY);
        } else {
            return intent.getDataString();
        }
    }

    private void adjustSourceAndDestFor(String text) {
        SourceAndDest sourceAndDest = data.sourceAndDestFor(text);
        sourceInput.setSelection(positionOf(sourceInput, sourceAndDest.source));
        destInput.setSelection(positionOf(destInput, sourceAndDest.dest));
    }

    private interface BookingModeAndSourceDestDependent {
        String labelName();
        void addAccount(String accountName);
        SimpleCursorAdapter adapter();
    }
    
    private class BankSpecific implements BookingModeAndSourceDestDependent {
        public String labelName() { return getString(R.string.bank); }

        public void addAccount(String accountName) {
            data.addBank(accountName);
            banksAdapter.getCursor().requery();
        }

        public SimpleCursorAdapter adapter() { return banksAdapter; }
    }
    
    private class DestCategorySpecific implements BookingModeAndSourceDestDependent {
        public String labelName() { return getString(R.string.category); }

        public void addAccount(String accountName) {
            data.addDestCategory(accountName);
            destCategoriesAdapter.getCursor().requery();
        }
        public SimpleCursorAdapter adapter() { return destCategoriesAdapter; }
    }
    
    private class IncomeAccountSpecific implements BookingModeAndSourceDestDependent {
        public String labelName() { return getString(R.string.income_source); }

        public void addAccount(String accountName) {
            data.addIncomeSource(accountName);
            incomeSourceAdapter.getCursor().requery();
        }
        public SimpleCursorAdapter adapter() { return incomeSourceAdapter; }
    }
    
    private class SourceAccountSpecific implements BookingModeAndSourceDestDependent {
        public String labelName() { return getString(R.string.source); }
        public void addAccount(String accountName) { showToast("Internal Error"); }
        public SimpleCursorAdapter adapter() { return allAccountsAdapter; }
    }
    
    private class DestAccountSpecific implements BookingModeAndSourceDestDependent {
        public String labelName() { return getString(R.string.destination); }
        public void addAccount(String accountName) { showToast("Internal Error"); }
        public SimpleCursorAdapter adapter() { return allAccountsAdapter; }
    }
    
    private interface SourceDestDependent {
        BookingModeAndSourceDestDependent combinedWith(ExpenseSpecific expenseSpecific);
        BookingModeAndSourceDestDependent combinedWith(IncomeSpecific incomeSpecific);
        BookingModeAndSourceDestDependent combinedWith(TransactionSpecific transactionSpecific);
    }
    
    private final SourceDestDependent SOURCE = new SourceDestDependent() {
        public BookingModeAndSourceDestDependent combinedWith(ExpenseSpecific expenseSpecific) {
            return new BankSpecific();
        }
        public BookingModeAndSourceDestDependent combinedWith(IncomeSpecific incomeSpecific) {
            return new IncomeAccountSpecific();
        }
        public BookingModeAndSourceDestDependent combinedWith(TransactionSpecific transactionSpecific) {
            return new SourceAccountSpecific();
        }
    };
    
    private final SourceDestDependent DEST = new SourceDestDependent() {
        public BookingModeAndSourceDestDependent combinedWith(ExpenseSpecific expenseSpecific) {
            return new DestCategorySpecific();
        }
        public BookingModeAndSourceDestDependent combinedWith(IncomeSpecific incomeSpecific) {
            return new BankSpecific();
        }
        public BookingModeAndSourceDestDependent combinedWith(TransactionSpecific transactionSpecific) {
            return new DestAccountSpecific();
        }
    };
    
    private interface BookingModeDependent {
        BookingModeAndSourceDestDependent combinedWith(SourceDestDependent sourceDestDependent);
    }
    
    private class ExpenseSpecific implements BookingModeDependent {
        public BookingModeAndSourceDestDependent combinedWith(SourceDestDependent sourceDestDependent) {
            return sourceDestDependent.combinedWith(this);
        }
    }
    
    private class IncomeSpecific implements BookingModeDependent {
        public BookingModeAndSourceDestDependent combinedWith(SourceDestDependent sourceDestDependent) {
            return sourceDestDependent.combinedWith(this);
        }
    }
    
    private class TransactionSpecific implements BookingModeDependent {
        public BookingModeAndSourceDestDependent combinedWith(SourceDestDependent sourceDestDependent) {
            return sourceDestDependent.combinedWith(this);
        }
    }
    
    private void setUpBookingTypeButton() {
        ArrayAdapter<CharSequence> bookingTypeAdapter = new ArrayAdapter<CharSequence>(this, 
                android.R.layout.simple_spinner_item, BOOKING_MODES);
        bookingTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        
        bookingTypeInput.setOnItemSelectedListener(new OnItemSelectedListener() {

            public void onItemSelected(AdapterView<?> av, View v, int index, long l) {
                if (index == BookingMode.EXPENSE.ordinal()) {
                    bookingMode = new ExpenseSpecific();
                } else if (index == BookingMode.INCOME.ordinal()) {
                    bookingMode = new IncomeSpecific();
                } else if (index == BookingMode.TRANSACTION.ordinal()) {
                    bookingMode = new TransactionSpecific();
                } else {
                    throw new UnsupportedOperationException();
                }
                sourceLabel.setText(bookingMode.combinedWith(SOURCE).labelName());
                destLabel.setText(bookingMode.combinedWith(DEST).labelName());
                sourceInput.setAdapter(bookingMode.combinedWith(SOURCE).adapter());
                destInput.setAdapter(bookingMode.combinedWith(DEST).adapter());
            }

            public void onNothingSelected(AdapterView<?> paramAdapterView) {
                throw new UnsupportedOperationException();
            }
        });

        bookingTypeInput.setAdapter(bookingTypeAdapter);
    }

    private SimpleCursorAdapter createAccountAdapter(AccountType type) {
        SimpleCursorAdapter result;
        if (type == null) {
            result = new SimpleCursorAdapter(this, R.layout.accountlistitem, 
                    data.queryAllAccounts(),
                    new String[] { Accounts.NAME }, new int[] { R.id.account_name });
        } else {
            result = new SimpleCursorAdapter(this, R.layout.accountlistitem, 
                    new MergeCursor(new Cursor[] {data.queryType(type), ADDITION_ENTRY}),
                    new String[] { Accounts.NAME }, new int[] { R.id.account_name });
        }
        result.setDropDownViewResource(R.layout.accountdropdownitem);

        return result;
    }
    
    private void setUpBookingButton() {
        createBookingButton.setOnClickListener(new OnClickListener() {
            public void onClick(View paramView) {
                try {
                    String formattedAmount = formatAmount(amountInput.getText().toString());
                    data.insert(formattedAmount, 
                            textInput.getText().toString(),
                            ((Cursor)sourceInput.getSelectedItem()).getString(Accounts.NAME_INDEX), 
                            ((Cursor)destInput.getSelectedItem()).getString(Accounts.NAME_INDEX),
                            formatDate(cal.getTime()));

                    showToast(String.format("Saved new booking \"%s\": %s", 
                            textInput.getText().toString(), formattedAmount));
                } catch (ParseException e) {
                    showToast("INVALID");
                }
            }
        });
    }
    
    private static String formatDate(Date date) {
        return DATE_FORMATTER.format(date);
    }

    private static String formatAmount(String amount) throws ParseException {
        Number number = NUMBER_FORMATTER.parse(amount);
        String currencyString = CURRENCY_FORMATTER.format(number);
        Number currencyNumber = CURRENCY_FORMATTER.parse(currencyString);
        if (currencyNumber instanceof Long) {
            return String.format(Locale.US, "%d.00", currencyNumber);
        } else {
            return String.format(Locale.US, "%10.2f", currencyNumber);
        }
    }

    private void setUpTextChooserButton() {
        textChooserButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                startActivityForResult(new Intent().
                    setComponent(new ComponentName("p3rg2z.accountant", getTextChooseActivityName())), 
                    CHOOSE_TEXT_REQUEST_CODE);
            }
        });
    }


    void setUpDateInput() {
        dateFormatter = DateFormat.getDateInstance(DateFormat.LONG);
        cal = Calendar.getInstance();
        dateInput.setText(dateFormatter.format(cal.getTime()));
        dateInput.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                showDialog(DATE_PICKER_DIALOG);
            }
        });
    }
    
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
        case ADD_SOURCE_DIALOG:
            return createAddAccountDialog(bookingMode.combinedWith(SOURCE), sourceInput);
        case ADD_DEST_DIALOG:
            return createAddAccountDialog(bookingMode.combinedWith(DEST), destInput);
        case DATE_PICKER_DIALOG:
            return createDatePickerDialog();
        default:
            throw new IllegalStateException();
        }
    }

    private Dialog createDatePickerDialog() {
        return new DatePickerDialog(this, new OnDateSetListener() {
            public void onDateSet(DatePicker picker, int year, int month, int day) {
                cal.set(Calendar.YEAR, year);
                cal.set(Calendar.MONTH, month);
                cal.set(Calendar.DAY_OF_MONTH, day);
                dateInput.setText(dateFormatter.format(cal.getTime()));
            }
        }, 
        cal.get(Calendar.YEAR),
        cal.get(Calendar.MONTH),
        cal.get(Calendar.DAY_OF_MONTH));
    }

    private Dialog createAddAccountDialog(final BookingModeAndSourceDestDependent modeDependent, final Spinner input) {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.add_account_dialog);

        final EditText accountNameInput = (EditText)dialog.findViewById(R.id.account_name_input);
        Button okButton = (Button)dialog.findViewById(R.id.ok_button);
        Button cancelButton = (Button)dialog.findViewById(R.id.cancel_button);

        dialog.setTitle("New " + modeDependent.labelName());
        accountNameInput.setHint(modeDependent.labelName() + " name");
        
        okButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                String accountName = accountNameInput.getText().toString();
                if (data.hasAccount(accountName)) {
                    showToast("An account with this name exists already.");
                } else {
                    modeDependent.addAccount(accountName);
                }
                input.setSelection(positionOf(input, accountName));
                dialog.dismiss();
            }
        });
        cancelButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                dialog.cancel();
            }
        });
        return dialog;
    }

    @Override
    public boolean onSearchRequested () {
        try {
            Data.setAmountForSuggestions(formatAmount(amountInput.getText().toString()));
        } catch (ParseException e) {
            Data.setAmountForSuggestions("");
        }
        startSearch(textInput.getText().toString(), true, null, false);
        return true;
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            textInput.setText(data.getAction());
        }
        showToast(""+ requestCode + " " + resultCode + " " + data);
    }
      
    protected void createData() {
        Data.instance().init(getApplicationContext(), getExternalFilesDir(null));
    }
    
    protected boolean isInTestMode() {
        return false; 
    }
    
    protected String getTextChooseActivityName() {
    	return "p3rg2z.accountant.TextChooseActivity";
    }

    private static int positionOf(Spinner spinner, String text) {
        SimpleCursorAdapter adapter = (SimpleCursorAdapter)spinner.getAdapter();
        for (int i = 0; i < adapter.getCount(); i++) {
            String entryText = ((Cursor) adapter.getItem(i)).getString(Accounts.NAME_INDEX);
            if (entryText.equals(text)) {
                return i;
            }
        }
        return 0;
    }

    private void showToast(String text) {
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();
    }

}