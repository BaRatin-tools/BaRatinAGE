package org.baratinage.jbam;

public class Parameter {
    public final String name;
    public final double initalGuess;
    public final Distribution distribution;

    public Parameter(String name, double initalGuess, Distribution distribution) {
        this.name = name;
        this.initalGuess = initalGuess;
        this.distribution = distribution;
    }

    public Parameter getRenamedClone(String name) {
        return new Parameter(name, initalGuess, distribution);
    }

    @Override
    public String toString() {
        return String.format(" > Parameter '%s': %f - %s",
                this.name, this.initalGuess, this.distribution.toString());
    }
}
