package cn.emoney.acg.data;

import java.io.IOException;
import java.io.Serializable;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import Decoder.BASE64Decoder;
import Decoder.BASE64Encoder;
import android.content.Intent;
import cn.emoney.acg.data.bdcast.BroadCastName;
import cn.emoney.acg.data.protocol.quiz.QuizDefine.Teacher;
import cn.emoney.acg.data.quiz.TeacherInfo;
import cn.emoney.acg.util.DataUtils;
import cn.emoney.acg.util.LogUtil;
import cn.emoney.acg.util.Util;
import cn.emoney.acg.util.security.AESUtil;
import cn.emoney.sky.libs.db.GlobalDBHelper;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

public class UserInfo implements Serializable {
    private static final long serialVersionUID = -8881272720797749065L;
    private final int TYPE_ROLE_NORMAL = 0;// 普通用户
    private final int TYPE_ROLE_TEACHER = 1;// 投顾(老师)
    private boolean mIsLogined = false;
    private String mUsername = "";
    private String mPassword = "";
    private String mToken = "";
    private String mChannel = "";// 渠道
    private int mAccountType = 0;
    private String mNickName = "";
    private String mUid = "";
    private String mHeadId = "";

    // mRole, 0:普通用户,1:投顾
    private int mRole = TYPE_ROLE_NORMAL;
    private String mRealName = "";
    private String mRealHeadId = "";

    public UserInfo() {

    }

    public UserInfo(String id, String nicName, String headId) {
        this.mUid = id;
        this.mNickName = nicName;
        this.mHeadId = headId;
    }

    public String getRealName() {
        if (getRole() == 1) {
            return mRealName;
        }
        return "";
    }

    public void setRealName(String name) {
        mRealName = name;
    }

    public int getRole() {
        return mRole;
    }

    public void setRole(int role) {
        mRole = role;
    }

    public boolean isRoleTeacher() {
        // 21987(我),21998（刘）,98
        if (LogUtil.isDebug() && ("22007".equals(mUid) || "21987".equals(mUid)) || "21955".equals(mUid)) {
            mRole = TYPE_ROLE_TEACHER;
        } else {
            mRole = TYPE_ROLE_NORMAL;
        }

        if (isLogined() && mRole == TYPE_ROLE_TEACHER) {
            return true;
        } else {
            return false;
        }
    }

    public String getRealHeadId() {
        if (getRole() == TYPE_ROLE_TEACHER) {
            if (DataUtils.convertToInt(mRealHeadId) > 0) {
                return mRealHeadId;
            }
            return mUid;
        }
        return mHeadId;
    }

    public void setRealHeadId(String headId) {
        mHeadId = headId;
    }

    public String getToken() {
        if (mIsLogined && !mToken.equals("")) {
            return mToken;
        } else {
            return DataModule.G_GUEST_TOKEN;
        }
    }

    public void setToken(String token) {
        mToken = token;
    }

    public String getReLoginToken() {
        return mToken;
    }

    public void setUsername(String username) {
        mUsername = username;
    }

    public String getUsername() {
        return mUsername;
    }

    public void setPassword(String password) {
        mPassword = password;
    }

    public String getPassword() {
        return mPassword;
    }

    public boolean isLogined() {
        return mIsLogined;
    }

    public void setLogined(boolean isLogined) {
        mIsLogined = isLogined;

        // 登录状态修改，发送广播
        Intent intent = new Intent(BroadCastName.BCDC_CHANGE_LOGIN_STATE_QIZE);
        Util.sendBroadcast(intent);
    }

    public void setChannel(String channel) {
        mChannel = channel;
    }

    public String getChannel() {
        return mChannel;
    }

    public void setAccountType(int type) {
        mAccountType = type;
    }

    public int getAccountTYpe() {
        return mAccountType;
    }

    public void setNickName(String nick) {
        mNickName = nick;
    }

    public String getNickName() {
        return mNickName;
    }

    public String getConvertNickName() {
        if (mNickName != null && mNickName.trim().length() > 0) {
            return mNickName;
        } else {
            return DataUtils.formatUserNameShade(DataModule.getInstance().getUserInfo().getUsername());
        }
    }

    public void setUid(String uid) {
        mUid = uid;
    }

    public String getUid() {
        return mUid;
    }

    public void setHeadId(String headid) {
        mHeadId = headid;
    }

    public String getHeadId() {
        return mHeadId;
    }

    public TeacherInfo converToTeacherInfo() {
        TeacherInfo info = new TeacherInfo();
        info.setId(DataUtils.convertToLong(mUid));
        info.setNick(getConvertNickName());
        info.setIcon(mHeadId);
        info.setTitle("股市才子");
        info.setType("金融");

        return info;
    }

