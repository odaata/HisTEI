xquery version "3.0";

(:~
 : Build the Amsterdam corpus based on values converted to tab delimited text from the Transcriptions.xlm Excel worksheet
 : - Update catRef with scheme AMST_GROUPS for every text, so the Amsterdam groupings are available for searches
 : - Update responsibility with references to editors Jamie & Mike for transcriptions
 : - Update revisionDesc with a change event for transcription and correction events for transcriptions
 : - Update all places, inserting a place based on the first dateline element encountered, or Amsterdam as a default
 : - Finally, tokenize everything into the tokenized directory
 :
 : @author Mike Olson
 : @version 0.1
 :)

import module namespace teix="http://histei.info/xquery/tei" at "tei.xqm";
import module namespace tok="http://histei.info/xquery/tei/tokenizer" at "tokenizer.xqm";
import module namespace txt="http://histei.info/xquery/tei2text" at "tei2text.xqm";
import module namespace utils="http://histei.info/xquery/utils" at "utils.xqm";

declare namespace file="http://expath.org/ns/file";
declare namespace map="http://www.w3.org/2005/xpath-functions/map";
declare namespace tei="http://www.tei-c.org/ns/1.0";

declare variable $userID as xs:string external := "MJO";
declare variable $fileExtension as xs:string external := ".xml";

declare variable $transToGetPath := "/home/mike/Amsterdam/Transcriptions.txt";
declare variable $transPath := "/home/mike/Amsterdam/transcriptions";
declare variable $corpusPath := "/home/mike/Amsterdam/corpus";
declare variable $dbnlPath := "/home/mike/Amsterdam/dbnl/tei";
declare variable $updatedPath := "/home/mike/Amsterdam/updated";
declare variable $tokenizedPath := "/home/mike/Amsterdam/tokenized";

declare variable $contextualInfoPath := "/home/mike/Amsterdam/contextual_info";

declare function local:move-corrected-to-corpus($transToGet as element(row)*) as element(fileMoved)* {
    if (empty($transPath) or empty($corpusPath)) then
        ()
    else
        for $trans in $transToGet[Status/text() eq "Corrected"]
        let $path := utils:resolve-path($transPath, concat($trans/Images/text(), $fileExtension))
        return
        if (file:is-file($path)) then
            (
                file:move($path, $corpusPath),
                element fileMoved { attribute corpusPath { $corpusPath }, element transPath { $path }}
            )
        else
            ()
};

declare function local:update-resp($tei as element(tei:TEI)) as element(tei:TEI) {
    let $header := $tei/tei:teiHeader
    let $fileDesc := $header/tei:fileDesc
    let $titleStmt := $fileDesc/tei:titleStmt
    let $respStmts := 
        for $respStmt in $titleStmt/tei:respStmt
        let $key := string($respStmt/tei:resp/@key)
        let $resp := normalize-space($respStmt/tei:resp/text())
        let $name := normalize-space($respStmt/tei:name/text())
        return
            switch(true())
            case contains($name, "Jamie") return
                teix:respStmt("trc", "JN", "Jamie Nelemans", "Transcriber")
            default return
                teix:respStmt($key, "MJO", "Mike Olson", $resp)
    let $respStmts := 
        if (exists($respStmts)) then 
            $respStmts 
        else 
            (teix:respStmt("trc", "JN", "Jamie Nelemans", "Transcriber"), teix:respStmt("crr", "MJO", "Mike Olson", "Corrector"))
    let $titleStmt := teix:update-titleStmt($titleStmt, $respStmts)
    let $fileDesc := teix:update-fileDesc($fileDesc, $titleStmt)
    
    let $revisionDesc := $header/tei:revisionDesc
    let $changes := 
        for $respStmt in $respStmts
        return
            switch($respStmt/tei:resp/@key) 
            case "trc" return 
                teix:change(
                    "transcribed", 
                    "Transcribed from facsimile", 
                    substring-after($respStmt/tei:name/@ref, "_"), 
                    dateTime(xs:date("2014-01-01"), xs:time("00:00:00"))
                )
            default return
                teix:change(
                    "corrected", 
                    "Corrected from facsimile", 
                    substring-after($respStmt/tei:name/@ref, "_"), 
                    dateTime(xs:date("2015-02-01"), xs:time("00:00:00"))
                )
    let $changes := 
        for $change in $changes
        let $oldChange := $revisionDesc/tei:change[@status eq $change/@status]
        return
            if (exists($oldChange)) then $oldChange else $change
    let $allChanges := 
        for $node in ($changes, $revisionDesc/node() except $revisionDesc/tei:change[@status = $changes/@status])
        order by if ($node instance of element(tei:change)) then data($node/@when) else ()
        return
            $node
    let $revisionDesc := teix:element-tei("revisionDesc", ( $revisionDesc/@*, $allChanges ))
    let $header := teix:update-teiHeader($header, ($fileDesc, $revisionDesc))
    return
        teix:update-TEI($tei, $header)
};

