package org.jason.lxcoff.lib;

import java.io.Serializable;

/**
 * Container of remote execution data - to send back results of the executed
 * operation, the state of the object and actual execution time
 * 
 * @author Andrius
 * 
 */
public class ResultContainer<T> implements Serializable {

	/**
     * 
     */
	//private static final long serialVersionUID = 6289277906217259082L;
	private static final long serialVersionUID = 1L;

	transient public Object objState;
	public Object functionResult;
	public Long pureExecutionDuration;
	public String retClassName;

	/**
	 * Wrapper of results returned by remote server - state of the object the
	 * call was executed on and function result itself
	 * 
	 * @param state
	 *            state of the remoted object
	 * @param result
	 *            result of the function executed on the object
	 */
	public ResultContainer(Object state, Object result, String retclassname, Long duration) {
		objState = state;
		functionResult = result;
		pureExecutionDuration = duration;
		retClassName = retclassname;
	}

	/**
	 * Used when an exception happens, to return the exception as a result of
	 * remote invocation
	 * 
	 * @param result
	 */
	public ResultContainer(Object result) {
		objState = null;
		functionResult = result;
		pureExecutionDuration = null;
		retClassName = null;
	}
}
