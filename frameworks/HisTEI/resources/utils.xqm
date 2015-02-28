xquery version "3.0";

(:~
 : A set of generic XML helper functions for use in XQuery scripts in the HisTEI framework
 :)
module namespace utils="http://histei.info/xquery/utils";

import module namespace functx="http://www.functx.com" at "functx.xql";

declare namespace decoder="java:java.net.URLDecoder";
declare namespace map="http://www.w3.org/2005/xpath-functions/map";

(: Errors :)
(: Raised by utils:update-content-ordered() if no fieldNames are provided either as xs:string+ or map(xs:string, xs:string+) :)
declare %private variable $utils:NO_FIELD_NAMES_ERROR := QName("http://histei.info/xquery/utils/error", "NoFieldNamesError");


(: Generic functions for processing XML :)

(:~
 : Gets the document URI and then returns the filename portion of the path for each input document-node()
 : 
 : @param $docs Set of document nodes (e.g. from a call to collection())
 : @return Set of filenames, one for each document-node in $docs
:)
declare function utils:filenames($docs as document-node()*) as xs:string* {
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
declare function utils:file-basenames($docs as document-node()*) as xs:string* {
    let $filenames := utils:filenames($docs)
    for $filename in $filenames
    return
        functx:substring-before-last($filename, ".")
};

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








