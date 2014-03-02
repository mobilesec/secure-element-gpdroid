package at.fhooe.usmile.gpjshell;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class GetDataActivity extends Activity {
	
	private Button mButtonOk;
	private Button mButtonCancel;
	private EditText mEditP1;
	private EditText mEditP2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_get_data);
		
		mEditP1 = (EditText) findViewById(R.id.edit_getdata_p1);
		mEditP2 = (EditText) findViewById(R.id.edit_getdata_p2);
		mButtonOk = (Button) findViewById(R.id.btn_getdata_ok);
		mButtonCancel = (Button) findViewById(R.id.btn_getdata_cancel);
		
		mButtonOk.setOnClickListener(new OnClickListener() {
			
			
			@Override
			public void onClick(View v) {
				//if both parameters are set, return these parameters as byte[] to calling activity
				if (mEditP1.getText() != null && mEditP2.getText() != null) {
					int p1 = Integer.parseInt(mEditP1.getText().toString(),16);
					int p2 = Integer.parseInt(mEditP2.getText().toString(),16);
					
					Intent intent = new Intent();
					intent.putExtra("p1", p1);
					intent.putExtra("p2", p2);
					
					setResult(RESULT_OK, intent);
					finish();
					
				} else {
					Toast.makeText(GetDataActivity.this, "Please check your parameters", Toast.LENGTH_LONG).show();
				}
			}
		});
		
		mButtonCancel.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				setResult(RESULT_CANCELED);
				finish();
			}
		});
		
	}
}
