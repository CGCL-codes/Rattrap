package com.example.ocr;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import org.jason.lxcoff.lib.Configuration;
import org.jason.lxcoff.lib.ControlMessages;
import org.jason.lxcoff.lib.ExecutionController;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.Settings.Secure;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

@TargetApi(Build.VERSION_CODES.GINGERBREAD)
@SuppressLint("NewApi")
public class OCRActivity extends Activity implements OnClickListener,OnSharedPreferenceChangeListener {
	//private TessOCR mTessOCR;
	
	private OffOCR mTessOCR;
	private TextView mResult;
	private ProgressDialog mProgressDialog;
	private ImageView mImage;
	private Button mButtonGallery, mButtonCamera;
	private String mCurrentPhotoPath;
	private static final int REQUEST_TAKE_PHOTO = 1;
	private static final int REQUEST_PICK_PHOTO = 2;
	
	private int picIndex = 0;
	
	SharedPreferences settings;
	
	private String TAG = "OCRActivity";
	private String imageFilePath = "/system/off-app/off-file/";
	
	Context context;
	private Configuration		config;
	private ExecutionController executionController;
	private Socket dirServiceSocket = null;
	InputStream is					= null;
	OutputStream os					= null;
	ObjectOutputStream oos			= null;
	ObjectInputStream ois			= null;
	
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		settings = PreferenceManager.getDefaultSharedPreferences(this); 
        settings.registerOnSharedPreferenceChangeListener(this);  

		mResult = (TextView) findViewById(R.id.tv_result);
		mImage = (ImageView) findViewById(R.id.image);
		mButtonGallery = (Button) findViewById(R.id.bt_gallery);
		mButtonGallery.setOnClickListener(this);
		mButtonCamera = (Button) findViewById(R.id.bt_camera);
		mButtonCamera.setOnClickListener(this);
		
		this.context = getApplicationContext();
		
		// I wanna use network in main thread, so ...
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        
        ExecutionController.myId = Secure.getString(context.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
        createNotOffloadedFile();
        try {
        	getInfoFromDirService();

        } catch (FileNotFoundException e) {
        	Log.e(TAG, "Could not read the config file: " + ControlMessages.PHONE_CONFIG_FILE);
			return ;
        } /*catch (UnknownHostException e) {
			Log.e(TAG, "Could not connect: " + e.getMessage());
		} */catch (IOException e) {
			Log.e(TAG, "IOException: " + e.getMessage());
		//return ;
		} catch (ClassNotFoundException e) {
			Log.e(TAG, "Could not find Clone class: " + e.getMessage());
			return;
		}
	// Create an execution controller	

        this.executionController = new ExecutionController(
				this.dirServiceSocket,
				is, os, ois, oos,
				context.getPackageName(),
				context.getPackageManager(),
				context);
        
        mTessOCR = new OffOCR(executionController);

        readPrefs();
	}
	
	/**
	 * Create an empty file on the phone in order to let the method know
	 * where is being executed (on the phone or on the clone).
	 */
	private void createNotOffloadedFile(){
		try {
			File f = new File(ControlMessages.FILE_NOT_OFFLOADED);
			f.createNewFile();
		} catch (FileNotFoundException e) {
			Log.e(TAG, e.getMessage());
		} catch (IOException e) {
			Log.e(TAG, e.getMessage());
		}
	}
	
	/**
	 * Read the config file to get the IP and port for DirectoryService.
	 * @throws IOException 
	 * @throws UnknownHostException 
	 * @throws ClassNotFoundException 
	 */
	private void getInfoFromDirService() throws UnknownHostException, IOException, ClassNotFoundException {
		config = new Configuration(ControlMessages.PHONE_CONFIG_FILE);
		config.parseConfigFile(null, null);
	    
	    try{
	    	dirServiceSocket = new Socket();
	    	Log.d(TAG,"DirService IP:"+config.getDirServiceIp());
	    	Log.d(TAG,"DirService Port:"+config.getDirServicePort());
			dirServiceSocket.connect(new InetSocketAddress(config.getDirServiceIp(), config.getDirServicePort()), 30000);
				
			os = dirServiceSocket.getOutputStream();
			is = dirServiceSocket.getInputStream();

			os.write(ControlMessages.PHONE_CONNECTION);
			
			oos = new ObjectOutputStream(os);
			ois = new ObjectInputStream(is);

				// Send the name and id to DirService
			os.write(ControlMessages.PHONE_AUTHENTICATION);
			oos.writeObject(ExecutionController.myId);
			oos.flush();
			
	     	}
	    	catch(IOException e){
	    		e.printStackTrace();
	    	}
	  } 
	
