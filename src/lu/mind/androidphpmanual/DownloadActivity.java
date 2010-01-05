package lu.mind.androidphpmanual;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

public class DownloadActivity extends Activity {

	private ProgressDialog pd;
	private SharedPreferences myPrefs;

	private Spinner language;
	private Spinner mirror;
	private Button saveButton;
	private Button cancelButton;
	private String lang;
	private int delete;

	protected PowerManager.WakeLock mWakeLock;

	String[] languageValues = {
			"English",
			"Bulgarian",
			"Brazilian Portuguese", 
			"French",
			"German",
			"Japanese",
			"Korean",
			"Polish",
			"Romanian",
			"Turkish"
			//,"Testing"
	};
	String[] languageKeys = {
			"en",
			"bg",
			"pt_BR",
			"fr",
			"de",
			"ja",
			"kr",
			"pl",
			"ro",
			"tr"
			//,"test"
	};


	private class DownloadAndInstall extends AsyncTask<SharedPreferences, String, Boolean> {

		protected void onPreExecute() {
			pd = new ProgressDialog(DownloadActivity.this);
			pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			pd.setMessage("Please wait...");
			pd.setIndeterminate(false);
			pd.setCancelable(false);
			pd.show();
		}

		@Override
		protected Boolean doInBackground(SharedPreferences... prefs) {
			String destPath = "php-manual";
			String fileName = "phpmanual.zip";

			language = (Spinner) findViewById(R.id.lang_spinner);
			lang = languageKeys[language.getSelectedItemPosition()];

			class DeleteMe {
				boolean deleteDirectory(File path) {
					if (path.exists()) {
						File[] files = path.listFiles();
						pd.setMax(files.length);
						for(int i=0; i<files.length; i++) {
							if(files[i].isDirectory()) {
								deleteDirectory(files[i]);
							} else {
								//								publishProgress(new String[] {String.valueOf(i), "Deleting files in "+files[i].getParent()+"..."});
								//								publishProgress(new String[] {String.valueOf(i), "Deleting "+files[i].getName()+"..."});
								if ( ( i % 10 ) == 0 ) {
									publishProgress(new String[] {String.valueOf(i), "Deleting old files..."});
									//Log.d("delete", "Deleting "+files[i].getName());
								}
								files[i].delete();
							}
						}
					}
					return( path.delete() );
				}
			}

			//KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Activity.KEYGUARD_SERVICE);  
			//KeyguardLock lock = keyguardManager.newKeyguardLock(KEYGUARD_SERVICE);  

			try {
				//lock.disableKeyguard();

				File root = Environment.getExternalStorageDirectory();

				//if (delete == 1) {
				//pd = ProgressDialog.show(DownloadActivity.this, null, "Deleting old manual...", true, false);
				//pd.setMax(1);
				//publishProgress(new String[] {"0", "Deleting zip file..."});
				//File toDelete = new File(root,"phpmanual.zip");
				//toDelete.delete();
				//pd.dismiss();
				/*
					toDelete = new File(Environment.getExternalStorageDirectory()+"/"+destPath+"/html/images");
					if (toDelete.exists()) {
						File[] temp = toDelete.listFiles();
						pd.setMax(temp.length);
						for(int i=0; i<temp.length; i++) {
							publishProgress(new String[] {String.valueOf(i), "Deleting image files..."});
							temp[i].delete();
						}
						toDelete.delete();
					}

					toDelete = new File(Environment.getExternalStorageDirectory()+"/"+destPath+"/html");
					if (toDelete.exists()) {
						File[] temp = toDelete.listFiles();
						pd.setMax(temp.length);
						for(int i=0; i<temp.length; i++) {
							publishProgress(new String[] {String.valueOf(i), "Deleting html files..."});
							temp[i].delete();
						}
						toDelete.delete();
					}

					toDelete = new File(Environment.getExternalStorageDirectory()+"/"+destPath);
					if (toDelete.exists()) {
						toDelete.delete();
					}
				 */
				/*pd.setIndeterminate(true);
					pd.setMessage("Deleting...");
					pd.show();
				 */
				DeleteMe dm = new DeleteMe();
				dm.deleteDirectory(new File(Environment.getExternalStorageDirectory()+"/"+destPath));
				//pd.dismiss();
				//pd.setIndeterminate(false);
				//}

				publishProgress(new String[] {"0", "Downloading PHP manual..."});

				URL u = new URL("http://android-phpmanual.googlecode.com/files/php_manual_"+lang+".zip");
				HttpURLConnection c = (HttpURLConnection) u.openConnection();
				c.setRequestMethod("GET");
				c.setDoOutput(true);
				c.connect();
				Integer fileSize = Integer.parseInt(c.getHeaderField("Content-Length"));
				pd.setMax(fileSize/1024);
				Log.i("download size", ""+c.getHeaderField("Content-Length"));
				FileOutputStream f = new FileOutputStream(new File(root,"phpmanual.zip"));

				InputStream in = c.getInputStream();

				byte[] buffer = new byte[1024];
				int len1 = 0;
				int iteration = 0;
				int onePercent = fileSize / 100;
				int doneSoFar = 0;
				int progress = 1;
				while ( (len1 = in.read(buffer)) != -1 ) {
					f.write(buffer,0, len1);
					doneSoFar = doneSoFar + 1024;
					iteration++;
					if (doneSoFar > (onePercent * progress) ) {
						if ( ( (doneSoFar/1024) % 10 ) == 0 ) {
							publishProgress(new String[] {String.valueOf(doneSoFar/1024), "Downloading PHP manual..."});
						}
						progress++;
					}
					//Log.w("iteration", "I:"+iteration+", size: "+1024*iteration);
				}    
				f.close();
			} catch (Exception e) {
				Log.e("error in download", e.getMessage());
			} finally {
				//lock.reenableKeyguard();				
			}


			publishProgress(new String[] {"0", "Extracting Files..."});
			//extractFile("phpmanual.zip", "php-manual");

			destPath = Environment.getExternalStorageDirectory()+"/"+destPath;
			try {
				//publishProgress(new String[] {"0", "Extracting Files..."});
				ZipFile zipFile = new ZipFile(Environment.getExternalStorageDirectory()+"/"+fileName);

				boolean status;
				status = new File(destPath).mkdirs();
				Log.w("status of mkdir1", "s:"+status);

				Enumeration entries = zipFile.entries();

				pd.setMax(zipFile.size());

				int progress = 0;

				while(entries.hasMoreElements()) {
					progress++;
					ZipEntry entry = (ZipEntry)entries.nextElement();

					if(entry.isDirectory()) {
						// Assume directories are stored parents first then children.
						//Log.w("unzip", "Extracting directory: " + destPath+"/"+entry.getName());
						// This is not robust, just for demonstration purposes.
						status = new File(destPath+"/"+entry.getName()).mkdirs();
						//Log.w("status of mkdir2", "s:"+status);
						continue;
					}

					//Log.w("unzip", "Extracting file: " + destPath+"/"+entry.getName());

					if ( ( progress % 10 ) == 0 ) {
						publishProgress(new String[] {String.valueOf(progress), "Extracting Files..."});						
					}
					Tools.copyInputStream(zipFile.getInputStream(entry),
							new BufferedOutputStream(new FileOutputStream(destPath+"/"+entry.getName()), 4096));
				}

				zipFile.close();
				//pd.dismiss();

				//pd.setIndeterminate(true);
				//pd.show();

				//pd = ProgressDialog.show(DownloadActivity.this, null, "Deleting temporary file...", true, false);

				//				DeleteMe dm = new DeleteMe();
				//				dm.deleteDirectory(new File(Environment.getExternalStorageDirectory()+"/phpmanual.zip"));
				new File(Environment.getExternalStorageDirectory()+"/phpmanual.zip").delete();
				new File(destPath+"/.nomedia").createNewFile();
			} catch (IOException ioe) {
				Log.w("unzip", "Unhandled exception:"+ioe.getMessage());
			}

			publishProgress(new String[] {"100", "Done!"});
			return true;
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
		}

