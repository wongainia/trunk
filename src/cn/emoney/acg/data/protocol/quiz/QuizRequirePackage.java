package cn.emoney.acg.data.protocol.quiz;

import cn.emoney.acg.data.protocol.QuotePackageImpl;
import cn.emoney.acg.data.protocol.quiz.QuizRequireReply.QuizRequire_Reply;
import cn.emoney.sky.libs.network.pkg.DataHeadImpl;

import com.google.protobuf.InvalidProtocolBufferException;

public class QuizRequirePackage extends QuotePackageImpl {
    private QuizRequire_Reply mReply = null;

    public QuizRequirePackage(DataHeadImpl head) {
        super(head);
    }

    @Override
    public boolean readData(byte[] data, int arg1, int arg2) {
        try {
            mReply = QuizRequire_Reply.parseFrom(data);
        } catch (InvalidProtocolBufferException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public QuizRequire_Reply getResponse() {
        return mReply;
    }

}
