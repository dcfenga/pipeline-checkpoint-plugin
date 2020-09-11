package com.cloudbees.workflow.cps.checkpoint;

import com.cloudbees.groovy.cps.Continuable;
import com.cloudbees.workflow.cps.checkpoint.CheckpointTask;
import com.google.inject.Inject;
import hudson.Extension;
import hudson.model.Executor;
import hudson.model.TaskListener;
import org.jenkinsci.plugins.workflow.actions.LabelAction;
import org.jenkinsci.plugins.workflow.actions.TimingAction;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.jenkinsci.plugins.workflow.steps.AbstractStepDescriptorImpl;
import org.jenkinsci.plugins.workflow.steps.AbstractStepImpl;
import org.jenkinsci.plugins.workflow.steps.StepContextParameter;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.kohsuke.stapler.DataBoundConstructor;

public class CheckpointStep extends AbstractStepImpl {
	private final String name;

	@DataBoundConstructor
	public CheckpointStep(String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}

	public static class Execution extends StepExecution {
		private static final long serialVersionUID = 1L;

		@Inject
		private transient CheckpointStep step;
		@StepContextParameter
		private transient WorkflowRun run;
		@StepContextParameter
		private transient FlowNode node;
		@StepContextParameter
		private transient TaskListener listener;

		public Execution() {
		}

		public boolean start() throws Exception {
			this.node.addAction(new LabelAction(this.step.name));
			if (this.getContext().get(Executor.class) != null) {
				this.listener.getLogger().println("Using checkpoint inside of node {} is unsupported and unreliable");
			}

			if (TimingAction.getStartTime(this.node) == 0L) {
				this.node.addAction(new TimingAction());
			}

			Continuable.suspend(new CheckpointTask(this.step.name, this.node));
			throw new AssertionError();
		}

		public void stop(Throwable throwable) throws Exception {
		}
	}

	@Extension
	public static class DescriptorImpl extends AbstractStepDescriptorImpl {
		public DescriptorImpl() {
			super(CheckpointStep.Execution.class);
		}

		public String getFunctionName() {
			return "checkpoint";
		}

		public String getDisplayName() {
			return "Capture the execution state so that it can be restarted later";
		}
	}
}
