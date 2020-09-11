package com.cloudbees.workflow.cps.checkpoint;

import org.jvnet.localizer.Localizable;
import org.jvnet.localizer.ResourceBundleHolder;

public class Messages {
	private static final ResourceBundleHolder holder = ResourceBundleHolder.get(Messages.class);

	public static String CLI_restart_checkpoint_shortDescription() {
		return holder.format("CLI.restart-checkpoint.shortDescription", new Object[0]);
	}

	public static Localizable _CLI_restart_checkpoint_shortDescription() {
		return new Localizable(holder, "CLI.restart-checkpoint.shortDescription", new Object[0]);
	}
}
