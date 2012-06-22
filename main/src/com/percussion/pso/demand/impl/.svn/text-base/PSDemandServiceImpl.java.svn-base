/******************************************************************************
 *
 * [ PSDemandServiceImpl.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.pso.demand.impl;

import com.percussion.pso.demand.IPSDemandService;
import com.percussion.pso.demand.PSDemandWork;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.log4j.Logger;

/**
 * Implementation of the demand service. This service takes requests and tracks
 * their status. It keeps status for each request until a day has passed, then
 * it removes the status.
 * 
 * @author dougrand
 * 
 */
public class PSDemandServiceImpl implements IPSDemandService, Runnable
{
   /**
    * Log4j logger for the demand service
    */
   private static Logger ms_logger = Logger
         .getLogger(PSDemandServiceImpl.class);

   /**
    * One day's worth of milliseconds
    */
   private static final long DAY_IN_MILLIS = 24 * 3600 * 1000;

   /**
    * Index for time in the object array that holds the request status 
    * information
    */
   private static final int TIME = 0;

   /**
    * Index for status in the object array that holds the request status 
    * information
    */
   private static final int STATUS = 1;

   /**
    * The request counter used to allocate request ids.
    */
   private static AtomicLong ms_requestIdCounter = new AtomicLong(1);

   /**
    * The key to the map is the request id. The value is an object array. The
    * first element of the array is the last time the status was updated or
    * read, the second element is the status value from {@link Status}. Access
    * to this must be threadsafe.
    */
   private Map<Long, Object[]> m_requestStatus = new HashMap<Long, Object[]>();

   /**
    * The key to the map is the edition id that is being processed. The values
    * are the processors themselves.
    */
   private Map<Integer, PSEditionProcessor> m_processors = new HashMap<Integer, PSEditionProcessor>();

   /**
    * This thread monitors the request status every so often and removes the
    * status for anything that's too old. There's
    */
   private Thread m_reaperdaemon;
   
   /**
    * The cms username to use for publishing
    */
   String m_user;
   
   /**
    * The cms password to use for publishing
    */
   String m_password;
   
   /**
    * The protocol to use for publishing, default to http 
    */
   String m_protocol = "http";

   /**
    * Ctor
    */
   public PSDemandServiceImpl() {
      m_reaperdaemon = new Thread(this, "DemandServiceReaper");
      m_reaperdaemon.setDaemon(true);
      m_reaperdaemon.start();
   }

   /**
    * Update the status for the work items
    * 
    * @param items the items, never <code>null</code>
    * @param status the new status, never <code>null</code>
    */
   synchronized void updateStatus(List<PSDemandWork> items, Status status)
   {
      if (items == null)
      {
         throw new IllegalArgumentException("items may not be null");
      }
      if (status == null)
      {
         throw new IllegalArgumentException("status may not be null");
      }
      for (PSDemandWork work : items)
      {
         setStatus(work.getRequest(), status);
      }
   }

   public synchronized Status getStatus(long requestId)
   {
      Object value[] = m_requestStatus.get(requestId);
      if (value == null)
      {
         return null;
      }
      value[TIME] = System.currentTimeMillis();
      return (Status) value[STATUS];
   }

   /**
    * Set the status, create an entry if none exists. Must be called from a
    * synchronized method.
    * 
    * @param requestId the request id
    * @param status the new status value
    */
   private void setStatus(long requestId, Status status)
   {
      Object value[] = m_requestStatus.get(requestId);
      if (value == null)
      {
         value = new Object[2];
         m_requestStatus.put(requestId, value);
      }
      value[TIME] = System.currentTimeMillis();
      value[STATUS] = status;
      ms_logger.debug("Set status for request " + requestId + " to " + status);
   }

   public synchronized long queue(int editionid, PSDemandWork work)
   {
      ms_logger.debug("Queueing work " + work + " for editionid " + editionid);
      PSEditionProcessor proc = m_processors.get(editionid);
      if (proc == null)
      {
         ms_logger.debug("Creating an Edition processor for edition id: " + editionid);
         proc = new PSEditionProcessor(this, editionid);
         m_processors.put(editionid, proc);
      }
      long rid = ms_requestIdCounter.getAndIncrement();
      work.setRequest(rid);
      proc.addWork(work); // Will callback with the status
      return rid;
   }

   public synchronized void run()
   {
      while (true)
      {
         try
         {
            int count = 0;
            // Wake every thirty minutes
            wait(1800 * 1000);
            // For testing wake every 15 seconds
            // wait(15000);

            Iterator<Entry<Long, Object[]>> eiter = 
               m_requestStatus.entrySet().iterator();
            while (eiter.hasNext())
            {
               Entry<Long, Object[]> entry = eiter.next();
               Long time = (Long) entry.getValue()[TIME];
               long delta = System.currentTimeMillis() - time.longValue();
               if (delta > DAY_IN_MILLIS /* for testing, change to 30000 */)
               {
                  eiter.remove();
                  count++;
               }
            }
            ms_logger.debug("Request reaper, removed: " + count + " requests");
         }
         catch (InterruptedException e)
         {
            // Ignore
         }
      }

   }

   public List<PSDemandWork> getEditionCurrentWork(int editionid)
   {
      PSEditionProcessor proc = m_processors.get(editionid);
      if (proc == null) return null;
      return proc.getCurrentWork();
   }

   /**
    * @return the password
    */
   public String getPassword()
   {
      return m_password;
   }

   /**
    * @param password the password to set
    */
   public void setPassword(String password)
   {
      m_password = password;
   }

   /**
    * @return the protocol
    */
   public String getProtocol()
   {
      return m_protocol;
   }

   /**
    * @param protocol the protocol to set
    */
   public void setProtocol(String protocol)
   {
      m_protocol = protocol;
   }

   /**
    * @return the user
    */
   public String getUser()
   {
      return m_user;
   }

   /**
    * @param user the user to set
    */
   public void setUser(String user)
   {
      m_user = user;
   }
   
   

}
