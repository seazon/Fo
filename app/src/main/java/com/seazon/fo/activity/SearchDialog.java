package com.seazon.fo.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import com.seazon.fo.R;

@Deprecated
public class SearchDialog extends Dialog implements OnClickListener {

	private String path;
	
	public SearchDialog(Context context, String path) {
		super(context);
		
		this.path = path;
	}

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.dialog_search);
		setCanceledOnTouchOutside(true);

		Button addBtn = (Button) findViewById(R.id.searchBtn);
		addBtn.setOnClickListener(this);
//		addBtn.setOnTouchListener(new OnBtnTransparentTouchListener(this
//				.getContext()));

		Button cancelBtn = (Button) findViewById(R.id.cancelBtn);
		cancelBtn.setOnClickListener(this);
//		cancelBtn.setOnTouchListener(new OnBtnTransparentTouchListener(this
//				.getContext()));
	}

	public void onClick(View v) {
		if (v.getId() == R.id.searchBtn) {

			EditText queryEdt = (EditText)findViewById(R.id.queryEdt);
			String query = queryEdt.getText().toString();
			
			Intent intent = new Intent();
			intent.putExtra("Query", query);
			intent.putExtra("Path", path);
			intent.setClass(this.getContext(), SearchActivity.class);
			getContext().startActivity(intent);

			this.dismiss();
		} else if (v.getId() == R.id.cancelBtn) {
			this.dismiss();
		}
	}

}
