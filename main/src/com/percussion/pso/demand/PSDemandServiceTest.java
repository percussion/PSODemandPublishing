/******************************************************************************
 *
 * [ PSDemandServiceTest.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.pso.demand;

import com.percussion.design.objectstore.PSLocator;
import com.percussion.pso.demand.IPSDemandService.Status;
import com.percussion.pso.demand.impl.PSDemandServiceImpl;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.utils.guid.IPSGuid;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;

import junit.framework.TestCase;

/**
 * This test as currently written is run against the prefunctional version to
 * check to see if all the threading is working correctly
 */
public class PSDemandServiceTest extends TestCase
{
   private static Logger ms_logger = Logger
         .getLogger(PSDemandServiceTest.class);

   private static PSDemandServiceImpl svc = new PSDemandServiceImpl();

   private static IPSGuidManager gmgr = PSGuidManagerLocator.getGuidMgr();

   private static Random rand = new Random();

   private static List<Long> m_requests = new ArrayList<Long>();

   class QueueItUp implements Runnable
   {
      int mi_edition;

      public QueueItUp(int eid) {
         mi_edition = eid;
      }

      public void run()
      {
         int count = Math.abs(rand.nextInt(10) + 1);
         PSDemandWork work = new PSDemandWork();
         for (int i = 0; i < count; i++)
         {
            int contentid = Math.abs(rand.nextInt(500) + 300);
            int folderid = Math.abs(rand.nextInt(100) + 800);
            IPSGuid fid = gmgr.makeGuid(new PSLocator(folderid));
            IPSGuid cid = gmgr.makeGuid(new PSLocator(contentid, Math.abs(rand
                  .nextInt(5))));
            work.addItem(fid, cid);
         }
         long requestid = svc.queue(mi_edition, work);
         synchronized (m_requests)
         {
            m_requests.add(requestid);
         }
      }
   }

   public void testSetup() throws InterruptedException
   {
      QueueItUp q1 = new QueueItUp(100);
      QueueItUp q2 = new QueueItUp(101);
      QueueItUp q3 = new QueueItUp(100);
      QueueItUp q4 = new QueueItUp(101);
      QueueItUp q5 = new QueueItUp(102);
      QueueItUp q6 = new QueueItUp(101);
      QueueItUp q7 = new QueueItUp(102);
      QueueItUp q8 = new QueueItUp(101);

      (new Thread(q1)).start();
      (new Thread(q2)).start();
      (new Thread(q3)).start();
      (new Thread(q4)).start();
      (new Thread(q5)).start();
      (new Thread(q6)).start();
      (new Thread(q7)).start();
      (new Thread(q8)).start();
      Thread.sleep(100); // Wait for everything to be queued
   }

   public void testRunStatusTillDone() throws InterruptedException
   {
      int completed, publishing, queued;

      do
      {
         completed = 0;
         publishing = 0;
         queued = 0;
         for (Long rid : m_requests)
         {
            Status s = svc.getStatus(rid);
            switch (s.ordinal())
            {
               case 0 :
                  // Ignore
                  break;
               case 1 :
                  queued++;
                  break;
               case 2 :
                  publishing++;
                  break;
               default :
                  completed++;
            }
         }
         ms_logger.info("queued/publishing/completed: " + queued + "/"
               + publishing + "/" + completed);
         Thread.sleep(500);
      }
      while (publishing > 0 || queued > 0);
   }

   public void testReaper() throws InterruptedException
   {
      // If we wait for 45 seconds or so they should all be gone
      Thread.sleep(45000);
      for (Long rid : m_requests)
      {
         Status s = svc.getStatus(rid);
         assertNull(s);
      }
   }
}
