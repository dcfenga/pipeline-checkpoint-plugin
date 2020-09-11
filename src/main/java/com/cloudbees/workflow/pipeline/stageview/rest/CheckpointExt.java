package com.cloudbees.workflow.pipeline.stageview.rest;

import com.cloudbees.workflow.cps.checkpoint.CheckpointNodeAction;
import com.cloudbees.workflow.cps.checkpoint.CpsCheckpoint;
import com.cloudbees.workflow.util.ModelUtil;

public class CheckpointExt {
	private int id;
	private String name;
	private String stageId;
	private String restartUrl;
	private String deleteUrl;

	public CheckpointExt() {
	}

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getStageId() {
		return this.stageId;
	}

	public void setStageId(String stageId) {
		this.stageId = stageId;
	}

	public String getRestartUrl() {
		return this.restartUrl;
	}

	public void setRestartUrl(String restartUrl) {
		this.restartUrl = restartUrl;
	}

	public String getDeleteUrl() {
		return this.deleteUrl;
	}

	public void setDeleteUrl(String deleteUrl) {
		this.deleteUrl = deleteUrl;
	}

	public static CheckpointExt create(CpsCheckpoint checkpoint) {
		return create(checkpoint.getId(), checkpoint.getName(), checkpoint.getOriginal().getUrl());
	}

	public static CheckpointExt create(CheckpointNodeAction checkpointNodeAction) {
		return create(checkpointNodeAction.getCheckpointId(), checkpointNodeAction.getCheckpointName(), checkpointNodeAction.getBaseUrl());
	}

	public static CheckpointExt create(int checkpointId, String checkpointName, String baseUrl) {
		CheckpointExt checkpointExt = new CheckpointExt();
		checkpointExt.setId(checkpointId);
		checkpointExt.setName(checkpointName);
		
		String checkpointUrl = ModelUtil.getFullItemUrl(baseUrl) + "checkpoints/" + checkpointExt.getId();
		checkpointExt.setRestartUrl(checkpointUrl + "/restart");
		checkpointExt.setDeleteUrl(checkpointUrl + "/delete");
		
		return checkpointExt;
	}
}
