package fr.skyost.launcher.utils;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.logging.Level;

import javax.net.ssl.HttpsURLConnection;

import fr.skyost.launcher.LauncherConstants;

public class ConnectionUtils {

	public static final boolean isOnline() {
		try {
			((HttpURLConnection)new URL("http://www.google.com").openConnection()).getContent();
			return true;
		}
		catch(final Exception ex) {}
		return false;
	}

	public static final String httpGet(final String url) throws IOException {
		final HttpURLConnection connection = (HttpURLConnection)new URL(url).openConnection();
		connection.setRequestMethod("GET");
		connection.setRequestProperty("User-Agent", LauncherConstants.LAUNCHER_NAME + " v" + LauncherConstants.LAUNCHER_VERSION);
		final String response = connection.getResponseCode() + " " + connection.getResponseMessage();
		if(!response.startsWith("200")) {
			LogUtils.log(Level.SEVERE, "Invalid response : " + response);
			return null;
		}
		final BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		String line;
		final StringBuilder builder = new StringBuilder();
		while((line = in.readLine()) != null) {
			builder.append(line);
		}
		in.close();
		return builder.toString();
	}

	public static final String httpPost(final String url, final String parameters) throws IOException {
		final HttpsURLConnection connection = (HttpsURLConnection)new URL(url).openConnection();
		connection.setRequestMethod("POST");
		connection.setRequestProperty("User-Agent", LauncherConstants.LAUNCHER_NAME + " v" + LauncherConstants.LAUNCHER_VERSION);
		connection.setDoOutput(true);
		final DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
		wr.writeBytes(parameters);
		wr.flush();
		wr.close();
		final String response = connection.getResponseCode() + " " + connection.getResponseMessage();
		if(!response.startsWith("200")) {
			LogUtils.log(Level.SEVERE, "Invalid response : " + response);
			return null;
		}
		final BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		String line;
		final StringBuilder builder = new StringBuilder();
		while((line = in.readLine()) != null) {
			builder.append(line);
		}
		in.close();
		return builder.toString();
	}

	public static final String httpJsonPost(final String url, final String json) throws UnsupportedEncodingException, IOException {
		final HttpsURLConnection connection = (HttpsURLConnection)new URL(url).openConnection();
		byte[] payloadAsBytes = json.getBytes(Charset.forName("UTF-8"));
		connection.setRequestMethod("POST");
		connection.setRequestProperty("User-Agent", LauncherConstants.LAUNCHER_NAME + " v" + LauncherConstants.LAUNCHER_VERSION);
		connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
		connection.setRequestProperty("Content-Length", String.valueOf(payloadAsBytes.length));
		connection.setUseCaches(false);
		connection.setDoInput(true);
		connection.setDoOutput(true);
		final DataOutputStream outStream = new DataOutputStream(connection.getOutputStream());
		outStream.write(payloadAsBytes);
		outStream.flush();
		outStream.close();
		InputStream inStream;
		try {
			inStream = connection.getInputStream();
		}
		catch(Exception e) {
			inStream = connection.getErrorStream();
		}
		final StringBuilder builder = new StringBuilder();
		final byte[] buffer = new byte[1024];
		int bytesRead;
		while((bytesRead = inStream.read(buffer)) > 0) {
			builder.append(new String(buffer, "UTF-8").substring(0, bytesRead));
		}
		return builder.toString();
	}

	public static final boolean download(final String site, final File pathTo) {
		try {
			final HttpURLConnection connection = (HttpURLConnection)new URL(site).openConnection();
			connection.addRequestProperty("User-Agent", LauncherConstants.LAUNCHER_NAME + " v" + LauncherConstants.LAUNCHER_VERSION);
			final String response = connection.getResponseCode() + " " + connection.getResponseMessage();
			if(!response.startsWith("200")) {
				LogUtils.log(Level.INFO, "Invalid response : " + response);
				return false;
			}
			final long size = connection.getContentLengthLong();
			long lastPercent = 0;
			long percent = 0;
			float totalDataRead = 0;
			final InputStream inputStream = connection.getInputStream();
			final FileOutputStream fileOutputStream = new FileOutputStream(pathTo);
			final BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream, 1024);
			final byte[] data = new byte[1024];
			int i = 0;
			while((i = inputStream.read(data, 0, 1024)) >= 0) {
				totalDataRead += i;
				bufferedOutputStream.write(data, 0, i);
				percent = ((long)(totalDataRead * 100) / size);
				if(lastPercent != percent) {
					lastPercent = percent;
					LogUtils.append(percent + "%");
				}
			}
			bufferedOutputStream.close();
			fileOutputStream.close();
			inputStream.close();
			LogUtils.log(null, null);
			return true;
		}
		catch(final Exception ex) {
			ex.printStackTrace();
		}
		return false;
	}
	
}
