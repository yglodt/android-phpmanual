package lu.mind.androidphpmanual;


import android.app.Activity;
import android.os.Bundle;
import lu.mind.androidphpmanual.R;

public class About extends Activity {

	//private Button closeButton;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);

		/*closeButton = (Button) this.findViewById(R.id.closeAbout);
    	closeButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
    	});
		 */
	}
}
