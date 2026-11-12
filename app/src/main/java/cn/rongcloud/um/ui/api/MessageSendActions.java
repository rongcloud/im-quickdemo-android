package cn.rongcloud.um.ui.api;

import android.net.Uri;
import cn.rongcloud.um.custom.CustomMessage;
import io.rong.imlib.RongCoreClient;
import io.rong.imkit.feature.forward.CombineMessage;
import io.rong.imlib.location.message.LocationMessage;
import io.rong.imlib.IRongCoreCallback;
import io.rong.imlib.IRongCoreEnum.CoreErrorCode;
import io.rong.imlib.model.MentionedInfo;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.MessageContent;
import io.rong.message.FileMessage;
import io.rong.message.GIFMessage;
import io.rong.message.HQVoiceMessage;
import io.rong.message.ImageMessage;
import io.rong.message.ReferenceMessage;
import io.rong.message.RichContentMessage;
import io.rong.message.SightMessage;
import io.rong.message.TextMessage;
import static cn.rongcloud.um.ui.api.ApiCatalog.Api.*;
import static cn.rongcloud.um.ui.api.ApiCatalog.Param.*;

/** A named method per message type, sharing only the actual send/callback mechanics. */
final class MessageSendActions {
    private MessageSendActions() { }
    static void register(ApiActionRegistry.Builder registry) {
        registry.register(SEND_TEXT, c -> send(c, TextMessage.obtain(c.request.text(CONTENT)), false, false));
        registry.register(SEND_IMAGE_UPLOADED, MessageSendActions::sendUploadedImage);
        registry.register(SEND_FILE_UPLOADED, MessageSendActions::sendUploadedFile);
        registry.register(SEND_SIGHT_UPLOADED, MessageSendActions::sendUploadedSight);
        registry.register(SEND_LOCATION, MessageSendActions::sendLocation);
        registry.register(SEND_COMBINE, MessageSendActions::sendCombine);
        registry.register(SEND_DIRECTED, MessageSendActions::sendDirected);
        registry.register(SEND_CUSTOM, c -> send(c, CustomMessage.obtain(c.request.text(CONTENT), c.request.text(IMAGE_URL)), false, true));
        registry.register(REGISTER_CUSTOM, c -> {
            c.log.write("固定参数: CustomMessage.class，objectName=app:custom；初始化时已注册，此处演示显式注册。");
            RongCoreClient.registerMessageType(CustomMessage.class);
            c.log.write("registerMessageType 已返回（void，无异步回调或错误码）。收发双方均需注册同一消息类型。");
        });
        registry.register(SEND_IMAGE, MessageSendActions::sendImage);
        registry.register(SEND_MENTION, MessageSendActions::sendMention);
        registry.register(SEND_VOICE, MessageSendActions::sendVoice);
        registry.register(SEND_FILE, MessageSendActions::sendFile);
        registry.register(SEND_SIGHT, MessageSendActions::sendSight);
        registry.register(SEND_HQ_VOICE, MessageSendActions::sendHqVoice);
        registry.register(SEND_RICH, MessageSendActions::sendRich);
        registry.register(SEND_REFERENCE, MessageSendActions::sendReference);
        registry.register(SEND_GIF, MessageSendActions::sendGif);
    }
    static void sendExpansionText(ApiContext c) {
        send(c, TextMessage.obtain(c.request.text(CONTENT)), false, true);
    }
    private static void sendImage(ApiContext c) {
        Uri uri = uri(c);
        Message message = Message.obtain(c.request.text(TARGET), c.request.type(),
                ImageMessage.obtain(uri, uri, c.request.bool(IMAGE_ORIGINAL)));
        c.log.write("SDK message 参数:\n" + ApiLogFormatter.format(message));
        c.core.sendImageMessage(message, c.request.text(PUSH_CONTENT), c.request.text(PUSH_DATA),
                new IRongCoreCallback.SendImageMessageCallback() {
                    @Override public void onAttached(Message value) { attached(c, value); }
                    @Override public void onProgress(Message value, int progress) {
                        c.log.write("回调 onProgress，进度: " + progress + "%\n结果:\n" + ApiLogFormatter.format(value));
                    }
                    @Override public void onSuccess(Message value) { c.success(value); }
                    @Override public void onError(Message value, CoreErrorCode error) { c.error(error, value); }
                });
    }
    private static void sendUploadedImage(ApiContext c) {
        // Only the remote resource is sent; no local path or SDK upload is involved.
        ImageMessage content = ImageMessage.obtain();
        content.setRemoteUri(Uri.parse(c.request.text(REMOTE_URL)));
        c.log.write("发送方式: sendMessage(ImageMessage)，直接使用 remoteUrl，无本地文件、无 SDK 上传");
        send(c, content, false, false);
    }
    private static void sendUploadedFile(ApiContext c) {
        // The public decode constructor avoids obtain(localUri)'s local file inspection.
        FileMessage content = new FileMessage("{}".getBytes(java.nio.charset.StandardCharsets.UTF_8));
        content.setFileUrl(Uri.parse(c.request.text(REMOTE_URL)));
        String name = c.request.text(FILE_NAME);
        String type = c.request.text(FILE_TYPE).trim();
        if (type.isEmpty() && name.lastIndexOf('.') >= 0) type = name.substring(name.lastIndexOf('.') + 1);
        content.setName(name);
        content.setSize(c.request.longValue(FILE_SIZE));
        content.setType(type);
        c.log.write("发送方式: sendMessage(FileMessage)，直接使用 remoteUrl，无本地文件、无 SDK 上传");
        send(c, content, false, false);
    }
    private static void sendUploadedSight(ApiContext c) {
        SightMessage content = new SightMessage();
        content.setMediaUrl(Uri.parse(c.request.text(REMOTE_URL)));
        content.setDuration(c.request.integer(DURATION));
        content.setSize(c.request.longValue(FILE_SIZE));
        c.log.write("发送方式: sendMessage(SightMessage)，直接使用 remoteUrl，无本地文件、无 SDK 上传");
        send(c, content, false, false);
    }
    private static void sendLocation(ApiContext c) {
        send(c, LocationMessage.obtain(c.request.decimal(LATITUDE), c.request.decimal(LONGITUDE),
                c.request.text(POI), uri(c)), false, false);
    }
    private static void sendCombine(ApiContext c) {
        CombineMessage content = CombineMessage.obtain(uri(c));
        content.setTitle(c.request.text(COMBINE_TITLE));
        content.setConversationType(c.request.type());
        content.setNameList(c.request.strings(COMBINE_NAMES));
        content.setSummaryList(c.request.strings(COMBINE_SUMMARIES));
        send(c, content, true, false);
    }
    private static void sendDirected(ApiContext c) {
        Message message = Message.obtain(c.request.text(TARGET), c.request.type(), TextMessage.obtain(c.request.text(CONTENT)));
        c.log.write("SDK message 参数:\n" + ApiLogFormatter.format(message));
        c.core.sendDirectionalMessage(message, c.request.strings(DIRECTED_IDS).toArray(new String[0]),
                c.request.text(PUSH_CONTENT), c.request.text(PUSH_DATA), sendCallback(c, false));
    }
    private static void sendMention(ApiContext c) {
        TextMessage content = TextMessage.obtain(c.request.text(CONTENT));
        MentionedInfo.MentionedType type = c.request.mentionType();
        content.setMentionedInfo(new MentionedInfo(type,
                c.request.strings(MENTION_IDS), c.request.text(MENTION_TEXT)));
        send(c, content, false, false);
    }
    private static void sendVoice(ApiContext c) {
        // Legacy VoiceMessage embeds Base64 audio; HQVoiceMessage carries a remote URL.
        HQVoiceMessage content = HQVoiceMessage.obtain(null, c.request.integer(DURATION));
        content.setFileUrl(Uri.parse(c.request.text(REMOTE_URL)));
        c.log.write("发送方式: sendMessage(HQVoiceMessage)，直接使用 remoteUrl，无本地文件、无 SDK 上传");
        send(c, content, false, false);
    }
    private static void sendFile(ApiContext c) { send(c, FileMessage.obtain(uri(c)), true, false); }
    private static void sendSight(ApiContext c) {
        send(c, SightMessage.obtain(uri(c), c.request.integer(DURATION)), true, false);
    }
    private static void sendHqVoice(ApiContext c) {
        send(c, HQVoiceMessage.obtain(uri(c), c.request.integer(DURATION)), true, false);
    }
    private static void sendRich(ApiContext c) {
        send(c, RichContentMessage.obtain(c.request.text(RICH_TITLE), c.request.text(CONTENT),
                c.request.text(IMAGE_URL), c.request.text(URL)), false, false);
    }
    private static void sendReference(ApiContext c) {
        send(c, ReferenceMessage.obtainMessage(c.request.text(UID), TextMessage.obtain(c.request.text(CONTENT))), false, false);
    }
    private static void sendGif(ApiContext c) { send(c, GIFMessage.obtain(uri(c)), true, false); }
    private static Uri uri(ApiContext c) { return Uri.fromFile(c.request.localFile()); }

