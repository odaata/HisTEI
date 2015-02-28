xquery version "3.0";

(:~
 : A set of functions for running XQuery scripts from within the HisTEI framework
 :)
module namespace histei="http://histei.info/xquery/utils/histei";

(:import module namespace functx="http://www.functx.com" at "functx.xql";:)
import module namespace rpt="http://histei.info/xquery/reports" at "reports.xqm";
import module namespace sax="http://histei.info/xquery/utils/saxon" at "utils-saxon.xqm";
import module namespace teix="http://histei.info/xquery/tei" at "tei.xqm";
import module namespace tok="http://histei.info/xquery/tei/tokenizer" at "tokenizer.xqm";

declare namespace map="http://www.w3.org/2005/xpath-functions/map";
declare namespace tei="http://www.tei-c.org/ns/1.0";

(: Errors :)
(: Raised by histei:headers-paths() if either the $teiPath or $contextualInfoPath is empty or invalid :)
declare %private variable $histei:INVALID_PATH_ERROR := QName("http://histei.info/xquery/utils/histei/error", "InvalidPathError");

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
declare function histei:headers-paths($teiPath as xs:anyAtomicType?, $contextualInfoPath as xs:anyAtomicType?) as element()* {
    let $uri := sax:collection-uri($teiPath)
    let $conInfoMap := teix:con-info-docs-map(sax:collection-uri($contextualInfoPath))
    return
        if (empty($uri) or empty(map:keys($conInfoMap))) then
            error($histei:INVALID_PATH_ERROR, concat("Either the TEI Path or the Contextual Info Path is invalid! ",
                "Given paths: teiPath: ", $teiPath, " contextualInfoPath: ", $contextualInfoPath))
        else
            rpt:headers(teix:collection($uri), $conInfoMap)
};

declare function histei:tokenize-collection($teiURI as xs:anyAtomicType, $tokenizedURI as xs:anyAtomicType, $userID as xs:string?) as element() {
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







