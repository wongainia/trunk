package cn.emoney.acg.network;

import android.os.AsyncTask;

public abstract class RequestAsyncTask extends AsyncTask<String, Void, Response> {
	public abstract void onSuccess(String content);

	public abstract void onFail(String errorMessage);

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
	}

	@Override
	protected void onPostExecute(Response result) {
		if (result.hasError()) {
			onFail(result.getErrorMessage());
		} else {
			onSuccess(result.getResult());
		}
	}
}
