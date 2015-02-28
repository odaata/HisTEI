xquery version "3.0";

(:~
 : A set of generic XML helper functions for use in XQuery scripts in the HisTEI framework
 :)
module namespace sax="http://histei.info/xquery/utils/saxon";

import module namespace functx="http://www.functx.com" at "functx.xql";
import module namespace rpt="http://histei.info/xquery/reports" at "reports.xqm";
import module namespace teix="http://histei.info/xquery/tei" at "tei.xqm";
import module namespace tok="http://histei.info/xquery/tei/tokenizer" at "tokenizer.xqm";

declare namespace decoder="java:java.net.URLDecoder";
declare namespace file="http://expath.org/ns/file";
declare namespace map="http://www.w3.org/2005/xpath-functions/map";
declare namespace output="http://www.w3.org/2010/xslt-xquery-serialization";
declare namespace tei="http://www.tei-c.org/ns/1.0";

(: Errors :)
(: Raised by sax:headers-paths() if either the $teiPath or $contextualInfoPath is empty or invalid :)
declare %private variable $sax:INVALID_PATH_ERROR := QName("http://histei.info/xquery/utils/saxon/error", "InvalidPathError");

declare variable $sax:OUTPUT_NO_INDENT := <output:serialization-parameters><output:indent value="no"/></output:serialization-parameters>;
declare variable $sax:DEFAULT_OUTPUT := $sax:OUTPUT_NO_INDENT;

(: File-related Functions :)

(:~
 : Converts a URI to a native path
 : - Removes file: prefix and replaces the URI / with whatever the platform separator is
 : - If the URI schema is NOT file:, it returns nothing, since there's no native path
 : 
 : @param $uri URI or native path to a local file. 
 : @return If a URI is provided, it is converted, 
 :  everything else is assumed to be a native path and returned as a string
:)
declare function sax:uri-to-path($uri as xs:anyAtomicType?) as xs:string? {
    typeswitch($uri)
    case xs:string return
        $uri
    case xs:anyURI return
        let $uriString := string($uri)
        let $path := 
            if (starts-with($uriString, "file:")) then
                if (file:dir-separator() eq "/") then
                    substring-after($uriString, "file:")
                else
                    replace(substring-after($uriString, "file:/"), "/", 
                        functx:escape-for-regex(file:dir-separator())
                    )
            else
                ()
        return
            decoder:decode($path, "UTF-8")
    default return
        string($uri)
};

(:~
 : Converts a native path to a URI
 : 
 : @param $path Native path to a local file or directory.
 : @return URI to the local file or directory.
:)
declare function sax:path-to-uri($path as xs:string?) as xs:anyURI? {
    if (empty($path) or $path eq "") then
        ()
    else
        let $path := 
            if (file:dir-separator() eq "/") then
                substring($path, 2)
            else
                replace($path, functx:escape-for-regex(file:dir-separator()), "/")
        let $encoded := 
            string-join(
                for $part in tokenize($path, "/")
                return
                    if (matches($part, "^\p{L}+:$")) then
                        $part
                    else
                        encode-for-uri($part)
                , "/"
            )
        return
            xs:anyURI(concat("file:/", $encoded))
};

(:~
 : Appends a filename to a base path using the correct local separator (i.e. (back)slash)
 : 
 : @param $dir Native path to a local directory.
 : @param $file Filename to be appended to the directory path.
 : @return Native path to a local file.
:)
declare function sax:resolve-path($dir as xs:string?, $file as xs:string?) as xs:string? {
    if (empty($dir) or $dir eq "") then
        ()
    else
        let $dir := 
            if (ends-with($dir, file:dir-separator())) then
                $dir
            else
                concat($dir, file:dir-separator())
        return
            concat($dir, $file)
};

declare function sax:resolve-path($dir as xs:string?) as xs:string? {
    sax:resolve-path($dir, ())
};

(:~
 : Gets the parent directory for a native path to a local file
 : 
 : @param $file Native path to a local file.
 : @return Native path to the parent directory for the given file. Ends with a (back)slash.
:)
declare function sax:dir-from-file($file as xs:string?) as xs:string? {
    if (empty($file) or $file eq "") then
        ()
    else
        let $dir := functx:substring-before-last($file, file:dir-separator())
        return
            if (empty($dir) or $dir eq "") then
                ()
            else
                sax:resolve-path($dir)
};