		@Override
		protected void onProgressUpdate(String... values) {
			super.onProgressUpdate(values);
			pd.setProgress(Integer.parseInt(values[0]));
			pd.setMessage(values[1]);
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			pd.dismiss();
			Intent intent = new Intent();
			startActivity(intent.setClass(DownloadActivity.this, MainActivity.class));
			finish();
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.download);

		/* This code together with the one in onDestroy()
		 * will make the screen be always on until this Activity gets destroyed. */
		// http://developer.android.com/intl/de/reference/android/os/PowerManager.html
		final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		this.mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "My Tag");
		this.mWakeLock.acquire();

		myPrefs = getSharedPreferences("PhpManualSettings", 0);

		Bundle bundle = getIntent().getExtras();
		if (bundle != null) delete = bundle.getInt("delete");

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(
				this,
				android.R.layout.simple_spinner_item,
				languageValues);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		Spinner s = (Spinner) findViewById(R.id.lang_spinner);
		s.setAdapter(adapter);

		//s.setOnClickListener(l)

		saveButton = (Button) findViewById(R.id.doDownload);
		saveButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//    			Toast.makeText(getBaseContext(),"Selected "+lang+"...", Toast.LENGTH_SHORT).show();
				new DownloadAndInstall().execute(myPrefs);
				//startService(new Intent(DownloadActivity.this, BackgroundService.class));
			}
		});

		cancelButton = (Button) findViewById(R.id.cancelDownload);
		cancelButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}

	@Override
	protected void onDestroy() {
		this.mWakeLock.release(); 
		super.onDestroy();
	}

}
