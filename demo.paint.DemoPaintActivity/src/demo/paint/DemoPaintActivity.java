package demo.paint;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Path;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore.Images.ImageColumns;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class DemoPaintActivity extends Activity implements OnClickListener {
	@SuppressWarnings("unused")
	private static final String TAG = "demo.paint.DemoPaintActivity";
    private static final int ACTION_LOAD_IMAGE = 10;
    private static final int ACTON_SAVING = 11;
	private Button mButtonload;
	private Button mButtonSave;
	private DemoCanvasView mDemoCanvasView;
//	private Bitmap mDecodeFile;
	
	private static List<Path> mPaths = new ArrayList<Path>();
	private static Bitmap mBitmap;
	private Button mButtonClear;
	

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mButtonload = (Button)findViewById(R.id.btn_load);
        mButtonload.setOnClickListener(this);
        mButtonSave = (Button)findViewById(R.id.btn_save);
        mButtonSave.setOnClickListener(this);
        mButtonClear = (Button)findViewById(R.id.btn_clear);
        mButtonClear.setOnClickListener(this);
        mDemoCanvasView = (DemoCanvasView)findViewById(R.id.canvas);
        mDemoCanvasView.setPaths(mPaths);
        mDemoCanvasView.setBitmap(mBitmap);
    }


    
    
    @Override
    protected Dialog onCreateDialog(int id, Bundle args) {
    	ProgressDialog progressDialog = new ProgressDialog(this);
    	progressDialog.setMessage("Saving...");
    	return progressDialog;
    }

	public void onClick(View v) {
		switch(v.getId()){
		case R.id.btn_load:
			Intent intent = new Intent();
			intent.setAction(Intent.ACTION_GET_CONTENT);
			intent.setType("image/png");
			startActivityForResult(intent, ACTION_LOAD_IMAGE);
			break;
		case R.id.btn_clear:
			if(mBitmap!=null){
				mBitmap.recycle();
				mBitmap = null;
			}
			mPaths.clear(); 
			mDemoCanvasView.setBitmap(null);
			break;
		case R.id.btn_save:
			if(mBitmap!=null){
				AsyncTask<Void, Void, Void> asyncTask = new AsyncTask<Void, Void, Void>(){
					
					@Override
					protected void onPreExecute() {
						super.onPreExecute();
						showDialog(ACTON_SAVING);
					}

					@Override
					protected Void doInBackground(Void... params) {
						if(!mBitmap.isMutable()){
							mBitmap = convertToMutable(mBitmap);
						}
						Canvas canvas = new Canvas(mBitmap);
						if(mPaths!=null){
							for(int i = 0; i<mPaths.size();i++){
								canvas.drawPath(mPaths.get(i), mDemoCanvasView.getPaint());
							}
						}
						
						File file = new File("/mnt/sdcard/samples/output"+System.currentTimeMillis()+".jpg");
						file.getParentFile().mkdirs();
						try {
							mBitmap.compress(CompressFormat.JPEG, 80, new FileOutputStream(file));
						} catch (FileNotFoundException e) {
							Log.i(TAG, "::onActivityResult:" + ""+Log.getStackTraceString(e));
						}
						return null;
					}
					
					@Override
					protected void onPostExecute(Void result) {
						mDemoCanvasView.setBitmap(mBitmap);
						removeDialog(ACTON_SAVING);
						super.onPostExecute(result);
					}
					
				};
				
				asyncTask.execute();
				

			}
			break;
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(resultCode==RESULT_OK){
			switch (requestCode) {
			case ACTION_LOAD_IMAGE:
				if(data!=null){
					Uri uri = data.getData();
					Cursor query = getContentResolver().query(uri, new String[]{ImageColumns.DATA}, null, null, null);
					if(query!=null && query.moveToFirst()){
						int columnIndex = query.getColumnIndex(ImageColumns.DATA);
						String path = query.getString(columnIndex);
						Log.i(TAG, "::onActivityResult:" + "path="+path);
						BitmapFactory.Options options = new BitmapFactory.Options();
						options.inPreferredConfig = Config.ARGB_8888;
						if(mBitmap!=null){
							mBitmap.recycle();
						}
						mBitmap = BitmapFactory.decodeFile(path,options);
						mDemoCanvasView.setBitmap(mBitmap);
					}
				}
				break;

			default:
				break;
			}
		}
	}

	private Bitmap convertToMutable(Bitmap bitmap) {
		try {
			File file = new File("/mnt/sdcard/sample/temp.txt");
			file.getParentFile().mkdirs();
			RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
			
			int width = bitmap.getWidth();
			int height = bitmap.getHeight();
			
			FileChannel channel = randomAccessFile.getChannel();
			MappedByteBuffer map = channel.map(MapMode.READ_WRITE, 0, width*height*4);
			bitmap.copyPixelsToBuffer(map);
			bitmap.recycle();
			
			bitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
			map.position(0);
			bitmap.copyPixelsFromBuffer(map);

			channel.close();
			randomAccessFile.close();
			
		} catch (FileNotFoundException e) {
			Log.i(TAG, "::onActivityResult:" + ""+Log.getStackTraceString(e));
		} catch (IOException e) {
			Log.i(TAG, "::onActivityResult:" + ""+Log.getStackTraceString(e));
		}
		return bitmap;
	}
	
}