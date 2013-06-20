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

import java.lang.reflect.*;

public class JiMinifierMojo_testRunner {

    public static void main(String[] args) {
        try {
            JiMinifierMojo jiMinifierMojo = new JiMinifierMojo();
            Field field = jiMinifierMojo.getClass().getDeclaredField("minifierToUse");
            String value = "SimpleConcatenation";
            setPrivateFieldValue(jiMinifierMojo, field, value);
            setPrivateFieldValue(jiMinifierMojo, jiMinifierMojo.getClass().getDeclaredField("minifyConfigFiles"), new String[] {"views/paymentoptions/paymentOptions.minconf.json"});
            setPrivateFieldValue(jiMinifierMojo, jiMinifierMojo.getClass().getDeclaredField("webappSourceDir"), "L:\\dev\\cm\\cm-server\\src\\main\\webapp");
            setPrivateFieldValue(jiMinifierMojo, jiMinifierMojo.getClass().getDeclaredField("unpackedDependencyJsAndCssDir"), "L:\\dev\\cm\\cm-server\\target/unpacked-dependency-jsandcss");
            setPrivateFieldValue(jiMinifierMojo, jiMinifierMojo.getClass().getDeclaredField("webappTargetDir"), "L:\\dev\\cm\\cm-server\\target/cm-server-1.0.0-SNAPSHOT");
            setPrivateFieldValue(jiMinifierMojo, jiMinifierMojo.getClass().getDeclaredField("projectVersion"), "42.0-SNAPSHOT");
            jiMinifierMojo.execute();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void setPrivateFieldValue(JiMinifierMojo jiMinifierMojo, Field field, Object value) throws IllegalAccessException {
        field.setAccessible(true);
        field.set(jiMinifierMojo, value);
    }

}
