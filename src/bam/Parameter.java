package bam;

public class Parameter {
    private String name;
    private double initalGuess;
    private Distribution distribution;

    public Parameter(String name, double initalGuess, Distribution distribution) {
        this.name = name;
        this.initalGuess = initalGuess;
        this.distribution = distribution;
    }

    public String getName() {
        return this.name;
    }

    public double getInitalGuess() {
        return this.initalGuess;
    }

    public Distribution getDistribution() {
        return this.distribution;
    }

    @Override
    public String toString() {
        return String.format(" > Parameter '%s': %f - %s",
                this.name, this.initalGuess, this.distribution.toString());
    }
}
