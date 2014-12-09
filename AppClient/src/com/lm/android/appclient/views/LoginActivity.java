package com.lm.android.appclient.views;

import java.util.concurrent.ExecutionException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lm.android.appclient.MainActivity;
import com.lm.android.appclient.R;
import com.lm.android.appclient.Utils;
import com.lm.android.appclient.model.NetResultInfo;
import com.lm.android.appclient.net.NetOptTask;
import com.lm.android.appclient.net.Urls;

public class LoginActivity extends Activity {
	private EditText name;
	private EditText passwd;
	private EditText mac;
	
	private ProgressDialog loading;
	private NetOptTask task;
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (msg.obj.toString().equalsIgnoreCase(Urls.login)) {
				try {
					String result = task.get();
					Gson gson = new Gson();
					NetResultInfo resultInfo = gson.fromJson(result,
							new TypeToken<NetResultInfo>() {}.getType());
					loading.dismiss();
					if (resultInfo.bIsOk.equalsIgnoreCase("1")) {
						// 登陆成功跳转
						Intent intent = new Intent(LoginActivity.this, MainActivity.class);
						startActivity(intent);
						finish();
					}
				} catch (Exception e) {
					e.printStackTrace();
					Intent intent = new Intent(LoginActivity.this, MainActivity.class);
					startActivity(intent);
					finish();
				}
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		
		loading = new ProgressDialog(LoginActivity.this);
		loading.setMessage("登录中,请稍候...");

		name = (EditText) findViewById(R.id.login_username);
		passwd = (EditText) findViewById(R.id.login_password);
		mac = (EditText) findViewById(R.id.login_device_macaddress);
		Button btn_login = (Button) findViewById(R.id.btn_login);
		
		mac.setText(getMacAddress());

		btn_login.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (canLogin()) {
					loading.show();
					
					String[] requestParams = new String[3];
					requestParams[0] = "username";
					requestParams[1] = "password";
					requestParams[2] = "deviceMac";
					task = new NetOptTask(Urls.login, requestParams, handler);

					String[] params = new String[3];
					params[0] = name.getText().toString().trim();
					params[1] = passwd.getText().toString().trim();
					params[2] = mac.getText().toString().trim();
					task.execute(params);
				}
			}
		});
	}
	
	// 判断是否可以自行登陆操作
	private boolean canLogin() {
		if (name.getText().toString().trim().equalsIgnoreCase("")) {
			Utils.showToast(LoginActivity.this, "请输入用户名");
			return false;
		} else if (passwd.getText().toString().trim().equalsIgnoreCase("")) {
			Utils.showToast(LoginActivity.this, "请输入密码");
			return false;
		} else if (mac.getText().toString().trim().equalsIgnoreCase("")) {
			Utils.showToast(LoginActivity.this, "请输入设备mac地址");
			return false;
		} else {
			return true;
		}
	}

	private String getMacAddress() {
		String macAddress = null;
		WifiManager wifiMgr = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		WifiInfo info = (null == wifiMgr ? null : wifiMgr.getConnectionInfo());
		if (null != info) {
			macAddress = info.getMacAddress();
		}
		return macAddress;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (loading != null) {
			loading.dismiss();
		}
		
		if (task != null) {
			task.cancel(true);
		}
	}
	
}
