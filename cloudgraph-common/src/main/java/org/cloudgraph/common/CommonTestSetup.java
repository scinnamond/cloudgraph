package org.cloudgraph.common;

import junit.extensions.TestSetup;
import junit.framework.TestSuite;

/**
 * Common unit test setup
 */
public class CommonTestSetup extends TestSetup
{
   public static CommonTestSetup newTestSetup(Class testClass)
   {
      return new CommonTestSetup(testClass);
   }
   
   protected CommonTestSetup(Class testClass)
   {
      super(new TestSuite(testClass));
   }   
}
