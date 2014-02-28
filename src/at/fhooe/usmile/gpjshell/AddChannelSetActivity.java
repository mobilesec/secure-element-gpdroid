package at.fhooe.usmile.gpjshell;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import at.fhooe.usmile.gpjshell.objects.GPChannelSet;

public class AddChannelSetActivity extends Activity {

	private EditText mEditName = null;
	private EditText mEditId = null;
	private EditText mEditSetName = null;
	private EditText mEditScpVersion = null;
	private EditText mEditSecurityLvl = null;
	private CheckBox mGemalto = null;

	private Button mPositive = null;
	private Button mNegative = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_channel_set);

		mEditName = (EditText) findViewById(R.id.edit_channel_name);
		mEditId = (EditText) findViewById(R.id.edit_channel_id);
		mEditSetName = (EditText) findViewById(R.id.edit_channel_setname);
		mEditScpVersion = (EditText) findViewById(R.id.edit_channel_version);
		mEditSecurityLvl = (EditText) findViewById(R.id.edit_channel_security);
		mGemalto = (CheckBox) findViewById(R.id.chkbx_gemalto);

		mPositive = (Button) findViewById(R.id.btn_channel_positive);
		mNegative = (Button) findViewById(R.id.btn_channel_negative);

		mPositive.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				GPChannelSet channel = new GPChannelSet();
				if (mEditId.getText() != null) {
					channel.setChannelId(Integer.valueOf(mEditId.getText().toString()));
					if (mEditName.getText() != null)
						channel.setChannelNameString(mEditName.getText().toString());
					if (mEditSetName.getText() != null)
						channel.setChannelSet(Integer.valueOf(mEditSetName.getText().toString()));
					if (mEditScpVersion.getText() != null)
						channel.setScpVersion(Integer.valueOf(mEditScpVersion.getText().toString()));
					if (mEditSecurityLvl.getText() != null)
						channel.setSecurityLevel(Integer.valueOf(mEditSecurityLvl.getText().toString()));
					channel.setGemalto(mGemalto.isChecked());
					
					Intent intent = new Intent();
					intent.putExtra(GPChannelSet.CHANNEL_SET, channel);
					setResult(RESULT_OK, intent);
					finish();

				} else {
					Toast.makeText(AddChannelSetActivity.this,
							"Please enter valid ID", Toast.LENGTH_LONG).show();
				}
			}
		});

		mNegative.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				setResult(RESULT_CANCELED);
				finish();
			}
		});
	}
}
