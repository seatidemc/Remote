package top.seatide.remote;

import com.google.common.base.Splitter;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.util.EntityUtils;
import org.bukkit.ChatColor;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;
import org.json.JSONObject;
import top.seatide.remote.Utils.Files;
import top.seatide.remote.Utils.LogUtil;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;


@WebSocket
public class Console {
    private static final Queue<Session> sessions = new ConcurrentLinkedDeque<>();

    public static void listen() {
        System.setOut(new PrintStream(System.out) {
            @Override
            public void println(String str) {
                super.println(str);
                sendMessageToAllSessions(ChatColor.stripColor(str));
            }
        });
    }

    /**
     * 将 token 送往后端进行检查。
     *
     * @param token Token 内容
     * @param host 后端地址
     * @param callback 回调
     */
    public static void checkTokenAtBackend(String token, String host, FutureCallback<HttpResponse> callback) {
        var client = HttpAsyncClients.createDefault();
        client.start();
        var data = new JSONObject();
        data.put("type", "checkAdmin");
        data.put("token", token);
        try {
            var post = new HttpPost(host + "/api/user/v1/auth");
            post.setHeader("content-type", "application/json");
            post.setEntity(new StringEntity(data.toString()));
            client.execute(post, callback);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @OnWebSocketConnect
    public void connected(Session s) throws IOException {
        s.setIdleTimeout(1800000L); // 30 mins
        var checkHost = Files.cfg.getString("token-check-host");
        // checkHost 是 null 就禁用验证
        if (checkHost == null) {
            LogUtil.info("已建立未验证的 WebSocket 连接。");
            s.getRemote().sendString("Successfully connected through WebSocket.");
            s.getRemote().sendString("[Token Verification Disabled]");
            sessions.add(s);
            return;
        }
        var queryString = s.getUpgradeRequest().getQueryString();
        if (queryString == null) {
            s.close(1003, "Missing token.");
            return;
        }
        //noinspection UnstableApiUsage
        var query = Splitter.on('&').trimResults().withKeyValueSeparator('=').split(queryString);
        if (!query.containsKey("token")) {
            s.close(1003, "Missing token.");
            return;
        }
        var token = query.get("token");
        if (token == null) {
            s.close(1003, "Token should not be null.");
            return;
        }
        checkTokenAtBackend(token, checkHost, new FutureCallback<HttpResponse>() {
            @Override
            public void completed(HttpResponse result) {
                try {
                    var res = new JSONObject(EntityUtils.toString(result.getEntity()));
                    if (res.getBoolean("data")) {
                        LogUtil.info("已建立已验证的 WebSocket 连接。");
                        s.getRemote().sendString("Successfully connected through WebSocket.");
                        s.getRemote().sendString("[Token Verification OK]");
                        sessions.add(s);
                    } else {
                        s.close(3000, "Permission denied.");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void failed(Exception ex) {
                s.close(1011, "Unable to verify the token.");
            }

            @Override
            public void cancelled() {
                s.close(1011, "Verification is cancelled unexpectedly.");
            }
        });
    }

    @OnWebSocketClose
    public void closed(Session s, int code, String reason) {
        if (code == 1000) return;
        LogUtil.info("已关闭 WebSocket 连接：" + reason);
        sessions.remove(s);
    }

    @OnWebSocketMessage
    public void onMessage(Session s, String message) {
        System.out.println(message);
    }

    @OnWebSocketError
    public void onError(Session session, Throwable error) {
        error.printStackTrace();
    }

    public static void sendMessageToAllSessions(String msg) {
        sessions.forEach(s -> {
            try {
                s.getRemote().sendString(ChatColor.stripColor(msg));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
