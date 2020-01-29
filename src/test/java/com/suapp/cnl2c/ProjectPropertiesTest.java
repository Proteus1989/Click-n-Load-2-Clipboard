/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.suapp.cnl2c;

import java.io.IOException;
import java.util.Properties;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Antonio
 */
public class ProjectPropertiesTest
{

    @Test
    public void testVersion() throws IOException
    {
        Properties properties = new Properties();
        properties.load(this.getClass().getClassLoader().getResourceAsStream("project.properties"));
        assertTrue(Double.parseDouble(properties.getProperty("version")) >= 1);
        assertTrue(Double.parseDouble(properties.getProperty("revision")) >= 0);
        assertTrue(properties.getProperty("name").equals("Click'n Load 2 Clipboard"));
    }


}
