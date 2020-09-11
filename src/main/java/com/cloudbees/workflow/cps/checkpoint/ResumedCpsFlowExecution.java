package com.cloudbees.workflow.cps.checkpoint;

import com.cloudbees.groovy.cps.Outcome;
import com.cloudbees.workflow.cps.checkpoint.CpsCheckpoint;
import com.google.common.util.concurrent.FutureCallback;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import hudson.XmlFile;
import org.jenkinsci.plugins.workflow.cps.CpsFlowExecution;
import org.jenkinsci.plugins.workflow.cps.CpsThreadGroup;
import org.jenkinsci.plugins.workflow.flow.FlowExecutionOwner;
import org.jenkinsci.plugins.workflow.support.concurrent.Futures;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

class ResumedCpsFlowExecution extends CpsFlowExecution {
	private static final Logger LOGGER = Logger.getLogger(ResumedCpsFlowExecution.class.getName());
	private final transient CpsCheckpoint checkpoint;
	private final transient int threadId;

	ResumedCpsFlowExecution(CpsCheckpoint checkpoint, FlowExecutionOwner owner, int threadId) throws IOException {
		super((String) null, false, owner);
		this.checkpoint = checkpoint;
		this.threadId = threadId;
	}

	void load(XmlFile executionStateFile) throws IOException {
		executionStateFile.unmarshal(this);
		this.initializeStorage();
	}

	public void start() throws IOException {
		this.loadProgramAsync(this.checkpoint.getProgramFile());
		Futures.addCallback(this.programPromise, new FutureCallback<CpsThreadGroup>() {
			public void onSuccess(CpsThreadGroup g) {
				g.getThread(ResumedCpsFlowExecution.this.threadId).resume(new Outcome(ResumedCpsFlowExecution.this.checkpoint, (Throwable) null));
			}

			public void onFailure(Throwable t) {
				ResumedCpsFlowExecution.LOGGER.log(Level.FINE, "Failed to load program", t);
			}
		});
	}

	public static final class ConverterImpl implements Converter {
		private final org.jenkinsci.plugins.workflow.cps.CpsFlowExecution.ConverterImpl base;

		public ConverterImpl(XStream xs) {
			this.base = new org.jenkinsci.plugins.workflow.cps.CpsFlowExecution.ConverterImpl(xs);
		}

		@SuppressWarnings("rawtypes")
		public boolean canConvert(Class type) {
			return ResumedCpsFlowExecution.class == type;
		}

		public void marshal(Object source, HierarchicalStreamWriter w, MarshallingContext context) {
			this.base.marshal(source, w, context);
		}

		public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
			return this.base.unmarshal(reader, context);
		}
	}
}
