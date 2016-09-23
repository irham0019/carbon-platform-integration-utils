/*
*Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/

package org.wso2.carbon.integration.common.utils.clients;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.neethi.Policy;
import org.apache.neethi.PolicyEngine;
import org.apache.rampart.RampartMessageData;
import org.apache.rampart.policy.model.CryptoConfig;
import org.apache.rampart.policy.model.RampartConfig;
import org.apache.ws.security.WSPasswordCallback;
import org.testng.Assert;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.automation.test.utils.axis2client.ConfigurationContextProvider;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.xml.stream.XMLStreamException;
import javax.xml.xpath.XPathExpressionException;

/**
 * Security service client for invoking secured services
 */
public class SecureAxisServiceClient implements CallbackHandler {
    private static final Log log = LogFactory.getLog(SecureAxisServiceClient.class);


    public OMElement sendReceive(String userName, String password, String endpointReference,
                                 String operation, OMElement payload, int securityScenarioNo)
            throws FileNotFoundException, AxisFault, XMLStreamException, XPathExpressionException {

        if (securityScenarioNo == 1) {
            Assert.assertTrue(endpointReference.startsWith("https:"), "Endpoint reference should be https");
        }

        AutomationContext autoContext = new AutomationContext();

        String keyPath =
                FrameworkPathUtil.getSystemResourceLocation() + File.separator +
                autoContext.getConfigurationValue("//keystore/fileName/text()");
        String securityPolicyPath =
                FrameworkPathUtil.getSystemResourceLocation() + File.separator + "security" + File.separator
                + "policies" + File.separator + "scenario" + securityScenarioNo + "-policy.xml";
        ServiceClient sc =
                getServiceClient(userName, password, endpointReference, operation,
                                 securityPolicyPath, "wso2carbon", "wso2carbon", keyPath, "wso2carbon");
        OMElement result;
        if (log.isDebugEnabled()) {
            log.debug("payload :" + payload);
            log.debug("Security Scenario No :" + securityScenarioNo);
            log.debug("Operation :" + operation);
            log.debug("username :" + userName);
            log.debug("password :" + password);
        }
        log.info("Endpoint reference :" + endpointReference);
        try {
            result = buildResponse(sc.sendReceive(payload));
            if (log.isDebugEnabled()) {
                log.debug("Response :" + result);
            }
        } catch (AxisFault axisFault) {
            log.error("AxisFault : " + axisFault.getMessage());
            throw axisFault;
        } finally {
            sc.cleanupTransport();
        }
        Assert.assertNotNull(result);
        return result;
    }

    public OMElement sendReceive(String userName, String password, String endpointReference,
                                 String operation, OMElement payload, String securityPolicyPath,
                                 String userCertAlias, String encryptionUser, String keyStorePath,
                                 String keyStorePassword)
            throws AxisFault, FileNotFoundException, XMLStreamException {
        ServiceClient sc =
                getServiceClient(userName, password, endpointReference, operation,
                                 securityPolicyPath, userCertAlias, encryptionUser, keyStorePath, keyStorePassword);
        OMElement result;
        if (log.isDebugEnabled()) {
            log.debug("payload :" + payload);
            log.debug("Policy Path :" + securityPolicyPath);
            log.debug("Operation :" + operation);
            log.debug("username :" + userName);
            log.debug("password :" + password);
        }
        log.info("Endpoint reference :" + endpointReference);
        try {
            result = buildResponse(sc.sendReceive(payload));
            if (log.isDebugEnabled()) {
                log.debug("Response :" + result);
            }
        } catch (AxisFault axisFault) {
            log.error("AxisFault : " + axisFault.getMessage());
            throw axisFault;
        } finally {
            sc.cleanupTransport();
        }
        Assert.assertNotNull(result);
        return result;
    }

    public void sendRobust(String userName, String password, String endpointReference,
                           String operation, OMElement payload, int securityScenarioNo)
            throws AxisFault, XPathExpressionException, FileNotFoundException, XMLStreamException {

        if (securityScenarioNo == 1) {
            Assert.assertTrue(endpointReference.startsWith("https:"), "Endpoint reference should be https");
        }
        AutomationContext autoContext = new AutomationContext();
        String keyPath = FrameworkPathUtil.getSystemResourceLocation() + File.separator +
                         autoContext.getConfigurationValue("//keystore/fileName/text()");

        String securityPolicyPath =
                FrameworkPathUtil.getSystemResourceLocation() + File.separator + "security" + File.separator
                + "policies" + File.separator + "scenario" + securityScenarioNo + "-policy.xml";

        ServiceClient sc =
                getServiceClient(userName, password, endpointReference, operation,
                                 securityPolicyPath, "wso2carbon", "wso2carbon", keyPath, "wso2carbon");
        try {
            sc.sendRobust(payload);
            log.info("Request Sent");
        } finally {
            sc.cleanupTransport();
        }
    }


    public void sendRobust(String userName, String password, String endpointReference,
                           String operation, OMElement payload, String securityPolicyPath,
                           String userCertAlias, String encryptionUser, String keyStorePath,
                           String keyStorePassword)
            throws FileNotFoundException, AxisFault, XMLStreamException {
        ServiceClient sc = getServiceClient(userName, password, endpointReference, operation,
                                            securityPolicyPath, userCertAlias, encryptionUser, keyStorePath, keyStorePassword);
        if (log.isDebugEnabled()) {
            log.debug("payload :" + payload);
            log.debug("Security Policy Path :" + securityPolicyPath);
            log.debug("Operation :" + operation);
            log.debug("username :" + userName);
            log.debug("password :" + password);
        }
        log.info("Endpoint reference :" + endpointReference);
        try {
            sc.sendRobust(payload);
        } catch (AxisFault axisFault) {
            log.error("AxisFault : " + axisFault.getMessage());
            throw axisFault;
        } finally {
            sc.cleanupTransport();
        }
    }

