/******************************************************************************
 *
 * [ PSDemandWorkflowAction.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.pso.demand.impl;

import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSWorkFlowContext;
import com.percussion.extension.IPSWorkflowAction;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.pso.demand.IPSDemandService;
import com.percussion.pso.demand.PSDemandServiceLocator;
import com.percussion.pso.demand.PSDemandWork;
import com.percussion.pso.demand.impl.config.PSDemandWorkflowActionRule;
import com.percussion.pso.demand.impl.config.PSDemandWorkflowActionConfig;
import com.percussion.server.IPSRequestContext;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.contentmgr.IPSContentMgr;
import com.percussion.services.contentmgr.PSContentMgrLocator;
import com.percussion.services.general.IPSRhythmyxInfo;
import com.percussion.services.general.PSRhythmyxInfoLocator;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.services.legacy.IPSCmsContentSummaries;
import com.percussion.services.legacy.PSCmsContentSummariesLocator;
import com.percussion.services.sitemgr.IPSSite;
import com.percussion.services.sitemgr.IPSSiteManager;
import com.percussion.services.sitemgr.PSSiteManagerLocator;
import com.percussion.util.PSSqlHelper;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.jdbc.PSConnectionDetail;
import com.percussion.utils.jdbc.PSConnectionHelper;
import com.thoughtworks.xstream.XStream;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

/**
 * The workflow action reads in a configuration file that maps  
 * workflow,transition,community,contenttype to a specific edition to
 * be executed.
 * 
 * <p>
 * The configuration file has the following format, it is an xml file:
 * 
 * <pre>
 *  &lt;?xml version=&quot;1.0&quot; encoding=&quot;UTF-8&quot;?&gt;
 *  &lt;config&gt;
 *      &lt;rule&gt;
 *          &lt;transition&gt;3&lt;/transition&gt;
 *          &lt;!-- edition is a required field --&gt;
 *          &lt;edition&gt;310&lt;/edition&gt;
 *      &lt;/rule&gt;
 *      &lt;rule&gt;
 *          &lt;workflow&gt;5&lt;/workflow&gt;
 *          &lt;edition&gt;320&lt;/edition&gt;
 *      &lt;/rule&gt;
 *      &lt;rule&gt;
 *          &lt;transition&gt;3&lt;/transition&gt;
 *          &lt;workflow&gt;5&lt;/workflow&gt;
 *          &lt;edition&gt;330&lt;/edition&gt;
 *      &lt;/rule&gt;
 *      &lt;rule&gt;
 *          &lt;transition&gt;3&lt;/transition&gt;
 *          &lt;workflow&gt;5&lt;/workflow&gt;
 *          &lt;community&gt;40&lt;/community&gt;
 *          &lt;edition&gt;340&lt;/edition&gt;
 *      &lt;/rule&gt;
 *      &lt;rule&gt;
 *          &lt;transition&gt;3&lt;/transition&gt;
 *          &lt;workflow&gt;5&lt;/workflow&gt;
 *          &lt;community&gt;40&lt;/community&gt;
 *          &lt;contentType&gt;300&lt;/contentType&gt;
 *          &lt;edition&gt;350&lt;/edition&gt;
 *      &lt;/rule&gt;
 *  &lt;/config&gt;
 * </pre>
 * 
 * The best matching rule is picked when the workflow is executed.
 * @see PSDemandWorkflowActionConfig
 * @author dougrand
 * @author adamgent
 * 
 */
public class PSDemandWorkflowAction implements IPSWorkflowAction {

    private PSDemandWorkflowActionConfig m_config;

    /**
     * Log4j logger for the demand service
     */
    private static Logger ms_logger = Logger
            .getLogger(PSDemandWorkflowAction.class);

    /**
     * Query to find the folders for a given content id in a path. This query is
     * used with the JCR engine to find a given content id in a site tree.
     */
    private static final String CONTENTQUERY = "select rx:sys_contentid, rx:sys_folderid from nt:base "
            + "where jcr:path like :path and rx:sys_contentid = :cid";

    /**
     * Guid manager service
     */
    private static IPSGuidManager gmgr = PSGuidManagerLocator.getGuidMgr();

    /**
     * Content manager service
     */
    private static IPSContentMgr cmgr = PSContentMgrLocator.getContentMgr();

    /**
     * Initialized in init. The configuration file is checked on each action to
     * see if it needs to be loaded or reloaded.
     */
    private File m_configFile;

    /**
     * The last modified time for the configuration file. Updated when the
     * configuration file is loaded.
     */
    private long m_lastModifiedTime = -1;

