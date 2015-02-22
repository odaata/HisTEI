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

(: Errors :)
(: Raised by rpt:header() if either the $teiPath or $contextualInfoPath is empty or invalid :)
declare %private variable $rpt:INVALID_PATH_ERROR := QName("http://histei.info/xquery/reports/error", "InvalidPathError");

(:~
 : Generate a tabular report with header values from all TEI documents in a given collection
 : 
 : @param $teiPath The path to the collection. Can be a string or uri. A string is assumed to be a path
 :      and as such is converted to a URI beforehand
 : @param $contextualInfoPath Path to the set of Contextual Info files. Can be a string or uri. A string is assumed to be a path
 :      and as such is converted to a URI beforehand
 : @return Set of teiDoc elements - one for every document in the given TEI collection 
 : @error If either $teiPath or $contextualInfoPath are invalid paths, an InvalidPathError is thrown
:)
declare function rpt:headers($teiPath as xs:anyAtomicType?, $contextualInfoPath as xs:anyAtomicType?) as element()* {
    let $uri := utils:saxon-collection-uri($teiPath)
    let $conInfoMap := teix:get-contextual-info-docs($contextualInfoPath)
    return
        if (empty($uri) or empty(map:keys($conInfoMap))) then
            error($rpt:INVALID_PATH_ERROR, concat("Either the TEI Path or the Contextual Info Path is invalid! ",
                "Given paths: teiPath: ", $teiPath, " contextualInfoPath: ", $contextualInfoPath))
        else
            let $teiDocs := collection($uri)[exists(tei:TEI)]
            let $genreSchemeRefs := 
                distinct-values($teiDocs//tei:profileDesc/tei:textClass/tei:catRef[not(txt:is-genre(.))]/@scheme)
            
            for $doc in $teiDocs
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
declare function rpt:get-creation-types($teiPath as xs:anyURI) as element(typeLabels) {
    let $uri := utils:saxon-collection-uri($teiPath)
    return
        if (empty($uri)) then
            error($rpt:INVALID_PATH_ERROR, concat("The TEI Path is invalid! teiPath: ", $teiPath))
        else
            let $docs := collection(utils:saxon-collection-uri($teiPath))[exists(tei:TEI)]
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










