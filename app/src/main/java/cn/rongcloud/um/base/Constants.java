package cn.rongcloud.um.base;

/**
 * 常量类
 */
public final class Constants {

    public static final String BASE_URL = "https://api-cn.ronghub.com";
    public static final String SECRET = "xxx";
    public static final String APP_KEY = "";// 传入业务申请的 AppKey
    public static final String TOKEN = "";// 传入 AppKey 对应的 Token
    public static final String USER_ID = "user1";
    public static final String PRIVATE = "userid1";
    public static final String GROUP = "group1";
    public static final String SIGN_METHOD_MD5 = "MD5";
    public static final String SIGN_METHOD_HMAC = "HMAC";
    public static final String CHARSET_UTF8 = "UTF-8";

    public static final String TAG = "retrofit";

    /**
     * ======================   sp key  start   ==============================
     */
    public static final String SP_KEY_APP_KEY = "appKey";
    public static final String SP_KEY_APP_SECRET = "appSecret";
    public static final String SP_KEY_NAV_URL = "naviServer";
    public static final String SP_KEY_FILE_URL = "fileServer";
    public static final String SP_KEY_APP_TOKEN = "token";
    public static final String SP_KEY_LANGUAGE_SETTING = "LanguageServer";
    public static final String SP_KEY_CONVERSATION_TYPE = "conversationType";
    public static final String SP_KEY_SUB_CONVERSATION_TYPE = "subConversationType";
    /**======================   sp key  end     ==============================*/

    /**
     * ======================   default avatar start   ==============================
     */
    public static final String[] DefaultAvatar = new String[]{
            "https://p26-flow-imagex-sign.byteimg.com/tos-cn-i-a9rns2rl98/rc_gen_image/c5590f863154474db0f23e089149d0d1.jpeg~tplv-a9rns2rl98-downsize_watermark_1_6_b.png?lk3s=8e244e95&rcl=202606041854004F0832CED6ACF43F7653&rrcfp=827586d3&x-expires=2095930453&x-signature=StdaONqjk1kikaroY%2BW9%2FX%2BdOk4%3D",
            "https://p26-flow-imagex-sign.byteimg.com/tos-cn-i-a9rns2rl98/rc_gen_image/239d30c1e60748deb1c613b05e0a12b0.jpeg~tplv-a9rns2rl98-downsize_watermark_1_6_b.png?lk3s=8e244e95&rcl=20260604185205EF88C804DC5AF1570400&rrcfp=827586d3&x-expires=2095930337&x-signature=Vhk24qpVPvSC7zKv3UhGh63t3rA%3D",
            "https://p6-flow-imagex-sign.byteimg.com/tos-cn-i-a9rns2rl98/rc_gen_image/be98af3dd6b248a5b0fe70d732652628.jpeg~tplv-a9rns2rl98-downsize_watermark_1_6_b.png?lk3s=8e244e95&rcl=202606041849122C6B42D278B81FB931A9&rrcfp=827586d3&x-expires=2095930166&x-signature=W%2BGmoJ4FPnu%2F2br%2Fn%2FO9Pb6cHXE%3D",
            "https://p11-flow-imagex-sign.byteimg.com/tos-cn-i-a9rns2rl98/rc_gen_image/f19aa8395b444c39b3020f0175d632c2.jpeg~tplv-a9rns2rl98-downsize_watermark_1_6_b.png?lk3s=8e244e95&rcl=20260604184817ADD124A3BAB17AB2A4DC&rrcfp=827586d3&x-expires=2095930109&x-signature=lpwcZ4ZJaJRJsIwzLCV17TS5S0M%3D",
            "https://p11-flow-imagex-sign.byteimg.com/tos-cn-i-a9rns2rl98/rc_gen_image/f19aa8395b444c39b3020f0175d632c2.jpeg~tplv-a9rns2rl98-downsize_watermark_1_6_b.png?lk3s=8e244e95&rcl=20260604184817ADD124A3BAB17AB2A4DC&rrcfp=827586d3&x-expires=2095930109&x-signature=lpwcZ4ZJaJRJsIwzLCV17TS5S0M%3D",
            "https://p11-flow-imagex-sign.byteimg.com/tos-cn-i-a9rns2rl98/rc_gen_image/f19aa8395b444c39b3020f0175d632c2.jpeg~tplv-a9rns2rl98-downsize_watermark_1_6_b.png?lk3s=8e244e95&rcl=20260604184817ADD124A3BAB17AB2A4DC&rrcfp=827586d3&x-expires=2095930109&x-signature=lpwcZ4ZJaJRJsIwzLCV17TS5S0M%3D",
            "https://p11-flow-imagex-sign.byteimg.com/tos-cn-i-a9rns2rl98/rc_gen_image/f19aa8395b444c39b3020f0175d632c2.jpeg~tplv-a9rns2rl98-downsize_watermark_1_6_b.png?lk3s=8e244e95&rcl=20260604184817ADD124A3BAB17AB2A4DC&rrcfp=827586d3&x-expires=2095930109&x-signature=lpwcZ4ZJaJRJsIwzLCV17TS5S0M%3D",
            "https://p11-flow-imagex-sign.byteimg.com/tos-cn-i-a9rns2rl98/rc_gen_image/f19aa8395b444c39b3020f0175d632c2.jpeg~tplv-a9rns2rl98-downsize_watermark_1_6_b.png?lk3s=8e244e95&rcl=20260604184817ADD124A3BAB17AB2A4DC&rrcfp=827586d3&x-expires=2095930109&x-signature=lpwcZ4ZJaJRJsIwzLCV17TS5S0M%3D",
            "https://p11-flow-imagex-sign.byteimg.com/tos-cn-i-a9rns2rl98/rc_gen_image/f19aa8395b444c39b3020f0175d632c2.jpeg~tplv-a9rns2rl98-downsize_watermark_1_6_b.png?lk3s=8e244e95&rcl=20260604184817ADD124A3BAB17AB2A4DC&rrcfp=827586d3&x-expires=2095930109&x-signature=lpwcZ4ZJaJRJsIwzLCV17TS5S0M%3D",
            "https://p11-flow-imagex-sign.byteimg.com/tos-cn-i-a9rns2rl98/rc_gen_image/f19aa8395b444c39b3020f0175d632c2.jpeg~tplv-a9rns2rl98-downsize_watermark_1_6_b.png?lk3s=8e244e95&rcl=20260604184817ADD124A3BAB17AB2A4DC&rrcfp=827586d3&x-expires=2095930109&x-signature=lpwcZ4ZJaJRJsIwzLCV17TS5S0M%3D",
           };

    /**======================   default avatar end     ==============================*/

}