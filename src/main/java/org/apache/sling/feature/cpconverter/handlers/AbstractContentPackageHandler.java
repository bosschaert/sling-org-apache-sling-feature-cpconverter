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

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.jackrabbit.vault.fs.io.Archive;
import org.apache.jackrabbit.vault.fs.io.Archive.Entry;
import org.apache.jackrabbit.vault.packaging.VaultPackage;
import org.apache.sling.feature.cpconverter.ContentPackage2FeatureModelConverter;

public abstract class AbstractContentPackageHandler extends AbstractRegexEntryHandler {

    private final File temporaryDir = new File(System.getProperty("java.io.tmpdir"), "sub-content-packages");

    public AbstractContentPackageHandler() {
        super("/jcr_root/etc/packages/.+\\.zip");
        temporaryDir.mkdirs();
    }

    @Override
    public final void handle(String path, Archive archive, Entry entry, ContentPackage2FeatureModelConverter converter)
            throws Exception {
        logger.info("Processing sub-content package '{}'...", entry.getName());

        File temporaryContentPackage = new File(temporaryDir, entry.getName());

        if (!temporaryContentPackage.exists()) {
            logger.debug("Extracting sub-content package '{}' to {} for future analysis...", entry.getName(), temporaryContentPackage);

            try (InputStream input = archive.openInputStream(entry);
                    OutputStream output = new FileOutputStream(temporaryContentPackage)) {
                IOUtils.copy(input, output);
            }

            logger.debug("Sub-content package '{}' successfully extracted to {} ", entry.getName(), temporaryContentPackage);
        }

        try (VaultPackage vaultPackage = converter.open(temporaryContentPackage)) {
            processSubPackage(path, vaultPackage, converter);
        }

        logger.info("Sub-content package '{}' processing is over", entry.getName());
    }

    protected abstract void processSubPackage(String path, VaultPackage contentPackage, ContentPackage2FeatureModelConverter converter) throws Exception;

}
