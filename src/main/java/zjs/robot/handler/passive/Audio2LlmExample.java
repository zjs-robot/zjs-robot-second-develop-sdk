package zjs.robot.handler.passive;

import com.agibot.aiem.sdk.AgentAuthCallback;
import com.agibot.aiem.sdk.AgentMeta;
import com.agibot.aiem.sdk.AgentParam;
import com.agibot.aiem.sdk.AgentSdk;
import com.agibot.aiem.sdk.passive.Audio2LlmCallback;
import com.agibot.aiem.sdk.passive.Audio2LlmResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Audio2LlmExample {

    public static void main(String[] args) {

        String url = "wss://open.agibot.com/api/V1/open-portal/app/wss/agent-sdk";
        String appId = "app_fbfa040235b345f288a3973096044d4b";
        String appKey = "ak_0c1a1a0ea09449c5b7ea7a4a4f1d0597";
        String appSecret = "sk_508f5e902a4947ae9b0d6fd673a1ab4d";
        String agentId = "AI_AGENT_04ae2eb5";

         AgentSdk agentSdk = AgentSdk.create(url, appId, appKey, appSecret);

        agentSdk.registerAuth(new AgentAuthCallback() {
            @Override
            public void onAuthState(String appId, int code, String msg) {
                log.info("onAuthState => appId: {}, code: {}, msg: {}", appId, code, msg);
            }
        });

        agentSdk.registerAudio2Llm(new Audio2LlmCallback(agentSdk) {

            @Override
            public void onRobotOnline(String agentId, AgentMeta agentMeta) {
                log.info("✅✅✅ 机器人真正上线了！ID: {}", agentId);
                log.info("唤醒词: {}", agentMeta.getWakeupWord());
            }

            @Override
            public void onRobotOffline(String agentId) {
                log.info("❌ 机器人下线了！ID: {}", agentId);
            }

            @Override
            public void onRequest(String agentId, String eventId, int flag,
                                  byte[] buf, AgentParam param, Audio2LlmResponse response) {

                log.info("请求开始-----------------");
                // 可选获取智能体默认及自定义元数据
                AgentMeta agentMeta = agentSdk.getAgentMeta(agentId);
                String defWakeupWord = agentMeta.getWakeupWord();
                String yourParamKey = agentMeta.getString("您的自定义参数key");

                // 1) ASR：根据 flag 分阶段送 ASR
                if (flag == 0) {
                    // 初始化 ASR 客户端
                } else if (flag == 1) {
                    // 给 ASR 传音频
                } else if (flag == 2) {
                    // VAD 结束
                }
                String asrMiddle = "今天";
                String asrFinal = "今天天气咋样";
                response.onAsrMiddle(eventId, asrMiddle);
                response.onAsrFinal(eventId, asrFinal);

                // 2) 语义理解 / LLM 流式输出
                String itemId = "itemId_000001";

                // 如语义理解为有效指令需返回打断，拒识则无需返回
                response.onInterrupt(eventId, "chat", null);

                // 如分类为技能则返回技能（可选）
                AgentParam skillParam = AgentParam.create().setInteger("step", 3);
                response.onSkill(eventId, itemId, "movement", "move_forward", skillParam);

                String textDelta = "假设这就是LLM流式生成的结果";
                response.onLlmItemDelta(eventId, itemId, textDelta);
                response.onLlmItemDone(eventId, itemId);
                response.onLlmDone(eventId);

                // 异常处理
                // response.onError(eventId, 1010, "假设调用asr出错了");
            }

            // ... 基类共享回调，见上文 ...
        });

        agentSdk.initialize();

    }
}
