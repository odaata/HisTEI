xquery version "3.1";

(:~
 : Transform tokenized Amsterdam texts to be submitted to the INL Succeed tagging service for Early Modern Dutch
 : - Sets each teiHeader to a blank (yet valid) one and removes facsimile element altogether
 : - Removes front and back material (replacing it with new back material in the form of del and note elements, if present)
 : - Removes all markup in mixed content within conent nodes, which are within the TEI body and found in $teix:CONTENT_ELEMENT_NAMES
 :      This leaves just elements that show the document structure and removes all other markup within them
 : - All punctuation is removed as it sometimes confuses the INL tagger (i.e. the incorporate external punctuation into existing w elements)
 : - Words found in note or del elements are removed from the main text and appended as back material - one p for each element in the main text
 :      This separates these out as they are not part of the text anyway (and will not be searched for ling. data) and it might 
 :      increase INL accuracy, as they usually don't make sense in conjunction with other undeleted/unnoted words around them
 : - Existing content in w elements are replaced with their attested-form (via txt:attested-forms())
 :      Because U.E. and U.L. are particuarly hard for the INL, they are replaced by ue or ul respectively,
 :      if they are marked as abbrevations and their raw text is found in the list of unique UE/UL forms (generated from a previous query/manual editing) 
 : - The actual submission is done via a Ruby script at /home/mike/aptana/amsterdam/inl_tagger.rb
 :      Tried first to install the EXPath HTTP client in Saxon locally and then use the one built into eXist,
 :      but sadly neither worked at all (after hours of trying) - then I did what I needed in Ruby in about an hour :-)
 :
 : @author Mike Olson
 : @version 0.1
 :)
(:import module namespace rpt="http://histei.info/xquery/reports" at "reports.xqm";:)
import module namespace sax="http://histei.info/xquery/utils/saxon" at "utils-saxon.xqm";
import module namespace teix="http://histei.info/xquery/tei" at "tei.xqm";
import module namespace txt="http://histei.info/xquery/tei2text" at "tei2text.xqm";
import module namespace utils="http://histei.info/xquery/utils" at "utils.xqm";

declare namespace map="http://www.w3.org/2005/xpath-functions/map";
declare namespace tei="http://www.tei-c.org/ns/1.0";

declare variable $dataRoot := "/home/mike/Amsterdam/";
declare variable $corpusPath := concat($dataRoot, "corpus/");
declare variable $tokenizedPath := concat($dataRoot, "tokenized/");
declare variable $transformedPath := concat($dataRoot, "inl_transformed/");

declare variable $ueSearchTerms := doc("/home/mike/Amsterdam/analysis/query_results/UE_unique.xml")/*/*;

declare variable $excludedElementNames := ( "note", "del" );

declare function local:attested-form($ann as element()) as element() {
    let $attForm := txt:attested-forms($ann)
    let $attForm := 
        if (empty($ann//tei:abbr)) then
            $attForm
        else
            let $rawText := lower-case($attForm)
            let $foundTerm := $ueSearchTerms[rawText eq $rawText]
            return
                if (exists($foundTerm)) then
                    string($foundTerm/wntLemma)
                else
                    $attForm
    return
        utils:replace-content($ann, $attForm)
};

declare function local:transform-content($nodes as node()*) {
    for $node in $nodes
    return
        typeswitch($node)
        case text() return
            $node
        case element() return
            let $name := local-name($node)
            return
                if ($name = ( "num", "w" )) then
                    ( text { " " }, local:attested-form($node) )
                else if ($name = ( "pc", $excludedElementNames )) then
                    ()
                else
                    ( text { " " }, local:transform-content($node/node()) )
        default return
            ()
};

declare function local:transform-doc($element as element()) as element() {
    utils:replace-content($element, 
        for $node in $element/node()
        return
            typeswitch($node)
            case element() return
                let $name := local-name($node)
                return
                    if (exists($node/ancestor::tei:body) and $name = $teix:CONTENT_ELEMENT_NAMES) then
                        utils:replace-content($node, local:transform-content($node/node()))
                    else if ($name eq "teiHeader") then
                        $teix:DEFAULT_HEADER
                    else if ($name = ( "facsimile", "front", "back" )) then
                        ()
                    else
                        local:transform-doc($node)
            
            case processing-instruction() return ()
            case comment() return ()
            default return $node
    )
};

declare function local:excludedElements($teiDoc as document-node()) as element(tei:div)* {
    for $name in $excludedElementNames
    let $excludedElements := $teiDoc//tei:body//*[local-name() = $teix:CONTENT_ELEMENT_NAMES]//*[local-name() eq $name]
        [exists(.//*[local-name() = $teix:ANNOTATION_ELEMENT_NAMES])]
    return
        if (empty($excludedElements)) then
            ()
        else
            teix:element-tei("div", (
                attribute type { $name },
                for $element in $excludedElements
                return
                    teix:element-tei("p", 
                        utils:replace-content($element, local:transform-content($element/node()) 
                    ))
            ))
};

declare function local:transform-docs($teiDocs as document-node()*) as element(tei:TEI)* {
    for $teiDoc in $teiDocs
    let $transformedTEI := local:transform-doc($teiDoc/tei:TEI[1])
    let $excludedDivs := local:excludedElements($teiDoc)
    return
        if (empty($excludedDivs)) then
            $transformedTEI
        else
            let $back := teix:element-tei("back", $excludedDivs)
            let $text := teix:update-text($transformedTEI/tei:text[1], $back)
            return
                teix:update-TEI($transformedTEI, $text)
};

let $docs := teix:collection(sax:collection-uri($tokenizedPath))
return
    element transformedFiles {
        attribute tokenizedPath { $tokenizedPath },
        attribute transformedPath { $transformedPath },
        attribute total { count($docs) },
        for $doc in $docs
        let $transformedTEI := local:transform-docs($doc)
        let $newFilePath := sax:write-transformation($tokenizedPath, $transformedPath, $doc, $transformedTEI)
        order by $newFilePath
        return
            element file { 
                element path {$newFilePath },
                element wordCount { count($transformedTEI//(tei:w | tei:num)) }
            }
    }





