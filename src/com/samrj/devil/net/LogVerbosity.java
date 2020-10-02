package com.samrj.devil.net;

import java.io.PrintStream;
import java.util.function.Supplier;

/**
 * Determines how information much a Client or Server logs.
 * 
 * @author Samuel Johnson (SmashMaster)
 * @copyright 2020 Samuel Johnson
 * @license https://github.com/SmashMaster/DevilUtil/blob/master/LICENSE
 */
public enum LogVerbosity
{
    /**
     * Performs no logging.
     */
    OFF(false, false, false),
    
    /**
     * Logs only successful connections and disconnections.
     */
    LOW(true, false, false),
    
    /**
     * Logs all handshake activity and packet exception summaries.
     */
    MEDIUM(true, true, false),
    
    /**
     * Logs all activity and packet exception stack-traces.
     */
    HIGH(true, true, true);
    
    private final boolean logLow, logMedium, logHigh;
    
    private LogVerbosity(boolean logLow, boolean logMedium, boolean logHigh)
    {
        this.logLow = logLow;
        this.logMedium = logMedium;
        this.logHigh = logHigh;
    }
    
    void low(PrintStream log, Supplier<String> strProvider)
    {
        if (logLow) log.println(strProvider.get());
    }
    
    void medium(PrintStream log, Supplier<String> strProvider)
    {
        if (logMedium) log.println(strProvider.get());
    }
    
    void high(PrintStream log, Supplier<String> strProvider)
    {
        if (logHigh) log.println(strProvider.get());
    }
}
