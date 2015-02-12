xquery version "3.0";

(:~
 : A set of generic XML helper functions for use in XQuery scripts in the HisTEI framework
 :)
module namespace utils="http://histei.info/xquery/utils";

import module namespace functx="http://www.functx.com" at "functx.xql";

declare namespace file="http://expath.org/ns/file";
declare namespace map="http://www.w3.org/2005/xpath-functions/map";
declare namespace tei="http://www.tei-c.org/ns/1.0";

(: File-related Functions :)

(: Checks if a path is valid and also a directory. 
    If a file is given, the path to the parent directory is returned.
    If a path is invalid or does not exist, an empty string is returned.   
    Paths are uniformly returned with a slash on the end (to make adding file names to them easier)
:)
declare function utils:get-dir-path($uri as xs:anyURI?) as xs:string? {
    if (empty($uri)) then
        ()
    else
        let $path := utils:uri-to-path($uri)
        let $path := 
            if (file:is-dir($path)) then
                $path
            else if (file:is-file($path)) then
                functx:substring-before-last($path, file:dir-separator()) 
            else
                ()
        return
            if (empty($path)) then
                ()
            else if (ends-with($path, file:dir-separator())) then
                $path
            else
                concat($path, file:dir-separator())
};

declare function utils:uri-to-path($uri as xs:anyURI?) as xs:string? {
    if (empty($uri)) then
        ()
    else
        let $uriString := string($uri)
        return
            if (starts-with($uriString, "file:/")) then
                replace(substring-after($uriString, "file:/"), "/", 
                    functx:escape-for-regex(file:dir-separator())
                )
            else
                ()
};

declare function utils:path-to-uri($path as xs:string?) as xs:anyURI? {
    if (empty($path) or $path eq "") then
        ()
    else
        let $path := 
            if (file:dir-separator() ne "/") then 
                replace($path, functx:escape-for-regex(file:dir-separator()), "/")
            else
                $path
        return
            xs:anyURI(concat("file:/", $path))
};

(: Generic functions for processing XML :)
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

declare function utils:is-empty-oxy-comment($textNode as node()?) as xs:boolean {
    (
        exists($textNode) 
        and $textNode instance of text()
        and $textNode/preceding-sibling::node()[1] instance of processing-instruction(oxy_comment_start)
        and $textNode/following-sibling::node()[1] instance of processing-instruction(oxy_comment_end)
        and normalize-space($textNode) eq ""
    )
};

declare function utils:non-empty-text-nodes($element as element()) as text()* {
    $element//text()[normalize-space() ne ""]
};



