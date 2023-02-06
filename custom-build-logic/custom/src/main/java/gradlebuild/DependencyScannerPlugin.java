/*
 * Copyright 2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package gradlebuild;

import org.gradle.api.Project;
import org.gradle.api.artifacts.ArtifactCollection;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.result.ResolvedArtifactResult;
import org.gradle.api.tasks.Exec;
import org.gradle.api.tasks.TaskProvider;

import java.util.Collections;
import java.util.stream.Collectors;

public abstract class DependencyScannerPlugin extends AbstractScannerPlugin {

    @Override
    public void apply(Project project) {
        super.apply(project);

        // Configuration to declare analysis dependencies on.
        Configuration dependencyScanner = project.getConfigurations().create("dependencyScanner");
        dependencyScanner.setCanBeConsumed(false);
        dependencyScanner.setCanBeResolved(false);
        dependencyScanner.setVisible(true);

//        // By default, we analyze the current project. Users can add additional dependencies themselves.
//        dependencyScanner.getDependencies().add(project.getDependencies().create(project));

        // Per-project config:
//        dependencyScanner.getDependencies().add(
//            project.getDependencies().project(Collections.singletonMap("path", ":testing-jvm-infrastructure")));

        // For some reason the platform dependency isn't inherited from the project dependency.
        dependencyScanner.getDependencies().add(project.getDependencies().platform(
            project.getDependencies().project(Collections.singletonMap("path", ":distributions-dependencies"))
        ));

        // We will resolve this classpath to get the classes-to-analyze.
        Configuration dependencyAnalysisClasspath = project.getConfigurations().create("dependencyAnalysisClasspath");
        dependencyAnalysisClasspath.setCanBeResolved(true);
        dependencyAnalysisClasspath.setCanBeConsumed(false);
        dependencyAnalysisClasspath.setVisible(false);
        dependencyAnalysisClasspath.extendsFrom(dependencyScanner);

        configureAsAnalysisClasspath(dependencyAnalysisClasspath);

        TaskProvider<RawJsonWriterTask> rawJson = project.getTasks().register("classAnalysisJson", RawJsonWriterTask.class, task -> {
            ArtifactCollection artifacts = dependencyAnalysisClasspath.getIncoming().getArtifacts();

            task.getAnalyzedClasspath().from(artifacts.getArtifactFiles());
            task.getArtifactIdentifiers().set(
                artifacts.getResolvedArtifacts().map(result ->
                    result.stream().map(ResolvedArtifactResult::getId).collect(Collectors.toList())
                )
            );
        });


//        TaskProvider<D3GraphWriterTask> d3 = project.getTasks().register("renderD3Json", D3GraphWriterTask.class, task -> {
//            task.getAnalyzedClasspath().set(dependencyAnalysisClasspath);
//        });
//
//        TaskProvider<Task> html = project.getTasks().register("generateD3Html", task -> {
//
//            Provider<RegularFile> outputFile = project.getLayout().getBuildDirectory().file("force-graph.html");
//            task.getOutputs().file(outputFile);
//
//            task.dependsOn(d3.get());
//
//            task.doLast(t -> {
//                try(InputStream input = DependencyScannerPlugin.class.getResource("force-graph.html").openStream()) {
//                    try (OutputStream output = Files.newOutputStream(outputFile.get().getAsFile().toPath())) {
//                        input.transferTo(output);
//                    }
//                } catch (IOException e) {
//                    throw new GradleException("Failed to write HTML", e);
//                }
//            });
//        });
//
//        setupDygraph(project, dependencyAnalysisClasspath);
    }

    private void setupDygraph(Project project, Configuration dependencyAnalysisClasspath) {
        TaskProvider<DependencyScannerTask> scanOutputs = project.getTasks().register("scanOutputs", DependencyScannerTask.class, task -> {
            task.getAnalyzedClasspath().set(dependencyAnalysisClasspath);
//            task.getAnalyzedClasspath().set(dependencyAnalysisClasspath.getIncoming().getArtifacts().getResolvedArtifacts());
        });

        TaskProvider<Exec> viz = project.getTasks().register("graphviz", Exec.class, task -> {
            task.args("-Tsvg", "-o",
                project.getLayout().getBuildDirectory().file("graph.svg").get().getAsFile().getAbsolutePath(),
                scanOutputs.get().getOutputFile().get().getAsFile().getAbsolutePath());
            task.executable("/opt/homebrew/bin/dot");
        });
        viz.get().dependsOn(scanOutputs);
    }
}