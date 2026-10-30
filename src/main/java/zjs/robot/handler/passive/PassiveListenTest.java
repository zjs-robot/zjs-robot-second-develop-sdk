package zjs.robot.handler.passive;

import com.agibot.aiem.sdk.AgentAuthCallback;
import com.agibot.aiem.sdk.AgentMeta;
import com.agibot.aiem.sdk.AgentParam;
import com.agibot.aiem.sdk.AgentSdk;
import com.agibot.aiem.sdk.passive.Audio2LlmCallback;
import com.agibot.aiem.sdk.passive.Audio2LlmResponse;

public class PassiveListenTest {
    public static void main(String[] args) throws InterruptedException {
        String url = "wss://open.agibot.com/api/V1/open-portal/app/wss/agent-sdk";
        String appId = "app_fbfa040235b345f288a3973096044d4b";
        String appKey = "ak_0c1a1a0ea09449c5b7ea7a4a4f1d0597";
        String appSecret = "sk_508f5e902a4947ae9b0d6fd673a1ab4d";

        AgentSdk agentSdk = AgentSdk.create(url, appId, appKey, appSecret);

        agentSdk.registerAuth((appId1, code, msg) -> {
            System.out.println("【认证结果】 code: " + code + ", msg: " + msg);
        });

        // 重点：注册被动回调，用来吃掉状态推送
        agentSdk.registerAudio2Llm(new Audio2LlmCallback(agentSdk) {

            // 🎯 加上这个方法，解决 agentCallbackType is null 的报错！
            @Override
            public void onState(String agentId, String eventId, String stateName, String stateValue) {
                System.out.println("📊 【收到机器人状态】 stateName: " + stateName + ", stateValue: " + stateValue);
            }

            @Override
            public void onRobotOnline(String agentId, AgentMeta agentMeta) {
                System.out.println("✅✅✅ 机器人成功上线！ID: " + agentId);
            }

            @Override
            public void onRobotOffline(String agentId) {
                System.out.println("❌ 机器人下线了！");
            }

            @Override
            public void onRequest(String agentId, String eventId, int flag, byte[] buf, AgentParam param, Audio2LlmResponse response) {
                System.out.println("🎤 收到用户语音/交互请求！");
            }
        });

        System.out.println("开始初始化...");
        agentSdk.initialize();

        // 阻塞主线程，让它一直监听！
        Thread.sleep(Long.MAX_VALUE);
    }
}