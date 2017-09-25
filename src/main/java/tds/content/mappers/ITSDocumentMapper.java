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

import org.springframework.stereotype.Component;

import tds.itemrenderer.data.ITSDocument;
import tds.itemrenderer.data.ITSSoundCue;
import tds.itemrenderer.data.ITSTutorial;
import tds.itemrenderer.data.ITSTypes;
import tds.itemrenderer.data.xml.itemrelease.ItemPassage;
import tds.itemrenderer.data.xml.itemrelease.Itemrelease;
import tds.itemrenderer.data.xml.itemrelease.Passage;

@Component
public class ITSDocumentMapper implements ItemDocumentMapper {
    @Override
    public void mapItemDocument(final ITSDocument document, final Itemrelease itemXml) {
        document.setValidated(true);
        if (itemXml.getVersion() != null) {
            document.setVersion(Double.parseDouble(itemXml.getVersion().trim()));
        } else {
            document.setVersion(1.0);
        }

        if (document.getVersion() == 0) {
            return;
        }

        if (itemXml.getItemPassage() != null) {
            if (itemXml.getItemPassage() instanceof Passage) {
                document.setType(ITSTypes.ITSEntityType.Passage);
            } else {
                document.setType(ITSTypes.ITSEntityType.Item);
            }
        } else if (document.getType() == ITSTypes.ITSEntityType.Unknown){
            // make sure the document was defined as either an item or passage
            return;
        }

        // get item/passage info
        ItemPassage item = itemXml.getItemPassage();
        document.setFormat(item.getFormat());
        if (item.getId() != null) {
            document.setId(Long.parseLong(item.getId().trim()));
        }
        if (item.getBankkey() != null) {
            document.setBankKey(Long.parseLong(item.getBankkey().trim()));
        }
        if (item.getVersion() != null) {
            document.setApprovedVersion(Integer.parseInt(item.getVersion().trim()));
        }
        if (item.getTutorial() != null) {
            ITSTutorial itsTutorial = new ITSTutorial();
            document.setTutorial(itsTutorial);
            if (item.getTutorial().getId() != null) {
                itsTutorial.setId(Long.parseLong(item.getTutorial().getId().trim()));
            }
            if (item.getTutorial().getBankkey() != null) {
                itsTutorial.setBankKey(Long.parseLong(item.getTutorial().getBankkey().trim()));
            }
        }

        if (item.getSoundcue() != null) {
            ITSSoundCue itsSoundCue = new ITSSoundCue();
            document.setSoundCue(itsSoundCue);
            if (item.getSoundcue().getId() != null) {
                itsSoundCue.setId(Long.parseLong(item.getSoundcue().getId().trim()));
            }
            if (item.getSoundcue().getBankkey() != null) {
                itsSoundCue.setBankKey(Long.parseLong(item.getSoundcue().getBankkey().trim()));
            }
        }

        if (item.getGridanswerspace() != null) {
            document.setGridAnswerSpace(item.getGridanswerspace().trim());
        }
    }
}
