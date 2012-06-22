/******************************************************************************
 *
 * [ PSDemandContentListGenerator.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.pso.demand.impl;

import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.pso.demand.IPSDemandService;
import com.percussion.pso.demand.PSDemandServiceLocator;
import com.percussion.pso.demand.PSDemandWork;
import com.percussion.server.PSRequest;
import com.percussion.server.webservices.PSServerFolderProcessor;
import com.percussion.services.contentmgr.IPSContentPropertyConstants;
import com.percussion.services.contentmgr.data.PSQueryResult;
import com.percussion.services.contentmgr.data.PSRow;
import com.percussion.services.contentmgr.data.PSRowComparator;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.services.legacy.PSCmsObjectMgrLocator;
import com.percussion.services.publisher.IPSContentListGenerator;
import com.percussion.services.publisher.impl.PSSelectedItemsGenerator;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.jsr170.PSLongValue;
import com.percussion.utils.types.PSPair;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Value;
import javax.jcr.query.QueryResult;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * Content list generator for demand publishing
 */
public class PSDemandContentListGenerator implements IPSContentListGenerator
{
   /**
    * CMS service
    */
   private static IPSCmsObjectMgr ms_cms = PSCmsObjectMgrLocator
         .getObjectManager();
   
   /**
    * Guid manager
    */
   private static IPSGuidManager ms_gmgr = PSGuidManagerLocator.getGuidMgr();

   /**
    * Logger
    */
   private static Logger ms_log = Logger
         .getLogger(PSSelectedItemsGenerator.class);

   @SuppressWarnings("unused")
   public QueryResult generate(Map<String, String> parameters)
   {
      IPSDemandService dsvc = PSDemandServiceLocator.getDemandService();
      List<PSPair<String, Boolean>> fields = new ArrayList<PSPair<String, Boolean>>();
      fields.add(new PSPair<String, Boolean>(
            IPSContentPropertyConstants.RX_SYS_CONTENTID, true));
      PSRowComparator comparator = new PSRowComparator(fields);
      String[] columns =
      {IPSContentPropertyConstants.RX_SYS_CONTENTID,
            IPSContentPropertyConstants.RX_SYS_FOLDERID};
      PSQueryResult qr = new PSQueryResult(columns, comparator);

      String edition = parameters.get(IPSHtmlParameters.SYS_EDITIONID);
      if (StringUtils.isBlank(edition))
         return qr;

      int editionid = Integer.parseInt(edition);
      List<PSDemandWork> items = dsvc.getEditionCurrentWork(editionid);
      
      if (items != null && items.size() > 0)
      {
         // Loop through the ids. If the id is a folder, then expand recursively,
         // building the result. If the id is an item, just add to the result.
         for(PSDemandWork work : items)
         {
            for(PSPair<IPSGuid,IPSGuid> item : work.getContent())
            {
               PSLocator folder = ms_gmgr.makeLocator(item.getFirst());
               PSLocator content = ms_gmgr.makeLocator(item.getSecond());
               PSComponentSummary sum = 
                  ms_cms.loadComponentSummary(content.getId());
               addIdToResult(qr, sum, folder.getId());
               
               // In here check the configuration and search related content
               // to add
            }
         }
      }
      return qr;
   }

   /**
    * For the given id, determine if it is a folder or a content item. Folders
    * are enumerated into subitems and recursed. Items are simply added
    * 
    * @param qr the query result
    * @param sum the sumary of the item
    * @param folderid the id of the parent folder
    */
   private void addIdToResult(PSQueryResult qr, PSComponentSummary sum,
         int folderid)
   {
      if (sum.getObjectType() == 1)
      {
         // Item
         Map<String, Object> data = new HashMap<String, Object>();
         Value idval = new PSLongValue(sum.getContentId());
         Value folderval = new PSLongValue(folderid);
         data.put(IPSContentPropertyConstants.RX_SYS_CONTENTID, idval);
         data.put(IPSContentPropertyConstants.RX_SYS_FOLDERID, folderval);
         PSRow row = new PSRow(data);
         qr.addRow(row);
      }
      else if (sum.getObjectType() == 2)
      {
         PSRequest req = PSRequest.getContextForRequest();
         PSServerFolderProcessor proc = new PSServerFolderProcessor(req, null);
         try
         {
            PSComponentSummary sums[] = proc.getChildSummaries(sum
                  .getCurrentLocator());
            for (PSComponentSummary s : sums)
            {
               addIdToResult(qr, s, sum.getContentId());
            }
         }
         catch (PSCmsException e)
         {
            ms_log.error("Problem extracting child ids from folder: "
                  + sum.getContentId(), e);
         }
      }
   }

   @SuppressWarnings("unused")
   public void init(IPSExtensionDef def, File codeRoot)
   {
      // TODO Auto-generated method stub

   }
}
