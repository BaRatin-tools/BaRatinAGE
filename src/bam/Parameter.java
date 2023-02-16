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

    public void log() {
        System.out.print(String.format(" > Parameter '%s': %f - ", this.name, this.initalGuess));
        this.distribution.log();
    }
}