	private void uriOCR(Uri uri) {
		if (uri != null) {
			InputStream is = null;
			try {
				is = getContentResolver().openInputStream(uri);
				Bitmap bitmap = BitmapFactory.decodeStream(is);
				mImage.setImageBitmap(bitmap);
				doOCR(bitmap);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				if (is != null) {
					try {
						is.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	private void readPrefs() {
        boolean alwaysLocal = settings.getBoolean("alwaysLocal", false);
        Log.d(TAG, "alwaysLocal is " + alwaysLocal);
        if(alwaysLocal){
        	this.executionController.setUserChoice(ControlMessages.STATIC_LOCAL);
        }else{
        	this.executionController.setUserChoice(ControlMessages.USER_CARES_ONLY_ENERGY);
        }
    }
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();

		Intent intent = getIntent();
		if (Intent.ACTION_SEND.equals(intent.getAction())) {
			Uri uri = (Uri) intent
					.getParcelableExtra(Intent.EXTRA_STREAM);
			uriOCR(uri);
		}
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		switch (item.getItemId()) {
        case R.id.action_settings:
        	Intent mIntent = new Intent();  
            mIntent.setClass(this, Preferences.class);  
            startActivity(mIntent);  
            break;
        }
        return super.onOptionsItemSelected(item);  
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();

		//mTessOCR.onDestroy();
	}

	private void dispatchTakePictureIntent() {
		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		// Ensure that there's a camera activity to handle the intent
		if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
			// Create the File where the photo should go
			File photoFile = null;
			try {
				photoFile = createImageFile();
			} catch (IOException ex) {
				// Error occurred while creating the File

			}
			// Continue only if the File was successfully created
			if (photoFile != null) {
				takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
						Uri.fromFile(photoFile));
				startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
			}
		}
	}

	/**
	 * http://developer.android.com/training/camera/photobasics.html
	 */
	@SuppressLint("SimpleDateFormat")
	private File createImageFile() throws IOException {
		// Create an image file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
				.format(new Date());
		String imageFileName = "JPEG_" + timeStamp + "_";
		String storageDir = Environment.getExternalStorageDirectory()
				+ "/TessOCR";
		File dir = new File(storageDir);
		if (!dir.exists())
			dir.mkdir();

		File image = new File(storageDir + "/" + imageFileName + ".jpg");

		// Save a file: path for use with ACTION_VIEW intents
		mCurrentPhotoPath = image.getAbsolutePath();
		return image;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		if (requestCode == REQUEST_TAKE_PHOTO
				&& resultCode == Activity.RESULT_OK) {
			setPic();
		}
		else if (requestCode == REQUEST_PICK_PHOTO
				&& resultCode == Activity.RESULT_OK) {
			Uri uri = data.getData();
			if (uri != null) {
				uriOCR(uri);
			}
		}
	}

	private void setPic() {
		// Get the dimensions of the View
		int targetW = mImage.getWidth();
		int targetH = mImage.getHeight();

		// Get the dimensions of the bitmap
		BitmapFactory.Options bmOptions = new BitmapFactory.Options();
		bmOptions.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
		int photoW = bmOptions.outWidth;
		int photoH = bmOptions.outHeight;

		// Determine how much to scale down the image
		int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

		// Decode the image file into a Bitmap sized to fill the View
		bmOptions.inJustDecodeBounds = false;
		bmOptions.inSampleSize = scaleFactor << 1;
		bmOptions.inPurgeable = true;

		Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
		mImage.setImageBitmap(bitmap);
		doOCR(bitmap);

	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		int id = v.getId();
		switch (id) {
		case R.id.bt_gallery:
			//pickPhoto();
			
			long stime = System.nanoTime();
			
			OffOcr2 offocr = new OffOcr2(this.executionController);

			Log.i(TAG, "OCR Begins. ");
			
			int target = 0;
			
			String result = null;
			for(int i = 0; i<20; i++){
				result = offocr.DoOCR(this.imageFilePath + "ocr" + i + ".png");
			}
			
			long dura = System.nanoTime() - stime;

			//Log.i(TAG, "OCR target is " + target + ".Result text: " + result + ". Cost " + dura/1000000 + "ms.");
			int sublen = result.length() > 20 ? 20 : result.length();
			Toast.makeText(this, "OCR target is " + target + ".Result text: " + result.substring(0, sublen) + ". Cost " + dura/1000000 + "ms.", Toast.LENGTH_LONG).show();
			picIndex++;
			break;
		case R.id.bt_camera:
			takePhoto();
			break;
		}
	}
	
	private void pickPhoto() {
		Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		startActivityForResult(intent, REQUEST_PICK_PHOTO);
	}

	private void takePhoto() {
		dispatchTakePictureIntent();
	}

	private void doOCR(final Bitmap bitmap) {
		if (mProgressDialog == null) {
			mProgressDialog = ProgressDialog.show(this, "Processing",
					"Doing OCR...", true);
		}
		else {
			mProgressDialog.show();
		}
		
		new Thread(new Runnable() {
			public void run() {
				//final String result = mTessOCR.getOCRResult(bitmap);
				final ScanResult result = mTessOCR.doOCR(bitmap);
				Log.e(TAG,"Result ="+result.ocrResult);

				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						if (result != null && !result.equals("")) {
							mResult.setText(result.ocrResult);
							/*Toast.makeText(getApplicationContext(), "Cost :" +result.costTime+" s",
								     Toast.LENGTH_SHORT).show();*/
						}

						mProgressDialog.dismiss();
					}

				});

			};
		}).start();
	}
	
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		// TODO Auto-generated method stub
		readPrefs();
	}
}
