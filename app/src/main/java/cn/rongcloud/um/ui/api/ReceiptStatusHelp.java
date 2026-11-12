package cn.rongcloud.um.ui.api;

/** Capability and testing notes shown on each receipt/status page. */
public final class ReceiptStatusHelp {
    private ReceiptStatusHelp() { }

    public static String forApi(ApiCatalog.Api api) {
        switch (api) {
            case SEND_PRIVATE_RECEIPT:
                return "由消息接收方调用。timestamp 填写已读最后一条消息的 sentTime，targetId 填消息发送者 ID；对方可在“已读回执接收监听”观察结果。";
            case SEND_GROUP_RECEIPT_REQUEST:
                return "Android 使用 sendReadReceiptRequest，SDK 内部构造回执请求。填写自己已发送成功的普通群消息 UID；本页先查询本地消息并核对会话，再发送请求。";
            case SEND_GROUP_RECEIPT_RESPONSE:
                return "旧版群回执：填写收到且已阅读的群消息 UID，可用逗号分隔。发送者需先发起群回执请求；SDK 根据本地消息的回执信息发送响应。所有消息须属于同一群的默认频道。";
            case SEND_GROUP_RECEIPT_RESPONSE_V2:
                return "新版群回执 V2：Android 使用 ReadReceiptV2Manager，需 AppKey 已开通对应群回执版本。填写已阅读的群消息 UID 和所属频道；不需要旧版请求流程。此功能不是 V5；版本不匹配等错误会显示 SDK 错误码。";
            case SEND_SYNC_READ_STATUS:
                return "在同一账号多端在线时调用。timestamp 填已读最后一条消息的 sentTime；另一端在“多端同步阅读状态监听”观察回调。";
            case SEND_TYPING_STATUS:
                return "仅支持单聊。typingContentType 默认 RC:TxtMsg。SDK 同一会话约 6 秒限频，方法返回 void，没有发送成功/失败回调；请在对端“输入中状态监听”确认。";
            case LISTEN_GROUP_RECEIPT_RESPONSE:
                return listenerNote() + "此页同时监听旧版响应（响应用户及时间）和 V2 响应（已读人数、总人数及频道）。请在原消息发送端观察。";
            case LISTEN_GROUP_RECEIPT_REQUEST:
                return listenerNote() + "在群消息接收端开始监听，再由消息发送者发起旧版回执请求。";
            case LISTEN_READ_RECEIPT:
                return listenerNote() + "在单聊消息发送端开始监听，再由接收者发送单聊已读回执。兼容 AppKey 的旧版与 V2 回执分发入口。";
            case LISTEN_SYNC_READ_STATUS:
                return listenerNote() + "使用同一账号在另一端同步阅读状态。此回调仅提供会话类型和 targetId，没有消息列表或时间戳。";
            case LISTEN_TYPING_STATUS:
                return listenerNote() + "由单聊对端发送输入状态；日志显示会话、用户及输入类型，空集合表示输入状态结束。";
            default: return "";
        }
    }
    private static String listenerNote() {
        return "点击开始监听后观察回调；停止或离开页面会移除本页监听。演示页面按需注册，真实应用建议 SDK 初始化后统一管理监听。";
    }
}
