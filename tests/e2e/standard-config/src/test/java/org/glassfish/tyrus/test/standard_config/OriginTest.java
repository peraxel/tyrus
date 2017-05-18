/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013-2016 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package org.glassfish.tyrus.test.standard_config;

import java.io.IOException;
import java.net.URISyntaxException;

import javax.websocket.ClientEndpointConfig;
import javax.websocket.DeploymentException;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.OnMessage;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import javax.websocket.server.ServerEndpointConfig;

import org.glassfish.tyrus.client.ClientManager;
import org.glassfish.tyrus.server.Server;
import org.glassfish.tyrus.test.tools.TestContainer;

import org.junit.Test;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Pavel Bucek (pavel.bucek at oracle.com)
 */
public class OriginTest extends TestContainer {

    private static final String SENT_MESSAGE = "Always pass on what you have learned.";

    @ServerEndpoint(value = "/echo7", configurator = MyServerConfigurator.class)
    public static class TestEndpointOriginTest1 {

        @OnMessage
        public String onMessage(String message) {
            return message;
        }
    }

    public static class MyServerConfigurator extends ServerEndpointConfig.Configurator {

        @Override
        public boolean checkOrigin(String originHeaderValue) {
            return false;
        }
    }

    @ServerEndpoint(value = "/testEndpointOriginTest2", configurator = AnotherServerConfigurator.class)
    public static class TestEndpointOriginTest2 {

        @OnMessage
        public String onMessage(String message) {
            return message;
        }
    }

    public static class AnotherServerConfigurator extends ServerEndpointConfig.Configurator {

        @Override
        public boolean checkOrigin(String originHeaderValue) {

            if (originHeaderValue != null && !originHeaderValue.startsWith("http://")) {
                return false;
            } else {
                return true;
            }
        }
    }

    @Test
    public void testInvalidOrigin() throws URISyntaxException, IOException, DeploymentException {
        Server server = startServer(TestEndpointOriginTest1.class);

        try {
            final ClientEndpointConfig cec = ClientEndpointConfig.Builder.create().build();

            ClientManager client = createClient();
            client.connectToServer(new Endpoint() {

                @Override
                public void onOpen(final Session session, EndpointConfig EndpointConfig) {
                }
            }, cec, getURI(TestEndpointOriginTest1.class));

            fail("DeploymentException expected.");
        } catch (DeploymentException e) {
            e.printStackTrace();
            assertTrue(e.getCause().getMessage().contains("403"));
        } finally {
            stopServer(server);
        }
    }

    @Test
    public void testOriginStartsWithHttp() throws URISyntaxException, IOException, DeploymentException {
        Server server = startServer(TestEndpointOriginTest2.class);

        try {
            final ClientEndpointConfig cec = ClientEndpointConfig.Builder.create().build();

            ClientManager client = createClient();
            client.connectToServer(new Endpoint() {

                @Override
                public void onOpen(final Session session, EndpointConfig EndpointConfig) {
                }
            }, cec, getURI(TestEndpointOriginTest2.class));

        } catch (DeploymentException e) {
            e.printStackTrace();
            throw e;
        } finally {
            stopServer(server);
        }
    }
}