declare function local:update-catRef($tei as element(tei:TEI), $group as xs:string) as element(tei:TEI) {
    let $group := replace(normalize-space($group), " ", "_")
    let $header := $tei/tei:teiHeader
    let $profileDesc := $header/tei:profileDesc[1]
    let $textClass := $profileDesc/tei:textClass[1]
    let $textClass := utils:replace-content($textClass,
        (
            $textClass/node(),
            teix:catRef($group, "AMST_GROUPS")
        )
    )
    let $profileDesc := teix:update-profileDesc($profileDesc, $textClass)
    let $header := teix:update-teiHeader($header, $profileDesc)
    return
        teix:update-TEI($tei, $header)
};

declare function local:update-DBNL-catRef() {
    let $docs := teix:collection(utils:saxon-collection-uri($dbnlPath))
    return (
        element transcriptions {
            attribute dbnlPath { $dbnlPath },
            attribute updatedPath { $updatedPath },
            attribute total { count($docs) },
            for $doc in $docs
            let $updatedTEI := local:update-catRef($doc/tei:TEI[1], "DBNL")
            let $newFilePath := utils:write-transformation($dbnlPath, $updatedPath, $doc, $updatedTEI)
            order by $newFilePath
            return
                element file { 
                     element path { 
                         $newFilePath
                     }
                }
        }
    )
};

declare function local:update-trans() {
    let $transToGet := utils:parse-tab-file($transToGetPath)
    let $docs := teix:collection(utils:saxon-collection-uri($corpusPath))
    return (
        element transcriptions {
            attribute corpusPath { $corpusPath },
            attribute updatedPath { $updatedPath },
            attribute total { count($docs) },
            for $doc in $docs
            let $basename := utils:get-file-basenames($doc)
            let $family := $transToGet[Images/text() eq $basename]/Family/text()
            let $updatedTEI := local:update-catRef($doc/tei:TEI[1], $family)
            let $updatedTEI := local:update-resp($updatedTEI)
            let $newFilePath := utils:write-transformation($corpusPath, $updatedPath, $doc, $updatedTEI)
            order by $newFilePath
            return
                element file { 
                     element path { 
                         $newFilePath
                     }
                }
        }
    )
};

