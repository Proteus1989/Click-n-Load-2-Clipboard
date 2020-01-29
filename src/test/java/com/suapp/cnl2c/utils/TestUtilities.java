/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.suapp.cnl2c.utils;

import java.lang.reflect.Method;

/**
 *
 * @author Antonio
 */
public class TestUtilities
{
    public static void catchCauseException(Method method, Object... param) throws Throwable
    {
        try
        {
            method.invoke(null, param);
        } catch (Exception e)
        {
            throw e.getCause();
        }
    }
}
