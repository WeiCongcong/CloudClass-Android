package io.agora.edu.common.bean.handsup;

public class HandsupBody {
    int retry; //是否重试举手
    int timeout; //超时时间，单位秒

    public HandsupBody() {
        this.retry = 1;
        this.timeout = 3;
    }

    public HandsupBody(int timeout) {
        this.retry = 1;
        this.timeout = timeout;
    }
}