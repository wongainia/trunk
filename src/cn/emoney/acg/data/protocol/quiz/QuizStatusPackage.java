package cn.emoney.acg.data.protocol.quiz;

import cn.emoney.acg.data.protocol.QuotePackageImpl;
import cn.emoney.acg.data.protocol.quiz.QuizStatusReply.QuizStatus_Reply;
import cn.emoney.sky.libs.network.pkg.DataHeadImpl;

import com.google.protobuf.InvalidProtocolBufferException;

public class QuizStatusPackage extends QuotePackageImpl {
    private QuizStatus_Reply mReply = null;

    public QuizStatusPackage(DataHeadImpl head) {
        super(head);
    }

    @Override
    public boolean readData(byte[] data, int arg1, int arg2) {
        try {
            mReply = QuizStatus_Reply.parseFrom(data);
        } catch (InvalidProtocolBufferException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public QuizStatus_Reply getResponse() {
        return mReply;
    }

}
