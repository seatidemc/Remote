package top.seatide.remote;

import top.seatide.remote.Utils.DeviceUtil;
import top.seatide.remote.Utils.LogUtil;

public class Test {
    public static void main(String[] args) {
        LogUtil.init();
        DeviceUtil.startCpuLoadCalc();
        Server.startServer(2077);
    }
}
