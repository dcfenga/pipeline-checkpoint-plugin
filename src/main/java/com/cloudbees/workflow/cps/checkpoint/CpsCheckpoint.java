//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.cloudbees.workflow.cps.checkpoint;

import com.google.common.base.Predicates;
import hudson.FilePath;
import hudson.XmlFile;
import hudson.cli.declarative.CLIMethod;
import hudson.cli.declarative.CLIResolver;
import hudson.init.InitMilestone;
import hudson.init.Initializer;
import hudson.model.*;
import hudson.model.queue.QueueTaskFuture;
import hudson.security.Permission;
import hudson.util.HttpResponses;
import hudson.util.XStream2;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.workflow.actions.NotExecutedNodeAction;
import org.jenkinsci.plugins.workflow.cps.CpsFlowExecution;
import org.jenkinsci.plugins.workflow.cps.CpsThread;
import org.jenkinsci.plugins.workflow.cps.CpsThreadGroup;
import org.jenkinsci.plugins.workflow.flow.FlowExecutionOwner;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.jenkinsci.plugins.workflow.graphanalysis.DepthFirstScanner;
import org.jenkinsci.plugins.workflow.graphanalysis.Filterator;
import org.jenkinsci.plugins.workflow.graphanalysis.FlowScanningUtils;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.jenkinsci.plugins.workflow.support.storage.SimpleXStreamFlowNodeStorage;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.DoNotUse;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.stapler.HttpResponse;
import org.kohsuke.stapler.interceptor.RequirePOST;
import org.kohsuke.stapler.interceptor.RespondSuccess;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

public class CpsCheckpoint extends Checkpoint {
	private static final XStream2 XSTREAM = new XStream2();
	
	static {
		XSTREAM.omitField(CpsFlowExecution.class, "owner");
	}

	private transient WorkflowRun owner;
	private final transient File dir;
	private final transient int id;
	private final String name;
	private final int threadId;
	private final String ownerId;

	private CpsCheckpoint(WorkflowRun owner, File dir) {
		this.owner = owner;
		this.ownerId = owner.getExternalizableId();
		this.dir = dir;
		this.id = Integer.parseInt(dir.getName());
		this.name = null;
		this.threadId = -1;
	}

	CpsCheckpoint(WorkflowRun owner, File dir, CpsThread thread, String name) throws IOException {
		assert owner != null;

		this.owner = owner;
		this.ownerId = owner.getExternalizableId();
		this.dir = dir;
		this.id = Integer.parseInt(dir.getName());
		this.threadId = thread.id;
		if (name == null) {
			name = "Checkpoint #" + dir.getName();
		}

		this.name = name;
		CpsThreadGroup program = thread.getGroup();
		CpsFlowExecution execution = program.getExecution();
		if (!dir.mkdirs()) {
			throw new IOException("Failed to create " + dir);
		} else {
			getConfigFile(dir).write(this);
			program.saveProgram(this.getProgramFile());
			this.getExecutionStateFile().write(execution);

			try {
				(new FilePath(execution.getStorageDir())).zip(new FilePath(this.getFlowNodeStorageZip()));
			} catch (InterruptedException ie) {
				throw new AssertionError(ie);
			}
		}
	}

	public static CpsCheckpoint load(WorkflowRun owner, File dir) throws IOException {
		CpsCheckpoint c = new CpsCheckpoint(owner, dir);
		XmlFile f = getConfigFile(dir);
		if (f.exists()) {
			f.unmarshal(c);
			return c;
		} else {
			return null;
		}
	}

	public static boolean exists(File dir) {
		return getConfigFile(dir).exists();
	}

	public int getId() {
		return this.id;
	}

	public String getName() {
		return this.name;
	}

	public CpsFlowExecution restore(FlowExecutionOwner owner) throws IOException {
		ResumedCpsFlowExecution e = new ResumedCpsFlowExecution(this, owner, this.threadId);

		try {
			(new FilePath(this.getFlowNodeStorageZip())).unzip((new FilePath(e.getStorageDir())).child(".."));
		} catch (InterruptedException ie) {
			throw new AssertionError(ie);
		}

		e.load(this.getExecutionStateFile());
		this.markAllNonExecuted(e.getCurrentHeads());
		return e;
	}

