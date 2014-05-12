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
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SetInstallParamActivity extends Activity {
	private EditText mEditParams = null;
	private EditText mEditPrivileges = null;
	private Button mSetBtn = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_set_install_param);

		mEditParams = (EditText) findViewById(R.id.edit_parameter_parameters);
		mEditPrivileges = (EditText) findViewById(R.id.edit_parameter_privileges);
		mSetBtn = (Button) findViewById(R.id.btn_parameter_set);

		mSetBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				byte[] params = null;
				byte privileges = 0;
				if (mEditParams.getText() != null && !mEditParams.getText().toString().equals("")
						&& mEditPrivileges.getText() != null && !mEditPrivileges.getText().toString().equals("")) {
					if(mEditParams.getText().toString().length()%2 != 0){
						Toast.makeText(SetInstallParamActivity.this,
								"Please check your parameters. Parameters must be a hex value with odd number of values.",
								Toast.LENGTH_LONG).show();
					}
					else{
						params = GPUtils.convertHexStringToByteArray(mEditParams
								.getText().toString());
						privileges = (byte)Integer.parseInt(mEditPrivileges
										.getText().toString());
	
						Intent intent = new Intent();
						intent.putExtra("params", params);
						intent.putExtra("privileges", privileges);
						
						//check if privileges is valid
						if (Integer.parseInt(mEditPrivileges.getText().toString()) > 255
								|| Integer.parseInt(mEditPrivileges.getText()
										.toString()) < 0) {
							Toast.makeText(SetInstallParamActivity.this,
									"Please check your privileges",
									Toast.LENGTH_LONG).show();
						} else {
							setResult(RESULT_OK, intent);
							finish();
						}
					}
				} else {
					setResult(RESULT_OK);
					finish();
				}
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.set_install_param, menu);
		return true;
	}
}
