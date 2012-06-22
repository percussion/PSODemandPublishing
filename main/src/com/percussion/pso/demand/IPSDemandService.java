/******************************************************************************
*
* [ IPSDemandService.java ]
*
* COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
* All rights reserved. This material contains unpublished, copyrighted
* work including confidential and proprietary information of Percussion.
*
*****************************************************************************/
package com.percussion.pso.demand;

import java.util.List;


/**
 * A service that enables the publishing of content to a site on demand.
 */
public interface IPSDemandService
{
   /**
    * The status of the request.
    */
   public enum Status
   {
      /**
       * The item is in the queue, but is not currently being published.
       */
      QUEUED,
      /**
       * The item is current being published, which may involve other items
       * at the same time.
       */
      PUBLISHING, 
      /**
       * The item is no longer being published and is considered complete.
       */
      COMPLETED
   }
   
   /**
    * Queues one or more content items for publishing using the given edition.
    * If the edition is already running, the items are queued and will be
    * published the next time the edition runs. The queue operation returns
    * an opaque identifier that can be used to query the status of the job. 
    * The status is retained for a long period of time and is then discarded.
    * 
    * @param editionid the edition id, must exist
    * @param work the work to publish, never <code>null</code>.
    * @return an opaque id that can be used to check the status of the
    *   request.
    */
   long queue(int editionid, PSDemandWork work);
   
   /**
    * Get the status of a job queued with {@link #queue(int, PSDemandWork)}. If
    * the job is unknown then <code>null</code> is returned.
    * 
    * @param requestId the request id returned from {@link 
    *   #queue(int, PSDemandWork)}.
    * @return the status, or <code>null</code> if the status is unknown.
    */
   Status getStatus(long requestId);
   
   /**
    * Get the current work to be done by the specified edition.
    * 
    * @param editionid the edition id
    * @return the work to be done, or <code>null</code> if the edition is 
    * unknown or empty if there is no work to be done.
    */
   List<PSDemandWork> getEditionCurrentWork(int editionid);
}
