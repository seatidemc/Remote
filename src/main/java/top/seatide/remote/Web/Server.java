package top.seatide.remote.Web;

import com.alibaba.fastjson.JSONObject;
import top.seatide.remote.Utils.DeviceUtil;
import top.seatide.remote.Utils.LogUtil;

import static spark.Spark.*;

public class Server {
    public static void startServer(int p) {
        port(p);
        initExceptionHandler((e) -> LogUtil.error("初始化 HTTP 出现错误：" + e.getMessage()));
        errors();
        map();
    }

    private static String ng(String error) {
        var json = new JSONObject();
        json.put("status", "ng");
        json.put("error", error);
        return json.toJSONString();
    }

    private static String ok(String content) {
        var json = new JSONObject();
        json.put("status", "ok");
        json.put("content", content);
        return json.toJSONString();
    }

    private static String ok(JSONObject content) {
        var json = new JSONObject();
        json.put("status", "ok");
        json.put("content", content);
        return json.toJSONString();
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
        exception(Exception.class, (e, req, res) -> LogUtil.error("Exception occurred: " + e.getMessage()));
    }

    private static void map() {
        get("/", (req, res) -> {
            res.type("text/html");
            return "<h1>Remote Default Page</h1>";
        });
        get("/device/constant/:type", (req, res) -> {
            var type = req.params("type");
            switch (type) {
                case "os": {
                    return ok(DeviceUtil.os());
                }

                case "cpu": {
                    return ok(DeviceUtil.cpu());
                }

                default: {
                    return ok("Invalid argument.");
                }
            }
        });
        get("/device/dynamic/:type", (req, res) -> {
            var type = req.params("type");
            switch (type) {
                case "ramload": {
                    var result = 100.0 - (100.0 * DeviceUtil.ramAvail() / DeviceUtil.ramMax());
                    return ok(String.format("%.2f", result));
                }

                case "cpuload": {
                    return ok(String.format("%.2f", DeviceUtil.cpuLoad()));
                }

                default: {
                    return ok("Invalid argument.");
                }
            }
        });
    }
}
