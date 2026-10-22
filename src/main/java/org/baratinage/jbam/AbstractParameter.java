package org.baratinage.jbam;

import org.baratinage.jbam.utils.ConfigFile;

public sealed abstract class AbstractParameter permits Parameter {
  public final String name;
  public final double initialGuess;

  public AbstractParameter(String name, double initialGuess) {
    this.name = name;
    this.initialGuess = initialGuess;
  }

  public abstract AbstractParameter getRenamedClone(String name);

  public abstract void toFile(ConfigFile modelConfigFile, String workspace);

  @Override
  public String toString() {
    return " > Parameter '%s' (initial guess: %f)".formatted(this.name, this.initialGuess);
  }

}
