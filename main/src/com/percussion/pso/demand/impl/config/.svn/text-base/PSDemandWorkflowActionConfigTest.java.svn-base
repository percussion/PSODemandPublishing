package com.percussion.pso.demand.impl.config;

import java.io.FileReader;

import com.thoughtworks.xstream.XStream;

import junit.framework.TestCase;

public class PSDemandWorkflowActionConfigTest extends TestCase {

    PSDemandWorkflowActionConfig config;
    XStream xs;
    protected void setUp() throws Exception {
        super.setUp();
        xs = new XStream();
        xs.alias("config", PSDemandWorkflowActionConfig.class);
        xs.alias("rule", PSDemandWorkflowActionRule.class);
        xs.addImplicitCollection(PSDemandWorkflowActionConfig.class, "rules");
        
        Object input = xs.fromXML(new FileReader("test/demandpublish.xml"));
        config = (PSDemandWorkflowActionConfig) input;
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testValidate() {
        assertTrue(config.validate());
    }
    public void testGetEdition() {
        System.out.println(xs.toXML(config));
        assertEquals(310, config.getEdition(3, 0, 0, 0));
        assertEquals(310, config.getEdition(3,100,100, 0));
        assertEquals(320, config.getEdition(100, 5, 100, 0));
        assertEquals(330, config.getEdition(3, 5, 20, 0));
        assertEquals(340, config.getEdition(3, 5, 40, 0));
        assertEquals(350, config.getEdition(3, 5, 40, 300));
        
    }
}
