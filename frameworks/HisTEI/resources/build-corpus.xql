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

declare function local:update-content-ordered($element as element(), $fieldNames as xs:string+, 
                                                            $newElements as element()*) as element() {
    let $nodes := $element/node()
    let $fieldsMap := map:new(
        for $fieldName in $fieldNames
        return
            map:entry($fieldName, 
                for $node at $pos in $nodes
                return
                    if ($node instance of element() and local-name($node) eq $fieldName) then
                        $pos
                    else
                        ()
            )
    )
    let $startFunc := function($pos as xs:integer) as xs:integer {
        if ($pos eq 1) then 
            1 
        else 
            let $prevLocs := for $n in (1 to $pos - 1) return $fieldsMap($fieldNames[$n])[last()]
            return
                if (exists($prevLocs)) then $prevLocs[last()] + 1 else 1
    }
    let $newNodes := 
        for $fieldName at $pos in $fieldNames
        let $newElement := $newElements[local-name() eq $fieldName]
        
        let $locs := $fieldsMap($fieldName)
        let $oldElement := 
            if (empty($locs)) then
                ()
            else if (count($locs) eq 1) then
                $nodes[$locs[1]]
            else
                subsequence($nodes, $locs[1], ($locs[last()] - $locs[1]) + 1)
        
        let $updatedElement := 
            if (exists($newElement)) then 
            (
                $newElement,
                $oldElement except $oldElement[local-name() eq $fieldName]
            )
            else 
                $oldElement
        
        let $prevNodes := 
            if (empty($locs)) then
                ()
            else
                let $start := $startFunc($pos)
                return
                    subsequence($nodes, $start, $locs[1] - $start)
        return
            ( $prevNodes, $updatedElement )
    return
        element { node-name($element) } {
            $element/@*,
            $newNodes,
            subsequence($nodes, $startFunc(count($fieldNames) + 1))
        }
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
    document {
        element transcriptions {
(:            local:update-content-ordered($testHeader, $headerFieldNames, (<profileDesc/>, <fileDesc/>, <encodingDesc/>)):)
            teix:update-teiHeader((<fileDesc/>, <encodingDesc/>, <profileDesc/>), $testHeader)
            (:for tumbling window $window in $testHeader/node()
            start $first at $firstLoc when true()
            end $last at $lastLoc when contains($last, "}")
            return
                for $n in ($firstLoc to $lastLoc) return $n:)
                    
            (:for $name in utils:get-file-basenames($transDocs)
            order by $name
            return (
                element basename { $name }
            ):)
        }
    }
)
    
    
    
    