	private void markAllNonExecuted(List<FlowNode> nodes) {
		if (nodes != null) {
			DepthFirstScanner scan = new DepthFirstScanner();
			scan.setup(nodes);
			Filterator<FlowNode> it = scan.filter(Predicates.not(FlowScanningUtils.hasActionPredicate(NotExecutedNodeAction.class)));

			while (it.hasNext()) {
				FlowNode f = (FlowNode) it.next();
				f.addAction(new NotExecutedNodeAction());
			}
		}
	}

	private XmlFile getExecutionStateFile() {
		return new XmlFile(XSTREAM, new File(this.dir, "execution.xml"));
	}

	File getProgramFile() {
		return new File(this.dir, "program.dat");
	}

	private File getFlowNodeStorageZip() {
		return new File(this.dir, "nodes.zip");
	}

	private static XmlFile getConfigFile(File dir) {
		return new XmlFile(XSTREAM, new File(dir, "checkpoint.xml"));
	}

	@RequirePOST
	public HttpResponse doDelete() throws IOException, InterruptedException {
		this.owner.checkPermission(Permission.DELETE);
		(new FilePath(this.dir)).deleteRecursive();
		return HttpResponses.ok();
	}

	@CLIMethod(name = "restart-checkpoint")
	@RequirePOST
	@RespondSuccess
	public QueueTaskFuture<WorkflowRun> doRestart() throws IOException, InterruptedException {
		((WorkflowJob) this.owner.getParent()).checkPermission(Item.BUILD);
		return ((WorkflowJob) this.owner.getParent()).scheduleBuild2(0, new Action[] {
				new RestoreFromCheckpointAction(this), new CauseAction(new RestoreFromCheckpointCause()) });
	}

	@SuppressWarnings({ "deprecation", "rawtypes" })
	@CLIResolver
	public static CpsCheckpoint resolveForCLI(
			@Argument(required = true, metaVar = "JOB", usage = "Job full name (e.g. myjob or folder/myjob)", index = 0) String job,
			@Argument(required = true, metaVar = "BUILD", usage = "Build number (e.g. 1 or 3 without #)", index = 1) Integer build,
			@Argument(required = true, metaVar = "CHECKPOINT", usage = "Checkpoint name", index = 2) String checkpoint)
			throws CmdLineException {
		Jenkins jenkins = Jenkins.getActiveInstance();
		Job theJob = (Job) jenkins.getItemByFullName(job, Job.class);
		if (theJob == null) {
			throw new CmdLineException("No such job found: " + job);
		} else {
			Run theBuild = theJob.getBuildByNumber(build.intValue());
			if (theBuild == null) {
				throw new CmdLineException("Build #" + build + " not found for job " + job);
			} else {
				CheckpointRunAction action = (CheckpointRunAction) theBuild.getAction(CheckpointRunAction.class);
				if (action == null) {
					throw new CmdLineException("No checkpoint found for job " + job + " and build #" + build);
				} else {
					Iterator<CpsCheckpoint> it = action.getCheckpoints().iterator();

					CpsCheckpoint theCheckpoint;
					do {
						if (!it.hasNext()) {
							throw new CmdLineException("Checkpoint " + checkpoint + " not found for job " + job + " and build #" + build);
						}

						theCheckpoint = (CpsCheckpoint) it.next();
					} while (!checkpoint.equals(theCheckpoint.getName()));

					return theCheckpoint;
				}
			}
		}
	}

	public void invalidate() {
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Run getOriginal() {
		if (this.owner == null) {
			this.owner = (WorkflowRun) Run.fromExternalizableId(this.ownerId);
		}

		return this.owner;
	}

	public void addValidator(CheckpointValidator validator) {
		throw new UnsupportedOperationException();
	}

	@Initializer(before = InitMilestone.EXTENSIONS_AUGMENTED, fatal = false)
	@Restricted({ DoNotUse.class })
	public static void setFlowNodeStorageXStreamAliases() {
		SimpleXStreamFlowNodeStorage.XSTREAM.addCompatibilityAlias("com.cloudbees.workflow.cps.checkpoint.NotExecutedNodeAction", NotExecutedNodeAction.class);
	}
}
