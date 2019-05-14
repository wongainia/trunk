package cn.emoney.acg.data.protocol.quiz;

import cn.emoney.acg.data.protocol.QuotePackageImpl;
import cn.emoney.acg.data.protocol.quiz.QuizDropReply.QuizDrop_Reply;
import cn.emoney.sky.libs.network.pkg.DataHeadImpl;

import com.google.protobuf.InvalidProtocolBufferException;

public class QuizDropPackage extends QuotePackageImpl {
    private QuizDrop_Reply mReply = null;

    public QuizDropPackage(DataHeadImpl head) {
        super(head);
    }

    @Override
    public boolean readData(byte[] data, int arg1, int arg2) {
        try {
            mReply = QuizDrop_Reply.parseFrom(data);
        } catch (InvalidProtocolBufferException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public QuizDrop_Reply getResponse() {
        return mReply;
    }

}
