xquery version "3.0";

(:~
 : A set of helper functions to generate TEI elements
 :)
 
module namespace teix="http://histei.info/xquery/tei";

import module namespace utils="http://histei.info/xquery/utils" at "utils.xqm";

declare default element namespace "http://www.tei-c.org/ns/1.0";

declare namespace map="http://www.w3.org/2005/xpath-functions/map";
declare namespace uuid="java:java.util.UUID";

declare %private variable $teix:HEADER_FIELDS := ("fileDesc", "encodingDesc", "profileDesc", "revisionDesc");
declare %private variable $teix:FILE_DESC_FIELDS := 
    ("titleStmt", "editionStmt", "extent", "publicationStmt", "seriesStmt", "notesStmt", "sourceDesc");
declare %private variable $teix:PROFILE_DESC_FIELDS := 
    ("creation", "particDesc", "settingDesc", "textClass", "textDesc", "langUsage", "calendarDesc", "listTranspose", "handNotes");

declare %private variable $teix:TITLE_STMT_FIELDS :=
    ("title", "author", "editor", "respStmt", "meeting", "sponsor", "funder", "principal");

declare %private variable $teix:DEFAULT_TITLE_STMT := <titleStmt><title/></titleStmt>;
declare %private variable $teix:DEFAULT_FILE_DESC := <fileDesc>{$teix:DEFAULT_TITLE_STMT}<publicationStmt/><sourceDesc/></fileDesc>;
declare %private variable $teix:DEFAULT_HEADER := element teiHeader { $teix:DEFAULT_FILE_DESC };


(: Header Updates :)
declare function teix:update-teiHeader($newElements as element()*, $header as element(teiHeader)?) as element(teiHeader) {
    let $header := if (empty($header)) then $teix:DEFAULT_HEADER else $header
    return
        utils:update-content-ordered($header, $teix:HEADER_FIELDS, $newElements)
};

declare function teix:update-fileDesc($newElements as element()*, $fileDesc as element(fileDesc)?) as element(fileDesc) {
    let $fileDesc := if (empty($fileDesc)) then $teix:DEFAULT_FILE_DESC else $fileDesc
    return
        utils:update-content-ordered($fileDesc, $teix:FILE_DESC_FIELDS, $newElements)
};

(: FileDesc :)
declare function teix:update-extent($quantity as xs:integer, $unit as xs:string, $extent as element(extent)?) as element(extent) {
    let $measure := <measure quantity="{$quantity}" unit="{$unit}">{concat($quantity, " ", $unit)}</measure>
    let $newContents := $extent/node() except $extent/measure[@unit eq $unit]
    return
        if (exists($extent)) then
            utils:replace-content($extent, ($newContents, $measure) )
        else
            element extent { $measure }
};

declare function teix:change($status as xs:string, $content, $userID as xs:string?, $when as xs:dateTime?) as element(change) {
    element change {
        attribute status { $status },
        attribute { "when" } { if (empty($when)) then current-dateTime() else $when },
        if ($userID ne "") then attribute who { "psn:person_" || $userID } else (),
        $content
    }
};

declare function teix:change($status as xs:string, $content, $userID as xs:string?) as element(change) {
    teix:change($status, $content, $userID, ())
};

declare function teix:update-revisionDesc($change as element(change)+, $revisionDesc as element(revisionDesc)?, 
                                            $status as xs:string?) as element(revisionDesc) {
    if (empty($revisionDesc)) then
        element revisionDesc { $change }
    else
        let $attrs := 
            if (empty($status)) then
                $revisionDesc/@*
            else
            ( $revisionDesc/@* except $revisionDesc/@status, attribute status { $status } ) 
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



