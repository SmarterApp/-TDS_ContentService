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

import AIR.Common.Utilities.Path;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import tds.itemrenderer.data.ITSAttachment;
import tds.itemrenderer.data.ITSContent;
import tds.itemrenderer.data.ITSDocument;
import tds.itemrenderer.data.ITSKeyboard;
import tds.itemrenderer.data.ITSKeyboardKey;
import tds.itemrenderer.data.ITSKeyboardRow;
import tds.itemrenderer.data.ITSOption;
import tds.itemrenderer.data.ITSOptionList;
import tds.itemrenderer.data.ITSQTI;
import tds.itemrenderer.data.apip.APIPAccessElement;
import tds.itemrenderer.data.apip.APIPBraille;
import tds.itemrenderer.data.apip.APIPBrailleCode;
import tds.itemrenderer.data.apip.APIPContentLinkInfo;
import tds.itemrenderer.data.apip.APIPReadAloud;
import tds.itemrenderer.data.apip.APIPRelatedElementInfo;
import tds.itemrenderer.data.apip.APIPXml;
import tds.itemrenderer.data.xml.itemrelease.AccessElement;
import tds.itemrenderer.data.xml.itemrelease.ApipAccessibility;
import tds.itemrenderer.data.xml.itemrelease.Attachment;
import tds.itemrenderer.data.xml.itemrelease.BrailleCode;
import tds.itemrenderer.data.xml.itemrelease.Content;
import tds.itemrenderer.data.xml.itemrelease.ContentLinkInfo;
import tds.itemrenderer.data.xml.itemrelease.ItemPassage;
import tds.itemrenderer.data.xml.itemrelease.Itemrelease;
import tds.itemrenderer.data.xml.itemrelease.Key;
import tds.itemrenderer.data.xml.itemrelease.Keyboard;
import tds.itemrenderer.data.xml.itemrelease.KeyboardRow;
import tds.itemrenderer.data.xml.itemrelease.Option;
import tds.itemrenderer.data.xml.itemrelease.Optionlist;
import tds.itemrenderer.data.xml.itemrelease.Qti;
import tds.itemrenderer.data.xml.itemrelease.RelatedElementInfo;

@Component
public class ContentsItemDocumentMapper implements ItemDocumentMapper {
    private final static String ITEMBODY_SPEC = "itemBody";
    private final static String ITEMBODY_SPEC_ELEMENT = "<itemBody>";

    @Override
    public void mapItemDocument(final ITSDocument document, final Itemrelease itemXml) {
        ItemPassage item = itemXml.getItemPassage();
        for (Content content : item.getContent()) {
            ITSContent itsContent = new ITSContent();
            itsContent.setLanguage(content.getLanguage());
            if (content.getQti() != null) {
                itsContent.setQti(readQti(content.getQti()));
            }

            itsContent.setIllustration(content.getIllustration());
            itsContent.setIllustrationTTS(content.getIllustrationTts());
            itsContent.setStem(content.getStem());
            itsContent.setStemTTS(content.getStemTts());
            itsContent.setTitle(content.getTitle());

            if (content.getOptionlist() != null) {
                itsContent.setOptions(readOptions(document, content.getOptionlist()));
            }
            if (content.getKeyboard() != null) {
                itsContent.setKeyboard(readKeyboard(content.getKeyboard()));
            }
            if (content.getApipAccessibility() != null) {
                itsContent.setApip(readApip(content.getApipAccessibility()));
            }
            if (content.getAttachmentlist() != null) {
                itsContent.setAttachments(readAttachments(document, content.getAttachmentlist().getAttachment()));
            }
            processGenericElements(itsContent, content);
            document.addContent(itsContent);
        }
    }

    private static ITSQTI readQti(Qti qti) {
        final ITSQTI itsQti = new ITSQTI();

        // get the qti specification ("itemBody" or "assessmentItem")
        itsQti.setSpecification(qti.getSpec());

        if (qti.getContent() != null) {
            itsQti.setXml(qti.getContent().trim());
        }
        // If "itemBody" spec is being used, but no itembody element is present, add it manually - see ITSDocumentParser.java
        if (itsQti.getSpecification() != null
            && itsQti.getSpecification().equals(ITEMBODY_SPEC)
            && itsQti.getXml() != null
            && !itsQti.getXml().startsWith(ITEMBODY_SPEC_ELEMENT)) {
            itsQti.setXml(String.format("<itemBody>%s</itemBody>", itsQti.getXml()));
        }

        return itsQti;
    }

