package top.seatide.remote;

import org.bukkit.plugin.java.JavaPlugin;
import top.seatide.remote.Utils.DeviceUtil;
import top.seatide.remote.Utils.LogUtil;

public final class Main extends JavaPlugin {

    @Override
    public void onEnable() {
        LogUtil.init();
        DeviceUtil.startCpuLoadCalc();
    }

    @Override
    public void onDisable() {

    }
}
