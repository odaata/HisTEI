xquery version "3.0";

(:~
 : Helper utilities for generating HisTEI reports
 : - The primary function, rpt:header(), returns a set of rows with the header data from a collection
 :      Output as text in elements with no namespace (e.g. for importing into Excel)
 :      These fields can be added to searches to create tabular datasheets for further refining/encoding ling. data
 :
 : @author Mike Olson
 : @version 0.1
 :
 : Fields
 : - DocID
 : - idNo
 : - Status
 : - StatusDate
 : - StatusUser
 : - Genre
 : - Other catRefs
 : - Date Written
 : - Date Certainty
 : - WriterID
 : - Writer
 : - Location Written
 : - Org as writer
 : - Hand Count
 : - Title
 : - Word Count
 : - Note Count
 : - Filename
 : - Full URI
 :)
 
module namespace rpt="http://histei.info/xquery/reports";

import module namespace teix="http://histei.info/xquery/tei" at "tei.xqm";
import module namespace txt="http://histei.info/xquery/tei2text" at "tei2text.xqm";
import module namespace utils="http://histei.info/xquery/utils" at "utils.xqm";

declare namespace map="http://www.w3.org/2005/xpath-functions/map";
declare namespace tei="http://www.tei-c.org/ns/1.0";

(:~
 : Generate the fields for a set of TEI event elements (i.e. birth, death, event)
 : 
 : @param $conInfoMap Map of contextual info document-nodes
 : @param $events Set of events from a contextual info record to generate headers for
 : @return Set of elements including a Year, YearCert, and Place for each event in the given set 
:)
declare function rpt:events($conInfoMap as map(xs:string, document-node()), $events as element()*) as element()* {
    for $event in $events
    let $elementName := local-name($event)
    let $elementName := if ($elementName eq "event") then string($event/@type) else $elementName
    let $yearInfo := txt:year-info($event)
    let $placeRef := if (utils:attr-exists($event/@where)) then string($event/@where) else ($event/*[txt:is-place(.)])[1]
    return (
        element { concat($elementName, "Year") } { $yearInfo("year") },
        element { concat($elementName, "YearCert") } { $yearInfo("cert") }, 
        element { concat($elementName, "Place") } { txt:place(teix:con-info-by-ref($conInfoMap, $placeRef)) }
    )
};

(:~
 : Generate a tabular report with header values from all TEI documents in a given collection
 : - It's best to do all documents in a collection at once!
 : 
 : @param $teiDocs Set of TEI documents to return headers for
 : @param $conInfoMap Map of contextual info document-nodes
 : @return Set of teiDoc elements - one for every document in the given set of TEI documents 
:)
declare function rpt:headers($teiDocs as document-node()*, $conInfoMap as map(xs:string, document-node())) as element(teiDoc)* {
            let $genreSchemeRefs := 
                distinct-values($teiDocs//tei:profileDesc/tei:textClass/tei:catRef[not(txt:is-genre(.))]/@scheme)
            
            for $doc in $teiDocs
            let $docURI := document-uri($doc)
            let $tei := $doc/tei:TEI[1]
            let $header := $tei/tei:teiHeader[1]
            
            let $teiID := substring-after($tei/@xml:id, "TEI_")
            let $idNo := normalize-space($header/tei:fileDesc/tei:publicationStmt/tei:idno[1]/text())
            
            let $status := txt:status($tei)
        
            let $genre := txt:genre($tei) 
            let $genreCat := teix:con-info-by-ref($conInfoMap, $genre)
            
            let $catRefs := $header/tei:profileDesc/tei:textClass/tei:catRef
            let $otherGenres := 
                let $otherCatRefs := $catRefs except $genre
                for $schemeRef in $genreSchemeRefs
                let $otherGenre := 
                    teix:con-info-by-ref($conInfoMap, $otherCatRefs[@scheme eq $schemeRef][1])
                order by $schemeRef
                return
                    element { teix:split-ref($schemeRef)[2] } { txt:category($otherGenre)[1] }
                    
            let $creation := txt:creation($tei)
            let $yearInfo := txt:year-info($creation[local-name() eq "date"])
            
            let $org := teix:con-info-by-ref($conInfoMap, $creation[local-name() eq "orgName"][1])
            
            let $person := teix:con-info-by-ref($conInfoMap, $creation[local-name() eq "persName"][1])
            let $birth := $person/tei:birth
            let $birth := if (empty($birth)) then teix:element-tei("birth", ()) else $birth
            let $death := $person/tei:death
            let $death := if (empty($death)) then teix:element-tei("death", ()) else $death
            
            let $place := teix:con-info-by-ref($conInfoMap, $creation[txt:is-place(.)][1])
            
            order by $idNo, $teiID
            return
                element teiDoc {
                    element teiID { $teiID },
                    element idno { $idNo },
                    element genre { txt:category($genreCat)[1] },
                    $otherGenres,
                    element year { $yearInfo("year") },
                    element certainty { $yearInfo("cert") },
                    element place { txt:place($place) },
                    element wordCount { data($header/tei:fileDesc/tei:extent/tei:measure[@unit eq "words"]/@quantity) },
                    element org { txt:org($org) },
                    element writerID { substring-after($person/@xml:id[1], "person_") }, 
                    element writer { txt:person($person) },
                    rpt:events($conInfoMap, ( $birth, $death )),
                    element handCount { count($header/tei:profileDesc/tei:handNotes/tei:handNote) },
                    element noteCount { count($header/tei:fileDesc/tei:notesStmt/tei:note) },
                    element title { normalize-space($header/tei:fileDesc/tei:titleStmt/tei:title[1]) },
                    element fileName { utils:filenames($doc) },
                    element fullURI { document-uri($doc) },
                    element status { txt:id-as-label($status/@status) },
                    element statusDate { data($status/@when) },
                    element statusEditor { txt:person(teix:con-info-by-ref($conInfoMap, $status)) }
                }
};

(:~
 : Generate a tabular report of all @type attributes in use on contextual info elements (i.e. dates, orgs, people, places)
 : 
 : @param $teiPath The path to the collection. Can be a string or uri. A string is assumed to be a path
 :      and as such is converted to a URI beforehand
 : @return A typeLabels element containing an element for each contextual info element type (i.e. dateTypes, orgTypes, personTypes, placeTypes)
 :      Each element then contains a list of types in use 
 : @error If $teiPath is invalid, an InvalidPathError is thrown
:)
declare function rpt:get-creation-types($teiURI as xs:anyURI) as element(typeLabels) {
    let $docs := teix:collection($teiURI)
    let $dateTypes := distinct-values($docs/tei:TEI//tei:date/@type)
    let $orgTypes := distinct-values($docs/tei:TEI//(tei:orgName | tei:repository)/@type)
    let $personTypes := distinct-values($docs/tei:TEI//(tei:persName | tei:name)/@type)
    let $placeTypes := distinct-values($docs/tei:TEI//*[txt:is-place(.)]/@type)
    return
        element typeLabels {
            element dateTypes { for $type in $dateTypes return element dateType { $type } },
            element orgTypes { for $type in $orgTypes return element orgType { $type } },
            element personTypes { for $type in $personTypes return element personType { $type } },
            element placeTypes { for $type in $placeTypes return element placeType { $type } }
        }
};










