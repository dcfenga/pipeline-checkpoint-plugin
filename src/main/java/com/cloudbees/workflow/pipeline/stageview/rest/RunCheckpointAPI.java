package com.cloudbees.workflow.pipeline.stageview.rest;

import com.cloudbees.workflow.cps.checkpoint.CheckpointNodeAction;
import com.cloudbees.workflow.flownode.FlowNodeUtil;
import com.cloudbees.workflow.pipeline.stageview.rest.CheckpointExt;
import com.cloudbees.workflow.pipeline.stageview.rest.CloudBeesFlowNodeUtil;
import com.cloudbees.workflow.rest.AbstractWorkflowRunActionHandler;
import com.cloudbees.workflow.util.ModelUtil;
import com.cloudbees.workflow.util.ServeJson;
import hudson.Extension;
import org.jenkinsci.plugins.workflow.flow.FlowExecution;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.DoNotUse;
import org.kohsuke.accmod.restrictions.NoExternalUse;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

@Extension
public class RunCheckpointAPI extends AbstractWorkflowRunActionHandler {
	public static final String URL_BASE = "wfapi-checkpoints";

	public String getUrlName() {
		return "wfapi-checkpoints";
	}

	public static String getUrl(WorkflowRun run) {
		return ModelUtil.getFullItemUrl(run.getUrl()) + "wfapi-checkpoints";
	}

	@ServeJson
	@Restricted({ DoNotUse.class })
	public List<CheckpointExt> doDynamic() {
		return getCheckpointInfo(this.getRun());
	}

	@Nonnull
	@Restricted({ NoExternalUse.class })
	protected static List<CheckpointExt> getCheckpointInfo(@Nonnull WorkflowRun run) {
		ArrayList<CheckpointExt> checkpointExts = new ArrayList<CheckpointExt>();

		FlowExecution exec = run.getExecution();
		if (exec == null) {
			return Collections.emptyList();
		} else {
			List<FlowNode> stageNodes = FlowNodeUtil.getStageNodes(exec);
			Iterator<FlowNode> it = stageNodes.iterator();

			while (it.hasNext()) {
				FlowNode stageNode = (FlowNode) it.next();
				List<CheckpointNodeAction> stageCheckpoints = CloudBeesFlowNodeUtil.getStageCheckpoints(stageNode);
				Iterator<CheckpointNodeAction> itCP = stageCheckpoints.iterator();

				while (itCP.hasNext()) {
					CheckpointNodeAction checkpointNodeAction = (CheckpointNodeAction) itCP.next();
					CheckpointExt checkpointExt = CheckpointExt.create(checkpointNodeAction);
					checkpointExt.setStageId(stageNode.getId());
					checkpointExts.add(checkpointExt);
				}
			}

			return checkpointExts;
		}
	}
}
