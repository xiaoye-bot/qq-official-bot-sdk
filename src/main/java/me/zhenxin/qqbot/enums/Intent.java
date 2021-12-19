package me.zhenxin.qqbot.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Intents
 *
 * @author 真心
 * @since 2021/12/9 0:37
 */
@SuppressWarnings("PointlessBitwiseExpression")
@Getter
@AllArgsConstructor
public enum Intent {
    /**
     * 频道相关事件
     */
    GUILDS(1 << 0),
    /**
     * 频道成员相关事件
     */
    GUILD_MEMBERS(1 << 1),
    /**
     * 用户消息事件(私域可用)
     */
    USER_MESSAGES(1 << 9),
    /**
     * 消息表态相关事件
     */
    GUILD_MESSAGE_REACTIONS(1 << 10),
    /**
     * 私聊消息相关事件
     */
    DIRECT_MESSAGE(1 << 12),
    /**
     * 论坛相关事件
     */
    FORUM_EVENT(1 << 28),
    /**
     * 音频相关事件
     */
    AUDIO_ACTION(1 << 29),
    /**
     * 艾特消息事件
     */
    AT_MESSAGES(1 << 30);

    private final Integer value;
}
