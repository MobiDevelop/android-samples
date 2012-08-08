package com.mobidevelop.samples.postimage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URLConnection;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import com.android.internal.http.multipart.FilePart;
import com.android.internal.http.multipart.InputStreamPartSource;
import com.android.internal.http.multipart.MultipartEntity;
import com.mobidevelop.samples.postimage.App.PostResponseReceiver;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

public class PostIntentService extends IntentService {
	
	public PostIntentService() {
		super("PostIntentService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		String action = intent.getAction();	
		if (Intent.ACTION_SEND.equals(action)) {
			Uri imageUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
			if (imageUri != null) {
				doUpload(imageUri);
			}
		}
		else
		if (Intent.ACTION_SEND_MULTIPLE.equals(action)) {
			ArrayList<Uri> imageUris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
			if (imageUris != null) {
				Uri[] imageUrisArray = new Uri[imageUris.size()];
				imageUris.toArray(imageUrisArray);
				doUpload(imageUrisArray);
			}
		}		
	}

	/**Performs the upload. As this is just a sample, the destination is hard-coded.
	 * @param params The Uris of the items to upload.
	 */
	public void doUpload(Uri... params) {
		Intent broadcastIntent = new Intent();
		broadcastIntent.setAction(PostResponseReceiver.ACTION_RESULT);
		broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
		broadcastIntent.putExtra(PostResponseReceiver.EXTRA_RESULT, false);
		try
		{
			ContentResolver resolver = getContentResolver();
			
			HttpClient http = new DefaultHttpClient();
			HttpPost   post = new HttpPost("http://www.nexsoftware.net/uploads/process/");
			
			ArrayList<FilePart> files = new ArrayList<FilePart>();
			for (Uri uri : params) {
				String uriScheme = uri.getScheme();
				String uriAuthority = uri.getAuthority();
				String fileName = null;
				String mimeType = null;
				if (ContentResolver.SCHEME_CONTENT.equals(uriScheme)) {
					if (uriAuthority.equals(MediaStore.AUTHORITY)) {
						Cursor cursor = resolver.query(uri, new String[] { MediaStore.Images.Media.MIME_TYPE, MediaStore.Images.Media.DATA }, null, null, null);
						if (cursor != null && cursor.moveToFirst()) {
							int mimeIndex = cursor.getColumnIndex(MediaStore.Images.Media.MIME_TYPE);
							int dataIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
							mimeType = cursor.getString(mimeIndex);
							File file = new File(cursor.getString(dataIndex));
							cursor.close();
							fileName = file.getName();
						}						
					}
				}
				else if (ContentResolver.SCHEME_FILE.equals(uriScheme)) {
					File file = new File(uri.getPath());
					fileName = file.getName();
					mimeType = URLConnection.guessContentTypeFromName(fileName);
				}
				else {
					continue;
				}
				files.add(new FilePart("file[]", new InputStreamPartSource(fileName, resolver.openInputStream(uri)), mimeType, null));
			}
			FilePart[] parts = new FilePart[files.size()];
			files.toArray(parts);
			MultipartEntity entity = new MultipartEntity(parts);
			post.setEntity(entity);
			HttpResponse response = http.execute(post);
			String responseString = new BasicResponseHandler().handleResponse(response);
			broadcastIntent.putExtra(PostResponseReceiver.EXTRA_RESULT, true);
			broadcastIntent.putExtra(PostResponseReceiver.EXTRA_TEXT, responseString);
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		catch (IOException e) {			
			e.printStackTrace();
		}
		sendBroadcast(broadcastIntent);		
	}
}
