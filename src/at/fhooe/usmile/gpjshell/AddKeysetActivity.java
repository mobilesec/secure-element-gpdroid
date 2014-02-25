package at.fhooe.usmile.gpjshell;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import at.fhooe.usmile.gpjshell.objects.GPKeyset;

public class AddKeysetActivity extends Activity {
	private EditText editID;
	private EditText editVersion;
	private EditText editName;
	private EditText editMAC;
	private EditText editENC;
	private EditText editKEK;
	private Button mPositive;
	private Button mNegative;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_keyset);
		editID = (EditText) findViewById(R.id.edit_keyset_id);
		editVersion = (EditText) findViewById(R.id.edit_keyset_version);
		editName = (EditText) findViewById(R.id.edit_keyset_name);
		editMAC = (EditText) findViewById(R.id.edit_keyset_mac);
		editENC = (EditText) findViewById(R.id.edit_keyset_enc);
		editKEK = (EditText) findViewById(R.id.edit_keyset_kek);

		mPositive = (Button) findViewById(R.id.btn_addkeyset_positive);
		mPositive.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (editID.getText() != null
						&& !editID.getText().toString().equals("")
						&& !editVersion.getText().toString().equals("")) {
					
					GPKeyset keyset = new GPKeyset(editName.getText()
							.toString(), Integer.valueOf(editID.getText()
							.toString()), Integer.valueOf(editVersion.getText()
							.toString()), editMAC.getText().toString(), editENC
							.getText().toString(), editKEK.getText().toString(), null);
					Intent intent = new Intent();
					intent.putExtra(GPKeyset.KEYSET, keyset);
					setResult(RESULT_OK, intent);
					finish();
				} else {
					Toast.makeText(AddKeysetActivity.this, "Please enter valid ID and Version", Toast.LENGTH_LONG).show();
				}
				
			}
		});

		mNegative = (Button) findViewById(R.id.btn_addkeyset_negative);
		mNegative.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				setResult(RESULT_CANCELED);
				finish();
			}
		});
	}
}
