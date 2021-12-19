package me.zhenxin.qqbot.core;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import me.zhenxin.qqbot.entity.*;
import me.zhenxin.qqbot.entity.ws.Hello;
import me.zhenxin.qqbot.entity.ws.Identify;
import me.zhenxin.qqbot.entity.ws.Payload;
import me.zhenxin.qqbot.entity.ws.Ready;
import me.zhenxin.qqbot.enums.Intent;
import me.zhenxin.qqbot.event.*;
import org.java_websocket.enums.ReadyState;

import java.util.Timer;
import java.util.TimerTask;

/**
 * WebSocket 事件
 *
 * @author 真心
 * @since 2021/12/17 13:05
 */
@Slf4j
class WSEvent {
    @Setter
    WSClient client;
    private Timer timer;
    private String sessionId;
    private User me;

    // OP 0
    public void onDispatch(Payload payload) {
        String e = payload.getT();
        switch (e) {
            case "READY":
                Ready ready = JSONUtil.toBean((JSONObject) payload.getD(), Ready.class);
                sessionId = ready.getSessionId();
                client.getEventHandler().setMe(ready.getUser());
                me = ready.getUser();
                log.info("机器人已上线!");
                break;
            case "GUILD_CREATE":
                Guild gc = JSONUtil.toBean((JSONObject) payload.getD(), Guild.class);
                log.info("新增频道: {}({})", gc.getName(), gc.getId());
                GuildCreateEvent guildCreateEvent = new GuildCreateEvent(this, gc);
                client.getEventHandler().onGuildCreate(guildCreateEvent);
                break;
            case "GUILD_UPDATE":
                Guild gu = JSONUtil.toBean((JSONObject) payload.getD(), Guild.class);
                log.info("频道信息变更: {}({})", gu.getName(), gu.getId());
                GuildUpdateEvent guildUpdateEvent = new GuildUpdateEvent(this, gu);
                client.getEventHandler().onGuildUpdate(guildUpdateEvent);
                break;
            case "GUILD_DELETE":
                Guild gd = JSONUtil.toBean((JSONObject) payload.getD(), Guild.class);
                log.info("退出频道: {}({})", gd.getName(), gd.getId());
                GuildDeleteEvent guildDeleteEvent = new GuildDeleteEvent(this, gd);
                client.getEventHandler().onGuildDelete(guildDeleteEvent);
                break;
            case "CHANNEL_CREATE":
                Channel cc = JSONUtil.toBean((JSONObject) payload.getD(), Channel.class);
                log.info("子频道创建: {}({})", cc.getName(), cc.getId());
                ChannelCreateEvent channelCreateEvent = new ChannelCreateEvent(this, cc);
                client.getEventHandler().onChannelCreate(channelCreateEvent);
                break;
            case "CHANNEL_UPDATE":
                Channel cu = JSONUtil.toBean((JSONObject) payload.getD(), Channel.class);
                log.info("子频道更新: {}({})", cu.getName(), cu.getId());
                ChannelUpdateEvent channelUpdateEvent = new ChannelUpdateEvent(this, cu);
                client.getEventHandler().onChannelUpdate(channelUpdateEvent);
                break;
            case "CHANNEL_DELETE":
                Channel cd = JSONUtil.toBean((JSONObject) payload.getD(), Channel.class);
                log.info("子频道删除: {}({})", cd.getName(), cd.getId());
                ChannelDeleteEvent channelDeleteEvent = new ChannelDeleteEvent(this, cd);
                client.getEventHandler().onChannelDelete(channelDeleteEvent);
                break;
            case "GUILD_MEMBER_ADD":
                Member ma = JSONUtil.toBean((JSONObject) payload.getD(), Member.class);
                log.info("频道用户增加: {}[{}]({})", ma.getUser().getUsername(), ma.getNick(), ma.getUser().getId());
                GuildMemberAddEvent guildMemberAddEvent = new GuildMemberAddEvent(this, ma);
                client.getEventHandler().onGuildMemberAdd(guildMemberAddEvent);
                break;
            case "GUILD_MEMBER_UPDATE":
                Member mu = JSONUtil.toBean((JSONObject) payload.getD(), Member.class);
                log.info("频道用户更新: {}[{}]({})", mu.getUser().getUsername(), mu.getNick(), mu.getUser().getId());
                GuildMemberUpdateEvent guildMemberUpdateEvent = new GuildMemberUpdateEvent(this, mu);
                client.getEventHandler().onGuildMemberUpdate(guildMemberUpdateEvent);
                break;
            case "GUILD_MEMBER_REMOVE":
                Member md = JSONUtil.toBean((JSONObject) payload.getD(), Member.class);
                log.info("频道用户删除: {}[{}]({})", md.getUser().getUsername(), md.getNick(), md.getUser().getId());
                GuildMemberRemoveEvent guildMemberRemoveEvent = new GuildMemberRemoveEvent(this, md);
                client.getEventHandler().onGuildMemberRemove(guildMemberRemoveEvent);
                break;
            case "MESSAGE_REACTION_ADD":
                MessageReaction ra = JSONUtil.toBean((JSONObject) payload.getD(), MessageReaction.class);
                log.info("表情添加: {}({})", JSONUtil.toJsonStr(ra.getTarget()), ra.getChannelId());
                MessageReactionAddEvent messageReactionAddEvent = new MessageReactionAddEvent(this, ra);
                client.getEventHandler().onMessageReactionAdd(messageReactionAddEvent);
                break;
            case "MESSAGE_REACTION_REMOVE":
                MessageReaction rr = JSONUtil.toBean((JSONObject) payload.getD(), MessageReaction.class);
                log.info("表情移除: {}({})", JSONUtil.toJsonStr(rr.getTarget()), rr.getChannelId());
                MessageReactionRemoveEvent messageReactionRemoveEvent = new MessageReactionRemoveEvent(this, rr);
                client.getEventHandler().onMessageReactionRemove(messageReactionRemoveEvent);
                break;
            case "AT_MESSAGE_CREATE":
                Message atMessage = JSONUtil.toBean((JSONObject) payload.getD(), Message.class);
                log.info(
                        "[AtMessage]: 频道({}) 子频道({}) {}({}): {}",
                        atMessage.getGuildId(),
                        atMessage.getChannelId(),
                        atMessage.getAuthor().getUsername(),
                        atMessage.getAuthor().getId(),
                        atMessage.getContent()
                );
                if (client.getEventHandler().isRemoveAt()) {
                    atMessage.setContent(atMessage.getContent().replaceAll("<@!" + me.getId() + "> ", ""));
                    atMessage.setContent(atMessage.getContent().replaceAll("<@!" + me.getId() + ">", ""));
                }
                AtMessageEvent atMessageEvent = new AtMessageEvent(this, atMessage);
                client.getEventHandler().onAtMessage(atMessageEvent);
                break;
            case "MESSAGE_CREATE":
                Message userMessage = JSONUtil.toBean((JSONObject) payload.getD(), Message.class);
                log.info(
                        "[UserMessage]: 频道({}) 子频道({}) {}({}): {}",
                        userMessage.getGuildId(),
                        userMessage.getChannelId(),
                        userMessage.getAuthor().getUsername(),
                        userMessage.getAuthor().getId(),
                        userMessage.getContent()
                );
                UserMessageEvent userMessageEvent = new UserMessageEvent(this, userMessage);
                client.getEventHandler().onUserMessage(userMessageEvent);
                break;
            case "RESUMED":
                log.info("恢复连接成功, 离线消息已处理!");
                break;
            default:
                log.warn("未知事件: " + e);
        }
    }

