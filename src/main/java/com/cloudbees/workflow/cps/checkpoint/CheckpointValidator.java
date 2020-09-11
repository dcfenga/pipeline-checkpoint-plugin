package com.cloudbees.workflow.cps.checkpoint;

import hudson.model.AbstractDescribableImpl;
import org.jenkinsci.plugins.workflow.flow.FlowExecutionOwner;

public abstract class CheckpointValidator extends AbstractDescribableImpl<CheckpointValidator> {
	public CheckpointValidator() {
	}

	public abstract boolean isValid(FlowExecutionOwner var1);
}
