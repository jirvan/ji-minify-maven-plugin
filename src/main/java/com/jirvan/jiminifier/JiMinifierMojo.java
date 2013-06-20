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

package com.jirvan.jiminifier;

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

    @Parameter(property = "minify.minifyConfigFile")
    private String minifyConfigFile;

    @Parameter(property = "minify.basedir", defaultValue = "${basedir}")
    private String basedir;

    @Parameter(property = "minify.webappSourceDir", defaultValue = "${basedir}/src/main/webapp")
    private String webappSourceDir;

    @Parameter(property = "minify.webappTargetDir", defaultValue = "${project.build.directory}/${project.build.finalName}")
    private String webappTargetDir;

//    @Parameter(property = "minify.buildDir", defaultValue = "${project.build.directory}")
//    private String buildDir;

    @Parameter(property = "minify.projectVersion", defaultValue = "${project.version}")
    private String projectVersion;

    public static void main(String[] args) {
        try {
            JiMinifierMojo jiMinifierMojo = new JiMinifierMojo();
            jiMinifierMojo.getClass().getDeclaredField("minifierToUse").set(jiMinifierMojo, "SimpleConcatenation");
            jiMinifierMojo.getClass().getDeclaredField("minifyConfigFile").set(jiMinifierMojo, "views/paymentoptions/paymentOptions.minconf.json");
            jiMinifierMojo.getClass().getDeclaredField("basedir").set(jiMinifierMojo, "L:\\dev\\cm\\cm-server");
            jiMinifierMojo.getClass().getDeclaredField("webappSourceDir").set(jiMinifierMojo, "L:\\dev\\cm\\cm-server\\src\\main\\webapp");
            jiMinifierMojo.getClass().getDeclaredField("webappTargetDir").set(jiMinifierMojo, "L:\\dev\\cm\\cm-server\\target/cm-server-1.0.0-SNAPSHOT");
//            jiMinifierMojo.getClass().getDeclaredField("buildDir").set(jiMinifierMojo, "L:\\dev\\cm\\cm-server\\target");
            jiMinifierMojo.getClass().getDeclaredField("projectVersion").set(jiMinifierMojo, "42.0-SNAPSHOT");
            jiMinifierMojo.execute();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void execute() throws MojoExecutionException {

        System.out.printf("\n\nwebappTargetDir \"%s\"\n\n", webappTargetDir);

        if (minifyConfigFile == null) throw new RuntimeException("Expected minify.minifyConfigFile to be set");
        File configFile = new File(webappSourceDir, minifyConfigFile);
        String basePath = minifyConfigFile.replaceFirst("\\.[^\\.]+$", "").replaceFirst("\\.minconf$", "");
        File outCssFile = new File(webappTargetDir, String.format("/%s_%s.css", basePath, projectVersion));
        File outJsFile = new File(webappTargetDir, String.format("/%s_%s.js", basePath, projectVersion));
//        File outCssFile = new File(buildDir, String.format("generated-resources/ji-minify/%s_%s.css", basePath, projectVersion));
//        File outJsFile = new File(buildDir, String.format("generated-resources/ji-minify/%s_%s.js", basePath, projectVersion));

//        assertIsDirectory(new File(buildDir));
//        Io.ensureDirectoryExists(new File(buildDir));

        MinifyConfig minifyConfig = Json.fromJsonFile(configFile, MinifyConfig.class);

        if ("SimpleConcatenation".equals(minifierToUse)) {
            createMinifiedFile(webappSourceDir, minifyConfig.jsPaths, outJsFile);
        } else {
            throw new RuntimeException(String.format("Don't recognize minifierToUse \"%s\" (SimpleConcatenation is the only minifier currently supported", minifierToUse));
        }


        getLog().info(outJsFile.getAbsolutePath());
    }

    private void createMinifiedFile(String webappSourceDir, String[] paths, File outFile) {

//        File webappSourceDirFile = new File(webappSourceDir);

        // Scan to get final pathlist
        DirectoryScanner ds = new DirectoryScanner();
        String[] includes = paths;
        ds.setIncludes(includes);
        ds.setBasedir(new File(webappSourceDir));
        ds.setCaseSensitive(true);
        ds.scan();
        String[] foundPaths = ds.getIncludedFiles();
        Arrays.sort(foundPaths, new Comparator<String>() {
            @Override public int compare(String o1, String o2) {
                return o1.replaceFirst(".*[\\\\/]", "").compareTo(o2.replaceFirst(".*[\\\\/]", ""));
            }
        });

        System.out.printf("\n\n\nzzzzzzzzzzzzzz\n\n\n");
        if (outFile.exists() && false) {
            throw new RuntimeException(String.format("%s already exists", outFile.getAbsolutePath()));
        } else {
            try {
                if (!outFile.getParentFile().exists()) {
                    outFile.getParentFile().mkdirs();
                }
                assertIsDirectory(outFile.getParentFile());
                FileWriter fileWriter = new FileWriter(outFile);
                try {
                    for (String foundPath : foundPaths) {
                        System.out.printf("\n\n//========== ");
                        System.out.printf(foundPath.replaceAll("\\\\", "/"));
                        System.out.printf(" ==========//\n");
                        fileWriter.write("\n\n//========== ");
                        fileWriter.write(foundPath.replaceAll("\\\\", "/"));
                        fileWriter.write(" ==========//\n");
                        InputStream inputStream = new FileInputStream(new File(webappSourceDir, foundPath));
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

    }

    public static class MinifyConfig {

        public String[] cssPaths;
        public String[] ieOnlyCssPaths;
        public String[] jsPaths;

    }

}