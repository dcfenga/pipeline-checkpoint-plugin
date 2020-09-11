package com.cloudbees.workflow.cps.checkpoint;

import com.cloudbees.workflow.cps.checkpoint.CheckpointRunAction;
import com.cloudbees.workflow.cps.checkpoint.CpsCheckpoint;
import hudson.Extension;
import hudson.ExtensionList;
import hudson.model.Action;
import hudson.model.InvisibleAction;
import hudson.model.Queue.QueueAction;
import hudson.model.Run;
import org.jenkinsci.plugins.workflow.cps.CpsFlowExecution;
import org.jenkinsci.plugins.workflow.cps.CpsFlowFactoryAction2;
import org.jenkinsci.plugins.workflow.flow.FlowCopier;
import org.jenkinsci.plugins.workflow.flow.FlowDefinition;
import org.jenkinsci.plugins.workflow.flow.FlowExecutionOwner;
import org.jenkinsci.plugins.workflow.flow.FlowExecutionOwner.Executable;
import org.jenkinsci.plugins.workflow.flow.StashManager.StashBehavior;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

public class RestoreFromCheckpointAction extends InvisibleAction implements CpsFlowFactoryAction2, QueueAction {
	final CpsCheckpoint checkpoint;

	public RestoreFromCheckpointAction(CpsCheckpoint checkpoint) {
		this.checkpoint = checkpoint;
	}

	public boolean shouldSchedule(List<Action> actions) {
		return true;
	}

	@SuppressWarnings("rawtypes")
	public CpsFlowExecution create(FlowDefinition def, FlowExecutionOwner owner, List<? extends Action> actions) throws IOException {
		Run original = this.checkpoint.getOriginal();
		if (original instanceof Executable) {
			FlowExecutionOwner originalOwner = ((Executable) original).asFlowExecutionOwner();

			try {
				Iterator x = ExtensionList.lookup(FlowCopier.class).iterator();

				while (x.hasNext()) {
					FlowCopier copier = (FlowCopier) x.next();
					copier.copy(originalOwner, owner);
				}
			} catch (InterruptedException ie) {
				throw new IOException("Failed to copy metadata", ie);
			}
		}

		return this.checkpoint.restore(owner);
	}

	@Extension
	public static class SaveStashes extends StashBehavior {
		public SaveStashes() {
		}

		public boolean shouldClearAll(Run<?, ?> build) {
			return build.getAction(CheckpointRunAction.class) == null;
		}
	}
}
