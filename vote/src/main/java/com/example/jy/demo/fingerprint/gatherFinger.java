package com.example.jy.demo.fingerprint;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.Toast;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.TabActivity;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Date;

//import com.io.io;
public class gatherFinger extends TabActivity {

	private EditText dcs_name, dcs_id, dcs_gender, dcs_address;
	private Button bt_next, bt_back, bt_save;
	private Button bt_lf1, bt_lf2, bt_lf3, bt_lf4, bt_lf5;
	private Button bt_rf1, bt_rf2, bt_rf3, bt_rf4, bt_rf5;

	private Vote_DBHelper mGatherDB;
	private Cursor mCursor_gather;
	private String GATHER_TABLE_NAME, GATHER_NAME;

	private SharedPreferences preferences;
	private String gather_id, gather_name, gender, birthday, address , id_no;
	private String filePath3;
	
	private static final int PICK_FROM_CAMERA_FOR_XYT = 0;
	private static final int PICK_FROM_CAMERA = 1;
	private static final int CROP_FROM_CAMERA = 2;
	
	private static final String IMAGE_NAME = "Photo";
	private static final String TXT_NAME = "BASIC INFO";

	private Uri mImageCaptureUri;
	private String fileName;
	private ImageView avatar;
	
	private File pic_fileFolder;
	private Bitmap bm;
	private int finger_num = 0; 
	
	private CallDecoder cd;
	private CallFprint cf;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// setContentView(R.layout.gather_finger);

		preferences = this.getSharedPreferences(getResources().getString(R.string.SystemConfig_sp),MODE_PRIVATE);
		gather_id = preferences.getString("id", "000001");
		gather_name = preferences.getString("name", "jack");
		gender = preferences.getString("gender", "mael");
		birthday = preferences.getString("birthday", "2015-01-01");
		address = preferences.getString("address", "shenzhen");
		id_no = preferences.getString("idno", "6688960542583245613");
		mGatherDB = new Vote_DBHelper(this);
		
		cd = new CallDecoder();				
		cf = new CallFprint();

		filePath3 = Environment.getExternalStorageDirectory() + "/" + getResources().getString(R.string.app_name) + "/" + gather_id;
		File fileFolder = new File(filePath3);
        if (!fileFolder.exists()) { // 如果目录不存在，则创建一个名为"finger"的目录    
            fileFolder.mkdir();  
        }  
        
        fileName = filePath3 + "/" + IMAGE_NAME +".jpg";  
		mImageCaptureUri = Uri.fromFile(new File(fileName));
		
		pic_fileFolder = new File(fileName);
		
		TabHost tabHost = getTabHost();
		LayoutInflater.from(this).inflate(R.layout.gather_finger,tabHost.getTabContentView(), true);
		tabHost.addTab(tabHost.newTabSpec("tab01").setIndicator(" ",getResources().getDrawable(R.drawable.lefthand)).setContent(R.id.tab01));
		tabHost.addTab(tabHost.newTabSpec("tab02").setIndicator(" ",getResources().getDrawable(R.drawable.righthand)).setContent(R.id.tab02));
		tabHost.addTab(tabHost.newTabSpec("tab03").setIndicator(" ",getResources().getDrawable(R.drawable.avatar)).setContent(R.id.tab03));

		avatar = (ImageView) findViewById(R.id.gf_img);
		
		tabHost.setOnTabChangedListener(new OnTabChangeListener() {
			@Override
			public void onTabChanged(String arg0) {
				// TODO Auto-generated method stub
				Log.v("crjlog", "arg0333333333333 = " + arg0); 
				if(arg0.equals("tab03")){
					
					//update 
			        if (!pic_fileFolder.exists()) { // 如果目录不存在，则创建一个名为"finger"的目录    
			        	
			        	avatar.setImageResource(R.drawable.pic_man);  
			        	
			        }else{
			        	
					//	mImageCaptureUri = Uri.fromFile(new File(fileName));
			        	//	avatar.setImageURI(mImageCaptureUri);	
						
				       // bm = BitmapFactory.decodeFile(fileName);
				       // avatar.setImageBitmap(bm);
				        
			        	
			        }
				}
			}
		});
		
