package cn.emoney.acg.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import cn.emoney.acg.data.DataModule;
import cn.emoney.acg.util.BackupTask.CompletionListener;

/**
 * Android数据库备份类
 */
public class BackupUtil extends AsyncTask<String, Void, Integer> implements CompletionListener {

	// 定义常量
	public static final int BACKUP_SUCCESS = 1;
	public static final int RESTORE_SUCCESS = 2;
	public static final int BACKUP_ERROR = 3;
	public static final int RESTORE_NOFLEERROR = 4;
	public static final String COMMAND_BACKUP = "backupDatabase";
	public static final String COMMAND_RESTORE = "restroeDatabase";
	private Context mContext;

	public BackupUtil(Context context) {
		this.mContext = context;
	}

	@Override
	protected Integer doInBackground(String... params) {
		// 1,获得数据库路径
		File dbFile = mContext.getDatabasePath("estockgoods");
		// 2,创建保存的数据库的路径
		String t_path = Environment.getExternalStorageDirectory() + "/" + DataModule.G_LOC_PATH + "DB_BACK";
		
		File exportDir = new File(t_path);
		if (!exportDir.exists()) {
			exportDir.mkdirs();
		}
		File backup = new File(exportDir, dbFile.getName() + DataModule.G_DATABASE_VERNUMBER);
		// 3,检查操作
		String command = params[0];
		if (command.equals(COMMAND_BACKUP)) {
			// 复制文件
			try {
				backup.createNewFile();
				fileCopy(dbFile, backup);
				return BACKUP_SUCCESS;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return BACKUP_ERROR;
			}
		} else {
			return BACKUP_ERROR;
		}

	}

	private void fileCopy(File source, File dest) throws IOException {
		FileChannel inChannel = new FileInputStream(source).getChannel();
		FileChannel outChannel = new FileOutputStream(dest).getChannel();
		// FileInputStream fis = new FileInputStream(dbFile);
		// FileOutputStream fos = new FileOutputStream(backup);
		// byte buffer[] = new byte[4 * 1024];
		// while(fis.read(buffer) != -1){
		// fos.write(buffer);
		// }
		// fos.flush();
		//
		long size = inChannel.size();
		try {
			inChannel.transferTo(0, inChannel.size(), outChannel);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (inChannel != null) {
				inChannel.close();
			}
			if (outChannel != null) {
				outChannel.close();
			}
		}
	}

	@Override
	protected void onPostExecute(Integer result) {
		// TODO Auto-generated method stub
		super.onPostExecute(result);
		switch (result) {
		case BACKUP_SUCCESS:
			onBackupComplete();
			break;

		default:
			break;
		}
	}

	@Override
	public void onBackupComplete() {

		LogUtil.easylog("backup", "ok");

	}

	@Override
	public void onRestoreComplete() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onError(int errorCode) {
		// TODO Auto-generated method stub

	}

}
