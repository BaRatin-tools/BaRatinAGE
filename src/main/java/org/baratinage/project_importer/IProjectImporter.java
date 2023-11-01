package org.baratinage.project_importer;

import java.util.function.Consumer;

import org.baratinage.ui.bam.BamProject;

public interface IProjectImporter {
    public void importProject(String pathToProjectFile, Consumer<BamProject> bamProjectConsumer);
}
