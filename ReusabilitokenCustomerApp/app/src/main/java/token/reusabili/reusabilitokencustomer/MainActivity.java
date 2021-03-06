package token.reusabili.reusabilitokencustomer;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Telephony;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.graphics.Point;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;

import Features.operations.HumanConfirmableClaim;
import Features.operations.HumanConfirmableOperation;
import Features.operations.actions.AHumanConfirmableAction;
import tests.DummyRepo;
import token.reusabili.reusabilitokencustomer.barcode.BarcodeCaptureActivity;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final int BARCODE_READER_REQUEST_CODE = 1;
    private static final String FILENAME = "ReusabiliStore";
    private static final String VAL_KEY = "ADDRESS";
    private SharedPreferences sharedPrefs;
    private EditText editAddress;
    public String Address = null;
    public static AHumanConfirmableAction choosenAction;
    public static HumanConfirmableClaim claimer;
    public static HumanConfirmableOperation hco;
    public static DummyRepo repo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sharedPrefs  = getSharedPreferences(FILENAME, 0);
        Address = sharedPrefs.getString(VAL_KEY, null);
        loadActivity();

    }

    private void loadActivity()
    {
        if (!checkAddress(Address)) {
            Toast.makeText(getApplicationContext(),"No correct ETH - Address set!", Toast.LENGTH_SHORT).show();
            editAddress = (EditText) findViewById(R.id.enter_Address);
            Button scanButton = (Button) findViewById(R.id.scan_barcode_button);
            scanButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getApplicationContext(), BarcodeCaptureActivity.class);
                    startActivityForResult(intent, BARCODE_READER_REQUEST_CODE);
                }
            });



            Button saveAddress = findViewById(R.id.save_address);
            saveAddress.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    InputMethodManager inputManager = (InputMethodManager)
                            getSystemService(Context.INPUT_METHOD_SERVICE);

                    inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                            InputMethodManager.HIDE_NOT_ALWAYS);
                    String notNormalizedAddress = editAddress.getText().toString();
                    if(checkAddress(notNormalizedAddress)) {
                        SharedPreferences.Editor editor = sharedPrefs.edit();
                        if (notNormalizedAddress.length() == 51) {
                            Address = notNormalizedAddress.substring(9);
                        } else
                            Address = notNormalizedAddress;
                        editor.putString(VAL_KEY, Address);
                        editor.commit();
                    }
                    loadActivity();

                }
            });

        }

        else
        {
            startActivity(new Intent(this, ClaimActivity.class));
        }
    }

    private boolean checkAddress(String address)
    {
        if(address == null)
            return false;
        String eth="ethereum:";
        String hexsig, number;
        boolean length = address.length()==42 || address.length()== 51;
        if(!(address.length()==51) && address.length()>2) {
            hexsig = address.substring(0, 2);
            number = address.substring(2);
        }
        else if((address.length()==51)){
            eth = address.substring(0,9);
            hexsig = address.substring(9, 11);
            number = address.substring(11);
        }
        else {hexsig=""; number="";}
        boolean ethsig = eth.toLowerCase().contains(new String("ethereum:"));
        boolean ishex = hexsig.toLowerCase().contains(new String("0x"));
        boolean asciichar = true;
        for (char character : number.toCharArray()) {
            int code = (int) character;
            if(!((code >= 48 && code <= 57) || (code >= 65 && code <= 70) || (code >= 97 && code <= 102)))
                asciichar = false;
        }


        return (length && asciichar && ishex && ethsig);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == BARCODE_READER_REQUEST_CODE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);
                    Point[] p = barcode.cornerPoints;
                    editAddress.setText(barcode.displayValue);
                }
            } else
                Log.e(LOG_TAG, String.format(getString(R.string.barcode_error_format), CommonStatusCodes.getStatusCodeString(resultCode)));
        } else
            super.onActivityResult(requestCode, resultCode, data);
    }

}