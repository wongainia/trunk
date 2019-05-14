package cn.emoney.acg.data.protocol.quiz;

import cn.emoney.acg.data.protocol.QuotePackageImpl;
import cn.emoney.acg.data.protocol.quiz.QuizConfigReply.QuizConfig_Reply;
import cn.emoney.sky.libs.network.pkg.DataHeadImpl;

import com.google.protobuf.InvalidProtocolBufferException;

public class QuizConfigPackage extends QuotePackageImpl {
    private QuizConfig_Reply mReply = null;

    public QuizConfigPackage(DataHeadImpl head) {
        super(head);
    }

    @Override
    public boolean readData(byte[] data, int arg1, int arg2) {
        try {
            mReply = QuizConfig_Reply.parseFrom(data);
        } catch (InvalidProtocolBufferException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public QuizConfig_Reply getResponse() {
        return mReply;
    }

}
