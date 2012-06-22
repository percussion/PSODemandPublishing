/**
 * 
 * @author adamgent
 */
package com.percussion.pso.demand.impl.config;

import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;


/**
 * Container for the workflow rules config.
 * The config is based on a list of rules see {@link PSDemandWorkflowActionRule}.
 * When you request for an edition {@link #getEdition(int, int, int, int)}
 * with the given parameters the configuration will find a rule that best matchs that
 * configuration. 
 * 
 * @see PSDemandWorkflowActionRule
 * @see #getEdition(int, int, int, int)
 * @author adamgent
 *
 */
public class PSDemandWorkflowActionConfig {

    List<PSDemandWorkflowActionRule> rules;
    
    /**
     * Validates the configuration.
     * @return true if the configuration is correct.
     * @throws RuntimeException if the configuration is bad.
     */
    public boolean validate() {
        if (rules == null) throw new RuntimeException("Configuration had no rules");
        for (PSDemandWorkflowActionRule r : rules) {
            if (r.edition <= 0 && ! r.noop) {
                throw new IllegalArgumentException("Edition cannot be less than " +
                        "or equal to zero unless noop is set.");
                
            }
            if (r.transition <= 0 && r.workflow <= 0 && r.community <=0) {
                throw new IllegalArgumentException("Publish Rule is not specific enough:" +
                        "Need to specify atleast either workflow, community, or transition");
            }
        }
        return true;
    }
    
    /**
     * Gets the edition from the best matching rule.
     * <br>
     * For example lets say you have two rules like:
     * <pre>
     * Rule r1 = new Rule();
     * r1.transition = 5;
     * r1.edition = 1;
     * Rule r2 = new Rule();
     * r2.workflow = 6;
     * r2.edition = 2;
     * </pre>
     * Get edition {@link #getEdition(int, int, int, int)} will return:
     * <pre>
     * 1 == getEdition(5,anyvalue,anyvalue,anyvalue);
     * 2 == getEdition(anyvalue,6,anyvalue,anyvalue);
     * </pre>
     * @param transition id 
     * @param workflow id
     * @param community id
     * @param contentType id
     * @return <code>-1</code> if the edition cannot be 
     *      found from any of the rules. 
     *      <code>0</code> is returned if the rule is a noop.
     * @see PSDemandWorkflowActionRule
     */
    public int getEdition(final int transition, 
            final int workflow, 
            final int community,
            final int contentType) {
        PriorityQueue<PSDemandWorkflowActionRule> matchs = new PriorityQueue<PSDemandWorkflowActionRule>(1,
                new Comparator<PSDemandWorkflowActionRule>() {
                    public int compare(PSDemandWorkflowActionRule o1, PSDemandWorkflowActionRule o2) {
                        int r2 = getRank(o2);
                        int r1 = getRank(o1) ;
                        return r1 - r2;
                    }
                    public int getRank(PSDemandWorkflowActionRule r) {
                        int rank = 0;
                        if (r.contentType == contentType) {
                            rank = rank - 1;
                        }
                        if (r.community == community) {
                            rank = rank - 10;
                        }
                        if (r.workflow == workflow) {
                            rank = rank - 1000;
                        }
                        if (r.transition == transition) {
                            rank = rank - 100;
                        }
                        return rank;
                    }
                }
        );

        for (PSDemandWorkflowActionRule r: rules) {
            boolean test = r.transition != transition && r.transition > 0;
            test = test || (r.community != community && r.community > 0);
            test = test || (r.workflow != workflow && r.workflow > 0);
            test = test || (r.contentType != contentType && r.contentType > 0);
            test = ! test;
            if (test)
                matchs.add(r);
        }
        
        if (matchs.size() > 0) {
            PSDemandWorkflowActionRule r = matchs.peek();
            if (r.noop) {
                return 0;
            }
            else {
                return r.edition;
            }
        }
        else {
            return -1;
        }
        
    }
}