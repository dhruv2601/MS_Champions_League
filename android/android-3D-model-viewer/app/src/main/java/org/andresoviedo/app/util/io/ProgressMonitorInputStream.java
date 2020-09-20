package org.andresoviedo.app.util.io;

import android.support.v7.app.AppCompatActivity;

import java.io.IOException;
import java.io.InputStream;

/**
 * Dummy InputStream wrapper. No implementation yet.
 *
 * @author andresoviedo
 */

public class ProgressMonitorInputStream extends InputStream {

	private final AppCompatActivity parentActivity;
	private final InputStream stream;

	public ProgressMonitorInputStream(AppCompatActivity parentActivity, String text, InputStream stream) {
		this.parentActivity = parentActivity;
		this.stream = stream;
	}

	@Override
	public int read() throws IOException {
		return stream.read();
	}
}
