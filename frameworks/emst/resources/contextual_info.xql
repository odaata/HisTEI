xquery version "3.0";

import module namespace teix="http://cohd.info/xquery/tei" at "tei.xqm";

declare namespace tei="http://www.tei-c.org/ns/1.0";

declare namespace map="http://www.w3.org/2005/xpath-functions/map";

(:declare variable $quote := "&#34;";:)

declare function local:list($list as element()) as element(item)* {
    for $node in $list/element()
    let $id := data($node/@xml:id)
    let $type := data($list/@type)
    return
(:    Fix this bit down here... :)
        typeswitch ($node) 
        case element(tei:person)
            return local:item($id, $type, teix:person($node), ()) 
        case element(tei:place)
            return local:item($id, $type, teix:place($node), ()) 
        case element(tei:org)
            return local:item($id, $type, teix:org($node), ()) 
        case element(tei:category)
            return teix:category($node)
        default
            return
            if (starts-with(local-name($node), "list")) then
                local:list($node)
            else
                ()
};

declare function local:item($id as xs:string, $type as xs:string?, 
    $label as xs:string?, $tooltip as xs:string?) as element(item) {
    
    element item { 
        attribute value { $id },
        if ($type != "") then attribute type { $type } else (),
        element label { if ($label != "") then normalize-space($label) else $id },
        if ($tooltip != "") then element tooltip { normalize-space($tooltip) } else ()
    }
};

declare function local:taxonomy($taxonomy as element()) as element(item)* {
    for $node in $taxonomy/element()
    let $id := data($node/@xml:id)
    let $type := data($taxonomy/@xml:id)
    return
        typeswitch ($node) 
        case element(tei:category) return
            if (exists($node/tei:category)) then
                local:taxonomy($node)
            else
                let $labels-tooltips := teix:category($node)
                return
                    local:item($id, $type, $labels-tooltips[1], $labels-tooltips[2])
        default
            return ()    
};

let $taxonomy := //tei:classDecl/tei:taxonomy[1]
let $items := 
    if (exists($taxonomy/tei:category)) then
        local:taxonomy($taxonomy)
    else
        local:list(//tei:body)
return
    for $item in $items
    order by $item/label/text()
    return $item    

