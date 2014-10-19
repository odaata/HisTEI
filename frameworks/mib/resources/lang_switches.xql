xquery version "3.0";

import module namespace teix="http://cohd.info/xquery/tei" at "tei.xqm";

declare namespace tei="http://www.tei-c.org/ns/1.0";

declare namespace map="http://www.w3.org/2005/xpath-functions/map";

declare function local:get-parent-ann($currentAnn as element()) as element() {
    $currentAnn/ancestor::*[local-name(.) = ("s", "cl", "phr", "w")][1]
};

declare function local:get-lang($element as element()) as xs:string? {
    $element/ancestor-or-self::*[attribute::xml:lang][1]/@xml:lang
};

element switches {
    for $switch at $n in //tei:s//*[local-name(.) = ("cl", "phr", "w") and (local:get-lang(.) ne local:get-lang(local:get-parent-ann(.)))]
    let $s := $switch/ancestor::tei:s[1]
    let $gloss := $switch/ancestor::tei:gloss[1]
    let $parentAnn := local:get-parent-ann($switch)
    return
        element switch {
            element fileName { replace (document-uri(/), concat('^.*', "/"), "")  },
            element order { $n },
            element sentID { data($s/@xml:id) },
            element sentText { normalize-space($s) },
            element annID { data($switch/@xml:id) },
            element annText { normalize-space($switch) },
            element annLang { local:get-lang($switch) },
            element annType { local-name($switch) },
            element ana { substring-after(data($switch/@ana), "ann:") },
            element function { substring-after(data($switch/@function), "ann:") },
            element parentID { data($parentAnn/@xml:id) },
            element parentType { local-name($parentAnn) },
            element parentText { normalize-space($parentAnn) },
            element parentLang { local:get-lang($parentAnn) }
        }
}


