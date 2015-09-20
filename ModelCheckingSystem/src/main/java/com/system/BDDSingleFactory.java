package com.system;

import net.sf.javabdd.BDDFactory;

/**
 *
 * @author Alexander Erin <arcquim@gmail.com>
 */
public class BDDSingleFactory {
    
    private static final String IMPLEMENTATION = "net.sf.javabdd.BuDDyFactory";
    private static BDDFactory factory;
    
    private BDDSingleFactory() {
    }
    
    /**
     *
     * @param numberOfVariables Number of boolean variables in BDD
     * @param cacheSize Size of cache
     * 
     * This method creates BDDFactory object if the one hasn't been created yet; 
     * otherwise it does nothing
     */
    public static void create(int numberOfVariables, int cacheSize) {
        if (factory == null) {
            factory = BDDFactory.init(IMPLEMENTATION, numberOfVariables, cacheSize);
            //factory.setMaxNodeNum((int)(numberOfVariables * 1.5));
        }
    }
    
    /**
     *
     * @param numberOfVariables Number of boolean variables in BDD
     * @param cacheSize Size of cache
     * 
     * This method creates BDDFactory object if the one has been created; 
     * otherwise it does nothing
     */
    public static void recreate(int numberOfVariables, int cacheSize) {
        if (factory != null) {
            factory = BDDFactory.init(IMPLEMENTATION, numberOfVariables, cacheSize);
        }
    }
    
    /**
     *
     * @return created of recreated BDDFactory object; if create or recreate 
     * methods hasn't been called yet, returns null
     */
    public static BDDFactory getInstanse() {
        return factory;
    }
}