declare function local:update-places() as element(placesAdded) {
    let $uniquePlaceNames := 
        <firstPlaces>
            <placeName origPlaceName="Alckmaer" newPlaceName="Alckmaar"/>
            <placeName origPlaceName="Amsterdam" newPlaceName="Amsterdam"/>
            <placeName origPlaceName="Breda" newPlaceName="Breda"/>
            <placeName origPlaceName="Breukelen" newPlaceName="Breukelen"/>
            <placeName origPlaceName="Bruxelles" newPlaceName="Brussel"/>
            <placeName origPlaceName="GENEVEN" newPlaceName="Genève"/>
            <placeName origPlaceName="Geneve" newPlaceName="Genève"/>
            <placeName origPlaceName="Geneven" newPlaceName="Genève"/>
            <placeName origPlaceName="Hage" newPlaceName="Den Haag"/>
            <placeName origPlaceName="Leijden" newPlaceName="Leiden"/>
            <placeName origPlaceName="Leyden" newPlaceName="Leiden"/>
            <placeName origPlaceName="Limburc" newPlaceName="Limburg"/>
            <placeName origPlaceName="Marseille" newPlaceName="Marseille"/>
            <placeName origPlaceName="Marzeille" newPlaceName="Marseille"/>
            <placeName origPlaceName="Napoli" newPlaceName="Napels"/>
            <placeName origPlaceName="S' Graven Hag." newPlaceName="Den Haag"/>
            <placeName origPlaceName="Utrecht" newPlaceName="Utrecht"/>
            <placeName origPlaceName="Venetia" newPlaceName="Venetië"/>
            <placeName origPlaceName="Voortwyk" newPlaceName="Voortwijck bij Breukelen"/>
            <placeName origPlaceName="hage" newPlaceName="Den Haag"/>
            <placeName origPlaceName="in Geneven" newPlaceName="Genève"/>
            <placeName origPlaceName="venetia" newPlaceName="Venetië"/>
            <placeName origPlaceName="venetie" newPlaceName="Venetië"/>
            <placeName origPlaceName="voors wijk" newPlaceName="Voortwijck bij Breukelen"/>
        </firstPlaces>
    
    let $docs := teix:collection(utils:saxon-collection-uri($corpusPath))
    let $conInfoMap := teix:con-info-docs-map($contextualInfoPath)
    let $places := $conInfoMap("plc")//tei:place
    return
        element placesAdded {
            attribute corpusPath { $corpusPath },
            attribute updatedPath { $updatedPath },
            attribute total { count($docs) },
            
            for $doc in $docs
            let $tei := $doc/tei:TEI[1]
            let $genre := txt:genre($tei)
            
            let $amsterdamPlace := teix:element-tei("settlement", $places[normalize-space(tei:placeName) eq "Amsterdam"]/@xml:id )
            let $creationPlace := txt:creation($tei)[txt:is-place(.)]
            let $newPlace :=
                if (utils:attr-exists($creationPlace/@ref)) then
                    ()
                else
                    let $dateLinePlace := ($tei/tei:text//tei:dateline//*[txt:is-place(.)])[1]
                    let $dateLinePlaceName := normalize-space($dateLinePlace)
                    
                    let $newPlaceName := $uniquePlaceNames/*[@origPlaceName eq $dateLinePlaceName]/@newPlaceName
                    let $place := $places[normalize-space(tei:placeName) eq $newPlaceName]
                    
                    let $newPlaceElementName := data($place/parent::tei:listPerson/@type)
                    let $newPlaceElementName := if (empty($newPlaceElementName) or $newPlaceElementName eq "") then "settlement" else $newPlaceElementName
                    
                    let $id := if (exists($place)) then $place/@xml:id else $amsterdamPlace/@xml:id
                    let $ref := if (utils:attr-exists($dateLinePlace/@ref)) then data($dateLinePlace/@ref) else concat("plc:", $id) 
                    return
                        teix:element-tei($newPlaceElementName, ( attribute ref { $ref }, attribute type { if ($genre/@target eq "gen:Brief") then "sent" else "written" } ))
            
            let $updatedTEI := 
                if (utils:attr-exists($creationPlace/@ref)) then
                    $tei
                else
                    let $creation := teix:update-creation($tei//tei:creation[1], $newPlace)
                    let $profileDesc := teix:update-profileDesc($tei//tei:profileDesc[1], $creation)
                    let $header := teix:update-teiHeader($tei/tei:teiHeader, $profileDesc)
                    return
                        teix:update-TEI($tei, $header)
            
            let $newFilePath := utils:write-transformation($corpusPath, $updatedPath, $doc, $updatedTEI)
            order by $newFilePath
            return
                element file { 
                     element path { $newFilePath },
                     element newPlace { $newPlace }
                }
    }
};


tok:tokenize-collection($corpusPath, $tokenizedPath, $userID)

(:    doc("/home/mike/Downloads/INL-Tagged_SAA_00231_Marquette_00366_0000000071.xml"):)
(:    doc("/home/mike/Downloads/INL-Tagged_SAA_00231_Marquette_00366_0000000071.xml")//tei:w[contains(string-join(text(), ""), "vande")]:)