    public Teacher.Builder convertToTeacherBuilder() {
        Teacher.Builder info = Teacher.newBuilder();
        info.setId(DataUtils.convertToLong(mUid));
        info.setNick(getConvertNickName());
        info.setIcon(mHeadId);
        info.setTitle("股市才子");
        info.setType("金融");
        return info;
    }

    public void save(GlobalDBHelper dbHelper) {
        String sCipherToken = "";
        try {
            AESUtil aesUtil = new AESUtil();
            byte[] b64ByteCipherToken = aesUtil.Encrytor(mToken);
            BASE64Encoder base64Encoder = new BASE64Encoder();
            sCipherToken = base64Encoder.encode(b64ByteCipherToken);
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (BadPaddingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            JSONObject jobj = new JSONObject();
            jobj.put(DataModule.G_KEY_USER_INFO_CHANNEL, mChannel);
            jobj.put(DataModule.G_KEY_USER_INFO_NICKNAME, mNickName);
            jobj.put(DataModule.G_KEY_USER_INFO_TOKOEN, sCipherToken);
            jobj.put(DataModule.G_KEY_USER_INFO_TYPE, mAccountType);
            jobj.put(DataModule.G_KEY_USER_INFO_PWD, mPassword);
            jobj.put(DataModule.G_KEY_USER_INFO_USERNMAE, mUsername);

            // 不备份角色
            // jobj.put(DataModule.G_KEY_USER_INFO_REALNAME, mRealName);
            // jobj.put(DataModule.G_KEY_USER_INFO_ROLE, 0);//不备份角色
            // jobj.put(DataModule.G_KEY_USER_INFO_REALHEADID, mRealHeadId);


            LogUtil.easylog("UserInfo->Save->uid:" + mUid);
            jobj.put(DataModule.G_KEY_USER_INFO_USERID, mUid);
            jobj.put(DataModule.G_KEY_USER_INFO_HEADID, mHeadId);

            LogUtil.easylog("sky", "UserInfo->Save jobj:" + jobj);
            LogUtil.easylog("sky", "UserInfo->Save string:" + jobj.toString());
            String strUserInfo = jobj.toString();

            dbHelper.setString(DataModule.G_KEY_USER_INFO, strUserInfo);
        } catch (JSONException e) {
        }
    }

    public void load(GlobalDBHelper dbHelper) {
        String jsonStr_userInfo = dbHelper.getString(DataModule.G_KEY_USER_INFO, "");
        if (!jsonStr_userInfo.equals("")) {
            try {
                JSONObject jobjUserInfo = JSON.parseObject(jsonStr_userInfo);

                String orgToken = "";
                AESUtil aesUtil = new AESUtil();
                String mixToken = jobjUserInfo.getString(DataModule.G_KEY_USER_INFO_TOKOEN);
                if (mixToken != null && !mixToken.equals("")) {
                    BASE64Decoder base64Decoder = new BASE64Decoder();
                    byte[] byteDecoder = base64Decoder.decodeBuffer(mixToken);
                    byte[] byteOut = aesUtil.Decryptor(byteDecoder);
                    String sOut = new String(byteOut, "utf-8");
                    if (sOut != null && !sOut.equals("")) {
                        orgToken = sOut;
                    }
                }

                setUsername(jobjUserInfo.getString(DataModule.G_KEY_USER_INFO_USERNMAE));
                setPassword(jobjUserInfo.getString(DataModule.G_KEY_USER_INFO_PWD));
                setToken(orgToken);
                setAccountType(jobjUserInfo.getIntValue(DataModule.G_KEY_USER_INFO_TYPE));
                setChannel(jobjUserInfo.getString(DataModule.G_KEY_USER_INFO_CHANNEL));
                setNickName(jobjUserInfo.getString(DataModule.G_KEY_USER_INFO_NICKNAME));
                setUid(jobjUserInfo.getString(DataModule.G_KEY_USER_INFO_USERID));
                setHeadId(jobjUserInfo.getString(DataModule.G_KEY_USER_INFO_HEADID));

                // 不加载角色
                // setRealName(jobjUserInfo.getString(DataModule.G_KEY_USER_INFO_REALNAME));
                // setRealHeadId(jobjUserInfo.getIntValue(DataModule.G_KEY_USER_INFO_REALHEADID));
                // setRole(jobjUserInfo.getIntValue(DataModule.G_KEY_USER_INFO_ROLE));
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (NoSuchPaddingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InvalidKeyException e) {
                e.printStackTrace();
            } catch (IllegalBlockSizeException e) {
                e.printStackTrace();
            } catch (BadPaddingException e) {
                e.printStackTrace();
            }

        }
    }
}
