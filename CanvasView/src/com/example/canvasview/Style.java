package com.example.canvasview;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;
/**
 *extends Activity 
 *holds the arrayList of Type TouchView to keep track of the object creation
 */
public class Style extends Activity {
	 
    private final static String TAG = "TEST";
 
    // to take a picture
    private static final int CAMERA_PIC_REQUEST = 1111;
    private static final int GALLERY_PIC_REQUEST = 1112;
 
    private Button mCameraButton; // get the img from either camera or gallery
    private Button mBwButton; // change color to black / white
    private Button mTrashButton; // delete obj 
 
    // current view is the current selected view 
    private int mCurrentView = 0;
 
    public int getmCurrentView() {
        return mCurrentView;
    }
 
    public void setmCurrentView(int mCurrentView) {
        this.mCurrentView = mCurrentView;
    }
 
    // the number of views we currently have.
    private int mViewsCount = 0;
 
    private ArrayList<View> mViewsArray = new ArrayList<View>();
 
    private static Style mStyle;
 
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.main);
 
        mStyle = this;
 
        mTrashButton = (Button) findViewById(R.id.trash_button);
        mTrashButton.setClickable(true);
        mTrashButton.setOnClickListener(new View.OnClickListener() {
 
            public void onClick(View v) {
                Log.i(TAG,"Trash clicked");
                Log.i(TAG,"Array size is: "+mViewsArray.size());
                if (mViewsArray.size() > 0){
                    Log.i(TAG,"Should remove this view");
                    RelativeLayout layout = (RelativeLayout) findViewById(R.id.style_layout);
                    layout.removeView(mViewsArray.get(mCurrentView));
                    mViewsArray.remove(mCurrentView);
                    mViewsCount -=1;
                }
 
            }
        });
 
        mCameraButton = (Button) findViewById(R.id.camera_button);
        mCameraButton.setClickable(true);
        mCameraButton.setOnClickListener(new View.OnClickListener() {
 
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setTitle("Select:");
                final CharSequence[] chars = {"Take Picture", "Choose from Gallery"};
                builder.setItems(chars, new android.content.DialogInterface.OnClickListener(){
 
                    public void onClick(DialogInterface dialog, int which) {
                        if(which == 0){
                            Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                            startActivityForResult(cameraIntent, CAMERA_PIC_REQUEST);
                        }else
                            if(which == 1){
                                Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                                startActivityForResult(intent,GALLERY_PIC_REQUEST);
                            }
                        dialog.dismiss();
                    }
 
                }
                );
                builder.show();
            }
        });
 
        mBwButton = (Button) findViewById(R.id.bw_button);
        mBwButton.setClickable(true);
        mBwButton.setOnClickListener(new View.OnClickListener() {
 
            public void onClick(View v) {
                if (mViewsArray.size() > 0){
                    ((TouchView) mViewsArray.get(mCurrentView)).greyScaler();
                }
                else{
                    Toast.makeText(v.getContext(), "Please select an image first before using this function.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        
       
        if (requestCode == CAMERA_PIC_REQUEST) {
            try{
                Uri selectedImage = data.getData();
                getPath(selectedImage);
                InputStream is;
                is = getContentResolver().openInputStream(selectedImage);
                BitmapFactory.Options opts = new BitmapFactory.Options();
                opts.inJustDecodeBounds = true;
                //BitmapFactory.decodeStream(bis,null,opts);
                BitmapFactory.decodeStream(is,null,opts);
 
                //The new size we want to scale to
                final int REQUIRED_SIZE=200;
 
                
                int scale=1;
                while(opts.outWidth/scale/2>=REQUIRED_SIZE || opts.outHeight/scale/2>=REQUIRED_SIZE)
                    scale*=2;
 
                Log.i(TAG,"Scale is: "+scale);
                opts.inSampleSize = scale;
                opts.inJustDecodeBounds = false;
                is = null;
                System.gc();
                InputStream is2 = getContentResolver().openInputStream(selectedImage);
 
                Bitmap returnedImage = BitmapFactory.decodeStream(is2, null, opts);
                Log.i(TAG,"Image width from bitmap: "+returnedImage.getWidth());
                Log.i(TAG,"Image height from bitmap: "+returnedImage.getHeight());
                Log.i(TAG,"Creating another View");
                TouchView newView = new TouchView(this,mStyle,new BitmapDrawable(returnedImage),mViewsCount,1f);
                newView.setImageLocation(getPath(selectedImage));
                newView.setClickable(true);
                
                // ensure red border is drawn on new selected image
                newView.setmSelected(true);
                mViewsArray.add(newView);
                RelativeLayout layout = (RelativeLayout) findViewById(R.id.style_layout);
                layout.addView(mViewsArray.get(mViewsCount));
                newView.invalidate();
                mViewsCount+=1;
            }
            catch(NullPointerException e){
                //Do nothing
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
     
    }

    //function to get path 
		public String getPath(Uri uri){
			String[] filePathColumn={MediaStore.Images.Media.DATA};

			Cursor cursor=getContentResolver().query(uri, filePathColumn, null, null, null);
			cursor.moveToFirst();
			int columnIndex=cursor.getColumnIndex(filePathColumn[0]);
			Log.i(TAG,"Image path is: "+cursor.getString(columnIndex));
			return cursor.getString(columnIndex);
			}
		
	
}