    public void performAction(IPSWorkFlowContext wfContext,
            IPSRequestContext request) throws PSExtensionProcessingException {
        try {
            if (m_configFile.lastModified() > m_lastModifiedTime) {
                loadConfigFile();
            }
            int editionid = computeEditionId(wfContext);
            if (editionid == 0) {
                ms_logger.debug("NO operation on this Workflow Action");
                return;
            }
            IPSSite site = findSiteForEdition(editionid);
            Query q = cmgr.createQuery(CONTENTQUERY, Query.SQL);
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("cid", wfContext.getContentID());
            params.put("path", site.getFolderRoot() + "/%");
            QueryResult results = cmgr.executeQuery(q, -1, params);
            // Now we have the material to queue
            PSDemandWork work = new PSDemandWork();
            RowIterator riter = results.getRows();
            int rev = wfContext.getBaseRevisionNum();
            PSLocator cloc = new PSLocator(wfContext.getContentID(), rev);
            IPSGuid cguid = gmgr.makeGuid(cloc);
            while (riter.hasNext()) {
                Row r = riter.nextRow();
                long fid = r.getValue("rx:sys_folderid").getLong();
                IPSGuid fguid = gmgr.makeGuid(new PSLocator((int) fid));
                work.addItem(fguid, cguid);
            }
            ms_logger.info("Edition: " + editionid + " queued work: " + work);
            IPSDemandService dsvc = PSDemandServiceLocator.getDemandService();
            dsvc.queue(editionid, work);
        } catch (Exception e) {
            ms_logger.error("Problem in action:", e);
            throw new PSExtensionProcessingException(this.getClass().getName(),
                    e);
        }
    }

    /**
     * Lookup destination site id for the given edition. Throws
     * {@link RuntimeException} if there's a problem.
     * 
     * @param editionid
     *            the edition id
     * @return the site id, or <code>0</code> if edition is not found.
     */
    private IPSSite findSiteForEdition(int editionid) {
        Connection c = null;
        try {
            c = PSConnectionHelper.getDbConnection();
            PSConnectionDetail details = PSConnectionHelper
                    .getConnectionDetail();
            String tablename = PSSqlHelper.qualifyTableName("RXEDITION",
                    details.getDatabase(), details.getOrigin(), details
                            .getDriver());
            String query = "SELECT DESTSITE FROM " + tablename
                    + " WHERE EDITIONID = ?";
            PreparedStatement s = c.prepareStatement(query);
            s.setInt(1, editionid);
            ResultSet rs = s.executeQuery();
            if (!rs.next()) {
                throw new RuntimeException(
                        "No site information found for edition " + editionid);
            } else {
                IPSSiteManager smgr = PSSiteManagerLocator.getSiteManager();
                int siteid = rs.getInt(1);
                return smgr.loadSite(gmgr.makeGuid(siteid, PSTypeEnum.SITE));
            }
        } catch (Exception e) {
            ms_logger.error(e);
            throw new RuntimeException("Unexpected problem", e);
        } finally {
            if (c != null) {
                try {
                    c.close();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    /**
     * Get the edition to run from the workflow context. This uses the map that
     * was loaded by reading in the configuration file specified in the
     * extension registration.
     * 
     * @param wfContext
     *            the workflow context
     * @return the edition id
     */
    private int computeEditionId(IPSWorkFlowContext wfContext) {
        IPSCmsContentSummaries csums = PSCmsContentSummariesLocator
                .getObjectManager();
        int contentid = wfContext.getContentID();
        PSComponentSummary sum = csums.loadComponentSummary(contentid);

        int transition = wfContext.getTransitionID();
        int workflow = wfContext.getWorkflowID();
        int community = sum.getCommunityId();
        int contenttype = new Long(sum.getContentTypeId()).intValue();
        int editionid = m_config.getEdition(transition, workflow, 
                community, contenttype);

        if (editionid < 0) {
            throw new RuntimeException(
                    "Could not find a rule to publish the edition that " +
                    "matchs the following parameters:\n"
                    + " transition=" + transition
                    + " workflow=" + workflow 
                    + " community=" + community
                    + " contenttype=" + contentid);
        }

        return editionid;
    }

    /**
     * Load the configuration file for the workflow action.
     * 
     * @throws IOException
     * @throws SAXException
     * @throws FileNotFoundException
     */
    protected void loadConfigFile() throws IOException, SAXException,
            FileNotFoundException {
        m_lastModifiedTime = m_configFile.lastModified();

        XStream xs = new XStream();
        xs.alias("config", PSDemandWorkflowActionConfig.class);
        xs.alias("rule", PSDemandWorkflowActionRule.class);
        xs.addImplicitCollection(PSDemandWorkflowActionConfig.class, "rules");

        Object input = xs.fromXML(new FileReader(m_configFile));
        m_config = (PSDemandWorkflowActionConfig) input;
        m_config.validate();

    }

    public void init(IPSExtensionDef def, File codeRoot)
            throws PSExtensionException {
        IPSRhythmyxInfo info = PSRhythmyxInfoLocator.getRhythmyxInfo();
        String configFile = info
                .getProperty(IPSRhythmyxInfo.Key.ROOT_DIRECTORY)
                + "/rxconfig/Workflow/demandpublish.xml";
        m_configFile = new File(configFile);
    }

}
