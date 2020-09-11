package com.cloudbees.workflow.pipeline.stageview;

import hudson.Extension;
import hudson.model.Action;
import hudson.model.InvisibleAction;
import jenkins.model.TransientActionFactory;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;

import java.util.Collection;
import java.util.Collections;

public class PipelineStageViewAction extends InvisibleAction {
	public final WorkflowJob job;

	private PipelineStageViewAction(WorkflowJob job) {
		this.job = job;
	}

	@Extension
	public static class Factory extends TransientActionFactory<WorkflowJob> {
		public Class<WorkflowJob> type() {
			return WorkflowJob.class;
		}

		public Collection<? extends Action> createFor(WorkflowJob job) {
			return Collections.singleton(new PipelineStageViewAction(job));
		}
	}
}
