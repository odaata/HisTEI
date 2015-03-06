xquery version "3.0";

(:~
 : A set of helper functions to generate TEI elements
 :)
 
module namespace teix="http://histei.info/xquery/tei";

import module namespace utils="http://histei.info/xquery/utils" at "utils.xqm";

declare default element namespace "http://www.tei-c.org/ns/1.0";

declare namespace map="http://www.w3.org/2005/xpath-functions/map";
declare namespace uuid="java:java.util.UUID";

declare variable $teix:NS_TEI as xs:string := "http://www.tei-c.org/ns/1.0";
declare variable $teix:DEFAULT_ID_SEPARATOR as xs:string := "_";

(: TEI-specific element names and their use in our corpus:)
declare variable $teix:CONTENT_ELEMENT_NAMES := ("p", "dateline", "signed", "salute", "head", "address", "byline", "argument", "epigraph", "trailer");
declare variable $teix:BREAK_ELEMENT_NAMES := ( "cb", "gb", "lb", "milestone", "pb");
declare variable $teix:MILESTONE_ELEMENT_NAMES := ($teix:BREAK_ELEMENT_NAMES, "handShift");
declare variable $teix:ANNOTATION_ELEMENT_NAMES := ("w", "pc", "num");
declare variable $teix:EDIT_ELEMENT_NAMES := ("add", "del", "hi");
declare variable $teix:PLACE_ELEMENT_NAMES := ("placeName", "district", "settlement", "region", "country", "bloc");

declare variable $teix:ORDERED_ELEMENTS_MAP as map(xs:string, xs:string+) := map {
    "TEI" := ("teiHeader", "fsdDecl", "facsimile", "sourceDoc", "text"),
    "teiHeader" := ("fileDesc", "encodingDesc", "profileDesc", "revisionDesc"),
    "fileDesc" := ("titleStmt", "editionStmt", "extent", "publicationStmt", "seriesStmt", "notesStmt", "sourceDesc"),
    "titleStmt" := ("title", "author", "editor", "respStmt", "meeting", "sponsor", "funder", "principal"),
    "profileDesc" := ("creation", "particDesc", "settingDesc", "textClass", "textDesc", "langUsage", "calendarDesc", "listTranspose", "handNotes"),
    "creation" := ("date", "persName", $teix:PLACE_ELEMENT_NAMES, "orgName"),
    "textClass" := ("classCode", "catRef", "keywords"),
    "text" := ( "front", "body", "group", "back" )
};

