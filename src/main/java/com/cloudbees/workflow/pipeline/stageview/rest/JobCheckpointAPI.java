package com.cloudbees.workflow.pipeline.stageview.rest;

import com.cloudbees.workflow.pipeline.stageview.rest.CheckpointExt;
import com.cloudbees.workflow.pipeline.stageview.rest.RunCheckpointAPI;
import com.cloudbees.workflow.rest.AbstractWorkflowJobActionHandler;
import com.cloudbees.workflow.util.ServeJson;
import hudson.Extension;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.DoNotUse;
import org.kohsuke.stapler.Stapler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Extension
public class JobCheckpointAPI extends AbstractWorkflowJobActionHandler {
	public static final String URL_BASE = "wfapi-checkpoints";

	public String getUrlName() {
		return "wfapi-checkpoints";
	}

	@ServeJson
	@Restricted({ DoNotUse.class })
	public Map<String, List<CheckpointExt>> doDynamic() {
		String[] runIds = Stapler.getCurrentRequest().getParameterValues("runId");

		HashMap<String, List<CheckpointExt>> checkpointExts = new HashMap<String, List<CheckpointExt>>();
		if (runIds != null && runIds.length > 0) {
			String[] runIdArr = runIds;
			int len = runIds.length;

			for (int i = 0; i < len; ++i) {
				String runId = runIdArr[i];
				WorkflowRun run = this.getJob().getBuildByNumber(Integer.parseInt(runId));
				if (run != null) {
					checkpointExts.put(runId, RunCheckpointAPI.getCheckpointInfo(run));
				}
			}
		}

		return checkpointExts;
	}
}
