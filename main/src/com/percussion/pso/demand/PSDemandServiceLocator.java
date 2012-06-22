/******************************************************************************
*
* [ PSDemandServiceLocator.java ]
*
* COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
* All rights reserved. This material contains unpublished, copyrighted
* work including confidential and proprietary information of Percussion.
*
*****************************************************************************/
package com.percussion.pso.demand;

import com.percussion.services.PSBaseServiceLocator;

/**
 * Find the demand publishing service
 */
public class PSDemandServiceLocator extends PSBaseServiceLocator
{
   /**
    * Find and return the demand service bean.
    * @return the demand service, never <code>null</code>. 
    */
   public static IPSDemandService getDemandService()
   {
      return (IPSDemandService) getBean("pso_demandService");
   }
}