		// lefthand finger
		bt_lf1 = (Button) findViewById(R.id.bt_left1);
		bt_lf1.setOnClickListener(listener);
		bt_lf2 = (Button) findViewById(R.id.bt_left2);
		bt_lf2.setOnClickListener(listener);
		bt_lf3 = (Button) findViewById(R.id.bt_left3);
		bt_lf3.setOnClickListener(listener);
		bt_lf4 = (Button) findViewById(R.id.bt_left4);
		bt_lf4.setOnClickListener(listener);
		bt_lf5 = (Button) findViewById(R.id.bt_left5); 
		bt_lf5.setOnClickListener(listener);
		// righthand finger
		bt_rf1 = (Button) findViewById(R.id.bt_right1);
		bt_rf1.setOnClickListener(listener);
		bt_rf2 = (Button) findViewById(R.id.bt_right2);
		bt_rf2.setOnClickListener(listener);
		bt_rf3 = (Button) findViewById(R.id.bt_right3);
		bt_rf3.setOnClickListener(listener);
		bt_rf4 = (Button) findViewById(R.id.bt_right4);
		bt_rf4.setOnClickListener(listener);
		bt_rf5 = (Button) findViewById(R.id.bt_right5);
		bt_rf5.setOnClickListener(listener);

		bt_next = (Button) findViewById(R.id.gather_finger_next);
		bt_next.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				
				//调用自定义相机
				Intent it = new Intent(gatherFinger.this, CameraOpen_back.class); 
				startActivityForResult(it, PICK_FROM_CAMERA);
				
				
			  // 调用系统相机	
//			  String fileName = filePath3 + "/" + gather_name +".jpg";  
//			  Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);  
//			  mImageCaptureUri = Uri.fromFile(new File(fileName));
//			  intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageCaptureUri);
//            startActivityForResult(intent, PICK_FROM_CAMERA);   
	              
				
			}
		});

		bt_back = (Button) findViewById(R.id.gather_finger_back);
		bt_back.setOnClickListener(new OnClickListener() { 
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent it = new Intent(gatherFinger.this, gatherMain.class);
				startActivity(it);
				finish();
			}
		});
		
		bt_save = (Button) findViewById(R.id.gather_finger_save);
		bt_save.setOnClickListener(new OnClickListener() { 
			@Override
			public void onClick(View arg0) { 
			// TODO Auto-generated method stub
				StringBuffer Buffer = new StringBuffer();
				String data = getCurrentTime();
				String txt = "[BasicInfo]" + "\r\n" 
							 + "NAME: " + gather_name + "\r\n"
							 + "SEX: "  + gender + "\r\n"
							 + "BIRTHDAY: " + birthday + "\r\n"
							 + "ADDRESS: " + address + "\r\n"
							 + "ISSUEDATE: " + data + "\r\n"
							 + "ID NO.: "+ id_no;
			    Buffer = Buffer.append(txt);

				Log.d("huangmin","Buffer.toString() = "+Buffer.toString());
				
				saveFileToDevice(Buffer.toString());
				
				mGatherDB.insert_gathertable(gather_name, gender, birthday, address); 
				
				Intent it = new Intent(gatherFinger.this, gatherMain.class);
				startActivity(it);
				
				finish();
			}
		});
	}
	

	public static String getCurrentTime() {//取得系统时间
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		String str = format.format(new Date());
		return str;
	}


	Button.OnClickListener listener = new Button.OnClickListener() {// 创建监听对象
		public void onClick(View v) {

			String toast_txt = null;
			
			if(v.getId() == R.id.bt_left1) {
				toast_txt = getResources().getString(R.string.finger_input_l1);
				finger_num = 6;
			}else if(v.getId() == R.id.bt_left2){
				toast_txt = getResources().getString(R.string.finger_input_l2);
				finger_num = 7;
			}else if(v.getId() == R.id.bt_left3){
				toast_txt = getResources().getString(R.string.finger_input_l3);
				finger_num = 8;
			}else if(v.getId() == R.id.bt_left4){
				toast_txt = getResources().getString(R.string.finger_input_l4);
				finger_num = 9;
			}else if(v.getId() == R.id.bt_left5){
				toast_txt = getResources().getString(R.string.finger_input_l5);
				finger_num = 10;
			}else if(v.getId() == R.id.bt_right1){
				toast_txt = getResources().getString(R.string.finger_input_r1);
				finger_num = 1;
			}else if(v.getId() == R.id.bt_right2){
				toast_txt = getResources().getString(R.string.finger_input_r2);
				finger_num = 2;
			}else if(v.getId() == R.id.bt_right3){
				toast_txt = getResources().getString(R.string.finger_input_r3);
				finger_num = 3;
			}else if(v.getId() == R.id.bt_right4){
				toast_txt = getResources().getString(R.string.finger_input_r4);
				finger_num = 4;
			}else if(v.getId() == R.id.bt_right5){
				toast_txt = getResources().getString(R.string.finger_input_r5);
				finger_num = 5;
			}
			
			Toast.makeText(gatherFinger.this,toast_txt,Toast.LENGTH_SHORT).show();
			Intent it = new Intent(gatherFinger.this, CameraOpen_gather.class); 
			it.putExtra("fingerNum", finger_num);
			startActivityForResult(it, PICK_FROM_CAMERA_FOR_XYT);

		}
	};
	public static void copyFile(String srcFileName, String destFileName) 
	    throws IOException {
		    Log.d("huangmin", "copyFile, begin");
		    File srcFile = new File(srcFileName);
		    File destFile = new File(destFileName);  
		    if(!srcFile.exists()) {
		        Log.d("huangmin", "copyFile, source file not exist.");
				return;
		    }
		    if(!srcFile.isFile()) {
		        Log.d("huangmin", "copyFile, source file not a file.");
				return;
		    }
		    if(!srcFile.canRead()) {
		        Log.d("huangmin", "copyFile, source file can't read.");
				return;
		    }
			
		    if(destFile.exists()){
		   		destFile.createNewFile();
		    }
		 
		    Log.d("huangmin", "destFile.exists()= "+destFile.exists());
		    try {
		       if (srcFile.exists()) {
			        InputStream inStream = new FileInputStream(srcFile);
			        FileOutputStream outStream = new FileOutputStream(destFile);
			        byte[] buf = new byte[1024];
			        int byteRead = 0;
			        while ((byteRead = inStream.read(buf)) != -1) {
			            outStream.write(buf, 0, byteRead);
			        }
					
			        outStream.flush();
			        outStream.close();
			        inStream.close();
		      }
		    } catch (IOException e) {
		        throw e;
		    } catch (Exception e) {
		        e.printStackTrace();
		    }
		 
	}

    private void isDirExist(String dir){  
       File file = new File(dir);  
        if(!file.exists()) { 
            file.mkdir();  
        }
    } 	
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		if (resultCode == Activity.RESULT_OK) {

			//compareFinger();
			Toast.makeText(gatherFinger.this,"success !",Toast.LENGTH_SHORT).show();
			
			switch (requestCode) {
			
			case PICK_FROM_CAMERA_FOR_XYT:
				String picPath = Environment.getExternalStorageDirectory() + "/" + getResources().getString(R.string.app_name) + "/" + gather_id + "/" + gather_id + "_" + finger_num;
				
				cd.Bmp2Pgm(picPath + ".bmp" , picPath + ".pgm"); 
				cf.pgmChangeToXyt(picPath + ".pgm", picPath + ".xyt");
				
				String oldpicPath = Environment.getExternalStorageDirectory() + "/" + getResources().getString(R.string.app_name) + "/" + gather_id + "/" + gather_id + "_" + finger_num+".xyt";
				//String newpicPath = Environment.getExternalStorageDirectory() + "/" + getResources().getString(R.string.app_name) + "/" + gather_id + "/" + "vote" + finger_num+".xyt";

				//File file = new File(oldpicPath);
  			    //file.renameTo(new File(newpicPath));
  			    
				String newFilepath = Environment.getExternalStorageDirectory() + "/" + getResources().getString(R.string.app_name) + "/" + gather_id + "/" + "Fingerprint";
				isDirExist(newFilepath);

				String destFilepath = Environment.getExternalStorageDirectory() + "/" + getResources().getString(R.string.app_name) + "/" + gather_id + "/" + "Fingerprint" + "/" + "vote" + finger_num+".xyt";
				try {
	 				copyFile(oldpicPath,destFilepath);
    			} catch (Exception e) {
        			e.printStackTrace();
    			}
				//delete old folder
				String picPath_bmp = Environment.getExternalStorageDirectory() + "/" + getResources().getString(R.string.app_name) + "/" + gather_id + "/" + gather_id + "_" + finger_num + ".bmp";
		        File fileFolder_bmp = new File(picPath_bmp);
				
				String picPath_pgm = Environment.getExternalStorageDirectory() + "/" + getResources().getString(R.string.app_name) + "/" + gather_id + "/" + gather_id + "_" + finger_num + ".pgm";
		        File fileFolder_pgm = new File(picPath_pgm);

				String picPath_xyt = Environment.getExternalStorageDirectory() + "/" + getResources().getString(R.string.app_name) + "/" + gather_id + "/" + gather_id + "_" + finger_num + ".xyt";
		        File fileFolder_xyt = new File(picPath_xyt);

				if (fileFolder_bmp.exists()) {
					delete(fileFolder_bmp); 
		        }
		        if (fileFolder_pgm.exists()) {
					delete(fileFolder_pgm); 
		        }
		        if (fileFolder_xyt.exists()) {
					delete(fileFolder_xyt); 
		        }
				break;
			
			case PICK_FROM_CAMERA:
				doCrop();
				break;
				
			case CROP_FROM_CAMERA:
				
				//if (null != data) {
				//	saveCutPic(data);
				//}
		        bm = BitmapFactory.decodeFile(fileName);
		        avatar.setImageBitmap(bm);
				
				break;

			}

			
//			 String fileName = "/sdcard/Image/"+gather_name+".bmp";  
//             File f = new File(fileName);  

//			 String path = f.getAbsolutePath();
//             Intent intent = new Intent();
//             intent.setAction( "com.android.camera.action.CROP" );
//             intent.setDataAndType(Uri. fromFile( newFile(path)), "image/*" );// mUri是已经选择的图片 Uri
//             intent.putExtra( "crop", "true");
//             intent.putExtra( "aspectX", 3); // 裁剪框比例
//             intent.putExtra( "aspectY", 3);
//             intent.putExtra( "outputX", 150); // 输出图片大小
//             intent.putExtra( "outputY", 150);
//             intent.putExtra( "return-data", true);
//             startActivityForResult(intent,8);
			
//			 Bundle bundle = data.getExtras();  
//	         Bitmap bitmap = (Bitmap) bundle.get("data");// 获取相机返回的数据，并转换为Bitmap图片格式  
//
//	         FileOutputStream b = null;           
//	            File file = new File(filePath3);  
//	            file.mkdirs();// 创建文件夹  
//	            //String fileName = "/sdcard/Image/"+gather_name+".bmp";  
//				String fileName = filePath3 + "/" + gather_name +".bmp";  
//	  
//	            try {  
//	                b = new FileOutputStream(fileName);  
//	                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, b);// 把数据写入文件  
//	            } catch (FileNotFoundException e) {  
//	                e.printStackTrace();  
//	            } finally {  
//	                try {  
//	                    b.flush();  
//	                    b.close();  
//	                } catch (IOException e) {  
//	                    e.printStackTrace();  
//	                }  
//	            }  
		}
	};
	
	
	
	private void doCrop() {
		final ArrayList<CropOption> cropOptions = new ArrayList<CropOption>();

		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setType("image/*");

		List<ResolveInfo> list = getPackageManager().queryIntentActivities(
				intent, 0);

		int size = list.size();

		if (size == 0) {
			Toast.makeText(this, "Can not find image crop app",
					Toast.LENGTH_SHORT).show();

			return;
		} else {
			
			mImageCaptureUri = Uri.fromFile(new File(fileName));

			intent.setData(mImageCaptureUri);
//			intent.putExtra("outputX", 320); 
//			intent.putExtra("outputY", 480);
			intent.putExtra("crop", "true");
//			intent.putExtra("aspectX", 1); 
//			intent.putExtra("aspectY", 1);
//			intent.putExtra("scale", true);
//			intent.putExtra("return-data", true);
			intent.putExtra("scale", true);
			intent.putExtra("return-data", false);
			intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
		    intent.putExtra("noFaceDetection", true); 
			
			intent.putExtra("output", Uri.fromFile(new File(filePath3 + "/" + IMAGE_NAME +".jpg"))); 
 
			if (size == 1) {
				Intent i = new Intent(intent);
				ResolveInfo res = list.get(0);

				i.setComponent(new ComponentName(res.activityInfo.packageName,
						res.activityInfo.name));

				startActivityForResult(i, CROP_FROM_CAMERA);
			} else {
				for (ResolveInfo res : list) {
					final CropOption co = new CropOption();

					co.title = getPackageManager().getApplicationLabel(
							res.activityInfo.applicationInfo);
					co.icon = getPackageManager().getApplicationIcon(
							res.activityInfo.applicationInfo);
					co.appIntent = new Intent(intent);

					co.appIntent
							.setComponent(new ComponentName(
									res.activityInfo.packageName,
									res.activityInfo.name));

					cropOptions.add(co);
				}

				CropOptionAdapter adapter = new CropOptionAdapter(
						getApplicationContext(), cropOptions);

				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle("Choose Crop App");
				builder.setAdapter(adapter,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int item) {
								startActivityForResult(
										cropOptions.get(item).appIntent,
										CROP_FROM_CAMERA);
							}
						});

				builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialog) {

						if (mImageCaptureUri != null) {
							getContentResolver().delete(mImageCaptureUri, null,
									null);
							mImageCaptureUri = null;
						}
					}
				});

				AlertDialog alert = builder.create();

				alert.show();
			}
		}
	}
	
	
	private void saveFileToDevice(String toSaveString) {
		try {

			String filePath = Environment.getExternalStorageDirectory() + "/" + getResources().getString(R.string.app_name) 
							  + "/" + gather_id + "/" + TXT_NAME + ".ini";

			File saveFile = new File(filePath);
			if (!saveFile.exists()) {
				File dir = new File(saveFile.getParent());
				dir.mkdirs();
				saveFile.createNewFile();
			}

			FileOutputStream outStream = new FileOutputStream(saveFile);
			outStream.write(toSaveString.getBytes());
			outStream.close();

			Toast.makeText(gatherFinger.this, R.string.dataentry_savefile_success,
					Toast.LENGTH_SHORT).show();

		} catch (FileNotFoundException e) {
			Toast.makeText(gatherFinger.this, R.string.dataentry_file_nonexistent,
					Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		} catch (IOException e) {
			Toast.makeText(gatherFinger.this, R.string.dataentry_savefile_error,
					Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}
	}

	
	
	private void saveCutPic(Intent picdata) {
		Bundle bundle = picdata.getExtras();
        Bitmap bitmap = (Bitmap) bundle.get("data");// 获取相机返回的数据，并转换为Bitmap图片格式  

        FileOutputStream b = null;           
           File file = new File(filePath3);  
           file.mkdirs();// 创建文件夹  
		   String fileName = filePath3 + "/" + gather_name +".bmp";   
 
           try {  
               b = new FileOutputStream(fileName);  
               bitmap.compress(Bitmap.CompressFormat.JPEG, 100, b);// 把数据写入文件  
           } catch (FileNotFoundException e) {  
               e.printStackTrace();  
           } finally {  
               try {  
                   b.flush();  
                   b.close();  
               } catch (IOException e) {  
                   e.printStackTrace();  
               }  
           }  
		
	}
	
	public void delete(File file) {
		if (file.isFile()) {
			file.delete();
			return;
		}
		if (file.isDirectory()) {
			File[] childFiles = file.listFiles();
			if (childFiles == null || childFiles.length == 0) {
				file.delete();
				return;
			}
			for (int i = 0; i < childFiles.length; i++) {
				delete(childFiles[i]);
			}
			file.delete();
		}
	}
	
}
