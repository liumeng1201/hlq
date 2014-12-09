package com.lm.android.appclient.net;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.kxml2.kdom.Element;
import org.kxml2.kdom.Node;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class NetOptTask extends AsyncTask<String, Integer, String> {
	private String optName;
	private String[] requestParams;
	private Handler handler;

	/**
	 * 网络操作构造方法
	 * 
	 * @param optName
	 *            网络操作方法名
	 * @param params
	 *            要传递的参数名列表
	 * @param handler
	 */
	public NetOptTask(String optName, String[] params, Handler handler) {
		this.optName = optName;
		this.requestParams = params;
		this.handler = handler;
	}

	@Override
	protected String doInBackground(String... params) {
		String soapAction = "http://tempuri.org/" + optName;

		Element[] header = new Element[1];
		header[0] = new Element().createElement(Urls.nameSpace, "MySoapHeader");
		Element username = new Element().createElement(Urls.nameSpace, "Uname");
		username.addChild(Node.TEXT, "Admin");
		header[0].addChild(Node.ELEMENT, username);
		Element pass = new Element().createElement(Urls.nameSpace, "Password");
		pass.addChild(Node.TEXT, "Admin");
		header[0].addChild(Node.ELEMENT, pass);

		SoapObject request = new SoapObject(Urls.nameSpace, optName);
		for (int i = 0; i < requestParams.length; i++) {
			request.addProperty(requestParams[i], params[i]);
		}
		
		SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
		envelope.headerOut = header;
		envelope.bodyOut = request;
		envelope.dotNet = true;

		HttpTransportSE transport = new HttpTransportSE(Urls.endPoint);
		transport.debug = true;
		try {
			transport.call(soapAction, envelope);
			Log.d("lm", transport.requestDump);
			Log.d("lm", transport.responseDump);
			SoapObject response = (SoapObject) envelope.bodyIn;
			return response.getProperty(0).toString();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	protected void onPostExecute(String result) {
		super.onPostExecute(result);
		Message msg = new Message();
		msg.obj = optName;
		handler.sendMessage(msg);
	}

}
