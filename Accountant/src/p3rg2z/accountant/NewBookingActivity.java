/*
   Copyright 2013 Peter Goetz

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

package p3rg2z.accountant;

import static android.widget.Toast.LENGTH_LONG;
import static p3rg2z.accountant.Data.*;
import static p3rg2z.accountant.FormatUtil.*;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Currency;
import java.util.Locale;

import p3rg2z.accountant.Data.SourceAndDest;
import p3rg2z.accountant.Tables.AccountType;
import p3rg2z.accountant.Tables.Accounts;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
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

    private static final String USE_AMOUNT_SEP = "useAmountSep";
    private static final int ADD_SOURCE_DIALOG = 1;
    private static final int DATE_PICKER_DIALOG = 2;
    private static final int ADD_DEST_DIALOG = 3;
    private static final int AMOUNT_INPUT_METHOD_DIALOG = 4;

    private static final int CHOOSE_TEXT_REQUEST = 0;
    private static final int CHOOSE_FILE_REQUEST = 1;
    private static final int MAPPING_REQUEST = 2;

    private Button bookingsListButton;
    private TextView title;
    private Button submitButton;
    private TextView currencySymbol;
    private EditText amountInput;
    private EditText textInput;
    private Spinner bookingTypeInput;
    private TextView sourceLabel;
    private Spinner sourceInput;
    private TextView destLabel;
    private Spinner destInput;
    private Button dateInput;
    private Calendar cal;

    String[] BOOKING_MODES;
    private Data data;

    private SimpleCursorAdapter banksAdapter;
    private SimpleCursorAdapter destCategoriesAdapter;
    private SimpleCursorAdapter incomeSourceAdapter;
    private SimpleCursorAdapter allAccountsAdapter;

    BookingModeDependent bookingMode;

    private MatrixCursor addNewAccountEntry = new MatrixCursor(new String[] { Accounts._ID, Accounts.NAME });

    public static enum BookingMode {
        EXPENSE, INCOME, TRANSACTION
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            showLongToast(R.string.sd_card_not_mounted);
            return;
        }

        setContentView(R.layout.newbooking);

        createData();
        runTestModeOperations();

        initMembers();

        setUpBookingsListButton();
        setUpCurrencySymbol();
        setUpBookingTypeInput();
        setUpTextInput();
        setUpSourceAndDestItemSelectListener();
        setUpDateInput();
        if (getIntent().getAction().equals(Intent.ACTION_EDIT)) {
            String bookingId = bookingIdFrom(getIntent());
            preFillInputs(data.bookingFor(bookingId));
            setUpSubmitAsApplyChangesButton(bookingId);
        } else {
            setUpSubmitAsCreateBookingButton();
        }
    }

    private void initMembers() {
        bookingsListButton = (Button)findViewById(R.id.bookings_list_button);
        title = (TextView)findViewById(R.id.title);
        currencySymbol = (TextView)findViewById(R.id.currency);
        amountInput = (EditText)findViewById(R.id.amount);
        textInput = (EditText)findViewById(R.id.text_edit);
        bookingTypeInput = (Spinner)findViewById(R.id.booking_type_spinner);
        sourceLabel = (TextView) findViewById(R.id.source_label);
        sourceInput = (Spinner) findViewById(R.id.source_spinner);
        destLabel = (TextView) findViewById(R.id.dest_label);
        destInput = (Spinner) findViewById(R.id.dest_spinner);
        submitButton = (Button)findViewById(R.id.create_booking_button);
        dateInput = (Button)findViewById(R.id.date_input);

        BOOKING_MODES = new String[] {
                getString(R.string.booking_type_out),
                getString(R.string.booking_type_in),
                getString(R.string.booking_type_transaction)};

        data = Data.instance();

        addNewAccountEntry = new MatrixCursor(new String[] { Accounts._ID, Accounts.NAME });
        addNewAccountEntry.addRow(new Object[] {-1, getString(R.string.add_new) });

        banksAdapter = createAccountAdapter(AccountType.BANK);
        destCategoriesAdapter = createAccountAdapter(AccountType.DEST_CATEGORY);
        incomeSourceAdapter = createAccountAdapter(AccountType.INCOME_SOURCE);
        allAccountsAdapter = createAccountAdapter(null);
    }

    private void setUpBookingsListButton() {
        bookingsListButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent().setClass(getApplicationContext(), bookingsListActivity()));
            }
        });
    }

    protected Class<?> bookingsListActivity() {
        return BookingsListActivity.class;
    }

    private void setUpCurrencySymbol() {
        currencySymbol.setText(Currency.getInstance(Locale.getDefault()).getCurrencyCode() +" ");
    }

    private void setUpSourceAndDestItemSelectListener() {
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
        textInput.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                startTextChooseActivity();
            }
        });
        textInput.setOnFocusChangeListener(new OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) startTextChooseActivity();
            }
        });
    }

    private void startTextChooseActivity() {
        String formattedAmount;
        try {
            formattedAmount = amountFormattedForDatabase();
        } catch (ParseException e) {
            formattedAmount = "";
        }
        startActivityForResult(
                new Intent().setClass(getApplicationContext(), textChooseActivity()).
                    putExtra("amount", formattedAmount).
                    putExtra("text", textInput.getText().toString()),
                CHOOSE_TEXT_REQUEST);
    }

    private void adjustSourceAndDestFor(String text) {
        SourceAndDest sourceAndDest = data.suggestedSourceAndDestFor(text);
        selectTextIn(sourceInput, sourceAndDest.source);
        selectTextIn(destInput, sourceAndDest.dest);
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
        public void addAccount(String accountName) { showLongToast(R.string.internal_error); }
        public SimpleCursorAdapter adapter() { return allAccountsAdapter; }
    }

    private class DestAccountSpecific implements BookingModeAndSourceDestDependent {
        public String labelName() { return getString(R.string.destination); }
        public void addAccount(String accountName) { showLongToast(R.string.internal_error); }
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

    private void setUpBookingTypeInput() {
        ArrayAdapter<CharSequence> bookingTypeAdapter = new ArrayAdapter<CharSequence>(this,
                android.R.layout.simple_spinner_item, BOOKING_MODES);
        bookingTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        bookingTypeInput.setOnItemSelectedListener(new OnItemSelectedListener() {

            public void onItemSelected(AdapterView<?> av, View v, int index, long l) {
                setUpSourceAndDestInputBasedOn(index);
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
                    data.allAccounts(),
                    new String[] { Accounts.NAME }, new int[] { R.id.account_name });
        } else {
            result = new SimpleCursorAdapter(this, R.layout.accountlistitem,
                    new MergeCursor(new Cursor[] {data.allAccountsOf(type), addNewAccountEntry}),
                    new String[] { Accounts.NAME }, new int[] { R.id.account_name });
        }
        result.setDropDownViewResource(R.layout.accountdropdownitem);

        return result;
    }

    private void setUpSubmitAsCreateBookingButton() {
        submitButton.setText(R.string.create);
        submitButton.setOnClickListener(new OnClickListener() {
            public void onClick(View paramView) {
                try {
                    String formattedAmount = amountFormattedForDatabase();
                    data.addNewBooking(formattedAmount,
                            textInput.getText().toString(),
                            ((Cursor)sourceInput.getSelectedItem()).getString(Accounts.NAME_INDEX),
                            ((Cursor)destInput.getSelectedItem()).getString(Accounts.NAME_INDEX),
                            formatAsCanonical(cal.getTime()));

                    showLongToast(R.string.created_new_booking,
                            textInput.getText().toString(),
                            reformatNumberAsLocalCurrency(formattedAmount),
                            ((Cursor)sourceInput.getSelectedItem()).getString(Accounts.NAME_INDEX),
                            ((Cursor)destInput.getSelectedItem()).getString(Accounts.NAME_INDEX));

                    textInput.setText("");
                    amountInput.setText("");
                } catch (ParseException e) {
                    showLongToast(R.string.invalid_booking);
                }
            }
        });
    }

    private static String bookingIdFrom(Intent intent) {
        return Uri.parse(intent.getDataString()).getLastPathSegment();
    }

    private void preFillInputs(Cursor booking) {
        title.setText(R.string.booking);
        amountInput.setText(amountFormattedForOutput(amountFrom(booking)));
        int bookingModeIndex = data.bookingTypeFrom(booking).ordinal();
        bookingTypeInput.setSelection(bookingModeIndex);

        textInput.setText(textFrom(booking));
        setUpSourceAndDestInputBasedOn(bookingModeIndex);
        selectTextIn(sourceInput, sourceFrom(booking));
        selectTextIn(destInput, destFrom(booking));

        try {
            cal.setTime(parseAsCanonicalDate(dateFrom(booking)));
            dateInput.setText(formatAsLocal(cal.getTime()));
        } catch (ParseException e) {
            cal = Calendar.getInstance();
            dateInput.setText(R.string.error);
        }
    }

    private void setUpSubmitAsApplyChangesButton(final String id) {
        submitButton.setText(R.string.change);
        submitButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                try {
                    String amount = amountFormattedForDatabase();
                    data.updateBooking(id, amount,
                            textInput.getText().toString(),
                            ((Cursor)sourceInput.getSelectedItem()).getString(Accounts.NAME_INDEX),
                            ((Cursor)destInput.getSelectedItem()).getString(Accounts.NAME_INDEX),
                            formatAsCanonical(cal.getTime()));

                    showLongToast(R.string.saved_changes_in_booking,
                            textInput.getText().toString(),
                            reformatNumberAsLocalCurrency(amount),
                            ((Cursor)sourceInput.getSelectedItem()).getString(Accounts.NAME_INDEX),
                            ((Cursor)destInput.getSelectedItem()).getString(Accounts.NAME_INDEX));
                    finish();
                } catch (ParseException e) {
                    showLongToast(R.string.invalid_booking);
                }
            }
        });
    }

    private Class<?> textChooseActivity() {
        return TextChooseActivity.class;
    }

    private void setUpDateInput() {
        cal = Calendar.getInstance();
        dateInput.setText(formatAsLocal(cal.getTime()));
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
        case AMOUNT_INPUT_METHOD_DIALOG:
            return createAmountInputMethodDialog();
        default:
            throw new IllegalStateException();
        }
    }

    private Dialog createAmountInputMethodDialog() {
        return new AlertDialog.Builder(this).
            setTitle("Enter "+ formatAsLocalCurrency(123) +" as").
            setSingleChoiceItems(new String[] { "123", String.valueOf(123*fractionDigits()) },
                    usesAmountSeparator() ? 0 : 1,
                    new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    if (which == 0) {
                        getPreferences(MODE_PRIVATE).edit().putBoolean(USE_AMOUNT_SEP, true).commit();
                    } else {
                        getPreferences(MODE_PRIVATE).edit().putBoolean(USE_AMOUNT_SEP, false).commit();
                    }
                    dialog.dismiss();
                }
            }).create();
    }

    private static  int fractionDigits() {
        return (int)Math.pow(10, Currency.getInstance(Locale.getDefault()).getDefaultFractionDigits());
    }

    private Dialog createDatePickerDialog() {
        return new DatePickerDialog(this, new OnDateSetListener() {
                public void onDateSet(DatePicker picker, int year, int month, int day) {
                    cal.set(Calendar.YEAR, year);
                    cal.set(Calendar.MONTH, month);
                    cal.set(Calendar.DAY_OF_MONTH, day);
                    dateInput.setText(formatAsLocal(cal.getTime()));
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
                if (data.accountExists(accountName)) {
                    showLongToast(R.string.an_account_with_this_name_exists_already);
                } else {
                    modeDependent.addAccount(accountName);
                }
                selectTextIn(input, accountName);
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CHOOSE_TEXT_REQUEST && resultCode == RESULT_OK) {
            if (data != null) {
                textInput.setText(data.getStringExtra("text"));
                adjustSourceAndDestFor(data.getStringExtra("text"));
            }
        } else if (requestCode == CHOOSE_FILE_REQUEST && resultCode == RESULT_OK) {
            Toast.makeText(this, data.getStringExtra("path"), Toast.LENGTH_LONG).show();
            startActivityForResult(new Intent().setClass(this, CSVMapperActivity.class), MAPPING_REQUEST);
        }
    }

    protected void createData() {
        Data.instance().init(getApplicationContext(), getExternalFilesDir(null));
    }

    protected void runTestModeOperations() {}

    private static void selectTextIn(Spinner spinner, String text) {
        spinner.setSelection(positionOf(spinner, text));
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

    private void showLongToast(int stringId, Object... formatArgs) {
        Toast.makeText(this, getString(stringId, formatArgs), LENGTH_LONG).show();
    }

    private void setUpSourceAndDestInputBasedOn(int bookingModeIndex) {
        if (bookingModeIndex == BookingMode.EXPENSE.ordinal()) {
            bookingMode = new ExpenseSpecific();
        } else if (bookingModeIndex == BookingMode.INCOME.ordinal()) {
            bookingMode = new IncomeSpecific();
        } else if (bookingModeIndex == BookingMode.TRANSACTION.ordinal()) {
            bookingMode = new TransactionSpecific();
        } else {
            throw new UnsupportedOperationException();
        }
        sourceLabel.setText(bookingMode.combinedWith(SOURCE).labelName());
        destLabel.setText(bookingMode.combinedWith(DEST).labelName());
        sourceInput.setAdapter(bookingMode.combinedWith(SOURCE).adapter());
        destInput.setAdapter(bookingMode.combinedWith(DEST).adapter());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.booking_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.import_csv:
            goToImportMode();
            return true;
        case R.id.settings:
            showDialog(AMOUNT_INPUT_METHOD_DIALOG);
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    private void goToImportMode() {

        startActivity(new Intent().setClass(this, FileChooserActivity.class));

    }

    private String amountFormattedForDatabase() throws ParseException {

        if (usesAmountSeparator()) {
            return reformatNumberAsCanonical(amountInput.getText().toString());
        } else {
            try {
                BigDecimal bd = new BigDecimal(Integer.parseInt(amountInput.getText().toString())).
                divide(new BigDecimal(fractionDigits()));
                return bd.toEngineeringString();
            } catch (NumberFormatException exception) {
                throw new ParseException(exception.toString(), -1);
            }
        }
    }

    private String amountFormattedForOutput(String amountFromDatabase) {

        try {
            if (usesAmountSeparator()) {
                return reformatNumberAsLocal(amountFromDatabase);
            } else {
                return new BigDecimal(amountFromDatabase).multiply(new BigDecimal(fractionDigits())).
                           toString();
            }
        } catch (ParseException e) {
            showLongToast(R.string.internal_error);
            return getString(R.string.error);
        }
    }

    private boolean usesAmountSeparator() {
        return getPreferences(MODE_PRIVATE).getBoolean(USE_AMOUNT_SEP, true);
    }

}
