package com.example.ruden.myapplication;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.CursorLoader;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;

public class MyActivity extends Activity implements LoaderManager.LoaderCallbacks<Cursor> {
    public final static String EXTRA_MESSAGE = "com.example.ruden.myapplication.MESSAGE";
    public final static String SAVED_MESSAGE_KEY = "com.example.ruden.myapplication.SAVED_MESSAGE";

    private final static int PICK_CONTACT_REQUEST = 1;  // The request code
    private final static int CONTACT_LOADER_ID = 0;
    private final static int PHONE_LOADER_ID = 1;
    private final static int EMAIL_LOADER_ID = 2;
    private final static String CONTACT_URI_KEY = "contactUri";
    private final static String NAME_KEY = "name";
    private final static String PHONE_KEY = "phone";
    private final static String EMAIL_KEY = "email";

    private Boolean contactLoaded = false;
    private Boolean phoneLoaded = false;
    private Boolean emailLoaded = false;

    private HashMap<String, String> contactData = new HashMap<>(3);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
        ActionBar ab = getActionBar();
        if (ab != null) {
            ab.setDisplayShowHomeEnabled(true);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        EditText editText = (EditText) findViewById(R.id.edit_message);
        String message = editText.getText().toString();
        if (message.isEmpty()) {
            SharedPreferences prefs = this.getPreferences(MODE_PRIVATE);
            String savedMessage = prefs.getString(SAVED_MESSAGE_KEY, "");
            editText.setText(savedMessage, TextView.BufferType.EDITABLE);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        EditText editText = (EditText) findViewById(R.id.edit_message);
        String message = editText.getText().toString();
        SharedPreferences prefs = this.getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(SAVED_MESSAGE_KEY, message);
        editor.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        ActionBar ab = getActionBar();
        if (ab != null) {
            ab.setTitle(" " + ab.getTitle());
        }
        // Inflate the menu items for use in the action bar.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_my, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case R.id.action_search:
                openSearch();
                return true;
            case R.id.action_settings:
                openSettings();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /** Called when the user clicks the Send button */
    public void sendMessage(View view) {
        Intent intent = new Intent(this, DisplayMessageActivity.class);
        EditText editText = (EditText) findViewById(R.id.edit_message);
        String message = editText.getText().toString();
        intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
    }

    private void openSearch() {
        //Do something here.
        startActivity(new Intent(Settings.ACTION_SEARCH_SETTINGS));
    }

    private void openSettings() {
        //Do something here.
        startActivity(new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS));
    }

    /** Called when the user clicks the Add Contact button */
    public void addContact(View view) {
        Intent pickContactIntent = new Intent(Intent.ACTION_PICK, Contacts.CONTENT_URI);
        //pickContactIntent.setType(Email.CONTENT_TYPE); // Show user only contacts with email
        startActivityForResult(pickContactIntent, PICK_CONTACT_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == PICK_CONTACT_REQUEST) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                contactLoaded = phoneLoaded = emailLoaded = false;
                contactData.clear();
                Uri contactUri = data.getData(); // Uri for picked contact
                Bundle args = new Bundle(1);
                args.putParcelable(CONTACT_URI_KEY, contactUri);
                LoaderManager loader = getLoaderManager();
                // Retrieve contact data async
                loader.restartLoader(CONTACT_LOADER_ID, args, this);
                loader.restartLoader(PHONE_LOADER_ID, args, this);
                loader.restartLoader(EMAIL_LOADER_ID, args, this);
            }
        }
    }

    private void NotifyUser() {
        String name = contactData.get(NAME_KEY);
        String email = contactData.get(EMAIL_KEY);
        String phone = contactData.get(PHONE_KEY);

        // Show alert dialog
        String message = "name: " + name + "\r\nemail: " + email + "\r\nphone: " + phone;
        new AlertDialog.Builder(this)
                .setTitle(name)
                .setMessage(message)
                .setPositiveButton("ok", null)
                .show();

        // Show toast message
        final Toast notifier = Toast.makeText(this, name + ", email: " + email + ", phone: " + phone, Toast.LENGTH_LONG);
        notifier.show();
        new CountDownTimer(9000, 1000)
        {
            public void onTick(long millisUntilFinished) {notifier.show();}
            public void onFinish() {notifier.show();
            }
        }.start();
    }

    // LoaderManager.LoaderCallbacks implementation
    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle args) {
        Uri contactUri = args.getParcelable(CONTACT_URI_KEY);
        String contactId = contactUri.getLastPathSegment();
        switch (loaderId) {
            case CONTACT_LOADER_ID:
                return new CursorLoader(this, contactUri, new String[] {Contacts.DISPLAY_NAME}, null, null, null);
            case PHONE_LOADER_ID:
                return new CursorLoader(this, Phone.CONTENT_URI, new String[] {Phone.NUMBER},
                        Phone.CONTACT_ID + "=?", new String[] { contactId }, null);
            case EMAIL_LOADER_ID:
                return new CursorLoader(this, Email.CONTENT_URI, new String[] {Email.ADDRESS},
                        Email.CONTACT_ID + "=?", new String[]{contactId}, null);
            default:
                // An invalid id was passed in
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Boolean isData = data.moveToFirst();
        switch (loader.getId()) {
            case CONTACT_LOADER_ID:
                if (isData) {
                    String name = data.getString(data.getColumnIndex(Contacts.DISPLAY_NAME));
                    contactData.put(NAME_KEY, name);
                }
                else
                    contactData.put(NAME_KEY, "");
                contactLoaded = true;
                break;
            case PHONE_LOADER_ID:
                if (isData) {
                    String phone = data.getString(data.getColumnIndex(Phone.NUMBER));
                    contactData.put(PHONE_KEY, phone);
                }
                else
                    contactData.put(PHONE_KEY, "");
                phoneLoaded = true;
                break;
            case EMAIL_LOADER_ID:
                if (isData) {
                    String email = data.getString(data.getColumnIndex(Email.ADDRESS));
                    contactData.put(EMAIL_KEY, email);
                }
                else
                    contactData.put(EMAIL_KEY, "");
                emailLoaded = true;
                break;
        }

        if (contactLoaded && phoneLoaded && emailLoaded)
            NotifyUser();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

}