    private static void send(ApiContext c, MessageContent content, boolean media, boolean expansion) {
        if (content == null) throw new IllegalArgumentException("无法构造消息，请检查文件格式与参数");
        Message message = Message.obtain(c.request.text(TARGET), c.request.type(), content);
        if (expansion) {
            message.setCanIncludeExpansion(true);
            c.log.write("固定发送参数: canIncludeExpansion=true");
        }
        c.log.write("SDK message 参数:\n" + ApiLogFormatter.format(message));
        if (media) c.core.sendMediaMessage(message, c.request.text(PUSH_CONTENT), c.request.text(PUSH_DATA), mediaCallback(c));
        else c.core.sendMessage(message, c.request.text(PUSH_CONTENT), c.request.text(PUSH_DATA), sendCallback(c, expansion));
    }
    private static IRongCoreCallback.ISendMessageCallback sendCallback(ApiContext c, boolean expansion) {
        return new IRongCoreCallback.ISendMessageCallback() {
            @Override public void onAttached(Message message) { attached(c, message); }
            @Override public void onSuccess(Message message) {
                c.success(message);
                if (expansion && message != null) c.log.write("后续设置 / 删除扩展使用的 messageUid: " + message.getUId());
            }
            @Override public void onError(Message message, CoreErrorCode error) { c.error(error, message); }
        };
    }
    private static IRongCoreCallback.ISendMediaMessageCallback mediaCallback(ApiContext c) {
        return new IRongCoreCallback.ISendMediaMessageCallback() {
            @Override public void onAttached(Message message) { attached(c, message); }
            @Override public void onProgress(Message message, int progress) {
                c.log.write("回调 onProgress，进度: " + progress + "%\n结果:\n" + ApiLogFormatter.format(message));
            }
            @Override public void onSuccess(Message message) { c.success(message); }
            @Override public void onError(Message message, CoreErrorCode error) { c.error(error, message); }
            @Override public void onCanceled(Message message) {
                c.log.write("状态: 已取消\n回调: onCanceled\n错误码: 无（取消回调）\n结果:\n" + ApiLogFormatter.format(message));
            }
        };
    }
    private static void attached(ApiContext c, Message message) {
        c.log.write("状态: 已入库，等待发送回调\n回调: onAttached\n结果:\n" + ApiLogFormatter.format(message));
    }
}
