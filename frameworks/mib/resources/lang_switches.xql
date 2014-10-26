xquery version "3.0";

import module namespace teix="http://cohd.info/xquery/tei" at "tei.xqm";

declare namespace tei="http://www.tei-c.org/ns/1.0";

declare namespace map="http://www.w3.org/2005/xpath-functions/map";

import module namespace txt="http://cohd.info/xquery/tei2text" at "tei2text.xqm";

declare namespace output = "http://www.w3.org/2010/xslt-xquery-serialization";
declare option output:indent "yes";

declare variable $local:annElements := ("s", "cl", "phr", "w");
declare variable $local:innerAnnElements := subsequence($local:annElements, 2);

declare function local:get-parent-ann($ann as element()?) as element()? {
    if (exists($ann)) then
        $ann/ancestor::*[local-name(.) = $local:annElements][1]
    else
        ()
};

declare function local:get-children-anns($ann as element()?) as element()* {
    if (exists($ann)) then
        $ann//*[local-name(.) = $local:innerAnnElements]
    else
        ()
};

declare function local:get-preceding-ann($ann as element()?) as element()? {
    if (exists($ann)) then
        $ann/preceding::*[local-name() = $local:innerAnnElements][1]
    else
        ()
};

declare function local:get-content-ann($ann as element()?) as element()? {
    if (exists($ann)) then
        $ann/ancestor::tei:s[1]
    else
        ()
};

declare function local:get-lang($node as node()?) as xs:string? {
    if (exists($node)) then
        $node/ancestor-or-self::*[attribute::xml:lang][1]/@xml:lang
    else
        ()
};

declare function local:get-id($field as xs:string?) as xs:string? {
    if (exists($field)) then
        substring-after($field, ":")
    else
        ()
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

declare function local:get-switches($sentences as element()*) as element()* {
    let $switches := 
        (: Loop through all annotations found within sentences and compare lang with preceding annotation :)
        for $ann in $sentences//*[local-name() = $local:innerAnnElements]
        let $annLang := local:get-lang($ann)
        let $preceding := local:get-preceding-ann($ann)
        let $isSwitch := 
            if (exists($preceding)) then 
                ($annLang ne local:get-lang($preceding)) 
            else 
                false()
        (: Only include annotations with children all of the same language or have no children at all (<w/>) :)
        let $isSwitch :=
            if ($isSwitch) then
                let $children := local:get-children-anns($ann)
                return
                    empty($children[local:get-lang(.) ne $annLang])
            else
                false()
        where $isSwitch
        return
            $ann
    (: Finally filter out annotations whose parents are already included immediately before
        This prevents situations, where e.g. a phrase is a switch and by definition the first word in the 
        phrase is returned as a switch because its language is different from preceding annotation as well 
        as its parents
    :)
    for $switch at $n in $switches
    let $parentAnn := local:get-parent-ann($switch)
    return
        if ($n gt 1 and exists($parentAnn) and $parentAnn is $switches[$n - 1]) then
            ()
        else
            $switch
};

declare function local:switches($sentences as element()*, $fileName as xs:string) as element(switches) {
    element switches {
        let $switches := local:get-switches($sentences)
        for $switch at $n in $switches
        let $s := local:get-content-ann($switch)
        let $preceding := local:get-preceding-ann($switch)
        return
            element switch {
                element fileName { $fileName },
                element order { $n },
                element sentID { data($s/@xml:id) },
                element sentText { txt:toText($s) },
                local:get-ann-info($switch, "switch"),
                local:get-ann-info($preceding, "pre"),
                local:get-gloss-info($switch)
            }
    }
};

let $fileName := replace (document-uri(/), '^.*/', "")
let $sentences := //tei:s
return
    local:switches($sentences, $fileName)