    private Policy loadPolicy(String userName, String securityPolicyPath, String keyStorePath,
                              String keyStorePassword, String userCertAlias, String encryptionUser)
            throws FileNotFoundException, XMLStreamException {
        Policy policy = null;
        StAXOMBuilder builder = null;
        try {
            builder = new StAXOMBuilder(securityPolicyPath);
            policy = PolicyEngine.getPolicy(builder.getDocumentElement());
            RampartConfig rc = new RampartConfig();
            rc.setUser(userName);
            rc.setUserCertAlias(userCertAlias);
            rc.setEncryptionUser(encryptionUser);
            rc.setPwCbClass(SecureAxisServiceClient.class.getName());
            CryptoConfig sigCryptoConfig = new CryptoConfig();
            sigCryptoConfig.setProvider("org.apache.ws.security.components.crypto.Merlin");
            Properties prop1 = new Properties();
            prop1.put("org.apache.ws.security.crypto.merlin.keystore.type", "JKS");
            prop1.put("org.apache.ws.security.crypto.merlin.file", keyStorePath);
            prop1.put("org.apache.ws.security.crypto.merlin.keystore.password", keyStorePassword);
            sigCryptoConfig.setProp(prop1);
            CryptoConfig encrCryptoConfig = new CryptoConfig();
            encrCryptoConfig.setProvider("org.apache.ws.security.components.crypto.Merlin");
            Properties prop2 = new Properties();
            prop2.put("org.apache.ws.security.crypto.merlin.keystore.type", "JKS");
            prop2.put("org.apache.ws.security.crypto.merlin.file", keyStorePath);
            prop2.put("org.apache.ws.security.crypto.merlin.keystore.password", keyStorePassword);
            encrCryptoConfig.setProp(prop2);
            rc.setSigCryptoConfig(sigCryptoConfig);
            rc.setEncrCryptoConfig(encrCryptoConfig);
            policy.addAssertion(rc);
        } finally {
            if (builder != null) {
                builder.close();
            }
        }
        Assert.assertNotNull(policy, "Policy cannot be null");
        return policy;
    }

    private ServiceClient getServiceClient(String userName, String password,
                                           String endpointReference, String operation,
                                           String securityPolicyPath, String userCertAlias,
                                           String encryptionUser, String keyStorePath,
                                           String keyStorePassword)
            throws AxisFault, FileNotFoundException, XMLStreamException {
        if (log.isDebugEnabled()) {
            log.debug("Key_Path :" + keyStorePath);
            log.debug("securityPolicyPath :" + securityPolicyPath);
        }

        System.setProperty("javax.net.ssl.trustStore", keyStorePath);
        System.setProperty("javax.net.ssl.trustStorePassword", keyStorePassword);

        if (log.isDebugEnabled()) {
            log.debug("javax.net.ssl.trustStore :" + System.getProperty("javax.net.ssl.trustStore"));
            log.debug("javax.net.ssl.trustStorePassword :" + System.getProperty("javax.net.ssl.trustStorePassword"));
        }
        ServiceClient sc = null;
        try {
            sc = new ServiceClient(ConfigurationContextProvider.getInstance().getConfigurationContext(), null);
            sc.engageModule("rampart");
            sc.engageModule("addressing");
            Options opts = new Options();
            opts.setProperty(
                    RampartMessageData.KEY_RAMPART_POLICY,
                    loadPolicy(userName, securityPolicyPath, keyStorePath, keyStorePassword,
                               userCertAlias, encryptionUser));
            opts.setTo(new EndpointReference(endpointReference));
            opts.setAction("urn:" + operation);
            //setting user credential
            opts.setUserName(userName);
            opts.setPassword(password);
            sc.setOptions(opts);
        } catch (AxisFault axisFault) {
            log.error("AxisFault : " + axisFault.getMessage());
            throw new AxisFault("Axis fault ", axisFault);
        } finally {
            if (sc != null) {
                sc.cleanupTransport();
            }
        }
        Assert.assertNotNull("ServiceClient object is null" + sc);
        return sc;
    }

    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
        WSPasswordCallback pwcb = (WSPasswordCallback) callbacks[0];
        String id = pwcb.getIdentifier();
        int usage = pwcb.getUsage();
        if (usage == WSPasswordCallback.SIGNATURE || usage == WSPasswordCallback.DECRYPT) {
            // Logic to get the private key password for signture or decryption
            if ("client".equals(id)) {
                pwcb.setPassword("automation");
            } else if ("service".equals(id)) {
                pwcb.setPassword("automation");
            } else if ("wso2carbon".equals(id)) {
                pwcb.setPassword("wso2carbon");
            } else if ("alice".equals(id)) {
                pwcb.setPassword("password");
            } else if ("bob".equals(id)) {
                pwcb.setPassword("password");
            }
        }
    }

    private static OMElement buildResponse(OMElement omElement) {
        omElement.build();
        return omElement;
    }
}