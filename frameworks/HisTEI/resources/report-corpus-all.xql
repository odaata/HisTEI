xquery version "3.0";

(:~
 : Tokenize an entire collection of TEI documents
 :
 : @author Mike Olson
 : @version 0.1
 :
 : Fields
 : - DocID
 : - Status
 : - StatusDate?
 : - StatusUser?
 : - Date Written
 : - Writer
 : - Location Written
 : - Recipient(s)?
 : - Hands?
 : - Genre
 : - Other catRefs
 : - Title
 : - Word Count
 : - Other Extent Fields? 
 : - FilePath
 : - Notes
 :)

import module namespace txt="http://histei.info/xquery/tei2text" at "tei2text.xqm";
import module namespace utils="http://histei.info/xquery/utils" at "utils.xqm";

declare namespace tei="http://www.tei-c.org/ns/1.0";

declare variable $teiURI as xs:anyURI external;
declare variable $contextualInfoURI as xs:anyURI external;

declare function local:report() as element(corpusAll) {
    element corpusAll {
        for $doc in collection(utils:saxon-collection-uri($teiURI))[exists(tei:TEI)]
        let $docURI := document-uri($doc)
        let $tei := $doc/tei:TEI[1]
        let $header := $tei/tei:teiHeader[1]
        let $status := txt:status($tei)
        let $creation := txt:creation($tei)
        let $yearInfo := txt:year-info($creation/tei:date)
        let $org := $creation/tei:orgName
        let $person := $creation/tei:persName
        let $place := txt:get-places($creation)
        return
            element teiDoc {
                element teiID { substring-after($tei/@xml:id, "TEI_") },
                element idNo { normalize-space($header/tei:fileDesc/tei:publicationStmt/tei:idno[1]/text()) },
                element status { if (empty($status)) then () else string($status/@status) },
                element statusDate { if (empty($status)) then () else string($status/@when) },
                element statusUserID { if (empty($status)) then () else substring-after($status/@status, "person_") },
                element year { $yearInfo("year") },
                element certainty { $yearInfo("cert") },
                element writerID { substring-after($status/@status, "person_") }, 
                element writer { txt:person($person) }
            }
    }
};

declare function local:get-creation-types() as element(typeLabels) {
    let $docs := collection(utils:saxon-collection-uri($teiURI))[exists(tei:TEI)]
    let $dateTypes := distinct-values($docs/tei:TEI//tei:date/@type)
    let $orgTypes := distinct-values($docs/tei:TEI//(tei:orgName | tei:repository)/@type)
    let $personTypes := distinct-values($docs/tei:TEI//(tei:persName | tei:name)/@type)
    let $placeTypes := distinct-values(txt:get-places($docs/tei:TEI//*)/@type)
    return
        element typeLabels {
            element dateTypes { for $type in $dateTypes return element dateType { $type } },
            element orgTypes { for $type in $orgTypes return element orgType { $type } },
            element personTypes { for $type in $personTypes return element personType { $type } },
            element placeTypes { for $type in $placeTypes return element placeType { $type } }
        }
};

(: Windows URIs:)
(:let $teiURI := xs:anyURI("file:/Z:/home/Amsterdam/tokenized"):)

(: Linux/Mac URIs :)
let $teiURI := xs:anyURI("file:/home/mike/Amsterdam/tokenized")
let $contextualInfoURI := xs:anyURI("file:/home/mike/Amsterdam/contextual_info")

let $testDoc := concat($teiURI, "/SAA_00231_Marquette_00366_0000000071.xml")

let $docs := collection(utils:saxon-collection-uri($teiURI))[exists(tei:TEI)]
let $conInfoDocs := collection(utils:saxon-collection-uri($contextualInfoURI))[exists(tei:TEI)]
return
    local:get-creation-types($teiURI)
    (:element places {
        for $place in txt:get-places($docs/tei:TEI//*)
        order by local-name($place)
        return
            element { local-name($place) } {
                $place
            }
    }:)








