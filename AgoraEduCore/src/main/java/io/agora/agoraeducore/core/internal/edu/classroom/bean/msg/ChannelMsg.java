package io.agora.agoraeducore.core.internal.edu.classroom.bean.msg;

import androidx.annotation.IntDef;

import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

import io.agora.agoraeducore.core.internal.base.bean.JsonBean;
import io.agora.agoraeducore.core.internal.framework.EduBaseUserInfo;
import io.agora.agoraeducore.core.internal.framework.data.EduChatMessage;

public class ChannelMsg extends JsonBean {

    @Cmd
    public int cmd;
    public Object data;

    @IntDef({Cmd.CHAT})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Cmd {
        /**
         * simple chat msg
         */
        int CHAT = 1;
    }

    public static class ChatMsg extends EduChatMessage {
        /**
         * 是否是自己发送的消息
         */
        public transient boolean isMe;
        /**
         * 是否显示消息发送者的角色
         */
        public transient boolean showRole = false;
        public String role;

        public ChatMsg(@NotNull EduBaseUserInfo fromUser, @NotNull String message, List<String> sensitiveWords, long timestamp,
                       int messageId, int type) {
            super(fromUser, message, sensitiveWords, timestamp, messageId, type);
        }

        public ChatMsg(@NotNull EduBaseUserInfo fromUser, @NotNull String message, List<String> sensitiveWords, long timestamp,
                       int type, int messageId, boolean showRole, String role) {
            super(fromUser, message, sensitiveWords, timestamp, messageId, type);
            this.showRole = showRole;
            this.role = role;
        }
    }

    public static class BreakoutChatMsgContent extends JsonBean {
        /*消息发送者在班级内的角色
         * 超小分为大班和小班，消息发送着在不同班级内的角色不一样*/
        private int role;
        private String content;
        /*消息来自哪个班级(目前此值约定为小班级的roomUuid)*/
        private String fromRoomUuid;
        private String fromRoomName;

        public BreakoutChatMsgContent(int role, String content, String fromRoomUuid, String fromRoomName) {
            this.role = role;
            this.content = content;
            this.fromRoomUuid = fromRoomUuid;
            this.fromRoomName = fromRoomName;
        }

        public int getRole() {
            return role;
        }

        public void setRole(int role) {
            this.role = role;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getFromRoomUuid() {
            return fromRoomUuid;
        }

        public void setFromRoomUuid(String fromRoomUuid) {
            this.fromRoomUuid = fromRoomUuid;
        }

        public String getFromRoomName() {
            return fromRoomName;
        }

        public void setFromRoomName(String fromRoomName) {
            this.fromRoomName = fromRoomName;
        }

        @Override
        public String toJsonString() {
            return super.toJsonString();
        }
    }

    public <T> T getMsg(Class<T> tClass) {
        return new Gson().fromJson(new Gson().toJson(data), tClass);
    }

    @Override
    public String toJsonString() {
        return super.toJsonString();
    }

}
