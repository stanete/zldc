package com.example.android.test.interfaces;

/**
 * Created xiaomo
 * Date on  2019/4/14
 *
 * @Desc 网络管理的基础结构
 */

public interface Imanager {
    /**
     * 链接
     */
    void connect();

    /**
     * 断开链接
     */
    void disConnect();

    /**
     * 服务器注册
     *
     * @param listener
     */
    void regeisterServerMsg(OnMqttAndroidConnectListener listener);

    /**
     * 服务器注销
     *
     * @param listener
     */
    void unRegeisterServerMsg(OnMqttAndroidConnectListener listener);

    /**
     * 发送消息
     *
     * @param topic   话题
     * @param message 消息
     */
    void sendMsg(String topic, String message);

    /**
     * 是否已连接
     *
     * @return
     */
    boolean isConnected();
}
