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

import module namespace teix="http://histei.info/xquery/tei" at "tei.xqm";
import module namespace txt="http://histei.info/xquery/tei2text" at "tei2text.xqm";
import module namespace utils="http://histei.info/xquery/utils" at "utils.xqm";

declare namespace map="http://www.w3.org/2005/xpath-functions/map";
declare namespace tei="http://www.tei-c.org/ns/1.0";

declare variable $teiURI as xs:anyURI external;
declare variable $contextualInfoURI as xs:anyURI external;

declare function local:report($teiURI as xs:anyURI, $contextualInfoURI as xs:anyURI) as element(corpusAll) {
    element corpusAll {
        let $uri := utils:saxon-collection-uri($teiURI)
        let $conInfoMap := teix:get-contextual-info-docs($contextualInfoURI)
        return
            if (empty($uri) or empty(map:keys($conInfoMap))) then
                element error { "The given path was invalid or no contextual information was found!" }
            else
                let $docs := collection($uri)[exists(tei:TEI)]
                let $genreSchemeRefs := 
                    distinct-values($docs//tei:profileDesc/tei:textClass/tei:catRef[not(txt:is-genre(.))]/@scheme)
                
                for $doc in $docs
                let $docURI := document-uri($doc)
                let $tei := $doc/tei:TEI[1]
                let $header := $tei/tei:teiHeader[1]
                
                let $teiID := substring-after($tei/@xml:id, "TEI_")
                let $idNo := normalize-space($header/tei:fileDesc/tei:publicationStmt/tei:idno[1]/text())
                order by $idNo, $teiID
                
                let $status := txt:status($tei)
        
                let $genre := txt:genre($tei) 
                let $genreCat := teix:get-contextual-info-by-ref($conInfoMap, $genre/@target)
                
                let $catRefs := $header/tei:profileDesc/tei:textClass/tei:catRef
                let $otherGenres := 
                    let $otherCatRefs := $catRefs except $genre
                    for $schemeRef in $genreSchemeRefs
                    let $otherGenre := 
                        teix:get-contextual-info-by-ref($conInfoMap, $otherCatRefs[@scheme eq $schemeRef]/@target[1])
                    order by $schemeRef
                    return
                        element { teix:split-ref($schemeRef)[2] } { txt:category($otherGenre)[1] }
                        
                let $creation := txt:creation($tei)
                let $yearInfo := txt:year-info($creation[local-name() eq "date"])
                let $org := teix:get-contextual-info-by-ref($conInfoMap, $creation[local-name() eq "orgName"]/@ref[1])
                let $person := teix:get-contextual-info-by-ref($conInfoMap, $creation[local-name() eq "persName"]/@ref[1])
                let $place := teix:get-contextual-info-by-ref($conInfoMap, $creation[txt:is-place(.)][1]/@ref)
                return
                    element teiDoc {
                        element teiID { $teiID },
                        element idNo { $idNo },
                        element status { if (empty($status)) then () else string($status/@status) },
                        element statusDate { if (empty($status)) then () else string($status/@when) },
                        element statusUserID { if (empty($status)) then () else substring-after($status/@who, "person_") },
                        element genre { txt:category($genreCat)[1] },
                        $otherGenres,
                        element year { $yearInfo("year") },
                        element certainty { $yearInfo("cert") },
                        element writerID { substring-after($person/@xml:id[1], "person_") }, 
                        element writer { txt:person($person) },
                        element place { txt:place($place) },
                        element org { txt:org($org) },
                        element numHands { count($header/tei:profileDesc/tei:handNotes/tei:handNote) },
                        element title { normalize-space($header/tei:fileDesc/tei:titleStmt/tei:title[1]) },
                        element wordCount { data($header/tei:fileDesc/tei:extent/tei:measure[@unit eq "words"]/@quantity) },
                        element noteCount { count($header/tei:fileDesc/tei:notesStmt/tei:note) },
                        element fileName { utils:get-filenames($doc) },
                        element fullURI { document-uri($doc) }
                    }
    }
};

declare function local:get-creation-types($teiURI as xs:anyURI) as element(typeLabels) {
    let $docs := collection(utils:saxon-collection-uri($teiURI))[exists(tei:TEI)]
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

(: Windows URIs:)
(:let $teiURI := xs:anyURI("file:/Z:/home/Amsterdam/tokenized"):)

(: Linux/Mac URIs :)
(:let $teiURI := xs:anyURI("file:/home/mike/Amsterdam/tokenized")
let $contextualInfoURI := xs:anyURI("file:/home/mike/Amsterdam/contextual_info")

let $testDoc := doc(concat($teiURI, "/SAA_00231_Marquette_00366_0000000071.xml"))

let $uri := utils:saxon-collection-uri($teiURI)
let $docs := collection($uri)
let $conInfoMap := teix:get-contextual-info-docs($contextualInfoURI)
let $creation := txt:creation($testDoc/tei:TEI)
let $conInfoRecord := teix:get-contextual-info-by-ref($conInfoMap, $creation/tei:persName/@ref[1])
return:)
    local:report($teiURI, $contextualInfoURI)










