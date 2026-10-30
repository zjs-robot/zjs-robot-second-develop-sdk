package zjs.robot.handler.active;

import com.agibot.aiem.sdk.AgentAuthCallback;
import com.agibot.aiem.sdk.AgentSdk;
import com.agibot.aiem.sdk.IdGenerator;
import com.agibot.aiem.sdk.active.FlowEndCallback;
import com.agibot.aiem.sdk.active.FlowStartCallback;
import com.agibot.aiem.sdk.active.TaskFlowRequest;

public class TaskFlowActiveTest {
    public static void main(String[] args) throws InterruptedException {
        String url = "wss://open.agibot.com/api/V1/open-portal/app/wss/agent-sdk";
        String appId = "app_fbfa040235b345f288a3973096044d4b";
        String appKey = "ak_0c1a1a0ea09449c5b7ea7a4a4f1d0597";
        String appSecret = "sk_508f5e902a4947ae9b0d6fd673a1ab4d";
        String agentId = "AI_AGENT_04ae2eb5";

        System.out.println("1. 开始创建 SDK 实例...");
        AgentSdk agentSdk = AgentSdk.create(url, appId, appKey, appSecret);

        // 注册鉴权回调
        agentSdk.registerAuth(new AgentAuthCallback() {
            @Override
            public void onAuthState(String appId, int code, String msg) {
                System.out.println("【认证回调】code: " + code + ", msg: " + msg);
            }
        });

        System.out.println("2. 初始化连接...");
        agentSdk.initialize();

        // 等待鉴权成功，给足 3 秒时间
        Thread.sleep(3000);

        // 生成唯一任务流 ID
        String flowId = IdGenerator.generateFlowId();
        System.out.println("3. 准备注册任务流, flowId: " + flowId);

        TaskFlowRequest taskFlowRequest = new TaskFlowRequest(agentSdk, agentId, flowId);
        agentSdk.registerTaskFlow(taskFlowRequest);

        // 构建合法的 TTS 播报 Payload
        // 注意：这里的 JSON 格式必须正确，tts 是动作类型，config 里填要播报的文字
        String startRequestPayload = "[{\"actions\":[{\"action_type\":\"tts\",\"config\":{\"tts\":\"你好，我是X2机器人，主动交互测试成功！\"}}]}]";

        System.out.println("4. 发送 Start 请求，Payload: " + startRequestPayload);

        // 发送开始请求
        taskFlowRequest.startRequest(startRequestPayload, new FlowStartCallback() {
            @Override
            public void onStartRequestAck(String flowId, String eventId, int code, String msg) {
                // 这里非常重要！如果打印了，说明网关收到了请求并转给了机器人
                System.out.println("🔥🔥🔥 【收到 Start 响应】 code: " + code + ", msg: " + msg);
                if (code != 0) {
                    System.out.println("❌ 失败原因请看上面的 msg！");
                }
            }

            @Override
            public void onStartExecuteAck(String flowId, String eventId, int code, String msg) {
                // 机器人真正执行完毕（比如语音播报完了）才会走到这里
                System.out.println("✅✅✅ 【机器人执行完毕 Start】 code: " + code + ", msg: " + msg);

                // 此时可以发送 end 请求结束流程
                taskFlowRequest.endRequest(new FlowEndCallback() {
                    @Override
                    public void onEndRequestAck(String flowId, String eventId, int code, String msg) {
                        System.out.println("【收到 End 响应】 code: " + code + ", msg: " + msg);
                    }
                }, 5000); // 给 end 请求留 5 秒超时
            }
        }, 10000); // ⚠️ 将超时时间放大到 10 秒！防止网络波动导致没收到 Ack

        // 阻塞主线程，不要让程序退出！让回调有足够的时间打印出来
        System.out.println("5. 请求已发送，等待回调... (程序不会退出，按 Ctrl+C 停止)");
        Thread.sleep(Long.MAX_VALUE);
    }
}