    private static ITSOptionList readOptions(final ITSDocument document, final Optionlist optionlist) {
        List<Option> options = optionlist.getOption();
        ITSOptionList itsOptionList = new ITSOptionList();

        if (!StringUtils.isBlank(optionlist.getMinChoices())) {
            itsOptionList.setMinChoices(Integer.parseInt(optionlist.getMinChoices()));
        }

        if (!StringUtils.isBlank(optionlist.getMaxChoices())) {
            itsOptionList.setMaxChoices(Integer.parseInt(optionlist.getMaxChoices()));
        }

        if (options != null && options.size() > 0) {
            for (Option option : options) {
                itsOptionList.add(readOption(document, option));
            }
        }
        return itsOptionList;
    }

    private static ITSOption readOption(final ITSDocument document, final Option option) {
        ITSOption itsOption = new ITSOption();
        String name = option.getName().trim();

        if (StringUtils.containsIgnoreCase(document.getFormat(), "SI")) {
            // e.g., <name>Option NR</name>
            itsOption.setKey(name.replaceAll("\\u00a0", " ").split(" ")[1]);
        } else {
            // e.g., <name>Option A</name>
            itsOption.setKey(name.substring(name.length() - 1));
        }
        itsOption.setValue(option.getVal());
        itsOption.setSound(option.getSound());
        itsOption.setFeedback(option.getFeedback());
        itsOption.setTts(option.getTts());

        return itsOption;
    }

    private static ITSKeyboard readKeyboard(final Keyboard keyboard) {
        ITSKeyboard itsKeyboard = new ITSKeyboard();

        if (keyboard.getKeyboardRow() != null && keyboard.getKeyboardRow().size() > 0) {
            itsKeyboard.setRows(new ArrayList<>());

            for (KeyboardRow keyboardRow : keyboard.getKeyboardRow()) {
                ITSKeyboardRow itsKeyboardRow = new ITSKeyboardRow();
                itsKeyboardRow.setId(keyboardRow.getId());
                itsKeyboard.getRows().add(itsKeyboardRow);

                if (keyboardRow.getKey() != null && keyboardRow.getKey().size() > 0) {
                    List<ITSKeyboardKey> itsKeyboardKeys = new ArrayList<>();
                    itsKeyboardRow.setKeys(itsKeyboardKeys);

                    for (Key key : keyboardRow.getKey()) {
                        ITSKeyboardKey itsKeyboardKey = new ITSKeyboardKey();
                        itsKeyboardKey.setId(key.getId());
                        itsKeyboardKey.setType(key.getValue());
                        itsKeyboardKey.setValue(key.getValue());
                        itsKeyboardKey.setDisplay(key.getDisplay());
                        itsKeyboardKeys.add(itsKeyboardKey);

                    }
                }
            }
        }

        return itsKeyboard;
    }

    private static List<ITSAttachment> readAttachments(final ITSDocument document, final List<Attachment> attachments) {
        List<ITSAttachment> itsAttachments = new ArrayList<>();

        if (attachments != null && attachments.size() > 0) {
            for (Attachment attachment : attachments) {
                ITSAttachment itsAttachment = new ITSAttachment();
                itsAttachment.setId(attachment.getId());
                itsAttachment.setType(attachment.getType());
                itsAttachment.setSubType(attachment.getSubtype());
                itsAttachment.setFile(attachment.getFile());

                if (!StringUtils.isEmpty(itsAttachment.getFile())) {
                    itsAttachment.setFile(getFilePath(document) + itsAttachment.getFile());
                }

                itsAttachments.add(itsAttachment);
            }
        }
        return itsAttachments;
    }

