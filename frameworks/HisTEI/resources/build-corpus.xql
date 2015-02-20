xquery version "3.0";

(:~
 : Build the Amsterdam corpus based on values converted to tab delimited text from the Transcriptions.xlm Excel worksheet
 :
 : @author Mike Olson
 : @version 0.1
 :)

import module namespace teix="http://histei.info/xquery/tei" at "tei.xqm";
import module namespace tok="http://histei.info/xquery/tei/tokenizer" at "tokenizer.xqm";
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
                    dateTime(xs:date("2015-01-01"), xs:time("00:00:00"))
                )
    let $changes := 
        for $change in $changes
        let $oldChange := $revisionDesc/tei:change[@status eq $change/@status]
        return
            if (exists($oldChange)) then $oldChange else $change
    let $revisionDesc := teix:element-tei("revisionDesc", 
        ( $revisionDesc/@*, $changes, $revisionDesc/node() except $revisionDesc/tei:change )
    )
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
    let $docs := collection(utils:saxon-collection-uri($dbnlPath))[exists(tei:TEI)]
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
    let $docs := collection(utils:saxon-collection-uri($corpusPath))[exists(tei:TEI)]
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

let $testLetter := doc("/home/mike/Amsterdam/test-letter.xml")
let $testHeader := $testLetter/tei:TEI/tei:teiHeader
let $testElement := <tei:change when="2015-01-01"><!-- Comment here -->This is a test</tei:change>

(:let $transToGet := utils:parse-tab-file($transToGetPath):)
(:let $idNo := normalize-space($tei/tei:TEI/tei:teiHeader/tei:fileDesc/tei:publicationStmt/tei:idno[1]):)
(:let $id := if (starts-with($idNo, "DBNL")) then "DBNL" else replace(normalize-space($transInfo/Family), " ", "_"):)

(:let $transFiles := for $file in file:list($transPath)[ends-with(., $fileExtension)] order by $file return $file
let $basenames := for $file in $transFiles return file:base-name($file, $fileExtension)
let $transDocs := collection($transPath)[exists(tei:TEI) and utils:get-file-basenames(.)]:)
(:let $docs := collection(utils:saxon-collection-uri($dbnlPath))[exists(tei:TEI)]:)
return (
    element corpus {
        element DBNL { local:update-DBNL-catRef() },
        element trans { local:update-trans() },
        tok:tokenize-collection($updatedPath, $tokenizedPath, $userID)
    }
)

(:    local:update-catRef($testLetter/tei:TEI, "Backer"):)
    
(:    local:update-resp(doc("/home/mike/Amsterdam/corpus/UA_00067_Huydecoper_00041_0000000001.xml")/tei:TEI):)

(:    doc("/home/mike/Downloads/INL-Tagged_SAA_00231_Marquette_00366_0000000071.xml"):)
(:    doc("/home/mike/Downloads/INL-Tagged_SAA_00231_Marquette_00366_0000000071.xml")//tei:w[contains(string-join(text(), ""), "vande")]:)




