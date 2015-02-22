xquery version "3.0";

(:~
 : A set of generic XML helper functions for use in XQuery scripts in the HisTEI framework
 :)
module namespace utils="http://histei.info/xquery/utils";

import module namespace functx="http://www.functx.com" at "functx.xql";

declare namespace file="http://expath.org/ns/file";
declare namespace map="http://www.w3.org/2005/xpath-functions/map";
declare namespace output="http://www.w3.org/2010/xslt-xquery-serialization";
declare namespace tei="http://www.tei-c.org/ns/1.0";

declare namespace decoder="java:java.net.URLDecoder";

(: Errors :)
(: Raised by utils:update-content-ordered() if no fieldNames are provided either as xs:string+ or map(xs:string, xs:string+) :)
declare %private variable $utils:NO_FIELD_NAMES_ERROR := QName("http://histei.info/xquery/utils/error", "NoFieldNamesError");

declare variable $utils:OUTPUT_NO_INDENT := <output:serialization-parameters><output:indent value="no"/></output:serialization-parameters>;
declare variable $utils:DEFAULT_OUTPUT := $utils:OUTPUT_NO_INDENT;

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
declare function utils:uri-to-path($uri as xs:anyAtomicType?) as xs:string? {
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
declare function utils:path-to-uri($path as xs:string?) as xs:anyURI? {
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
 : Gets the document URI and then returns the filename portion of the path for each input document-node()
 : 
 : @param $docs Set of document nodes (e.g. from a call to collection())
 : @return Set of filenames, one for each document-node in $docs
:)
declare function utils:get-filenames($docs as document-node()*) as xs:string* {
    for $doc in $docs
    return
        decoder:decode(functx:substring-after-last(string(document-uri($doc)), "/"), "UTF-8")
};

(:~
 : Gets the document URI and then returns the base portion (without the extension) of the filename for each input document-node()
 : 
 : @param $docs Set of document nodes (e.g. from a call to collection())
 : @return Set of file basenames (without extension), one for each document-node in $docs
:)
declare function utils:get-file-basenames($docs as document-node()*) as xs:string* {
    let $filenames := utils:get-filenames($docs)
    for $filename in $filenames
    return
        functx:substring-before-last($filename, ".")
};

(:~
 : Appends a filename to a base path using the correct local separator (i.e. (back)slash)
 : 
 : @param $dir Native path to a local directory.
 : @param $file Filename to be appended to the directory path.
 : @return Native path to a local file.
:)
declare function utils:resolve-path($dir as xs:string?, $file as xs:string?) as xs:string? {
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

declare function utils:resolve-path($dir as xs:string?) as xs:string? {
    utils:resolve-path($dir, ())
};

(:~
 : Gets the parent directory for a native path to a local file
 : 
 : @param $file Native path to a local file.
 : @return Native path to the parent directory for the given file. Ends with a (back)slash.
:)
declare function utils:get-dir-from-file($file as xs:string?) as xs:string? {
    if (empty($file) or $file eq "") then
        ()
    else
        let $dir := functx:substring-before-last($file, file:dir-separator())
        return
            if (empty($dir) or $dir eq "") then
                ()
            else
                utils:resolve-path($dir)
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
declare function utils:get-dir-path($path as xs:anyAtomicType?) as xs:string? {
    if (empty($path) or $path eq "") then
        ()
    else
        let $path := utils:uri-to-path($path)
        let $path := 
            if (file:is-dir($path)) then
                $path
            else if (file:is-file($path)) then
                utils:get-dir-from-file($path) 
            else
                ()
        return
            if (empty($path)) then
                ()
            else
                utils:resolve-path($path)
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
declare function utils:saxon-collection-uri($path as xs:anyAtomicType?, $recurse as xs:boolean?, 
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
        
        let $uri := utils:path-to-uri(utils:get-dir-path($path))
        return
            if (exists($uri)) then
                xs:anyURI(concat($uri, "?", $queryParms))
            else
                ()
};

declare function utils:saxon-collection-uri($path as xs:anyAtomicType?, $recurse as xs:boolean?, 
                                    $select as xs:string?) as xs:anyURI? {
    utils:saxon-collection-uri($path, $recurse, $select, ())
};

declare function utils:saxon-collection-uri($path as xs:anyAtomicType?, $recurse as xs:boolean?) as xs:anyURI? {
    utils:saxon-collection-uri($path, $recurse, ())
};

declare function utils:saxon-collection-uri($path as xs:anyAtomicType?) as xs:anyURI? {
    utils:saxon-collection-uri($path, ())
};

(:~
 : Writes a (transformed) document to a target directory, 
 :  maintaining the same directory structure found below the source directory
 : 
 : @param $sourceDir Native path or URI to the source directory where the original document is stored
 : @param $targetDir Native path or URI to the target directory where the transformed document will be written
 : @param $originalDoc Document-node() of the original document before transformation
 : @param $newItems Transformed XML document to be output
 : @param $outputParms Serialization paramaters. If none provided, $utils:DEFAULT_OUTPUT is used
 : @return Native path to a local file.
:)
declare function utils:write-transformation($sourceDir as xs:anyAtomicType, $targetDir as xs:anyAtomicType, 
                                                $originalDoc as document-node(), $newItems,
                                                $outputParms as element(output:serialization-parameters)?) {
    
    let $sourceDir := utils:uri-to-path($sourceDir)
    let $targetDir := utils:uri-to-path($targetDir)
    
    let $newPathEnd := substring-after(utils:uri-to-path(document-uri($originalDoc)), $sourceDir)
    let $newPath := concat($targetDir, $newPathEnd)
    return
        utils:write-file($newPath, $newItems, $outputParms)
};

declare function utils:write-transformation($sourceDir as xs:anyAtomicType, $targetDir as xs:anyAtomicType, 
                                                $originalDoc as document-node(), $newItems) {
    
    utils:write-transformation($sourceDir, $targetDir, $originalDoc, $newItems, ())
};

(:~
 : Writes $items to a local file at the given native path
 : 
 : @param $path Native path or URI to the target file
 : @param $items XML document to be output
 : @param $outputParms Serialization paramaters. If none provided, $utils:DEFAULT_OUTPUT is used
 : @return Native path to the newly written local file.
:)
declare function utils:write-file($path as xs:anyAtomicType, $items, $outputParms as element(output:serialization-parameters)?) {
    let $outputParms := if (empty($outputParms)) then $utils:DEFAULT_OUTPUT else $outputParms
    let $path := utils:uri-to-path($path)
    let $dir := utils:get-dir-from-file($path)
    return (
        file:create-dir($dir),
        file:write($path, $items, $outputParms),
        $path
    )
};

declare function utils:write-file($path as xs:anyAtomicType, $items) {
    utils:write-file($path, $items, ())
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
declare function utils:parse-tab-file($path as xs:string, $hasHeaders as xs:boolean?) as element(row)* {
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

declare function utils:parse-tab-file($path as xs:string) as element(row)* {
    utils:parse-tab-file($path, ())
};


(: Generic functions for processing XML :)

(:~
 : Returns true if the given attribute exists and does not contain an empty string
 : 
 : @param $string String to be checked for whitespace
 : @param $type Can be "anywhere", "starts", "ends", "all". If none of those options are given, the default is "anywhere".
 : @return True if whitespace is present for the given $type
:)
declare function utils:attr-exists($attribute as attribute()?) as xs:boolean {
    exists($attribute) and $attribute ne ""
};

(:~
 : Returns true if the given string contains whitespace specified by the type of check
 :  - Field names that are not valid QNames are prefixed with f_ (this is always the case if the file has no headers)
 : 
 : @param $string String to be checked for whitespace
 : @param $type Can be "anywhere", "starts", "ends", "all". If none of those options are given, the default is "anywhere".
 : @return True if whitespace is present for the given $type
:)
declare function utils:contains-ws($string as xs:string?, $type as xs:string?) as xs:boolean {
    let $regex :=
        switch ($type)
        case "starts" return "^\s"
        case "ends" return "\s$"
        case "all" return "^\s+$"
        default return "\s"
    return
        matches($string, $regex)
};

declare function utils:contains-ws($string as xs:string?) as xs:boolean {
    utils:contains-ws($string, ())
};

(:~
 : Checks if a text() node is an empty Oxygen comment 
 :  (i.e. all whitesapce surrounded by oxy_comment_start and oxy_comment_end processing instructions) 
 : 
 : @param $textNode text() node to be checked
 : @return True if the text() node is all whitespace and surrounded by Oxygen comment processing instructions
:)
declare function utils:is-empty-oxy-comment($textNode as node()?) as xs:boolean {
    (
        exists($textNode) 
        and $textNode instance of text()
        and $textNode/preceding-sibling::node()[1] instance of processing-instruction(oxy_comment_start)
        and $textNode/following-sibling::node()[1] instance of processing-instruction(oxy_comment_end)
        and normalize-space($textNode) eq ""
    )
};

(:~
 : Get all text() nodes below the given element that contain text (i.e. not just whitespace) 
 : 
 : @param $element element() node to retrieve text() nodes from
 : @return Set of text() nodes that contain text 
:)
declare function utils:non-empty-text-nodes($element as element()) as text()* {
    $element//text()[normalize-space() ne ""]
};

(:~
 : Generate a new element with the given name within the given namespace
 : - If no namespace is given, an element in the 'empty' namespace is returned
 : 
 : @param $name name for the new element, including the optional prefix
 : @param $content content to go inside the new element
 : @param $namespace XML namespace for the new element, no namespace results in an element in the 'empty' namespace
 : @return New element() node with the given name within the given namespace containing the supplied content
:)
declare function utils:element-NS($name as xs:string, $content, $namespace as xs:string?) as element() {
    element { QName($namespace, $name) } { $content }
};

declare function utils:element-NS($name as xs:string, $content) as element() {
    utils:element-NS($name, $content, ())
};

(:~
 : Generate a new attribute with the given name within the given namespace
 : - If no namespace is given, an attribute in the 'empty' namespace is returned
 : 
 : @param $name name for the new attribute, including the optional prefix
 : @param $value the value for the new attribute
 : @param $namespace XML namespace for the new attribute, no namespace results in an attribute in the 'empty' namespace
 : @return New attribute() node with the given name within the given namespace containing the supplied value
:)
declare function utils:attribute-NS($name as xs:string, $value as xs:anyAtomicType?, $namespace as xs:string?) as attribute()? {
    if (empty($value) or string($value) eq "") then
        ()
    else
        attribute { QName($namespace, $name) } { $value }
};

declare function utils:attribute-NS($name as xs:string, $value as xs:anyAtomicType?) as attribute()? {
    utils:attribute-NS($name, $value, ())
};

(:~
 : Transform an element by replacing its contents
 : - If new attributes are provided, existing attributes are overwritten, otherwise they are copied 
 : 
 : @param $element element() node to have its content replaced
 : @param $newContent Set of item() values that will replace existing content
 : @param $newAttributes Set of attribute() nodes to overwrite the element() node's existing attributes
 : @return New element() node containing $newContent, with the same node-name() as the original element() node
:)
declare function utils:replace-content($element as element(), $newContent, 
                                            $newAttributes as attribute()*) as element() {
    element { node-name($element) } {
        if (exists($newAttributes)) then $newAttributes else $element/@*,
        $newContent
    }
};

declare function utils:replace-content($element as element(), $newContent) as element() {
    utils:replace-content($element, $newContent, ())
};

(:~
 : Merge new attributes with those on an existing element
 : 
 : @param $element element() node to update the attributes on
 : @param $newAttributes Set of attribute() nodes to merge with the element() node's existing attributes
 : @return New element() Node containing $newAttributes merged with existing attributes
:)
declare function utils:update-attributes($element as element(), $newAttributes as attribute()*) as element() {
    let $newAttrNames := for $attr in $newAttributes return local-name($attr)
    return
        element { node-name($element) } {
            $element/@* except $element/@*[local-name() = $newAttrNames], 
            $newAttributes
        }
};

(:~
 : Update the content of an element() node that requires the elements below it to be in a specific order
 : - Also returns original nodes that are not affected by the update (e.g. comments, processing instructions)
 : - Items being added take precedence over older nodes and therefore come first in the output
 : 
 : @param $element element() node with ordered sub-elements to be updated
 : @param $fieldNames Ordered set of all possible element names that could appear beneath the main element() node.
 :  Either a set of at least one string, i.e. xs:string+ or a map using the element() node's local-name as the key 
 :  with the value being the ordered set of element names, i.e. map(xs:string, xs:string+) 
 : @param $newElements Set of new element() nodes to update the existing content with
 : @return New element() node containing updated content in the order of $fieldNames, 
 :  with $newElements replacing the original element where present
 : @error If $fieldNames is neither of type xs:string+ nor map(xs:string, xs:string+), a NoFieldNamesError is thrown
:)
declare function utils:update-content-ordered($element as element(), $fieldNames as item()+, 
                                                            $newElements as element()*) as element() {
    let $nodes := $element/node()
    let $fieldNames := 
        if ($fieldNames instance of xs:string+) then
            $fieldNames
        else if ($fieldNames instance of map(xs:string, xs:string+)) then
            $fieldNames(local-name($element))
        else
            error($utils:NO_FIELD_NAMES_ERROR, concat("No valid fieldNames were provided! ",
                "The $fieldNames variable must be either xs:string+ or map(xs:string, xs:string+)."))
    
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








