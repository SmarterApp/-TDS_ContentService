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

package tds.content.mappers;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import tds.itemrenderer.data.ITSDocument;
import tds.itemrenderer.data.ITSResource;
import tds.itemrenderer.data.xml.itemrelease.Item;
import tds.itemrenderer.data.xml.itemrelease.ItemPassage;
import tds.itemrenderer.data.xml.itemrelease.Itemrelease;
import tds.itemrenderer.data.xml.itemrelease.Resource;
import tds.itemrenderer.data.xml.itemrelease.Resourceslist;

import static io.github.benas.randombeans.api.EnhancedRandom.random;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class ResourcesItemDocumentMapperTest {
    @Test
    public void shouldMapResourcesToItemDocument() {
        ItemDocumentMapper resourcesItemDocumentMapper = new ResourcesItemDocumentMapper();
        ITSDocument document = new ITSDocument();
        document.setBaseUri("/something/");
        Itemrelease itemrelease = random(Itemrelease.class, "itemPassage");
        ItemPassage itemPassage = new Item();
        Resourceslist list = new Resourceslist();
        List<Resource> resources = list.getResource();
        Resource resource = new Resource();
        resource.setBankkey("187");
        resource.setType("resource-type");
        resource.setId("1234");
        resources.add(resource);
        itemPassage.setResourceslist(list);

        itemrelease.setItemPassage(itemPassage);

        resourcesItemDocumentMapper.mapItemDocument(document, itemrelease);
        ITSResource itsResource = document.getResources().get(0);
        assertThat(itsResource.getId()).isEqualTo(1234);
        assertThat(itsResource.getType()).isEqualTo("resource-type");
        assertThat(itsResource.getBankKey()).isEqualTo(187);
    }
}
