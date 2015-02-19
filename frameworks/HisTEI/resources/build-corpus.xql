xquery version "3.0";

(:~
 : Build the Amsterdam corpus based on values converted to tab delimited text from the Transcriptions.xlm Excel worksheet
 :
 : @author Mike Olson
 : @version 0.1
 :)

import module namespace teix="http://histei.info/xquery/tei" at "tei.xqm";
import module namespace utils="http://histei.info/xquery/utils" at "utils.xqm";

declare namespace file="http://expath.org/ns/file";
declare namespace map="http://www.w3.org/2005/xpath-functions/map";
declare namespace tei="http://www.tei-c.org/ns/1.0";

declare variable $userID as xs:string external := "MJO";
declare variable $fileExtension as xs:string external := ".xml";

declare variable $transToGetPath := "/home/mike/Amsterdam/Transcriptions.txt";
declare variable $transPath := "/home/mike/Amsterdam/transcriptions";
declare variable $corpusPath := "/home/mike/Amsterdam/corpus";
declare variable $updatedPath := "/home/mike/Amsterdam/updated";

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

declare function local:update-resp($tei as element(tei:TEI)) {
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
    let $titleStmt := teix:update-titleStmt($titleStmt, $respStmts)
    let $fileDesc := teix:update-fileDesc($fileDesc, $titleStmt)
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
    let $revisionDesc := teix:update-revisionDesc($changes, $header/tei:revisionDesc)
    let $header := teix:update-teiHeader($header, ($fileDesc, $revisionDesc))
    return
        teix:update-TEI($tei, $header)
};

let $headerFieldNames := ("fileDesc", "encodingDesc", "profileDesc", "revisionDesc")

let $testLetter := doc("/home/mike/Amsterdam/test-letter.xml")
let $testHeader := $testLetter/tei:TEI/tei:teiHeader
let $testElement := <tei:change when="2015-01-01"><!-- Comment here -->This is a test</tei:change>

let $transToGet := utils:parse-tab-file($transToGetPath)

(:let $transFiles := for $file in file:list($transPath)[ends-with(., $fileExtension)] order by $file return $file
let $basenames := for $file in $transFiles return file:base-name($file, $fileExtension)
let $transDocs := collection($transPath)[exists(tei:TEI) and utils:get-file-basenames(.)]:)
return (
    
(:    local:update-resp(doc("/home/mike/Amsterdam/corpus/UA_00067_Huydecoper_00041_0000000001.xml")/tei:TEI):)


(:    doc("/home/mike/Downloads/INL-Tagged_SAA_00231_Marquette_00366_0000000071.xml"):)
(:    doc("/home/mike/Downloads/INL-Tagged_SAA_00231_Marquette_00366_0000000071.xml")//tei:w[contains(string-join(text(), ""), "vande")]:)

    element category {
        attribute xml:id { "AMST_GROUPS" },
        for $family in distinct-values($transToGet/Family/normalize-space(text()))
        return
            <category xml:id="{replace($family, "\s+", "_")}">
                <catDesc>{$family}</catDesc>
            </category> 
        (:for $name in utils:get-file-basenames($transDocs)
        order by $name
        return (
            element basename { $name }
        ):)
    }
)
    
    
    
    