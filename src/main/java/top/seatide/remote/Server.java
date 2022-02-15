package top.seatide.remote;

import com.google.common.base.Charsets;
import org.bukkit.Bukkit;
import org.json.JSONException;
import org.json.JSONObject;
import spark.Spark;
import top.seatide.remote.Utils.DeviceUtil;
import top.seatide.remote.Utils.Files;
import top.seatide.remote.Utils.LogUtil;

import java.io.File;

import static spark.Spark.*;

public class Server {
    public static void startServer(int p) {
        port(p);
        initExceptionHandler((e) -> LogUtil.error("初始化 HTTP 出现错误：" + e.getMessage()));
        webSocket("/server/console", Console.class);
        errors();
        map();
    }

    public static void stop() {
        Spark.stop();
    }

    private static String ng(String error) {
        var json = new JSONObject();
        json.put("status", "ng");
        json.put("error", error);
        return json.toString();
    }

    private static String ok(String content) {
        var json = new JSONObject();
        json.put("status", "ok");
        json.put("content", content);
        return json.toString();
    }

    private static String ok(JSONObject content) {
        var json = new JSONObject();
        json.put("status", "ok");
        json.put("content", content);
        return json.toString();
    }

    private static void errors() {
        notFound((req, res) -> {
            res.type("application/json");
            return ng("http-404");
        });
        internalServerError((req, res) -> {
            res.type("application/json");
            return ng("http-500");
        });
        exception(Exception.class, (e, req, res) -> {
            LogUtil.error("处理 HTTP 请求时出现了内部问题。");
            e.printStackTrace();
        });
    }

    private static void map() {
        var indexPage = Files.cfg.getString("index-page");
        if (indexPage != null) {
            get("/", (req, res) -> {
                res.type("text/html");
                //noinspection UnstableApiUsage
                return com.google.common.io.Files.toString(Files.getFile(new File(Files.cwd), indexPage), Charsets.UTF_8);
            });
        }
        get("/device/constant/:type", (req, res) -> {
            res.type("application/json");
            var type = req.params("type");
            switch (type) {
                case "os": {
                    return ok(DeviceUtil.os());
                }

                case "cpu": {
                    return ok(DeviceUtil.cpu());
                }

                default: {
                    return ng("Invalid argument.");
                }
            }
        });
        get("/device/dynamic/:type", (req, res) -> {
            res.type("application/json");
            var type = req.params("type");
            switch (type) {
                case "ramload": {
                    return ok(String.format("%.2f", 100.0 - (100.0 * DeviceUtil.ramAvail() / DeviceUtil.ramMax())));
                }

                case "cpuload": {
                    return ok(String.format("%.2f", DeviceUtil.cpuLoad()));
                }

                case "tps": {
                    return ok(String.format("%.2f", DeviceUtil.tps(100)));
                }

                case "onlinePlayerCount": {
                    return ok(String.valueOf(Bukkit.getOnlinePlayers().size()));
                }

                case "totalPlayerCount": {
                    return ok(String.valueOf(Bukkit.getOfflinePlayers().length));
                }

                case "onlinePlayerList": {
                    return ok(DeviceUtil.onlinePlayers().toString());
                }

                case "allPlayerList": {
                    return ok(DeviceUtil.offlinePlayers().toString());
                }

                case "uptime": {
                    return ok(String.valueOf(DeviceUtil.uptime()));
                }

                default: {
                    return ng("Invalid argument.");
                }
            }
        });
        get("/all-info", (req,res) -> {
            res.type("application/json");
            var result = new JSONObject();
            // device
            var device = new JSONObject();
            // device.ram
            var ram = new JSONObject();
            ram.put("max", DeviceUtil.ramMax());
            ram.put("avail", DeviceUtil.ramAvail());
            ram.put("physical", DeviceUtil.ram());
            device.put("ram", ram);
            // device.cpu
            var cpu = new JSONObject();
            cpu.put("usage", DeviceUtil.cpuLoad());
            cpu.put("name", DeviceUtil.cpu());
            var cpuPhysical = new JSONObject();
            cpuPhysical.put("lcores", DeviceUtil.cpuLCores());
            cpuPhysical.put("pcores", DeviceUtil.cpuPCores());
            cpu.put("physical", cpuPhysical);
            device.put("cpu", cpu);
            // server
            var server = new JSONObject();
            server.put("tps", DeviceUtil.tps(100));
            server.put("online", DeviceUtil.onlinePlayers());
            server.put("total", DeviceUtil.offlinePlayers());
            server.put("uptime", DeviceUtil.uptime());
            result.put("device", device);
            result.put("server", server);
            return result.toString();
        });
        post("/server/exec", (req, res) -> {
            res.type("application/json");
            JSONObject input;
            String password;
            String command;
            try {
                input = new JSONObject(req.body());
                password = input.getString("password");
                command = input.getString("command");
            } catch (JSONException e) {
                return ng("Invalid request body.");
            }
            if (password == null || command == null) {
                res.status(500);
                return ng("Missing required argument.");
            }
            if (!password.equals(Main.serverPassword)) {
                res.status(400);
                return ng("Permission denied");
            }
            var success = Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
            return ok(String.valueOf(success));
        });
    }
}
