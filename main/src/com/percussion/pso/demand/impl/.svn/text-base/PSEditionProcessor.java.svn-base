/******************************************************************************
 *
 * [ PSEditionProcessor.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.pso.demand.impl;

import com.percussion.pso.demand.IPSDemandService;
import com.percussion.pso.demand.PSDemandWork;
import com.percussion.publisher.server.PSPublisherHandlerResponse;
import com.percussion.services.publisher.IPSPublisherService;
import com.percussion.services.publisher.PSPublisherServiceLocator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Processes an edition queue. Work is registered by calling the
 * {@link #addWork(PSDemandWork)} method. Te work is done in the {@link #run()}
 * method.
 */
public class PSEditionProcessor implements Runnable
{
   /**
    * Log4j logger.
    */
   private static Logger ms_logger = Logger.getLogger(PSEditionProcessor.class);

   /**
    * This is the queue of work waiting to be done. While the thread is running,
    * multiple pieces of work will be queued up here. If the thread is not
    * running, then the queue will be immediately transferred to the current
    * work being done.
    */
   private List<PSDemandWork> m_queue = new ArrayList<PSDemandWork>();

   /**
    * The work that is currently in place for the publisher. This class invokes
    * the publishing service on the given edition. It is required that the
    * invoked edition pick up the current work via a generator plugin. The
    * plugin will call the demand service, which will delegate to the 
    * {@link #getCurrentWork()} method in this class.
    */
   private List<PSDemandWork> m_current = new ArrayList<PSDemandWork>();
   
   /**
    * Publisher service, used to run the edition.
    */
   private IPSPublisherService m_psvc = 
      PSPublisherServiceLocator.getPublisherService();

   /**
    * The execution thread, never <code>null</code> after construction. The
    * thread only runs when there is work to be done.
    */
   private Thread m_thread;

   /**
    * The id of the edition that this processor runs for.
    */
   private int m_editionid;

   /**
    * The demand service, never <code>null</code> after ctor.
    */
   private PSDemandServiceImpl m_service;

   /**
    * Ctor
    * @param svc a pointer back to the demand service, used to update the status
    * of each request, never <code>null</code>.
    * @param editionid the id of the edition that this processor runs.
    */
   PSEditionProcessor(PSDemandServiceImpl svc, int editionid)
   {
      if (svc == null)
      {
         throw new IllegalArgumentException("svc may not be null");
      }
      m_service = svc;
      m_editionid = editionid;
      m_thread = new Thread(this, "EditionProcessor-" + m_editionid);
      m_thread.setDaemon(true);
      m_thread.start();
   }

   /**
    * Runs the edition. The thread remains alive once the edition processor
    * is created. If there is no work to be done it calls {@link #wait()},
    * and it will be notified once work is queued via 
    * {@link #addWork(PSDemandWork)}.
    */
   public void run()
   {
      ms_logger.debug("EditionProcessor thread started for edition id: " + m_editionid);
      while (true)
      {
         try
         {
            synchronized (m_service)
            {
               while (m_queue.size() == 0)
               {
                  ms_logger.debug("Edition Processor Paused - queue is empty " +
                        "- edition id:" + m_editionid);
                  m_service.wait();
                  ms_logger.debug("Edition Processor Resumed - edition id: " + m_editionid);
               }
               m_current.addAll(m_queue);
               m_service.updateStatus(m_current,
                     IPSDemandService.Status.PUBLISHING);
               m_queue.clear();
            }

            String edid = Integer.toString(m_editionid);
            ms_logger.info("Run edition " + m_editionid + " work: " + m_current);
            m_psvc.executePublish(edid, m_service.getUser(),
                  m_service.getPassword(), null, m_service.getProtocol());
            boolean done = false;
            while(!done)
            {
               PSPublisherHandlerResponse response =
                  m_psvc.executePublishStatus(edid);
               NodeList nodes =
                  response.getDocument().getElementsByTagName("response");
               Element resel = (Element) nodes.item(0);
               String code = resel.getAttribute("code");
               done = code.equals("notInProgress");
               if (!done)
               {
//                  ms_logger.debug("Not done. Status: " + 
//                        PSXmlDocumentBuilder.toString(response.getDocument()));
                  Thread.sleep(250);
               }
            }
         }
         catch(Exception e)
         {
            ms_logger.error("Problem running edition", e);
         }
         finally
         {
            synchronized (m_service)
            {
               m_service.updateStatus(m_current,
                     IPSDemandService.Status.COMPLETED);
               m_current.clear();
            }
         }
      }
   }

   /**
    * Adds work to the queue. If the thread isn't running then this method will
    * kick off the thread. Work items are initially registered back with the 
    * demand service as being in the queued state.
    * 
    * @param work the work to do, never <code>null</code>, must have a non-empty
    * list of work items.
    */
   public void addWork(PSDemandWork work)
   {
       ms_logger.debug("Attempting to add work to the queue. " +
            "Edition Id: " + m_editionid + " Work: " + work);
      if (work == null)
      {
         throw new IllegalArgumentException("work may not be null");
      }
      if (work.getContent() == null || work.getContent().size() == 0)
      {
         throw new IllegalArgumentException("work must have work to do");
      }
      synchronized (m_service)
      {
         m_queue.add(work);
         m_service.updateStatus(Collections.singletonList(work), 
               IPSDemandService.Status.QUEUED);
         ms_logger.debug("Notifying threads that need demand service " +
                "- edition id: " + m_editionid);
         m_service.notifyAll();
      }
      
      ms_logger.debug("Finished adding work to queue. Work: " + work);
   }
   
   /**
    * Get the current work. The current work does not change while the edition
    * is being run. This call is only valid from the content generation code.
    * @return the current work, never <code>null</code>, should never be
    * empty.
    */
   public List<PSDemandWork> getCurrentWork()
   {
      return m_current;
   }

   /* (non-Javadoc)
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString()
   {
      return "<PSEditionProcessor edition: " + m_editionid + ">";
   }
   
   
}
