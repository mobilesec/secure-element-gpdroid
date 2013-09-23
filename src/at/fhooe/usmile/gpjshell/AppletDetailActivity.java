package at.fhooe.usmile.gpjshell;

import net.sourceforge.gpj.cardservices.AID;
import net.sourceforge.gpj.cardservices.AIDRegistryEntry;
import net.sourceforge.gpj.cardservices.GPUtil;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class AppletDetailActivity extends DialogFragment {

	public interface NoticeAppletEventListener {
		public void onDialogDeleteClick(DialogFragment dialog);

		public void onDialogOkClick(DialogFragment dialog);
	}

	// Use this instance of the interface to deliver action events
	private NoticeAppletEventListener mListener;

	private TextView mAppletTitle, mAppletIDs, mAppletPriviliges;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		LayoutInflater inflater = getActivity().getLayoutInflater();

		View v = inflater.inflate(R.layout.activity_applet_detail, null);
		builder.setView(v)
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						mListener.onDialogOkClick(AppletDetailActivity.this);
					}
				})
				.setNegativeButton("Delete",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface _dialog, int id) {
								AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
						        builder.setMessage("Are you sure you want to delete the Applet with all including AIDs?")
						               .setPositiveButton("Yes, delete!", new DialogInterface.OnClickListener() {
						                   public void onClick(DialogInterface dialog, int id) {
						                       mListener.onDialogDeleteClick(AppletDetailActivity.this);
						                   }
						               })
						               .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
						                   public void onClick(DialogInterface dialog, int id) {
						                	   mListener.onDialogOkClick(AppletDetailActivity.this);
						                   }
						               });
						        // Create the AlertDialog object and return it
						        builder.create().show();
							}
						});
		// setContentView(R.layout.activity_applet_detail);

		mAppletTitle = (TextView) v.findViewById(R.id.detAppl_readableTitle);
		mAppletIDs = (TextView) v.findViewById(R.id.detAppl_allIDs);
		mAppletPriviliges = (TextView) v.findViewById(R.id.detAppl_priviliges);

		setupAppletDataDetails(builder);

		return builder.create();
	}

	private void setupAppletDataDetails(Builder _builder) {
		AIDRegistryEntry data = GPConnection.getInstance().getSelectedApplet();
		_builder.setTitle(data.getAID().toString());

		String aIDText = "", spaces = "";
		int numSpaces = 0;

		String aid = GPUtil.byteArrayToReadableString(data.getAID().getBytes());
		mAppletTitle.setText("Readable string: "+ aid.substring(1, aid.length()-1));
		mAppletPriviliges.setText(String.format("Kind: %s LifeCyc: %d Priv: 0x%02X\n", data
				.getKind().toShortString(), data.getLifeCycleState(), data
				.getPrivileges()));
		for (AID a : data.getExecutableAIDs()) {
			numSpaces = (10 - a.getLength()) * 3;
			spaces = "";
			for (int i = 0; i < numSpaces; i++) {
				spaces = spaces + " ";
			}
			aIDText += "" + GPUtil.byteArrayToString(a.getBytes())
					+ spaces + " "
					+ GPUtil.byteArrayToReadableString(a.getBytes()) + "\n";
		}

		mAppletIDs.setText(aIDText);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mListener = (NoticeAppletEventListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement NoticeDialogListener");
		}
	}

}
