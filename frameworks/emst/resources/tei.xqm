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

declare variable $teix:ORDERED_ELEMENTS_MAP as map(xs:string, xs:string+) := map {
    "TEI" := ("teiHeader", "fsdDecl", "facsimile", "sourceDoc", "text"),
    "teiHeader" := ("fileDesc", "encodingDesc", "profileDesc", "revisionDesc"),
    "fileDesc" := ("titleStmt", "editionStmt", "extent", "publicationStmt", "seriesStmt", "notesStmt", "sourceDesc"),
    "profileDesc" := ("creation", "particDesc", "settingDesc", "textClass", "textDesc", "langUsage", "calendarDesc", "listTranspose", "handNotes"),
    "titleStmt" := ("title", "author", "editor", "respStmt", "meeting", "sponsor", "funder", "principal"),
    "textClass" := ("classCode", "catRef", "keywors")
};

declare %private variable $teix:DEFAULT_TITLE_STMT := <titleStmt><title/></titleStmt>;
declare %private variable $teix:DEFAULT_FILE_DESC := <fileDesc>{$teix:DEFAULT_TITLE_STMT}<publicationStmt/><sourceDesc/></fileDesc>;
declare %private variable $teix:DEFAULT_HEADER := element teiHeader { $teix:DEFAULT_FILE_DESC };

declare %private variable $teix:DEFAULT_TEXT_CLASS := <textClass><catRef/></textClass>;
declare %private variable $teix:DEFAULT_PROFILE_DESC := 
    <profileDesc>
        <creation>
            <date/>
            <persName/>
            <settlement/>
        </creation>
        {$teix:DEFAULT_TEXT_CLASS}
        <handNotes>
            <handNote xml:id="hand_001"/>
        </handNotes>
    </profileDesc>;

(: Generic XML Functions :)

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
            if (exists($userID)) then attribute ref { teix:format-context-info-ref("psn", $userID) } else (),
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

declare function teix:update-textClass($textClass as element(textClass)?, $newElements as element()*) as element(textClass) {
    let $textClass := if (empty($textClass)) then $teix:DEFAULT_TEXT_CLASS else $textClass
    return
        teix:update-tei-content-ordered($textClass, $newElements)
};

declare function teix:catRef($targetID as xs:string?, $schemeID as xs:string?) as element(catRef) {
    element catRef {
        if ($schemeID ne "") then attribute scheme { teix:format-context-info-ref("gen", $schemeID) } else (),
        if ($targetID ne "") then attribute target { teix:format-context-info-ref("gen", $targetID) } else ()
    }
};

(: revisionDesc :)

declare function teix:change($status as xs:string, $content, $userID as xs:string?, $when as xs:dateTime?) as element(change) {
    element change {
        attribute status { $status },
        attribute { "when" } { if (empty($when)) then current-dateTime() else $when },
        if ($userID ne "") then attribute who { teix:format-context-info-ref("psn", $userID) } else (),
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
            attribute xml:id { concat("num_", uuid:randomUUID()) },
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
            attribute xml:id { concat("w_", uuid:randomUUID()) },
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
declare function teix:format-context-info-ref($type as xs:string, $id as xs:string?, $idSep as xs:string?) as xs:string? {
    if (exists($id)) then
        let $id := replace(normalize-space($id), " ", "_")
        let $idSep := if (empty($idSep)) then "_" else $idSep
        let $idPrefix := 
            switch($type)
            case "psn" return "person"
            case "plc" return "place"
            case "org" return "org"
            default return ()
        let $refID := if (exists($idPrefix)) then concat($idPrefix, $idSep, $id) else $id
        return
            concat($type, ":", $refID)
    else
        ()
};

declare function teix:format-context-info-ref($type as xs:string, $id as xs:string?) as xs:string? {
    teix:format-context-info-ref($type, $id, ())
};

declare function teix:get-xml-id-from-ref($ref as xs:string?) as xs:string? {
    $ref
};

declare function teix:get-contextual-info-docs($contextualInfoPath as xs:anyAtomicType?) as map(xs:string, document-node()) {
    let $uri := utils:saxon-collection-uri($contextualInfoPath, false())
    return
        map:new(
            if (empty($uri)) then 
                ()
            else
                for $doc in collection($uri)[exists(TEI)]
                let $key := utils:get-file-basenames($doc)
                return
                    if (empty($key) and $key ne "") then
                        ()
                    else
                        map:entry($key, $doc)
        )
};









