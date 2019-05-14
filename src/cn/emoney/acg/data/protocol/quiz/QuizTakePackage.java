package cn.emoney.acg.data.protocol.quiz;

import cn.emoney.acg.data.protocol.QuotePackageImpl;
import cn.emoney.acg.data.protocol.quiz.QuizTakeReply.QuizTake_Reply;
import cn.emoney.sky.libs.network.pkg.DataHeadImpl;

import com.google.protobuf.InvalidProtocolBufferException;

public class QuizTakePackage extends QuotePackageImpl {
    private QuizTake_Reply mReply = null;

    public QuizTakePackage(DataHeadImpl head) {
        super(head);
    }

    @Override
    public boolean readData(byte[] data, int arg1, int arg2) {
        try {
            mReply = QuizTake_Reply.parseFrom(data);
        } catch (InvalidProtocolBufferException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public QuizTake_Reply getResponse() {
        return mReply;
    }

}
