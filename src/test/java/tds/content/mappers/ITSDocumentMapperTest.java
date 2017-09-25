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

import tds.itemrenderer.data.ITSDocument;
import tds.itemrenderer.data.ITSTypes;
import tds.itemrenderer.data.xml.itemrelease.Item;
import tds.itemrenderer.data.xml.itemrelease.ItemPassage;
import tds.itemrenderer.data.xml.itemrelease.Itemrelease;
import tds.itemrenderer.data.xml.itemrelease.Soundcue;
import tds.itemrenderer.data.xml.itemrelease.Tutorial;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class ITSDocumentMapperTest {

    @Test
    public void shouldMapITSDocumentItem() {
        ItemDocumentMapper itsDocumentMapper = new ITSDocumentMapper();
        ITSDocument document = new ITSDocument();
        document.setBaseUri("/something/");
        Itemrelease itemrelease = new Itemrelease();
        itemrelease.setVersion("3.2");

        ItemPassage itemPassage = new Item();
        itemPassage.setFormat("MI");
        itemPassage.setBankkey("187");
        itemPassage.setId("1234");
        itemPassage.setVersion("2112");

        Tutorial tutorial = new Tutorial();
        tutorial.setBankkey("187");
        tutorial.setId("1337");
        itemPassage.setTutorial(tutorial);

        Soundcue soundcue = new Soundcue();
        soundcue.setId("200");
        soundcue.setBankkey("9876");
        itemPassage.setSoundcue(soundcue);
        itemPassage.setGridanswerspace("gridSpace");
        itemrelease.setItemPassage(itemPassage);

        itsDocumentMapper.mapItemDocument(document, itemrelease);
        assertThat(document.getType()).isEqualTo(ITSTypes.ITSEntityType.Item);
        assertThat(document.getVersion()).isEqualTo(3.2);
        assertThat(document.getValidated()).isTrue();
        assertThat(document.getId()).isEqualTo(1234);
        assertThat(document.getBankKey()).isEqualTo(187);
        assertThat(document.getApprovedVersion()).isEqualTo(2112);
        assertThat(document.getTutorial().getId()).isEqualTo(1337);
        assertThat(document.getTutorial().getBankKey()).isEqualTo(187);
        assertThat(document.getSoundCue().getBankKey()).isEqualTo(9876);
        assertThat(document.getSoundCue().getId()).isEqualTo(200);

        assertThat(document.getGridAnswerSpace()).isEqualTo("gridSpace");
    }
}
