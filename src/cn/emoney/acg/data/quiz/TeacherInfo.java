package cn.emoney.acg.data.quiz;

import java.io.Serializable;

import cn.emoney.acg.data.protocol.quiz.QuizDefine.Teacher;

/**
 * @ClassName: TeacherInfo
 * @Description:老师的信息
 * @author xiechengfa
 * @date 2015年12月31日 上午10:24:11
 *
 */
public class TeacherInfo implements Serializable {
    private static final long serialVersionUID = 2414988292185127241L;
    private long id = 0;// 解答老师userId
    private String nick = null;// 解答老师名字 ??? 如果有nick和icon变更，是否需要变更。如果需要，此处是否冗余存储
    private String icon = null;// 解答老师头像
    private String type = null;// 老师类型, 跟老师有关的都加上
    private String title = null;// 老师头衔, 跟老师有关的都加上

    public TeacherInfo() {}

    public TeacherInfo(long id, String nick, String icon, String type, String title) {
        super();
        this.id = id;
        this.nick = nick;
        this.icon = icon;
        this.type = type;
        this.title = title;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void initOfServerTeacher(Teacher teacher) {
        if (teacher == null) {
            return;
        }

        this.id = teacher.getId();
        this.nick = teacher.getNick();
        this.icon = teacher.getIcon();
        this.type = teacher.getType();
        this.title = teacher.getTitle();
    }
}