    // OP 7
    public void onReconnect() {
        log.info("服务端通知客户端重连!");
    }

    // OP 9
    public void onInvalidSession() {
        log.error("鉴权失败!");
    }

    // OP 10
    public void onHello(Payload payload) {
        Hello hello = JSONUtil.toBean((JSONObject) payload.getD(), Hello.class);
        if (sessionId == null || sessionId.isEmpty()) {
            sendIdentify();
        } else {
            timer.cancel();
            sendResumed();
        }
        startHeartbeatTimer(hello.getHeartbeatInterval());
    }

    // OP 11
    public void onHeartbeatACK() {
        log.debug("已收到服务端心跳.");
    }

    public void onClientClose(int code, String reason, boolean remote) {
        if (remote) {
            log.info("服务端关闭连接, 原因 {} {}", code, reason);
        } else {
            log.info("客户端关闭连接, 原因 {} {}", code, reason);
        }
        if (code == 4014) {
            System.exit(code);
        }

        log.info("5秒后开始尝试恢复连接...");
        try {
            Thread.sleep(5000);
            new Thread(() -> reConnect(code)).start();
        } catch (Exception e) {
            e.printStackTrace();
            log.info("重新连接失败,请检查网络!");
        }
    }

    private void startHeartbeatTimer(Integer i) {
        timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                Payload payload = new Payload();
                payload.setOp(1);
                payload.setD(client.getSeq());
                if (client.getReadyState() == ReadyState.OPEN) {
                    client.send(JSONUtil.toJsonStr(payload));
                }
            }
        };
        timer.schedule(task, i, i);
    }

    private void reConnect(Integer code) {
        log.info("正在重新连接...");
        client.reconnect();
        if (code != 4009) {
            sessionId = null;
        }
    }

    private void sendIdentify() {
        int intentsNum = 0;
        for (Intent intent : client.getIntents()) {
            intentsNum = intentsNum | intent.getValue();
        }
        Identify identify = new Identify();
        identify.setToken(client.getToken());
        identify.setIntents(intentsNum);
        Payload identifyPayload = new Payload();
        identifyPayload.setOp(2);
        identifyPayload.setD(identify);
        client.send(JSONUtil.toJsonStr(identifyPayload));
    }

    private void sendResumed() {
        JSONObject data = new JSONObject();
        data.set("token", client.getToken());
        data.set("session_id", sessionId);
        data.set("seq", client.getSeq());
        Payload payload = new Payload();
        payload.setOp(6);
        payload.setD(data);
        client.send(JSONUtil.toJsonStr(payload));
    }
}
