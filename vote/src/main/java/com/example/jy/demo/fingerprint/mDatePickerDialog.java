package com.example.jy.demo.fingerprint;

import java.lang.reflect.Field;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.LinearLayout;
/**
 * @author joker
 * 自定义的日期控件，只有年和月，没有日
 * 2012-10-17 上午11:02:17 
 */
public class mDatePickerDialog extends DatePickerDialog {
	 public mDatePickerDialog(Context context,
             OnDateSetListener callBack, int Month, int dayOfMonth) {
         super(context, callBack, 2000, Month, dayOfMonth);
//         this.setTitle(Month+1+"月"+ dayOfMonth + "日");
         this.setTitle(R.string.System_Log_query_dialog_title);
     }

     @Override
     public void onDateChanged(DatePicker view, int year, int month, int day) {
         super.onDateChanged(view, year, month, day); 
//         this.setTitle(month+1+"月"+ day + "日" );
         this.setTitle(R.string.System_Log_query_dialog_title);
     }

	/* (non-Javadoc)
	 * @see android.app.DatePickerDialog#show()
	 */
	@Override
	public void show() {
		// TODO Auto-generated method stub
		super.show();
		 DatePicker dp = findDatePicker((ViewGroup) this.getWindow().getDecorView());
	        if (dp != null) {
	        	Class c=dp.getClass();
	        	Field f;
				try {
					f = c.getDeclaredField("mYearSpinner");
					f.setAccessible(true );  
					LinearLayout l= (LinearLayout)f.get(dp);   
					l.setVisibility(View.GONE);
				} catch (SecurityException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NoSuchFieldException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}  
	        	
	        } 
	}
	/**
     * 从当前Dialog中查找DatePicker子控件
     * 
     * @param group
     * @return
     */
    private DatePicker findDatePicker(ViewGroup group) {
        if (group != null) {
            for (int i = 0, j = group.getChildCount(); i < j; i++) {
                View child = group.getChildAt(i);
                if (child instanceof DatePicker) {
                    return (DatePicker) child;
                } else if (child instanceof ViewGroup) {
                    DatePicker result = findDatePicker((ViewGroup) child);
                    if (result != null)
                        return result;
                } 
            }
        }
        return null;

    }
	
	@Override
	public void onClick(DialogInterface dialog, int which) {
		// TODO Auto-generated method stub
		super.onClick(dialog, which);
	}
}