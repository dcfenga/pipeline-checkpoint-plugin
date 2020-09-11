package com.cloudbees.workflow.cps.checkpoint;

import com.cloudbees.groovy.cps.Outcome;
import com.cloudbees.workflow.cps.checkpoint.CheckpointNodeAction;
import com.cloudbees.workflow.cps.checkpoint.CheckpointRunAction;
import com.cloudbees.workflow.cps.checkpoint.CpsCheckpoint;
import hudson.model.Queue.Executable;
import org.jenkinsci.plugins.workflow.cps.CpsThread;
import org.jenkinsci.plugins.workflow.cps.ThreadTask;
import org.jenkinsci.plugins.workflow.cps.ThreadTaskResult;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;

import java.io.IOException;

class CheckpointTask extends ThreadTask {
	private final String name;
	private final FlowNode flowNode;

	CheckpointTask(String name, FlowNode flowNode) {
		this.name = name;
		this.flowNode = flowNode;
	}

	public ThreadTaskResult eval(CpsThread thread) {
		try {
			Executable executable = thread.getGroup().getExecution().getOwner().getExecutable();
			if (!(executable instanceof WorkflowRun)) {
				throw new IllegalStateException("Checkpointing only works with the workflow job type");
			} else {
				WorkflowRun run = (WorkflowRun) executable;
				CheckpointRunAction action = (CheckpointRunAction) run.getAction(CheckpointRunAction.class);
				if (action == null) {
					run.addAction(action = new CheckpointRunAction());
				}

				CpsCheckpoint checkpoint = action.createCheckpoint(this.name, thread);
				CheckpointNodeAction checkpointNodeAction = new CheckpointNodeAction(checkpoint);
				this.flowNode.addAction(checkpointNodeAction);
				return ThreadTaskResult.resumeWith(new Outcome((Object) null, (Throwable) null));
			}
		} catch (IOException ioe) {
			return ThreadTaskResult.resumeWith(new Outcome((Object) null, ioe));
		}
	}
}
