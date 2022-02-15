package top.seatide.remote;

import top.seatide.remote.Utils.DeviceUtil;
import top.seatide.remote.Utils.LogUtil;

/**
 * 用于在 idea 中快速启动一个测试网页服务器（点击 class 左侧的绿色开始图标）
 * 该服务器可以调用 Bukkit 静态 API，但是有些依赖于服务器本身的方法会报错。
 */
public class Test {
    public static void main(String[] args) {
        LogUtil.init();
        DeviceUtil.startCpuLoadCalc();
        Server.startServer(2077);
    }
}
