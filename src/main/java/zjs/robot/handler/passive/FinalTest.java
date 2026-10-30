package zjs.robot.handler.passive;

import com.agibot.aiem.sdk.AgentMeta;
import com.agibot.aiem.sdk.AgentParam;
import com.agibot.aiem.sdk.AgentSdk;
import com.agibot.aiem.sdk.passive.Audio2LlmCallback;
import com.agibot.aiem.sdk.passive.Audio2LlmResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FinalTest {
    public static void main(String[] args) throws InterruptedException {
        String url = "wss://open.agibot.com/api/V1/open-portal/app/wss/agent-sdk";
        String appId = "app_fbfa040235b345f288a3973096044d4b";
        String appKey = "ak_0c1a1a0ea09449c5b7ea7a4a4f1d0597";
        String appSecret = "sk_508f5e902a4947ae9b0d6fd673a1ab4d";

        AgentSdk agentSdk = AgentSdk.create(url, appId, appKey, appSecret);

        // 1. 注册认证
        agentSdk.registerAuth((appId1, code, msg) -> {
            log.info("认证结果 => code: {}, msg: {}", code, msg);
            if (code == 0) {
                log.info("✅ 鉴权成功！请保持程序运行，然后去网页点击“测试连通性”");
            } else {
                log.error("❌ 鉴权失败");
            }
        });

        // 2. 注册被动回调（必须在 initialize 之前）
        agentSdk.registerAudio2Llm(new Audio2LlmCallback(agentSdk) {
            @Override
            public void onRobotOnline(String agentId, AgentMeta agentMeta) {
                log.info("🔥🔥🔥 机器人上线了！ID: {}, 唤醒词: {}", agentId, agentMeta.getWakeupWord());
            }

            @Override
            public void onRobotOffline(String agentId) {
                log.info("⚠️ 机器人下线了: {}", agentId);
            }

            @Override
            public void onRequest(String agentId, String eventId, int flag, byte[] buf, AgentParam param, Audio2LlmResponse response) {
                log.info("🎉 收到机器人发来的语音/数据！flag: {}", flag);
                // 这里可以写死回复
                response.onAsrFinal(eventId, "你好");
                response.onLlmDone(eventId);
            }
        });

        // 3. 最后初始化
        agentSdk.initialize();

        // 4. 阻塞主线程，防止程序退出
        Thread.sleep(Long.MAX_VALUE);
    }
}