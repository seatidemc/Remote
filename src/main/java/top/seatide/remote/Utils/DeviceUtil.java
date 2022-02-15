package top.seatide.remote.Utils;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.json.JSONArray;
import org.json.JSONObject;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.OperatingSystem;
import top.seatide.remote.Main;

import java.util.Arrays;
import java.util.stream.Collectors;

public class DeviceUtil {
    public static SystemInfo si = new SystemInfo();
    public static OperatingSystem os = si.getOperatingSystem();
    public static HardwareAbstractionLayer hal = si.getHardware();
    public static CentralProcessor cpu = hal.getProcessor();
    public static GlobalMemory ram = hal.getMemory();
    public static double cpuLoad = 0.0;
    public static Thread cpuLoadCalcThread;
    public static Thread serverTickThread;
    // can be used as a switch of the while loop.
    public static Boolean doCalc = true;
    public static int serverTickCount = 0;
    public static long[] serverTicks = new long[600];
    public static long uptime = 0;

    public static double tps(int ticks)
    {
        if (serverTickCount < ticks) {
            return 20.0D;
        }
        int target = (serverTickCount - 1 - ticks) % serverTicks.length;
        long elapsed = System.currentTimeMillis() - serverTicks[target];

        return ticks / (elapsed / 1000.0D);
    }

    public static void startUptimeCalc(Main plugin) {
        var calc = new Runnable() {
            @Override
            public void run() {
                uptime += 1;
            }
        };
        Bukkit.getServer().getScheduler().runTaskTimerAsynchronously(plugin, calc, 0, 20L);
    }

    public static void startTpsCalc(Main plugin) {
        var calc = new Runnable() {
            @Override
            public void run() {
                serverTicks[(serverTickCount % serverTicks.length)] = System.currentTimeMillis();
                serverTickCount += 1;
            }
        };
        Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, calc, 100L, 1L);
    }

    public static long uptime() {
        return uptime;
    }

    public static void startCpuLoadCalc() {
        var calc = new Runnable() {
            @SuppressWarnings("BusyWait")
            @Override
            public void run() {
                while (doCalc) {
                    long[] prevTicks = cpu.getSystemCpuLoadTicks();
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    cpuLoad = cpu.getSystemCpuLoadBetweenTicks(prevTicks) * 100;
                }
            }
        };
        cpuLoadCalcThread = new Thread(calc);
        cpuLoadCalcThread.start();
    }

    /**
     * 获得一秒内 CPU 占用信息。
     * @return CPU 占用比例
     */
    public static double cpuLoad() {
        return cpuLoad;
    }

    /**
     * 获取操作系统名称
     * @return 系统名称
     */
    public static String os() {
        var ver = os.getVersionInfo();
        return os.getFamily() + " " + ver.getVersion() + " (build " + ver.getBuildNumber() + ")";
    }

    /**
     * 获取处理器全名和频率
     * @return 处理器全名和频率
     */
    public static String cpu() {
        var id = cpu.getProcessorIdentifier();
        return id.getName();
    }

    public static int cpuLCores() {
        return cpu.getLogicalProcessorCount();
    }

    public static int cpuPCores() {
        return cpu.getPhysicalProcessorCount();
    }

    public static long ramMax() {
        return ram.getTotal();
    }

    public static long ramAvail() {
        return ram.getAvailable();
    }

    public static JSONArray ram() {
        var result = new JSONArray();
        for (var pm : ram.getPhysicalMemory()) {
            var curr = new JSONObject();
            curr.put("capacity", pm.getCapacity());
            curr.put("clocksp", pm.getClockSpeed());
            curr.put("type", pm.getMemoryType());
            result.put(curr);
        }
        return result;
    }

    public static JSONArray onlinePlayers() {
        var result = new JSONArray();
        result.putAll(Bukkit.getOnlinePlayers().stream().map(Player::getName).distinct().collect(Collectors.toList()));
        return result;
    }

    public static JSONArray offlinePlayers() {
        var result = new JSONArray();
        result.putAll(Arrays.stream(Bukkit.getOfflinePlayers()).map(OfflinePlayer::getName).distinct().collect(Collectors.toList()));
        return result;
    }
}
