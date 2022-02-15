package top.seatide.remote;

import org.bukkit.plugin.java.JavaPlugin;
import top.seatide.remote.Utils.DeviceUtil;
import top.seatide.remote.Utils.Files;
import top.seatide.remote.Utils.LogUtil;

public final class Main extends JavaPlugin {
    public static String serverPassword;

    @Override
    public void onEnable() {
        LogUtil.init();
        Files.init(this);
        serverPassword = Files.cfg.getString("password");
        DeviceUtil.startCpuLoadCalc();
        DeviceUtil.startUptimeCalc(this);
        DeviceUtil.startTpsCalc(this);
        var port = Files.cfg.getInt("port");
        Console.listen();
        Server.startServer(port, Files.cfg.getString("index-page"));
        LogUtil.success("HTTP 服务现运行于 " + port + " 端口。");
    }

    @Override
    public void onDisable() {

    }
}
