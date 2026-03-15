package uk.ac.shef.wit.simmetrics.similaritymetrics;

public abstract class AbstractStringMetric {
    public abstract float getSimilarity(String first, String second);
    public abstract String getLongDescriptionString();
    public abstract String getShortDescriptionString();
}
