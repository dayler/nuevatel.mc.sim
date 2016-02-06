package com.nuevatel.mcsim;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class AppTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp()
    {
        String metadata = "[59165380073, 112]";
        metadata = metadata.substring(1, metadata.length()).substring(0, metadata.length() - 2);
        System.out.println(metadata);
        String[] arrMetadata = metadata.split(",");
        System.out.println(arrMetadata.length);
        byte type = (byte) Integer.parseInt("220");
        System.out.println(type & 0xff);
        //
        String tmp = "name=59165380073".substring("name=".length(), "name=59165380073".length());
        System.out.println(tmp);
        assertTrue( true );
    }
}
