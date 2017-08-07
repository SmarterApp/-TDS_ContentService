/***************************************************************************************************
 * Copyright 2017 Regents of the University of California. Licensed under the Educational
 * Community License, Version 2.0 (the “license”); you may not use this file except in
 * compliance with the License. You may obtain a copy of the license at
 *
 * https://opensource.org/licenses/ECL-2.0
 *
 * Unless required under applicable law or agreed to in writing, software distributed under the
 * License is distributed in an “AS IS” BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for specific language governing permissions
 * and limitations under the license.
 **************************************************************************************************/

package tds.content.repositories.impl;

import com.google.common.io.Files;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import tds.content.repositories.ItemDataRepository;

import static org.apache.commons.io.Charsets.UTF_8;

@Repository
@Profile("fileSystemContent")
public class FileSystemItemDataRepository implements ItemDataRepository {
    @Override
    public String findOne(final String itemPath) throws IOException {
        return Files.toString(new File(itemPath), UTF_8);
    }

    @Override
    public InputStream findResource(final String resourcePath) throws IOException {
        File resourceFile = new File(resourcePath);
        return Files.asByteSource(resourceFile).openStream();
    }

    @Override
    public boolean doesItemExists(final String itemPath) throws IOException {
        File itemFile = new File(itemPath);
        return itemFile.isFile();
    }
}
