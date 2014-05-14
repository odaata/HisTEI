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
        typeswitch ($node) 
        case element(tei:person)
            return local:item($id, $type, teix:person($node), ()) 
        case element(tei:place)
            return local:item($id, $type, teix:place($node), ()) 
        case element(tei:org)
            return local:item($id, $type, teix:org($node), ()) 
        case element(tei:category)
            return teix:category($node)
        default return
            if (local-name($node) = ("listPerson", "listPlace", "listOrg")) then
                local:list($node)
            else
                ()
};

declare function local:item($id as xs:string?, $type as xs:string?, 
    $label as xs:string?, $tooltip as xs:string?) as element(item)? {
    
    if ($id != "") then
        element item { 
            attribute value { $id },
            if ($type != "") then attribute type { $type } else (),
            element label { if ($label != "") then normalize-space($label) else $id },
            if ($tooltip != "") then element tooltip { normalize-space($tooltip) } else ()
        }
    else
        ()
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
                let $label-tooltip := teix:category($node)
                return
                    local:item($id, $type, $label-tooltip[1], $label-tooltip[2])
        default
            return ()    
};

declare function local:interpGrp($nodes as element(tei:interpGrp)+) as element(item)* {
    for $interpGrp in $nodes
    let $type := data($interpGrp/@type)
    
    for $interp in $interpGrp/element(tei:interp)
    let $id := data($interp/@xml:id)
    let $label := $id
    let $tooltip := data(normalize-space($interp/text()))
    return
        local:item($id, $type, $label, $tooltip)
};

let $taxonomy := //tei:classDecl/tei:taxonomy[1]
let $interpGrps := //tei:interpGrp
let $items := 
    switch(true())
    case exists($taxonomy/tei:category) return 
(:    if (exists($taxonomy/tei:category)) then:)
        local:taxonomy($taxonomy)
    case exists($interpGrps) return
        local:interpGrp($interpGrps)
(:    else:)
    default return 
        local:list(//tei:body)
return
    for $item in $items
    order by $item/@type, $item/label/text()
    return $item    

