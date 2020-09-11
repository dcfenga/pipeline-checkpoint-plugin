package com.cloudbees.workflow.cps.checkpoint;

import com.cloudbees.workflow.cps.checkpoint.CheckpointValidator;
import hudson.model.Run;

public abstract class Checkpoint {

	public abstract void invalidate();

	public abstract Run<?, ?> getOriginal();

	public abstract void addValidator(CheckpointValidator var1);
}
