package zjs.robot.handler.passive;

import com.agibot.aiem.sdk.*;
import com.agibot.aiem.sdk.passive.*;

public class QuickStart {
    public static void main(String[] args) {

        String url = "wss://open.agibot.com/api/V1/open-portal/app/wss/agent-sdk";
        String appId = "app_d33f0951534144308ba9609b4e1b16b3";
        String appKey = "ak_d360053b65a841478bf95a86931ff403";
        String appSecret = "sk_fd054cf6d83448d9a46ec1b47faf9be9";

        // ① 创建SDK实例（单例，同一appId多次调用返回同一实例）
        AgentSdk agentSdk = AgentSdk.create(url, appId, appKey, appSecret);

        // ② 注册认证回调（监听连接状态）
        agentSdk.registerAuth((appId1, code, msg) -> {
            if (code == 0) {
                System.out.println("连接认证成功");
            } else {
                System.err.println("认证失败: code=" + code + ", msg=" + msg);
            }
        });

        // ③ 注册被动回调：Asr2Llm（机器人已完成 ASR，SDK 仅做语义理解/LLM）
        agentSdk.registerAsr2Llm(new Asr2LlmCallback(agentSdk) {

            @Override
            public void onRobotOnline(String agentId, AgentMeta agentMeta) {
                // 机器人上线，可获取元数据（唤醒词、城市等）
                System.out.println("机器人上线: " + agentId);
                System.out.println("唤醒词: " + agentMeta.getWakeupWord());
            }

            @Override
            public void onRobotOffline(String agentId) {
                // 机器人下线，清理相关资源
                System.out.println("机器人下线: " + agentId);
            }

            /**
             * @param text     上游 ASR 识别完成的文本（一般为 final asr）
             * @param param    扩展参数
             * @param response 应答对象，回填 LLM/技能/打断结果
             */
            @Override
            public void onRequest(String agentId, String eventId, String text,
                                  AgentParam param, Asr2LlmResponse response) {

                // 拿 text（例如"今天天气怎么样"）做语义理解 + 知识库查询 + LLM 总结
                String itemId = "itemId_000001";

                // 如语义理解为有效指令需返回打断，拒识则无需返回
                response.onInterrupt(eventId, "chat", null);

                // 如分类为技能则下发技能指令（可选）
                AgentParam skillParam = AgentParam.create().setInteger("step", 3);
                response.onSkill(eventId, itemId, "movement", "move_forward", skillParam);

                // LLM 流式输出
                response.onLlmItemDelta(eventId, itemId, "今天天气不错，");
                response.onLlmItemDelta(eventId, itemId, "适合出行。");
                response.onLlmItemDone(eventId, itemId);
                response.onLlmDone(eventId);

                // 异常处理（按需启用）
                // response.onError(eventId, 1010, "LLM 调用失败");
            }
        });

        // ④ 初始化（建立WebSocket连接，自动重连）
        agentSdk.initialize();
    }
}