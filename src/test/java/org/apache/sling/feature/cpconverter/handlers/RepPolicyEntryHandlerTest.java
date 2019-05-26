/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership. The ASF
 * licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.apache.sling.feature.cpconverter.handlers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;

import org.apache.jackrabbit.vault.fs.io.Archive;
import org.apache.jackrabbit.vault.fs.io.Archive.Entry;
import org.apache.sling.feature.Extension;
import org.apache.sling.feature.ExtensionType;
import org.apache.sling.feature.Feature;
import org.apache.sling.feature.cpconverter.acl.AclManager;
import org.apache.sling.feature.cpconverter.features.FeaturesManager;
import org.apache.sling.feature.cpconverter.shared.AbstractContentPackage2FeatureModelConverterTest;
import org.apache.sling.feature.cpconverter.vltpkg.VaultPackageAssembler;
import org.junit.Test;

import com.google.inject.Inject;

public final class RepPolicyEntryHandlerTest extends AbstractContentPackage2FeatureModelConverterTest {

    @Inject
    private RepPolicyEntryHandler handler;

    @Inject
    private FeaturesManager featuresManager;

    @Inject
    private AclManager aclManager;

    @Test
    public void doesNotMatch() {
        assertFalse(handler.matches("/this/is/a/path/not/pointing/to/a/valid/policy.xml"));
    }

    @Test
    public void matches() {
        assertTrue(handler.matches("/home/users/system/asd-share-commons/asd-index-definition-reader/_rep_policy.xml"));
        assertTrue(handler.matches("jcr_root/home/users/system/asd-share-commons/asd-index-definition-reader/_rep_policy.xml"));
    }

    @Test
    public void parseAcl() throws Exception {
        Extension repoinitExtension = parseAndSetRepoinit("acs-commons-ensure-oak-index-service",
                                                          "acs-commons-dispatcher-flush-service",
                                                          "acs-commons-package-replication-status-event-service",
                                                          "acs-commons-ensure-service-user-service",
                                                          "acs-commons-automatic-package-replicator-service",
                                                          "acs-commons-on-deploy-scripts-service");
        assertNotNull(repoinitExtension);
        assertEquals(ExtensionType.TEXT, repoinitExtension.getType());

        String expected = "create path (sling:Folder) /asd\n" + 
                "create path (sling:Folder) /asd/public\n" + 
                "create service user acs-commons-ensure-oak-index-service\n" + 
                "set ACL for acs-commons-ensure-oak-index-service\n" + 
                "allow jcr:read,rep:write,rep:indexDefinitionManagement on /asd/public restriction(*/oak:index/*)\n" + 
                "end\n" + 
                "create service user acs-commons-dispatcher-flush-service\n" + 
                "set ACL for acs-commons-dispatcher-flush-service\n" + 
                "allow jcr:read,crx:replicate,jcr:removeNode on /asd/public\n" + 
                "end\n" + 
                "create service user acs-commons-package-replication-status-event-service\n" + 
                "set ACL for acs-commons-package-replication-status-event-service\n" + 
                "allow jcr:read,rep:write,jcr:readAccessControl,jcr:modifyAccessControl on /asd/public\n" + 
                "end\n" + 
                "create service user acs-commons-ensure-service-user-service\n" + 
                "set ACL for acs-commons-ensure-service-user-service\n" + 
                "allow jcr:read,rep:write,jcr:readAccessControl,jcr:modifyAccessControl on /asd/public\n" + 
                "end\n" + 
                "create service user acs-commons-automatic-package-replicator-service\n" + 
                "set ACL for acs-commons-automatic-package-replicator-service\n" + 
                "allow jcr:read on /asd/public\n" + 
                "end\n" + 
                "create service user acs-commons-on-deploy-scripts-service\n" + 
                "set ACL for acs-commons-on-deploy-scripts-service\n" + 
                "allow jcr:read on /asd/public\n" + 
                "end\n";
        String actual = repoinitExtension.getText();
        assertEquals(expected, actual);
    }

    @Test
    public void notDeclaredSystemUsersWillNotHaveAclSettings() throws Exception {
        Extension repoinitExtension = parseAndSetRepoinit("acs-commons-package-replication-status-event-service",
                                                          "acs-commons-ensure-service-user-service",
                                                          "acs-commons-automatic-package-replicator-service",
                                                          "acs-commons-on-deploy-scripts-service");
        assertNotNull(repoinitExtension);
        assertEquals(ExtensionType.TEXT, repoinitExtension.getType());

        String expected = "create path (sling:Folder) /asd\n" + 
                "create path (sling:Folder) /asd/public\n" + 
                "create service user acs-commons-package-replication-status-event-service\n" + 
                "set ACL for acs-commons-package-replication-status-event-service\n" + 
                "allow jcr:read,rep:write,jcr:readAccessControl,jcr:modifyAccessControl on /asd/public\n" + 
                "end\n" + 
                "create service user acs-commons-ensure-service-user-service\n" + 
                "set ACL for acs-commons-ensure-service-user-service\n" + 
                "allow jcr:read,rep:write,jcr:readAccessControl,jcr:modifyAccessControl on /asd/public\n" + 
                "end\n" + 
                "create service user acs-commons-automatic-package-replicator-service\n" + 
                "set ACL for acs-commons-automatic-package-replicator-service\n" + 
                "allow jcr:read on /asd/public\n" + 
                "end\n" + 
                "create service user acs-commons-on-deploy-scripts-service\n" + 
                "set ACL for acs-commons-on-deploy-scripts-service\n" + 
                "allow jcr:read on /asd/public\n" + 
                "end\n";
        String actual = repoinitExtension.getText();
        assertEquals(expected, actual);
    }

    @Test
    public void parseEmptyAcl() throws Exception {
        Extension repoinitExtension = parseAndSetRepoinit();
        assertNull(repoinitExtension);
    }

    private Extension parseAndSetRepoinit(String...systemUsers) throws Exception {
        String path = "jcr_root/asd/public/_rep_policy.xml";
        Archive archive = mock(Archive.class);
        Entry entry = mock(Entry.class);
        VaultPackageAssembler packageAssembler = mock(VaultPackageAssembler.class);

        when(archive.openInputStream(entry)).thenReturn(getClass().getResourceAsStream(path));

        featuresManager.init("org.apache.sling", "org.apache.sling.cp2fm", "0.0.1");

        handler.handle(path, archive, entry);

        Feature feature = featuresManager.getTargetFeature();

        if (systemUsers != null) {
            for (String systemUser : systemUsers) {
                aclManager.addSystemUser(systemUser);
            }
        }

        when(packageAssembler.getEntry(anyString())).thenReturn(new File("itdoesnotexist"));

        aclManager.addRepoinitExtension(packageAssembler, feature);
        return feature.getExtensions().getByName(Extension.EXTENSION_NAME_REPOINIT);
    }

}
