/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright © 2011 ForgeRock AS. All rights reserved.
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at
 * http://forgerock.org/license/CDDLv1.0.html
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at http://forgerock.org/license/CDDLv1.0.html
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * $Id$
 */

package org.forgerock.openidm.provisioner.openicf.impl;

import org.forgerock.json.fluent.JsonPointer;
import org.forgerock.json.fluent.JsonValue;
import org.forgerock.openidm.core.ServerConstants;
import org.forgerock.openidm.provisioner.ProvisionerService;
import org.forgerock.openidm.provisioner.openicf.ConnectorInfoProvider;
import org.forgerock.openidm.provisioner.openicf.ConnectorReference;
import org.forgerock.openidm.provisioner.openicf.connector.TestConfiguration;
import org.forgerock.openidm.provisioner.openicf.connector.TestConnector;
import org.forgerock.openidm.provisioner.openicf.internal.SystemAction;
import org.identityconnectors.common.event.ConnectorEventHandler;
import org.identityconnectors.framework.api.APIConfiguration;
import org.identityconnectors.framework.api.ConnectorInfo;
import org.identityconnectors.framework.common.FrameworkUtil;
import org.identityconnectors.framework.impl.api.APIConfigurationImpl;
import org.identityconnectors.framework.impl.api.ConfigurationPropertiesImpl;
import org.identityconnectors.framework.impl.api.local.JavaClassProperties;
import org.identityconnectors.framework.impl.api.local.LocalConnectorInfoImpl;
import org.identityconnectors.framework.impl.test.TestHelpersImpl;
import org.identityconnectors.framework.spi.PoolableConnector;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author $author$
 * @version $Revision$ $Date$
 */
public class OpenICFProvisionerServiceTestConnectorTest extends OpenICFProvisionerServiceTestBase {

    @Test
    public void testHelloWorldAction() throws Exception {
        Map<String, Object> action = getEmptyScript();
        action.put(SystemAction.SCRIPT_ID, "ConnectorScript#1");
        JsonValue result = getService().handle(buildRequest("action", "system/Test/account", null, action, null));
        assertThat(result.get(new JsonPointer("actions/0/result")).getObject()).isEqualTo("Arthur Dent");

        action.put(SystemAction.SCRIPT_ID, "ConnectorScript#2");
        action.put("testArgument", "Zaphod Beeblebrox");
        result = getService().handle(buildRequest("action", "system/Test/account", null, action, null));
        assertThat(result.get(new JsonPointer("actions/0/result")).getObject()).isEqualTo("Zaphod Beeblebrox");

        action.put(SystemAction.SCRIPT_ID, "ConnectorScript#3");
        action.put("testArgument", Arrays.asList("Ford Prefect", "Tricia McMillan"));
        result = getService().handle(buildRequest("action", "system/Test/account", null, action, null));
        assertThat(result.get(new JsonPointer("actions/0/result")).getObject()).isEqualTo(2);

        action.put(SystemAction.SCRIPT_ID, "ConnectorScript#4");
        result = getService().handle(buildRequest("action", "system/Test/account", null, action, null));
        assertThat(result.get(new JsonPointer("actions/0/error")).getObject()).isEqualTo("Marvin");
    }


    protected Map<String, Object> getEmptyScript() {
        Map<String, Object> result = new HashMap<String, Object>(4);
        result.put(ServerConstants.ACTION_NAME, "script");
        return result;
    }


    @Override
    protected String updateRuntimeConfiguration(String config) throws Exception {
        return config;
    }

    @Override
    protected ProvisionerService createInitialService() {
        return new OpenICFProvisionerService();
    }

    @Override
    protected ConnectorInfoProvider getConnectorInfoProvider() {
        return new LocalConnectorInfoProviderStub();
    }

    private class LocalConnectorInfoProviderStub implements ConnectorInfoProvider {

        /**
         * {@inheritDoc}
         */
        public ConnectorInfo findConnectorInfo(ConnectorReference connectorReference) {
            LocalConnectorInfoImpl info = new LocalConnectorInfoImpl();
            info.setConnectorConfigurationClass(TestConfiguration.class);
            info.setConnectorClass(TestConnector.class);
            info.setConnectorDisplayNameKey("DUMMY_DISPLAY_NAME");
            info.setConnectorKey(connectorReference.getConnectorKey());
            info.setMessages(new TestHelpersImpl().createDummyMessages());

            APIConfigurationImpl rv = new APIConfigurationImpl();
            rv.setConnectorPoolingSupported(
                    PoolableConnector.class.isAssignableFrom(TestConnector.class));
            ConfigurationPropertiesImpl properties =
                    JavaClassProperties.createConfigurationProperties(new TestConfiguration());
            rv.setConfigurationProperties(properties);
            rv.setConnectorInfo(info);
            rv.getResultsHandlerConfiguration().setEnableAttributesToGetSearchResultsHandler(false);
            rv.getResultsHandlerConfiguration().setEnableFilteredResultsHandler(false);
            rv.setSupportedOperations(
                    FrameworkUtil.getDefaultSupportedOperations(TestConnector.class));
            info.setDefaultAPIConfiguration(
                    rv);
            return info;
        }

        /**
         * Get all available {@link org.identityconnectors.framework.api.ConnectorInfo} from the local and the remote
         * {@link org.identityconnectors.framework.api.ConnectorInfoManager}s
         *
         * @return list of all available {@link org.identityconnectors.framework.api.ConnectorInfo}s
         */
        @Override
        public List<ConnectorInfo> getAllConnectorInfo() {
            return null;
        }

        /**
         * Tests the {@link org.identityconnectors.framework.api.APIConfiguration Configuration} with the connector.
         *
         * @param configuration
         * @throws RuntimeException if the configuration is not valid or the test failed.
         */
        @Override
        public void testConnector(APIConfiguration configuration) {
        }

        public void addConnectorEventHandler(ConnectorReference connectorReference, ConnectorEventHandler handler){};

        public void deleteConnectorEventHandler(ConnectorEventHandler handler){};

        /**
         * Create a new configuration object from the {@code configuration} parameter.
         * <p/>
         *
         * @param configuration
         * @param validate
         * @return
         */
        @Override
        public JsonValue createSystemConfiguration(APIConfiguration configuration, boolean validate) {
            return new JsonValue(new HashMap<String, Object>());
        }
    }

}
