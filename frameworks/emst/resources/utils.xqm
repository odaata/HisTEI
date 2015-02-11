xquery version "3.0";

(:~
 : A set of generic XML helper functions for use in XQuery scripts in the HisTEI framework
 :)
module namespace utils="http://histei.info/xquery/utils";

declare namespace map="http://www.w3.org/2005/xpath-functions/map";

declare namespace tei="http://www.tei-c.org/ns/1.0";

declare function utils:replace-content($element as element(), $newContent, 
                                            $newAttributes as attribute()*) as element() {
    element { node-name($element) } {
        if (exists($newAttributes)) then $newAttributes else $element/@*,
        $newContent
    }
};

declare function utils:replace-content($element as element(), $newContent) as element() {
    utils:replace-content($element, $newContent, ())
};

(: $type can be "anywhere", "starts", "ends", "all" nothing defaults to "anywhere" :)
declare function utils:contains-ws($string as xs:string?, $type as xs:string?) as xs:boolean {
    let $regex :=
        switch ($type)
        case "starts" return "^\s"
        case "ends" return "\s$"
        case "all" return "^\s+$"
        default return "\s"
    return
        matches($string, $regex)
};

declare function utils:contains-ws($string as xs:string?) as xs:boolean {
    utils:contains-ws($string, ())
};