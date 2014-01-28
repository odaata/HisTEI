xquery version "3.0";

import module namespace teix="http://cohd.info/xquery/tei" at "tei.xqm";

declare namespace tei="http://www.tei-c.org/ns/1.0";

declare namespace map="http://www.w3.org/2005/xpath-functions/map";

declare variable $quote := "&#34;";

declare function local:list($list as element()) as element()* {
    for $item in $list/element()
    let $id := data($item/@xml:id)
    let $label := 
        typeswitch ($item) 
        case element(tei:person)
            return teix:person($item) 
        case element(tei:place)
            return teix:place($item)
        case element(tei:org)
            return teix:org($item)
        default
            return ""
    order by $label
    return 
        element item { 
            attribute value { $id },
            attribute label { $label }
        }
};

local:list(//tei:body/element())
