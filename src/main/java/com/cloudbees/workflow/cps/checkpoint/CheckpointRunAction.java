package com.cloudbees.workflow.cps.checkpoint;

import com.cloudbees.workflow.cps.checkpoint.CpsCheckpoint;
import hudson.model.Run;
import jenkins.model.RunAction2;
import org.jenkinsci.plugins.workflow.cps.CpsThread;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CheckpointRunAction implements RunAction2 {
	private static final Logger LOGGER = Logger.getLogger(CheckpointRunAction.class.getName());
	private transient WorkflowRun owner;

	public void onLoad(Run<?, ?> r) {
		this.owner = (WorkflowRun) r;
	}

	public void onAttached(Run<?, ?> r) {
		this.owner = (WorkflowRun) r;
	}

	public String getDisplayName() {
		return "Checkpoints";
	}

	public String getUrlName() {
		return "checkpoints";
	}

	public String getIconFileName() {
		return "package.png";
	}

	public File getRootDir() {
		return new File(this.owner.getRootDir(), "checkpoints");
	}

	public WorkflowRun getOwner() {
		return this.owner;
	}

	public CpsCheckpoint createCheckpoint(String name, CpsThread thread) throws IOException {
		int iota = 0;

		File dir;
		do {
			++iota;
			dir = this.getCheckpointDirFor(iota);
		} while (dir.exists());

		return new CpsCheckpoint(this.owner, dir, thread, name);
	}

	public boolean hasCheckpoint(int id) {
		File dir = this.getCheckpointDirFor(id);
		return CpsCheckpoint.exists(dir);
	}

	public List<CpsCheckpoint> getCheckpoints() {
		ArrayList<CpsCheckpoint> cList = new ArrayList<CpsCheckpoint>();
		File[] files = this.getRootDir().listFiles();
		Arrays.asList(new Object[] { files, this.SORTER });
		if (files != null) {
			File[] fileArr = files;
			int len = files.length;

			for (int i = 0; i < len; ++i) {
				File file = fileArr[i];
				if (CpsCheckpoint.exists(file)) {
					try {
						CpsCheckpoint e = CpsCheckpoint.load(this.owner, file);
						cList.add(e);
					} catch (IOException ioe) {
						LOGGER.log(Level.WARNING, "Failed to load checkpoint from " + file, ioe);
					}
				}
			}
		}

		return cList;
	}

	public CpsCheckpoint getCheckpoint(int id) throws IOException {
		File dir = this.getCheckpointDirFor(id);
		return CpsCheckpoint.load(this.owner, dir);
	}

	private File getCheckpointDirFor(int index) {
		return new File(this.getRootDir(), String.valueOf(index));
	}

	public Object getDynamic(String token) throws IOException {
		try {
			int e = Integer.parseInt(token);
			return this.getCheckpoint(e);
		} catch (NumberFormatException var3) {
			return null;
		}
	}

	private Comparator<File> SORTER = new Comparator<File>() {
		public int compare(File o1, File o2) {
			return this.f(o1) - this.f(o2);
		}

		private int f(File f) {
			String s = f.getName();

			try {
				return Integer.parseInt(s);
			} catch (NumberFormatException var4) {
				return -1;
			}
		}
	};
}