(:~
 : Checks if a path is valid and returns the directory portion as a native file path
 :  If a file is given, the path to the parent directory is returned.
 :  If a path is invalid or does not exist, an empty string is returned.   
 :  Paths are uniformly returned with a (back)slash on the end (to make adding file names to them easier)
 : 
 : @param $path Native path to a local file. Can be a string or uri. A string is assumed to be a native path
 :      while a URI is converted to a native path.
 : @return Native path or URI referring to the directory
:)
declare function sax:dir-path($path as xs:anyAtomicType?) as xs:string? {
    if (empty($path) or $path eq "") then
        ()
    else
        let $path := sax:uri-to-path($path)
        let $path := 
            if (file:is-dir($path)) then
                $path
            else if (file:is-file($path)) then
                sax:dir-from-file($path) 
            else
                ()
        return
            if (empty($path)) then
                ()
            else
                sax:resolve-path($path)
};

(:~
 : Appends Saxon-specific queryParameters to the URI after checking if the directory exists
 : 
 : @param $path The path to the collection. Can be a string or uri. A string is assumed to be a path
 :      and as such is converted to a URI before concatenation
 : @param $recurse Whether the collection should include all subdirectories recursively. Default is true().
 : @param $select The pattern for filtering files contained in a collection directory. Default is XML files: *.(xml|XML).
 : @param $recurse Whether the collection should be treated as unparsed text (for reading text files). Default is false().
 : @return A URI with the Saxon queryParamaters appended, if the collection directory exists
:)
declare function sax:collection-uri($path as xs:anyAtomicType?, $recurse as xs:boolean?, 
                                    $select as xs:string?, $unparsed as xs:boolean?) as xs:anyURI? {
    if (empty($path) or $path eq "") then
        ()
    else
        let $recurse := if (empty($recurse)) then true() else $recurse
        let $select := if (empty($select) or $select eq "") then "*.(xml|XML)" else $select
        let $unparsed := if (empty($unparsed)) then false() else $unparsed
        
        let $recurseParm := if ($recurse) then "recurse=yes" else ()
        let $selectParm := concat("select=", encode-for-uri($select))
        let $unparsed := if ($unparsed) then "unparsed=yes" else ()
        let $queryParms := string-join(( $recurseParm, $selectParm, $unparsed ), ";")
        
        let $uri := sax:path-to-uri(sax:dir-path($path))
        return
            if (exists($uri)) then
                xs:anyURI(concat($uri, "?", $queryParms))
            else
                ()
};

declare function sax:collection-uri($path as xs:anyAtomicType?, $recurse as xs:boolean?, 
                                    $select as xs:string?) as xs:anyURI? {
    sax:collection-uri($path, $recurse, $select, ())
};

declare function sax:collection-uri($path as xs:anyAtomicType?, $recurse as xs:boolean?) as xs:anyURI? {
    sax:collection-uri($path, $recurse, ())
};

declare function sax:collection-uri($path as xs:anyAtomicType?) as xs:anyURI? {
    sax:collection-uri($path, ())
};

(:~
 : Writes a (transformed) document to a target directory, 
 :  maintaining the same directory structure found below the source directory
 : 
 : @param $sourceDir Native path or URI to the source directory where the original document is stored
 : @param $targetDir Native path or URI to the target directory where the transformed document will be written
 : @param $originalDoc Document-node() of the original document before transformation
 : @param $newItems Transformed XML document to be output
 : @param $outputParms Serialization paramaters. If none provided, $sax:DEFAULT_OUTPUT is used
 : @return Native path to a local file.
:)
declare function sax:write-transformation($sourceDir as xs:anyAtomicType, $targetDir as xs:anyAtomicType, 
                                                $originalDoc as document-node(), $newItems,
                                                $outputParms as element(output:serialization-parameters)?) {
    
    let $sourceDir := sax:uri-to-path($sourceDir)
    let $targetDir := sax:uri-to-path($targetDir)
    
    let $newPathEnd := substring-after(sax:uri-to-path(document-uri($originalDoc)), $sourceDir)
    let $newPath := concat($targetDir, $newPathEnd)
    return
        sax:write-file($newPath, $newItems, $outputParms)
};

declare function sax:write-transformation($sourceDir as xs:anyAtomicType, $targetDir as xs:anyAtomicType, 
                                                $originalDoc as document-node(), $newItems) {
    
    sax:write-transformation($sourceDir, $targetDir, $originalDoc, $newItems, ())
};

