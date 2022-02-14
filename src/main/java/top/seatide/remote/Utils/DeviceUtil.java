package top.seatide.remote.Utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.OperatingSystem;

public class DeviceUtil {
    public static SystemInfo si = new SystemInfo();
    public static OperatingSystem os = si.getOperatingSystem();
    public static HardwareAbstractionLayer hal = si.getHardware();
    public static CentralProcessor cpu = hal.getProcessor();
    public static GlobalMemory ram = hal.getMemory();
    public static double cpuLoad = 0.0;
    public static Thread cpuLoadCalcThread;
    // can be used as a switch of the while loop.
    public static Boolean doCalc = true;

    public static void startCpuLoadCalc() {
        var calc = new Runnable() {
            @SuppressWarnings("BusyWait")
            @Override
            public void run() {
                while (doCalc) {
                    long[] prevTicks = cpu.getSystemCpuLoadTicks();
                    long[][] prevProcTicks = cpu.getProcessorCpuLoadTicks();
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    long[] ticks = cpu.getSystemCpuLoadTicks();
                    cpuLoad = cpu.getSystemCpuLoadBetweenTicks(prevTicks) * 100;
                }
            }
        };;
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
            curr.put("manufaturer", pm.getManufacturer());
            result.add(curr);
        }
        return result;
    }
}
