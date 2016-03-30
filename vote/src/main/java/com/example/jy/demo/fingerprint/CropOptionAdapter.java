package com.example.jy.demo.fingerprint;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import android.content.Context;

import java.util.ArrayList;

/**
 * Adapter for crop option list.
 *
 * @author cmj qq:571204977 欢迎一起学习Android
 *
 */
public class CropOptionAdapter extends ArrayAdapter<CropOption> {
	private ArrayList<CropOption> mOptions;
	private LayoutInflater mInflater;

	public CropOptionAdapter(Context context, ArrayList<CropOption> options) {
		super(context, R.layout.crop_selector, options);

		mOptions 	= options;

		mInflater	= LayoutInflater.from(context);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup group) {
		if (convertView == null)
			convertView = mInflater.inflate(R.layout.crop_selector, null);

		CropOption item = mOptions.get(position);

		if (item != null) {
			((ImageView) convertView.findViewById(R.id.iv_icon)).setImageDrawable(item.icon);
			((TextView) convertView.findViewById(R.id.tv_name)).setText(item.title);

			return convertView;
		}

		return null;
	}
}