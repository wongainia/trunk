package cn.emoney.acg.network;

public class Response {
	private boolean error;
	private int errorType;
	private String errorMessage;
	private String result;
	
	public int getErrorType() {
		return errorType;
	}

	public void setErrorType(int errorType) {
		this.errorType = errorType;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public boolean hasError() {
		return error;
	}
	
	public void setError(boolean hasError) {
		this.error = hasError;
	}
	
	
}
