/*

Copyright (c) 2013, Jirvan Pty Ltd
All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice,
      this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright notice,
      this list of conditions and the following disclaimer in the documentation
      and/or other materials provided with the distribution.
    * Neither the name of Jirvan Pty Ltd nor the names of its contributors
      may be used to endorse or promote products derived from this software
      without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

*/

package com.jirvan.jiminify;

import com.jirvan.util.*;
import org.apache.maven.plugin.*;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.*;
import org.codehaus.plexus.util.*;

import java.io.*;
import java.util.*;

import static com.jirvan.util.Assertions.assertIsDirectory;

@Mojo(name = "minify")
public class JiMinifierMojo extends AbstractMojo {

    @Parameter(property = "minify.minifierToUse", defaultValue = "SimpleConcatenation")
    private String minifierToUse;

    @Parameter(property = "minify.minifyConfigFiles")
    private String[] minifyConfigFiles;

    @Parameter(property = "minify.overlaysDir", defaultValue = "${basedir}/src/main/webapp")
    private String overlaysDir;

    @Parameter(property = "minify.webappSourceDir", defaultValue = "${basedir}/src/main/webapp")
    private String webappSourceDir;

    @Parameter(property = "minify.unpackedDependencyJsAndCssDir", defaultValue = "${project.build.directory}/unpacked-dependency-jsandcss")
    private String unpackedDependencyJsAndCssDir;

    @Parameter(property = "minify.webappTargetDir", defaultValue = "${project.build.directory}/${project.build.finalName}")
    private String webappTargetDir;

    @Parameter(property = "minify.projectVersion", defaultValue = "${project.version}")
    private String projectVersion;

    public void execute() throws MojoExecutionException {
        if (minifyConfigFiles == null) throw new RuntimeException("Expected minify.minifyConfigFiles to be set");
        if (minifyConfigFiles.length == 0) throw new RuntimeException("Expected minify.minifyConfigFiles to contain at least one param");
        for (String minifyConfigFile : minifyConfigFiles) {
            executeForConfigFile(minifyConfigFile);
        }
    }

    private void executeForConfigFile(String minifyConfigFile) {
        getLog().info(String.format("Processing %s", minifyConfigFile));

        MinifyConfig minifyConfig = MinifyConfig.fromJsonFile(new File(webappSourceDir, minifyConfigFile));

        if ("SimpleConcatenation".equals(minifierToUse)) {
            if (minifyConfig.cssFiles != null) {
                createMinifiedFile(minifyConfig.cssFiles, false);
            }
            if (minifyConfig.ieOnlyCssFiles != null) {
                createMinifiedFile(minifyConfig.ieOnlyCssFiles, false);
            }
            if (minifyConfig.jsFiles != null) {
                createMinifiedFile(minifyConfig.jsFiles, true);
            }
        } else {
            throw new RuntimeException(String.format("Don't recognize minifierToUse \"%s\" (SimpleConcatenation is the only minifier currently supported", minifierToUse));
        }

    }

    private void createMinifiedFile(MinifyConfig.FileSet fileSet, boolean isJsFile) {

        // Scan to get final pathlist
        List<String> orderedPaths = new ArrayList<String>();
        for (String path : fileSet.sourceFiles) {
            DirectoryScanner ds = new DirectoryScanner();
            ds.setIncludes(new String[]{path});
            ds.setBasedir(new File(webappSourceDir));
            ds.setCaseSensitive(true);
            ds.scan();
            String[] foundPaths = ds.getIncludedFiles();
            if (foundPaths.length == 0) {
                ds.setBasedir(new File(unpackedDependencyJsAndCssDir));
                ds.scan();
                foundPaths = ds.getIncludedFiles();
            }
            Arrays.sort(foundPaths, new Comparator<String>() {
                @Override public int compare(String o1, String o2) {
                    return o1.replaceFirst(".*[\\\\/]", "").compareTo(o2.replaceFirst(".*[\\\\/]", ""));
                }
            });
            for (String foundPath : foundPaths) {
                orderedPaths.add(foundPath);
            }
        }

        try {
            File outFile = new File(webappTargetDir, fileSet.minFile);
            if (!outFile.getParentFile().exists()) {
                outFile.getParentFile().mkdirs();
            }
            assertIsDirectory(outFile.getParentFile());
            FileWriter fileWriter = new FileWriter(outFile);
            try {
                for (int i = 0; i < orderedPaths.size(); i++) {
                    if (i > 0) fileWriter.write("\n\n");
                    fileWriter.write(isJsFile ? "//========== " : "/*========== ");
                    fileWriter.write(orderedPaths.get(i).replaceAll("\\\\", "/"));
                    fileWriter.write(isJsFile ? " ==========//\n" : " ==========*/\n");
                    File file = new File(webappSourceDir, orderedPaths.get(i));
                    if (!file.exists()) {
                        file = new File(unpackedDependencyJsAndCssDir, orderedPaths.get(i));
                    }
                    if (!file.exists()) {
                        throw new RuntimeException(String.format("Neither %s or %s exist", new File(webappSourceDir, orderedPaths.get(i)).getAbsolutePath(), file.getAbsolutePath()));
                    }
                    InputStream inputStream = new FileInputStream(file);
                    try {
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                        try {
                            int character;
                            while ((character = bufferedReader.read()) != -1) {
                                fileWriter.write(character);
                            }
                        } finally {
                            bufferedReader.close();
                        }
                    } finally {
                        inputStream.close();
                    }
                }
            } finally {
                fileWriter.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public static class MinifyConfig {

        public FileSet cssFiles;
        public FileSet ieOnlyCssFiles;
        public FileSet jsFiles;

        public static MinifyConfig fromJsonFile(File configFile) {
            MinifyConfig minifyConfig = Json.fromJsonFile(configFile, MinifyConfig.class);
            verifyFileSet(configFile, minifyConfig.cssFiles, "cssFiles");
            verifyFileSet(configFile, minifyConfig.ieOnlyCssFiles, "ieOnlyCssFiles");
            verifyFileSet(configFile, minifyConfig.jsFiles, "jsFiles");
            return minifyConfig;
        }

        private static void verifyFileSet(File configFile, FileSet fileSet, String setName) {
            if (fileSet != null) {
                if (fileSet.minFile == null) {
                    throw new RuntimeException(String.format("%s is invalid, %s.minFile must be specified (if %s is)", configFile.getAbsolutePath(), setName, setName));
                }
                if (fileSet.sourceFiles == null) {
                    throw new RuntimeException(String.format("%s is invalid, %s.sourceFiles must be specified (if %s is)", configFile.getAbsolutePath(), setName, setName));
                }
                if (fileSet.sourceFiles.length == 0) {
                    throw new RuntimeException(String.format("%s is invalid, %s.sourceFiles must have at least one item", configFile.getAbsolutePath(), setName));
                }
            }
        }

        public static class FileSet {

            public String minFile;
            public String[] sourceFiles;

        }

    }

}