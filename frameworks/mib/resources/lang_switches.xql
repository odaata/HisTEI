xquery version "3.0";

import module namespace teix="http://cohd.info/xquery/tei" at "tei.xqm";

declare namespace tei="http://www.tei-c.org/ns/1.0";

declare namespace map="http://www.w3.org/2005/xpath-functions/map";

import module namespace txt="http://cohd.info/xquery/tei2text" at "tei2text.xqm";

declare function local:get-parent-ann($currentAnn as element()) as element() {
    $currentAnn/ancestor::*[local-name(.) = ("s", "cl", "phr", "w")][1]
};

declare function local:get-lang($element as element()) as xs:string? {
    $element/ancestor-or-self::*[attribute::xml:lang][1]/@xml:lang
};

declare function local:get-id($field as xs:string?) as xs:string? {
    substring-after($field, ":")
};

declare function local:get-gloss-info($switch as element()) as element()* {
    let $gloss := $switch/ancestor::tei:gloss[1]
    return
        if (exists($gloss)) then
            let $target := data($gloss/@target)
            let $target := 
                if ($target eq "") then 
                    ()
                else if (starts-with($target, "#")) then 
                    substring-after($target, "#")
                else 
                    $target
            let $term :=
                if (empty($target)) then
                    ()
                else
                    root($gloss)//*[@xml:id eq $target]
            return
            (
                element termID { $target },
                element termText { txt:toText($term) },
                element termLang { local:get-lang($term) }
            )   
        else
            ()
};

declare function local:get-ann-info($ann as element()?, $prefix as xs:string) as element()* {
    let $id := data($ann/@xml:id)
    let $lang := if (exists($ann)) then local:get-lang($ann) else ()
    let $name := if (exists($ann)) then local-name($ann) else ()
    let $wordType := if (exists($ann)) then local:get-id($ann/@type) else ()
    let $ana := if (exists($ann)) then local:get-id($ann/@ana) else ()
    let $function := if (exists($ann)) then local:get-id($ann/@function) else ()
    return
    (
        element { concat($prefix, "ID") } { $id },
        element { concat($prefix, "Text") } { txt:toText($ann) },
        element { concat($prefix, "Lang") } { $lang },
        element { concat($prefix, "Name") } { $name },
        element { concat($prefix, "WordType") } { $wordType },
        element { concat($prefix, "Ana") } { $ana },
        element { concat($prefix, "Function") } { $function }
    )   
};

element switches {
    let $innerAnnElements := ("cl", "phr", "w")
    for $switch at $n in //tei:s//*[local-name(.) = $innerAnnElements and (local:get-lang(.) ne local:get-lang(local:get-parent-ann(.)))]
    let $s := $switch/ancestor::tei:s[1]
    let $parentAnn := local:get-parent-ann($switch)
    
    let $preNodes := txt:outerTextNodes($switch, true(), $s)
    let $preAnn := $preNodes[local-name(.) = $innerAnnElements][1]
    
    let $postNodes := txt:outerTextNodes($switch, false(), $s)
    let $postAnn := $postNodes[local-name(.) = $innerAnnElements][1]
    return
        element switch {
            element fileName { replace (document-uri(/), concat('^.*', "/"), "")  },
            element order { $n },
            element sentID { data($s/@xml:id) },
            element sentText { txt:toText($s) },
            local:get-ann-info($switch, "switch"),
            local:get-ann-info($preAnn, "preSwitch"),
            local:get-ann-info($postAnn, "postSwitch"),
            local:get-ann-info($parentAnn, "parent"),
            local:get-gloss-info($switch)
        }
}


