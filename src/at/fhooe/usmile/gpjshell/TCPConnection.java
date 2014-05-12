package at.fhooe.usmile.gpjshell;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import android.app.Activity;
import android.util.Log;

public class TCPConnection implements Runnable {

	private boolean mRunning = false;
	public static final short TCP_ADB_PORT = 9010;
	public static final String TCP_TEMP_CAP_FILE = "tmpapplet.cap";
	private static final String TCP_TEMP_CAP_DIRECTORY = "/tcpapplets/";
	private Activity mMainContext;
	private TCPFileResultListener mListener;
	private ServerSocket mServerSocket;

	public TCPConnection(Activity _context, TCPFileResultListener _listener) {
		mMainContext = _context;
		mListener = _listener;
	}

	public void stopConnection() {
		mRunning = false;
		if(mServerSocket!=null){
			try {
				mServerSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
				sendLogOutput("IOException, could not close connection" +e.getMessage());
			}
		}
	}

	@Override
	public void run() {
		mRunning = true;

		try {
			Boolean end = false;
			int u;
			mServerSocket = new ServerSocket(TCP_ADB_PORT);

			while (!end) {
				// Server is waiting for client here, if needed
				Socket s = mServerSocket.accept();
				// receive file
				byte[] jj = new byte[1024];
				InputStream is = s.getInputStream();
				BufferedInputStream get = new BufferedInputStream(is);
				PrintWriter output = new PrintWriter(s.getOutputStream());

				final File dir = new File(mMainContext.getFilesDir()
						+ TCP_TEMP_CAP_DIRECTORY);
				dir.mkdirs();
				final File file = new File(dir, TCP_TEMP_CAP_FILE);
				FileOutputStream fs = new FileOutputStream(file);

				u = 0;
				while ((u = get.read(jj, 0, 1024)) != -1) {
					fs.write(jj, 0, u);
				}

				fs.close();
				sendLogOutput("File received");

				output.println("Good bye and thanks for all the fish :)");
				s.close();

				mMainContext.runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						mListener.fileReceived("file://"+file.getAbsolutePath(), 1, 0, 1);
					}
				});
				
				if (!mRunning) {
					end = true;
				}
			}
//			mServerSocket.close();

		} 
		catch (SocketException e) {
			Log.d("SocketException", e.getMessage());
	    }
		catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			sendLogOutput("UnknownHostException" +e.getMessage());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			sendLogOutput("IOException " +e.getMessage());
		}

	}

	private void sendLogOutput(final String st) {
		mMainContext.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				MainActivity.log().d("TCP", "From client: " + st);
			}
		});
	}
}
