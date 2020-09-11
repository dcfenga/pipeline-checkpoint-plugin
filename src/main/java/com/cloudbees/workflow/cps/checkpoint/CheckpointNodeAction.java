package com.cloudbees.workflow.cps.checkpoint;

import com.cloudbees.workflow.cps.checkpoint.CpsCheckpoint;
import hudson.model.InvisibleAction;
import org.jenkinsci.plugins.workflow.graph.FlowNode;

public class CheckpointNodeAction extends InvisibleAction {
	private final int checkpointId;
	private final String checkpointName;
	private final String baseUrl;

	public CheckpointNodeAction(CpsCheckpoint checkpoint) {
		this.checkpointId = checkpoint.getId();
		this.checkpointName = checkpoint.getName();
		this.baseUrl = checkpoint.getOriginal().getUrl();
	}

	public int getCheckpointId() {
		return this.checkpointId;
	}

	public String getCheckpointName() {
		return this.checkpointName;
	}

	public String getBaseUrl() {
		return this.baseUrl;
	}

	public static CheckpointNodeAction getAction(FlowNode node) {
		return (CheckpointNodeAction) node.getAction(CheckpointNodeAction.class);
	}
}
