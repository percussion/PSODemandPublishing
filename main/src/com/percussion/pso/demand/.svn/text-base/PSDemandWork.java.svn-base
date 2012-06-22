/******************************************************************************
 *
 * [ PSEditionWork.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.pso.demand;

import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.types.PSPair;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;

/**
 * Represents a single unit of work.
 */
public class PSDemandWork
{
   /**
    * The list of content to publish. Each pair consists of a folder id and a
    * content id.
    */
   private List<PSPair<IPSGuid, IPSGuid>> m_content = new ArrayList<PSPair<IPSGuid, IPSGuid>>();

   /**
    * The request that this work is associated with. Set when the work is
    * queued.
    */
   private long m_request = 0;

   /**
    * Add a content item to the work to be published
    * 
    * @param folderid the folder id, never <code>null</code>
    * @param contentitem the content item, never <code>null</code>
    */
   public void addItem(IPSGuid folderid, IPSGuid contentitem)
   {
      if (folderid == null)
      {
         throw new IllegalArgumentException("folderid may not be null");
      }
      if (contentitem == null)
      {
         throw new IllegalArgumentException("contentitem may not be null");
      }
      m_content.add(new PSPair<IPSGuid, IPSGuid>(folderid, contentitem));
   }

   /**
    * Get the content to be published.
    * 
    * @return the content never <code>null</code>.
    */
   public List<PSPair<IPSGuid, IPSGuid>> getContent()
   {
      return m_content;
   }

   /**
    * The request id is not set by the user of this object, rather it is set
    * when the work is queued with the service. The id is returned from the
    * queuing call as well as being set into this property for retrieval.
    * 
    * @return the request id
    */
   public long getRequest()
   {
      return m_request;
   }

   /**
    * @param request the request to set
    */
   public void setRequest(long request)
   {
      m_request = request;
   }

   /*
    * (non-Javadoc)
    * 
    * @see java.lang.Object#equals(java.lang.Object)
    */
   @Override
   public boolean equals(Object obj)
   {
      return EqualsBuilder.reflectionEquals(obj, this);
   }

   /*
    * (non-Javadoc)
    * 
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode()
   {
      return (int) m_request;
   }

   /*
    * (non-Javadoc)
    * 
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString()
   {
      StringBuilder buf = new StringBuilder();
      buf.append("\n<Work request: ");
      buf.append(m_request);
      buf.append(" item count: ");
      buf.append(m_content.size());
      buf.append(">");
      return buf.toString();
   }
}