declare variable $teix:CON_INFO_TYPES := element contextualTypes {
    element contextualType { attribute key { "psn" }, attribute file { "person.xml" }, attribute idPrefix { "person" } },
    element contextualType { attribute key { "plc" }, attribute file { "place.xml" }, attribute idPrefix { "place" } },
    element contextualType { attribute key { "org" }, attribute file { "org.xml" }, attribute idPrefix { "org" } },
    element contextualType { attribute key { "gen" }, attribute file { "genre.xml" } },
    element contextualType { attribute key { "ann" }, attribute file { "annotation.xml" } }
};
declare variable $teix:CON_INFO_REGEX := concat("(", string-join($teix:CON_INFO_TYPES/*/string(@key), "|"), "):(\S+)");
declare variable $teix:CON_INFO_REF_ATTR_NAMES := ( "ref", "scribeRef", "target", "who" );


declare variable $teix:DEFAULT_TITLE_STMT := <titleStmt><title/></titleStmt>;
declare variable $teix:DEFAULT_PUBLICATION_STMT := <publicationStmt><authority/><idno/></publicationStmt>;
declare variable $teix:DEFAULT_SOURCE_DESC := <sourceDesc><bibl/></sourceDesc>;
declare variable $teix:DEFAULT_FILE_DESC := element fileDesc { $teix:DEFAULT_TITLE_STMT, $teix:DEFAULT_PUBLICATION_STMT, $teix:DEFAULT_SOURCE_DESC };
declare variable $teix:DEFAULT_HEADER := element teiHeader { $teix:DEFAULT_FILE_DESC };

declare variable $teix:DEFAULT_CREATION := <creation><date/><persName/><settlement/></creation>;
declare variable $teix:DEFAULT_TEXT_CLASS := <textClass><catRef/></textClass>;
declare variable $teix:DEFAULT_HAND_NOTES := <handNotes><handNote xml:id="hand_001"/></handNotes>;
declare variable $teix:DEFAULT_PROFILE_DESC := element profileDesc { $teix:DEFAULT_CREATION, $teix:DEFAULT_TEXT_CLASS, $teix:DEFAULT_HAND_NOTES };

(: Generic Functions :)

(:~
 : Return a formatted ID with the given element name and id concatenated with the separator given or the default ($teix:DEFAULT_ID_SEPARATOR)
 : 
 : @param $elementName Name of the element to be concatenated with the id (e.g. TEI, person, w, etc.)
 : @param $id The raw ID of the element, i.e. without any preceding element name, e.g. for a user: MJO but NOT person_MJO 
 : @param $idSep The separator between the element name and raw ID in a reference. Default is underscore, "_" ($teix:DEFAULT_ID_SEPARATOR).
 : @return String with the element name and id concatenated with the separator 
:)
declare function teix:format-id($elementName as xs:string?, $id as xs:string?, $idSep as xs:string?) as xs:string? {
    if (empty($id)) then
        ()
    else
        let $idSep := if (empty($idSep)) then $teix:DEFAULT_ID_SEPARATOR else $idSep
        let $id := replace(normalize-space($id), "\s+", "_")
        return
            if (exists($elementName)) then 
                concat($elementName, $idSep, $id)
            else
                $id
};

declare function teix:format-id($elementName as xs:string?, $id as xs:string?) as xs:string? {
    teix:format-id($elementName, $id, ())
};

(:~
 : Generate a new element with the given name within the TEI namespace
 : - Convenience function for generating lots of TEI elements
 : 
 : @param $name name for the new element, including the optional prefix
 : @param $content content to go inside the new element
 : @return New element() node with the given name within the TEI namespace containing the supplied content
:)
declare function teix:element-tei($name as xs:string, $content) as element() {
    utils:element-NS($name, $content, $teix:NS_TEI)
};

(:~
 : Generate a new element with the given name within the TEI namespace
 : - Convenience function for generating lots of TEI elements
 : 
 : @param $name name for the new element, including the optional prefix
 : @param $content content to go inside the new element
 : @return New element() node with the given name within the TEI namespace containing the supplied content
:)
declare function teix:collection($uri as xs:anyURI) as document-node()* {
    collection($uri)[exists(TEI)]
};

(:~
 : Return the parent content element from within a TEI document (e.g. p, head, dateline, salute, etc.)
 : 
 : @param $element element found within a TEI content element
 : @return Parent content element, if present (e.g. p, head, dateline, salute, etc.)
:)
declare function teix:content-element($element as element()?) as element()? {
    $element/ancestor::*[local-name() = $teix:CONTENT_ELEMENT_NAMES][1]
};

(:~
 : Generate a new attribute with the given name within the TEI namespace
 : - Convenience function for generating lots of TEI attributes
 : 
 : @param $name name for the new attribute, including the optional prefix
 : @param $value the value for the new attribute
 : @return New attribute() node with the given name within the TEI namespace containing the supplied value
:)
declare function teix:attribute-NS($name as xs:string, $value as xs:anyAtomicType?) as attribute()? {
    if (empty($value) or string($value) eq "") then
        ()
    else
        utils:attribute-NS($name, $value, $teix:NS_TEI)
};

(: Update any ordered TEI component using the $teix:ORDERED_ELEMENTS_MAP variable to select the fieldNames
    If no fieldNames are found, an error is thrown
:)
declare function teix:update-tei-content-ordered($element as element(), $newElements as element()*) as element() {
    utils:update-content-ordered($element, $teix:ORDERED_ELEMENTS_MAP, $newElements)
};

(: Functions for specific TEI nodes starting with the entire document :)

declare function teix:update-TEI($element as element(TEI), $newElements as element()*) as element(TEI) {
    teix:update-tei-content-ordered($element, $newElements)
};

declare function teix:update-text($element as element(text), $newElements as element()*) as element(text) {
    teix:update-tei-content-ordered($element, $newElements)
};

(: Header Updates :)
declare function teix:update-teiHeader($header as element(teiHeader)?, $newElements as element()*) as element(teiHeader) {
    let $header := if (empty($header)) then $teix:DEFAULT_HEADER else $header
    return
        teix:update-tei-content-ordered($header, $newElements)
};

(: fileDesc :)

declare function teix:update-fileDesc($fileDesc as element(fileDesc)?, $newElements as element()*) as element(fileDesc) {
    let $fileDesc := if (empty($fileDesc)) then $teix:DEFAULT_FILE_DESC else $fileDesc
    return
        teix:update-tei-content-ordered($fileDesc, $newElements)
};

(: titleStmt :)

declare function teix:update-titleStmt($titleStmt as element(titleStmt)?, $newElements as element()*) as element(titleStmt) {
    let $titleStmt := if (empty($titleStmt)) then $teix:DEFAULT_TITLE_STMT else $titleStmt
    return
        teix:update-tei-content-ordered($titleStmt,$newElements)
};

declare function teix:respStmt($respKey as xs:string?, $userID as xs:string?, 
                                    $userText as xs:string?, $respText as xs:string?) as element(respStmt) {
    element respStmt {
        element resp {
            if (exists($respKey)) then attribute key { $respKey } else (),
            $respText
        },
        element name {
            if (exists($userID)) then attribute ref { teix:format-con-info-ref("psn", $userID) } else (),
            $userText
        }
    }
};

declare function teix:update-extent($quantity as xs:integer, $unit as xs:string, $extent as element(extent)?) as element(extent) {
    let $measure := <measure quantity="{$quantity}" unit="{$unit}">{concat($quantity, " ", $unit)}</measure>
    let $newContents := $extent/node() except $extent/measure[@unit eq $unit]
    return
        if (exists($extent)) then
            utils:replace-content($extent, ($newContents, $measure) )
        else
            element extent { $measure }
};


(: profileDesc :)

declare function teix:update-profileDesc($profileDesc as element(profileDesc)?, $newElements as element()*) as element(profileDesc) {
    let $profileDesc := if (empty($profileDesc)) then $teix:DEFAULT_PROFILE_DESC else $profileDesc
    return
        teix:update-tei-content-ordered($profileDesc, $newElements)
};

declare function teix:update-creation($creation as element(creation)?, $newElements as element()*) as element(creation) {
    let $creation := if (empty($creation)) then $teix:DEFAULT_CREATION else $creation
    return
        teix:update-tei-content-ordered($creation, $newElements)
};

declare function teix:update-textClass($textClass as element(textClass)?, $newElements as element()*) as element(textClass) {
    let $textClass := if (empty($textClass)) then $teix:DEFAULT_TEXT_CLASS else $textClass
    return
        teix:update-tei-content-ordered($textClass, $newElements)
};

declare function teix:catRef($targetID as xs:string?, $schemeID as xs:string?) as element(catRef) {
    element catRef {
        if ($schemeID ne "") then attribute scheme { teix:format-con-info-ref("gen", $schemeID) } else (),
        if ($targetID ne "") then attribute target { teix:format-con-info-ref("gen", $targetID) } else ()
    }
};

(: revisionDesc :)

declare function teix:change($status as xs:string, $content, $userID as xs:string?, $when as xs:dateTime?) as element(change) {
    element change {
        attribute status { $status },
        attribute { "when" } { if (empty($when)) then current-dateTime() else $when },
        if ($userID ne "") then attribute who { teix:format-con-info-ref("psn", $userID) } else (),
        $content
    }
};

declare function teix:change($status as xs:string, $content, $userID as xs:string?) as element(change) {
    teix:change($status, $content, $userID, ())
};

declare function teix:update-revisionDesc($change as element(change)+, $revisionDesc as element(revisionDesc)?, 
                                            $status as xs:string?) as element(revisionDesc) {
    let $statusAttr := if (exists($status) and $status ne "") then attribute status { $status } else ()
    return
    if (empty($revisionDesc)) then
        element revisionDesc { $statusAttr, $change }
    else
        let $attrs := 
            if (empty($statusAttr)) then
                $revisionDesc/@*
            else
            ( $revisionDesc/@* except $revisionDesc/@status, $statusAttr ) 
        return
            utils:replace-content($revisionDesc, ( $revisionDesc/node(), $change ), $attrs)
};

declare function teix:update-revisionDesc($change as element(change)+, $revisionDesc as element(revisionDesc)?) as element(revisionDesc) {
    teix:update-revisionDesc($change, $revisionDesc, ())
};

(: Annotation Functions :)

declare function teix:num($content, $type as xs:string?, $value as xs:string?) as element(num)? {
    if (empty($content)) then
        $content
    else
        element num {
            attribute xml:id { teix:format-id("num", uuid:randomUUID()) },
            if (exists($type)) then attribute type { $type } else (),
            if (exists($value)) then attribute value { $value } else (),
            $content
        }
};

declare function teix:num($content, $type as xs:string?) as element(num)? {
    teix:num($content, $type, ())
};

declare function teix:num($content) as element(num)? {
    teix:num($content, ())
};

declare function teix:pc($content, $force as xs:string?, $type as xs:string?) as element(pc)? {
    if (empty($content)) then
        $content
    else
        element pc {
            if (exists($force)) then attribute force { $force } else (),
            if (exists($type)) then attribute type { $type } else (),
            $content
        }
};

declare function teix:pc($content, $force as xs:string?) as element(pc)? {
    teix:pc($content, $force, ())
};

declare function teix:pc($content) as element(pc)? {
    teix:pc($content, ())
};

declare function teix:word($content) as element(w)? {
    if (empty($content)) then
        $content
    else
        element w {
            attribute xml:id { teix:format-id("w", uuid:randomUUID()) },
            $content
        }
};

(: Contextual Info :)

(:~
 : Return a reference to a Contextual Info record using the HisTEI private URI scheme (e.g. psn:person_MJO)
 : 
 : @param $type Contextual info type and first part of the private URI (e.g. ann, gen, org, plc, psn)
 : @param $id The raw ID of the Contextual Info record, i.e. without any preceding element name, e.g. for a user: MJO but NOT person_MJO 
 : @param $idSep The separator between the element name and raw ID in a reference. Default is underscore, "_".
 : @return Reference to the Contextual Info record including the private URI schema for the given Contextual Info type (e.g. psn:person_MJO).
 :  This return value can be saved to any TEI ref-like attribute (e.g. ref, scribeRef, who) 
:)
declare function teix:format-con-info-ref($type as xs:string, $id as xs:string?, $idSep as xs:string?) as xs:string? {
    let $conType := $teix:CON_INFO_TYPES/*[@key eq $type]
    let $refID := teix:format-id($conType/@idPrefix, $id)
    return
        if (empty($conType) or empty($id)) then
            ()
        else
            concat($type, ":", $refID)
};

declare function teix:format-con-info-ref($type as xs:string, $id as xs:string?) as xs:string? {
    teix:format-con-info-ref($type, $id, ())
};

declare function teix:split-ref($ref as xs:string?) as xs:string* {
    if (matches($ref, $teix:CON_INFO_REGEX)) then
        ( replace($ref, $teix:CON_INFO_REGEX, "$1"), replace($ref, $teix:CON_INFO_REGEX, "$2") )
    else
        ()
};

declare function teix:con-info-docs-map($contextualInfoURI as xs:anyURI) as map(xs:string, document-node()) {
        map:new(
            for $doc in teix:collection($contextualInfoURI)
            let $filename := utils:filenames($doc)
            let $conType := $teix:CON_INFO_TYPES/*[@file eq $filename]
            return
                if (exists($conType)) then
                    map:entry(string($conType/@key), $doc)
                else
                    ()
        )
};

declare function teix:con-info-by-ref($contextualInfoMap as map(xs:string, document-node()), $ref as item()?) as element()? {
    let $ref := 
        if (empty($ref) or $ref instance of xs:string) then 
            $ref 
        else if ($ref instance of element()) then 
            string(($ref/@*[local-name() = $teix:CON_INFO_REF_ATTR_NAMES])[1])
        else
            string($ref)
    
    let $refParts := teix:split-ref($ref)
    return
        if (exists($refParts[1])) then
            ($contextualInfoMap($refParts[1])//*[@xml:id eq $refParts[2]])[1]
        else
            ()
};









