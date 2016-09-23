/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.integration.common.extensions.usermgt;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.carbon.automation.engine.configurations.AutomationConfiguration;
import org.wso2.carbon.automation.engine.context.InstanceType;
import org.wso2.carbon.automation.engine.exceptions.AutomationFrameworkException;
import org.wso2.carbon.automation.engine.extensions.ExecutionListenerExtension;
import org.wso2.carbon.integration.common.extensions.exceptions.AutomationExtensionException;
import org.wso2.carbon.integration.common.extensions.utils.AutomationXpathConstants;
import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPathExpressionException;

/**
 * Test automation extension class to populate users before test suite execution
 * This class will remove all populated users at the end of test suites.
 * And users/tenants to be populated is taken from automation.xml.
 */
public class UserPopulateExtension extends ExecutionListenerExtension {
    private static final Log log = LogFactory.getLog(UserPopulateExtension.class);


    private List<Node> productGroupsList;
    private List<UserPopulator> userPopulatorList = new ArrayList<UserPopulator>();

    public void initiate() throws AutomationFrameworkException {
        try {
            productGroupsList = getAllProductNodes();
        } catch (XPathExpressionException e) {
            throw new AutomationFrameworkException("Error while retrieving product groups ", e);
        }
    }

    /**
     * Populate all tenants, users, roles and permissions on execution start of the test
     *
     * @throws AutomationFrameworkException - throws if user population fails
     */
    public void onExecutionStart() throws AutomationFrameworkException {
        for (Node aProductGroupsList : productGroupsList) {
            String productGroupName = aProductGroupsList.getAttributes().
                    getNamedItem(AutomationXpathConstants.NAME).getNodeValue();
            String instanceName = getProductGroupInstance(aProductGroupsList);
            try {
                UserPopulator userPopulator = new UserPopulator(productGroupName, instanceName);
                userPopulator.populateUsers();
                userPopulatorList.add(userPopulator);
            } catch (AutomationExtensionException e) {
                throw new AutomationFrameworkException("Error while populating users ", e);
            } catch (XPathExpressionException e) {
                throw new AutomationFrameworkException("Error while populating users ", e);
            }
        }
    }

    /**
     * Remove the populated tenants, users, roles and permissions on execution finish of the test
     *
     * @throws AutomationFrameworkException - throws if users cannot be deleted
     */
    public void onExecutionFinish() throws AutomationFrameworkException {
        for (UserPopulator userPopulator : userPopulatorList) {
            try {
                userPopulator.deleteUsers();
            } catch (AutomationExtensionException e) {
                throw new AutomationFrameworkException("Error while deleting users ", e);
            }
        }
    }

    //get the instance which can call admin services for provided product group
    private String getProductGroupInstance(Node productGroup) {
        String instanceName = "";
        Boolean isClusteringEnabled = Boolean.parseBoolean(productGroup.getAttributes().
                getNamedItem(AutomationXpathConstants.CLUSTERING_ENABLED).getNodeValue());
        if (!isClusteringEnabled) {
            instanceName = getInstanceList(productGroup, InstanceType.standalone.name()).get(0);
        } else {
            if (getInstanceList(productGroup, InstanceType.lb_worker_manager.name()).size() > 0) {
                instanceName = getInstanceList(productGroup, InstanceType.lb_worker_manager.name()).get(0);
            } else if (getInstanceList(productGroup, InstanceType.lb_manager.name()).size() > 0) {
                instanceName = getInstanceList(productGroup, InstanceType.lb_manager.name()).get(0);
            } else if (getInstanceList(productGroup, InstanceType.manager.name()).size() > 0) {
                instanceName = getInstanceList(productGroup, InstanceType.manager.name()).get(0);
            }
        }
        return instanceName;
    }

    // get all specific typed instances in provided productGroup
    private List<String> getInstanceList(Node productGroup, String type) {
        List<String> instanceList = new ArrayList<String>();
        int numberOfInstances = productGroup.getChildNodes().getLength();
        for (int i = 0; i < numberOfInstances; i++) {
            NamedNodeMap attributes = productGroup.getChildNodes().item(i).getAttributes();
            String instanceName = attributes.getNamedItem(AutomationXpathConstants.NAME).getNodeValue();
            String instanceType = attributes.getNamedItem(AutomationXpathConstants.TYPE).getNodeValue();
            if (instanceType.equals(type)) {
                instanceList.add(instanceName);
            }
        }
        return instanceList;
    }

    private List<Node> getAllProductNodes() throws XPathExpressionException {
        List<Node> nodeList = new ArrayList<Node>();
        NodeList productGroups = AutomationConfiguration.getConfigurationNodeList(AutomationXpathConstants.PRODUCT_GROUP);
        for (int i = 0; i < productGroups.getLength(); i++) {
            nodeList.add(productGroups.item(i));
        }
        return nodeList;
    }
}
