package org.andresoviedo.app.util.view;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import org.andresoviedo.dddmodel2.R;

public class TextActivity extends AppCompatActivity {

	private TextView text;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_text);

		Bundle b = getIntent().getExtras();
		String title = b.getString("title");
		setTitle(title);
		
		String value = b.getString("text");
		text = (TextView) findViewById(R.id.text_activity_text);
		text.setMovementMethod(LinkMovementMethod.getInstance());
		text.setText(value);
	}
}
