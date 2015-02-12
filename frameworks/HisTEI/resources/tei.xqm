xquery version "3.0";

(:~
 : A set of helper functions to generate TEI elements
 :)
module namespace teix="http://histei.info/xquery/tei";

import module namespace utils="http://histei.info/xquery/utils" at "utils.xqm";

declare default element namespace "http://www.tei-c.org/ns/1.0";

declare namespace map="http://www.w3.org/2005/xpath-functions/map";
declare namespace uuid="java:java.util.UUID";

(: Header Updates :)

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

declare function teix:update-revisionDesc($change as element(change), $revisionDesc as element(revisionDesc)?, 
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

declare function teix:update-revisionDesc($change as element(change), $revisionDesc as element(revisionDesc)?) as element(revisionDesc) {
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