    private static APIPXml readApip(ApipAccessibility apipAccessibility) {
        APIPXml apipXml = null;

        if (apipAccessibility.getAccessibilityInfo() != null && apipAccessibility.getAccessibilityInfo().getAccessElement() != null) {
            List<AccessElement> accessElements = apipAccessibility.getAccessibilityInfo().getAccessElement();
            if (accessElements != null && accessElements.size() > 0) {
                apipXml = new APIPXml();
                for (AccessElement accessElement : accessElements) {
                    apipXml.addAccessElement(getApipAccessElement(accessElement));
                }
            }
        }

        return apipXml;
    }

    private static APIPAccessElement getApipAccessElement(AccessElement accessElement) {
        APIPAccessElement apipAccessElement = new APIPAccessElement();
        apipAccessElement.setIdentifier(accessElement.getIdentifier());
        ContentLinkInfo contentLinkInfo = accessElement.getContentLinkInfo();

        if (contentLinkInfo != null) {
            APIPContentLinkInfo apipContentLinkInfo = new APIPContentLinkInfo();
            apipContentLinkInfo.setItsLinkIdentifierRef(contentLinkInfo.getItsLinkIdentifierRef());
            apipContentLinkInfo.setType(contentLinkInfo.getType());
            apipContentLinkInfo.setSubType(contentLinkInfo.getSubtype());
            apipAccessElement.setContentLinkInfo(apipContentLinkInfo);
        }

        RelatedElementInfo relatedElementInfo = accessElement.getRelatedElementInfo();
        if (relatedElementInfo != null) {
            apipAccessElement.setRelatedElementInfo(getApipRelatedElementInfo(relatedElementInfo));
        } // </relatedElementInfo>
        return apipAccessElement;
    }

    private static APIPRelatedElementInfo getApipRelatedElementInfo(final RelatedElementInfo relatedElementInfo) {
        APIPRelatedElementInfo apipRelatedElementInfo = new APIPRelatedElementInfo();

        if (relatedElementInfo.getReadAloud() != null) {
            APIPReadAloud apipReadAloud = new APIPReadAloud();
            apipRelatedElementInfo.setReadAloud(apipReadAloud);
            apipReadAloud.setAudioText(relatedElementInfo.getReadAloud().getAudioText());
            apipReadAloud.setAudioShortDesc(relatedElementInfo.getReadAloud().getAudioShortDesc());
            apipReadAloud.setAudioLongDesc(relatedElementInfo.getReadAloud().getAudioLongDesc());
            apipReadAloud.setTtsPronunciation(relatedElementInfo.getReadAloud().getTextToSpeechPronunciation());
            // Assumption that either AudioShortDesc or TextToSpeechPronunciationAlternate XML tags should be used
            if (StringUtils.isBlank(relatedElementInfo.getReadAloud().getAudioShortDesc())) {
                apipReadAloud.setAudioShortDesc(relatedElementInfo.getReadAloud().getTextToSpeechPronunciationAlternate());
            }
        }

        if (relatedElementInfo.getBrailleText() != null) {
            apipRelatedElementInfo.setBraille(new APIPBraille());
            apipRelatedElementInfo.getBraille().setText(relatedElementInfo.getBrailleText().getBrailleTextString());
            if (relatedElementInfo.getBrailleText().getBrailleCode() != null) {
                apipRelatedElementInfo.getBraille().setText(relatedElementInfo.getBrailleText().getBrailleTextString());
                BrailleCode brailleCode = relatedElementInfo.getBrailleText().getBrailleCode();
                APIPBrailleCode apipBrailleCode = new APIPBrailleCode(brailleCode.getType(), brailleCode.getContent());
                apipRelatedElementInfo.getBraille().getBrailleCodes().add(apipBrailleCode);
            }
        }

        return apipRelatedElementInfo;
    }

    private static void processGenericElements(final ITSContent itsContent, final Content content) {
        if (content.getConstraints() != null) {
            itsContent.getGenericElements().add(content.getConstraints());
        }
        if (content.getSearch() != null) {
            itsContent.getGenericElements().add(content.getSearch());
        }
    }

    private static String getFilePath(final ITSDocument document) {
        return document.getBaseUri().replace(Path.getFileName(document.getBaseUri()), "");
    }
}
