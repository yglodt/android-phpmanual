package lu.mind.androidphpmanual;

import java.io.File;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class MainActivity extends Activity {

	private ProgressDialog pd;
	private WebView webView;
	SharedPreferences myPrefs;

	//add code to preserve view on rotate 

	/*
	private static void extractFile(String fileName, String destPath) {		

		destPath = Environment.getExternalStorageDirectory()+"/"+destPath;
		try {
			ZipFile zipFile = new ZipFile(Environment.getExternalStorageDirectory()+"/"+fileName);

			boolean status;
			status = new File(destPath).mkdirs();
			Log.w("status of mkdir1", "s:"+status);

			Enumeration entries = zipFile.entries();

			int i1 = 3;
			int i2 = 2;
			Log.w("test", "3/2: "+i1/i2);

			while(entries.hasMoreElements()) {
				ZipEntry entry = (ZipEntry)entries.nextElement();

				if(entry.isDirectory()) {
					// Assume directories are stored parents first then children.
					Log.w("unzip", "Extracting directory: " + destPath+"/"+entry.getName());
					// This is not robust, just for demonstration purposes.
					status = new File(destPath+"/"+entry.getName()).mkdirs();
					Log.w("status of mkdir2", "s:"+status);
					continue;
				}

				Log.w("unzip", "Extracting file: " + destPath+"/"+entry.getName());
				copyInputStream(zipFile.getInputStream(entry),
						new BufferedOutputStream(new FileOutputStream(destPath+"/"+entry.getName())));
			}

			zipFile.close();
		} catch (IOException ioe) {
			Log.w("unzip", "Unhandled exception:"+ioe.getMessage());
			return;
		}


	}
	 */
	/*
	private final OnClickListener mOkListener = new OnClickListener() {
        public void onClick(DialogInterface dialog, int whichButton) {
        	new DownloadAndInstall().execute(myPrefs);
        	return;
            //	MainActivity.this.finish();
        }
    };

	private final OnClickListener mNokListener = new OnClickListener() {
        public void onClick(DialogInterface dialog, int whichButton) {
            MainActivity.this.finish();
        }
    };
	 */

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// http://blog.tourizo.com/2009/02/how-to-display-local-file-in-android.html
		// http://www.techjini.com/blog/2009/01/10/android-tip-1-contentprovider-accessing-local-file-system-from-webview-showing-image-in-webview-using-content/
		super.onCreate(savedInstanceState);

		myPrefs = getSharedPreferences("PhpManualSettings", 0);

		//Log.d("path1", getFilesDir().getAbsolutePath());
		//Log.d("path2", Environment.getExternalStorageDirectory()+"/aaa");

		getWindow().requestFeature(Window.FEATURE_PROGRESS);

		setContentView(R.layout.main);
		boolean filesDoExist = new File(Environment.getExternalStorageDirectory()+"/php-manual/html/index.html").exists();

		if (!filesDoExist) {
			Log.d("php-manual", "Files to not exist, redirecting to download view");
			Intent i = new Intent();
			startActivity(i.setClass(MainActivity.this, DownloadActivity.class));
			finish();
			// start download view
			/*new AlertDialog.Builder(this)
			.setMessage("Do you want to download and extract the PHP documentation (15MB) now?")
			.setPositiveButton("Yes", mOkListener)
			.setNeutralButton("No, not now", mNokListener)
			.setCancelable(false)
			.show();*/
		} else {
			Log.d("php-manual", "Files exist, showing them");
			webView = (WebView) findViewById(R.id.webview);
			webView.setWebViewClient(new PhpManualView());
			webView.getSettings().setJavaScriptEnabled(false);
			webView.loadUrl("content://lu.mind.androidphpmanual/sdcard/php-manual/html/index.html");
			restoreState(savedInstanceState);				

			final Activity activity = this;
			webView.setWebChromeClient(new WebChromeClient() {
				public void onProgressChanged(WebView view, int progress) {
					// Activities and WebViews measure progress with different scales.
					// The progress meter will automatically disappear when we reach 100%
					activity.setProgress(progress * 1000);
				}
			});

			webView.setWebViewClient(new WebViewClient() {
				public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
					//Toast.makeText(activity, description, Toast.LENGTH_SHORT).show();
				}
			});        
		}
	}


	private class PhpManualView extends WebViewClient {
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			view.loadUrl(url);
			return true;
		}
	}


	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		Log.d("path", webView.getUrl());
		if ((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()) {
			if (webView.getUrl().endsWith("/html/index.html")) {
				finish();
			} else {
				webView.goBack();				
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		//menu.add(0, 1234, 0, "Back to index");
		//menu.add(0, 1234, 0, "Re-download documentation");
		inflater.inflate(R.menu.menu_main, menu);
		return true;
	}

	/* Handles item selections */
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent i = new Intent();
		Dialog d = new Dialog(MainActivity.this);
		Window window = d.getWindow();
		switch (item.getItemId()) {
		case R.id.mainMenuReturnToIndex:
			webView.loadUrl("content://lu.mind.androidphpmanual/sdcard/php-manual/html/index.html");
			return true;
		case R.id.mainMenuReDownloadDocumentation:
			i.putExtra("delete", 1);
			startActivity(i.setClass(MainActivity.this, DownloadActivity.class));
			//startActivityForResult(i.setClass(MainView.this, UserSettings.class), 2);
			return true;
		case R.id.about:
			window.setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND, WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
			window.requestFeature(Window.FEATURE_NO_TITLE);
			d.setContentView(R.layout.about);
			d.show();
			//startActivityForResult(i.setClass(MainActivity.this, About.class), 3);
			return true;
			//case R.id.quit:
			//    this.finish();
		}
		return false;
	}

	
	private void restoreState(Bundle state) {
		if (state != null) {
			String lastUrl = state.getString("lastUrl");
			if ((webView != null) && (lastUrl != null)) {
				webView.loadUrl(lastUrl);
			}
		} else {
			Toast.makeText(this, "not restoring state, bundle was null.", Toast.LENGTH_SHORT).show(); 			
		}
	}


	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		Toast.makeText(this, "onRestoreInstanceState", Toast.LENGTH_SHORT).show(); 
		restoreState(savedInstanceState);
	}


	@Override
	protected void onSaveInstanceState(Bundle outState) {
		Toast.makeText(this, "onSaveInstanceState", Toast.LENGTH_SHORT).show();
		if (webView != null) {
			outState.putString("lastUrl", webView.getUrl());
			Toast.makeText(this, "saving state", Toast.LENGTH_SHORT).show(); 
		}
		super.onSaveInstanceState(outState);
	}
	
    protected void onStart() {    	
		Toast.makeText(this, "onStart", Toast.LENGTH_SHORT).show();
    }
    
    protected void onRestart() {
		Toast.makeText(this, "onRestart", Toast.LENGTH_SHORT).show();
    }

    protected void onResume() {
		Toast.makeText(this, "onResume", Toast.LENGTH_SHORT).show();
    }

    protected void onPause() {
		Toast.makeText(this, "onPause", Toast.LENGTH_SHORT).show();
    }

    protected void onStop() {
		Toast.makeText(this, "onStop", Toast.LENGTH_SHORT).show();
    }

    protected void onDestroy() {
		Toast.makeText(this, "onDestroy", Toast.LENGTH_SHORT).show();
    }

}