/**
 * 
 */
package com.percussion.pso.demand.impl.config;

/**
 * Workflow config rules are used to map the edition to be published
 * to workflow context parameters:
 * <ul>
 * <li>{@link #edition}</li>
 * <li>{@link #workflow}</li>
 * <li>{@link #community}</li>
 * <li>{@link #transition}</li>
 * <li>{@link #contentType}</li>
 * </ul>
 * 
 * To provide flexibility not all the parameters need to be
 * specified which allows for generic matching.
 * @author adamgent
 *
 */
public class PSDemandWorkflowActionRule {
    /**
     * The edition id to publish if this rule is matched.
     */
    public int edition = -1;
    /**
     * Workflow id.
     */
    public int workflow = -1;
    /**
     * Community id.
     */
    public int community = -1;
    /**
     * transition id.
     */
    public int transition = -1;
    /**
     * Content type id.
     */
    public int contentType = -1;
    /**
     * If noop is true then do not publish.
     */
    public boolean noop = false;
    
}