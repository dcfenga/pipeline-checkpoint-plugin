package com.cloudbees.workflow.pipeline.stageview.rest;

import com.cloudbees.workflow.cps.checkpoint.CheckpointNodeAction;
import com.cloudbees.workflow.flownode.FlowNodeUtil;
import org.jenkinsci.plugins.workflow.graph.FlowNode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CloudBeesFlowNodeUtil {
	public static List<CheckpointNodeAction> getStageCheckpoints(FlowNode stageStartNode) {
		List<FlowNode> stageNodes = FlowNodeUtil.getStageNodes(stageStartNode);
		Iterator<FlowNode> it = stageNodes.iterator();

		ArrayList<CheckpointNodeAction> cpsCheckpoints = new ArrayList<CheckpointNodeAction>();
		while (it.hasNext()) {
			FlowNode stageNode = (FlowNode) it.next();
			CheckpointNodeAction checkpointAction = CheckpointNodeAction.getAction(stageNode);
			if (checkpointAction != null) {
				cpsCheckpoints.add(checkpointAction);
			}
		}

		return cpsCheckpoints;
	}
}
