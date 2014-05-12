/*******************************************************************************
 * Copyright (c) 2014 Michael Hölzl <mihoelzl@gmail.com>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Michael Hölzl <mihoelzl@gmail.com> - initial implementation
 *     Thomas Sigmund - data base, key set, channel set selection and GET DATA integration
 ******************************************************************************/
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
		mEditScpVersion = (EditText) findViewById(R.id.edit_channel_version);
		mEditSecurityLvl = (EditText) findViewById(R.id.edit_channel_security);
		mGemalto = (CheckBox) findViewById(R.id.chkbx_gemalto);

		mPositive = (Button) findViewById(R.id.btn_channel_positive);
		mNegative = (Button) findViewById(R.id.btn_channel_negative);

		mPositive.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				GPChannelSet channel = new GPChannelSet();
				if (mEditName.getText() != null) {
					channel.setChannelNameString(mEditName.getText().toString());
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
							"Please enter valid name", Toast.LENGTH_LONG).show();
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