(:~
 : Writes $items to a local file at the given native path
 : 
 : @param $path Native path or URI to the target file
 : @param $items XML document to be output
 : @param $outputParms Serialization paramaters. If none provided, $sax:DEFAULT_OUTPUT is used
 : @return Native path to the newly written local file.
:)
declare function sax:write-file($path as xs:anyAtomicType, $items, $outputParms as element(output:serialization-parameters)?) {
    let $outputParms := if (empty($outputParms)) then $sax:DEFAULT_OUTPUT else $outputParms
    let $path := sax:uri-to-path($path)
    let $dir := sax:dir-from-file($path)
    return (
        file:create-dir($dir),
        file:write($path, $items, $outputParms),
        $path
    )
};

declare function sax:write-file($path as xs:anyAtomicType, $items) {
    sax:write-file($path, $items, ())
};

(:~
 : Parse a tab-delimited file and return its rows as a collection of maps with the keys being
 :  either header names taken from the first row or the ordinal position of each field
 :  - Field names that are not valid QNames are prefixed with f_ (this is always the case if the file has no headers)
 : 
 : @param $path Native path to the tab-delimited file
 : @param $hasHeaders Whether the first row in the file contains the names of the fields. Default is true().
 : @return Set of row elements containing the fields using headers in the tab-delimited file or ordinal names
:)
declare function sax:parse-tab-file($path as xs:string, $hasHeaders as xs:boolean?) as element(row)* {
    let $hasHeaders := if (empty($hasHeaders)) then true() else $hasHeaders
    let $fieldPrefix := "f_"
    
    let $lines := file:read-text-lines($path)
    let $fieldNames := 
        for $field at $pos in tokenize($lines[1], "\t")
        return
            if ($hasHeaders) then
                let $field := normalize-space($field)
                return
                    if ($field castable as xs:QName) then 
                        $field 
                    else 
                        concat($fieldPrefix, $field)
            else
                concat($fieldPrefix, $pos)
           
    let $body := if ($hasHeaders) then subsequence($lines, 2) else $lines
    
    for $line in $body
    return
        element row {
            for $field at $pos in tokenize($line, "\t")
            let $fieldName := $fieldNames[$pos]
            let $fieldName := if (empty($fieldName) or $fieldName eq "") then concat($fieldPrefix, $pos) else $fieldName
            return
                element { $fieldName } { normalize-space($field) }
        }
};

declare function sax:parse-tab-file($path as xs:string) as element(row)* {
    sax:parse-tab-file($path, ())
};

(: Specific functions that require reading/writing to the disk (i.e. when using Saxon locally in Oxygen) :)

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
declare function sax:headers-paths($teiPath as xs:anyAtomicType?, $contextualInfoPath as xs:anyAtomicType?) as element()* {
    let $uri := sax:collection-uri($teiPath)
    let $conInfoMap := teix:con-info-docs-map(sax:collection-uri($contextualInfoPath))
    return
        if (empty($uri) or empty(map:keys($conInfoMap))) then
            error($sax:INVALID_PATH_ERROR, concat("Either the TEI Path or the Contextual Info Path is invalid! ",
                "Given paths: teiPath: ", $teiPath, " contextualInfoPath: ", $contextualInfoPath))
        else
            rpt:headers(teix:collection($uri), $conInfoMap)
};

declare function sax:tokenize-collection($teiURI as xs:anyAtomicType, $tokenizedURI as xs:anyAtomicType, $userID as xs:string?) as element() {
    let $teiPath := sax:dir-path($teiURI)
    let $tokenizedPath := sax:dir-path($tokenizedURI)
    return
        element tokenizedFiles {
                attribute userID { $userID },
                if (exists($teiPath) and exists($tokenizedPath)) then
                    let $docs := teix:collection(sax:collection-uri($teiPath))
                    return
                    (
                        attribute teiPath { $teiPath },
                        attribute tokenizedPath { $tokenizedPath },
                        attribute total { count($docs) },
                        for $doc in $docs
                        let $tokenizedTrans := tok:tokenize($doc/tei:TEI[1], $userID)
                        let $newFilePath := sax:write-transformation($teiPath, $tokenizedPath, $doc, $tokenizedTrans)
                        order by $newFilePath
                        return
                            element file { 
                                element path {$newFilePath },
                                element wordCount { data(($tokenizedTrans//tei:fileDesc/tei:extent/tei:measure[@unit eq "words"]/@quantity)[1]) }
                            }
                    )
                else
                (
                    element transDir { attribute invalid { empty($teiPath) }, $teiURI },
                    element tokenizedDir { attribute invalid { empty($tokenizedPath) }, $tokenizedURI },
                    element error { "The provided directories are invalid!" }
                )
        }
};







