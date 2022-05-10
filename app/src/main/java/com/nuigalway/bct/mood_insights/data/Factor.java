package com.nuigalway.bct.mood_insights.data;

/**
 * Factor class wraps the factors defined in the strings.xml file
 *
 * @author Karl Gordon
 */
public class Factor {
    // private final field holds the factor string
    private final String factorName;

    /**
     * Constructor method for Factor class
     *
     * @param factorName - String, represents the factor to hold
     */
    public Factor(String factorName) {
        this.factorName = factorName;
    }

    /**
     * Getter method returns the factor
     *
     * @return the factor value
     */
    public String getFactorName() {
        return factorName;
    }
}
