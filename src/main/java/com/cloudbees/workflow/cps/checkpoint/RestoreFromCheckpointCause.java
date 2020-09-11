package com.cloudbees.workflow.cps.checkpoint;

import com.cloudbees.workflow.cps.checkpoint.CpsCheckpoint;
import com.cloudbees.workflow.cps.checkpoint.RestoreFromCheckpointAction;
import hudson.console.ModelHyperlinkNote;
import hudson.model.Cause;
import hudson.model.Run;
import hudson.model.TaskListener;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.NoExternalUse;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

@ExportedBean
public final class RestoreFromCheckpointCause extends Cause {
	private transient Run<?, ?> build;

	private CpsCheckpoint checkpoint() {
		RestoreFromCheckpointAction action = (RestoreFromCheckpointAction) this.build.getAction(RestoreFromCheckpointAction.class);
		return action != null ? action.checkpoint : null;
	}

	@Restricted({ NoExternalUse.class })
	public Run<?, ?> getOriginalBuild() {
		CpsCheckpoint cp = this.checkpoint();
		return cp != null ? cp.getOriginal() : null;
	}

	@SuppressWarnings("rawtypes")
	@Exported
	@Restricted({ NoExternalUse.class })
	public Integer getOriginalBuildNumber() {
		Run original = this.getOriginalBuild();
		return original != null ? Integer.valueOf(original.getNumber()) : null;
	}

	@Exported
	@Restricted({ NoExternalUse.class })
	public String getCheckpointName() {
		CpsCheckpoint cp = this.checkpoint();
		return cp != null ? cp.getName() : null;
	}

	@SuppressWarnings("rawtypes")
	public String getShortDescription() {
		Run original = this.getOriginalBuild();
		return original != null ? "Restored from " + this.getCheckpointName() + " in " + original.getDisplayName(): "Restored from checkpoint";
	}

	@SuppressWarnings("rawtypes")
	public void print(TaskListener listener) {
		Run original = this.getOriginalBuild();

		if (original != null) {
			listener.getLogger().println("Restored from " + this.getCheckpointName() + " in " + ModelHyperlinkNote.encodeTo(original));
		}
	}

	@SuppressWarnings("rawtypes")
	public void onAddedTo(Run build) {
		this.build = build;
	}

	public void onLoad(Run<?, ?> build) {
		this.build = build;
	}
}
