package zjs.robot.handler.active;

import com.agibot.aiem.sdk.AgentAuthCallback;
import com.agibot.aiem.sdk.AgentSdk;
import com.agibot.aiem.sdk.IdGenerator;
import com.agibot.aiem.sdk.active.FlowEndCallback;
import com.agibot.aiem.sdk.active.FlowStartCallback;
import com.agibot.aiem.sdk.active.FlowTaskCallback;
import com.agibot.aiem.sdk.active.TaskFlowRequest;

public class TaskFlowExample {

    public static void main(String[] args) throws InterruptedException {

        String url = "wss://open.agibot.com/api/V1/open-portal/app/wss/agent-sdk";
        String appId = "您通过灵心开放平台创建的应用获取的appId";
        String appKey = "您通过灵心开放平台创建的应用获取的appKey";
        String appSecret = "您通过灵心开放平台创建的应用获取的appSecret";
        String agentId = "您通过灵心开放平台配置的智能体 agentId";

        AgentSdk agentSdk = AgentSdk.create(url, appId, appKey, appSecret);

        agentSdk.registerAuth(new AgentAuthCallback() {
            @Override
            public void onAuthState(String appId, int code, String msg) {
                System.out.println("onAuthState => appId: " + appId
                        + ", code: " + code + ", msg: " + msg);
            }
        });

        agentSdk.initialize();

        Thread.sleep(2000);   // 等待鉴权完成


        String flowId = IdGenerator.generateFlowId();
        TaskFlowRequest taskFlowRequest = new TaskFlowRequest(agentSdk, agentId, flowId);
        agentSdk.registerTaskFlow(taskFlowRequest);


        String startRequestPayload = "[{}]";
        taskFlowRequest.startRequest(startRequestPayload, new FlowStartCallback() {

            @Override
            public void onStartRequestAck(String flowId, String eventId, int code, String msg) {
                // 机器人已收到 start 请求
                System.out.println("onStartRequestAck => flowId: " + flowId
                        + ", eventId: " + eventId + ", code: " + code + ", msg: " + msg);
            }

            @Override
            public void onStartExecuteAck(String flowId, String eventId, int code, String msg) {
                // 机器人 start 阶段执行完成
                System.out.println("onStartExecuteAck => flowId: " + flowId
                        + ", eventId: " + eventId + ", code: " + code + ", msg: " + msg);
            }

        }, 2000);

        //
        String taskRequestPayload = "[{}]";
        taskFlowRequest.taskRequest(taskRequestPayload, new FlowTaskCallback() {

            @Override
            public void onTaskRequestAck(String flowId, String eventId, int code, String msg) {
                System.out.println("onTaskRequestAck => flowId: " + flowId
                        + ", eventId: " + eventId + ", code: " + code + ", msg: " + msg);
            }

            @Override
            public void onTaskExecuteAck(String flowId, String eventId, int code, String msg) {
                System.out.println("onTaskExecuteAck => flowId: " + flowId
                        + ", eventId: " + eventId + ", code: " + code + ", msg: " + msg);
            }

        }, 2000);

        taskFlowRequest.endRequest(new FlowEndCallback() {

            @Override
            public void onEndRequestAck(String flowId, String eventId, int code, String msg) {
                System.out.println("onEndRequestAck => flowId: " + flowId
                        + ", eventId: " + eventId + ", code: " + code + ", msg: " + msg);
            }

        }, 2000);


    }
}
