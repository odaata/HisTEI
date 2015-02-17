xquery version "3.0";

(:~
 : Tokenize an entire collection of TEI documents
 :
 : @author Mike Olson
 : @version 0.1
 :)

import module namespace tok="http://histei.info/xquery/tei/tokenizer" at "tokenizer.xqm";
import module namespace utils="http://histei.info/xquery/utils" at "utils.xqm";

declare namespace file="http://expath.org/ns/file";
declare namespace tei="http://www.tei-c.org/ns/1.0";

declare variable $userID as xs:string external := "";
declare variable $teiURI as xs:anyURI external;
declare variable $tokenizedURI as xs:anyURI external;

(:let $userID := "MJO":)

(: Windows URIs:)
(:let $teiURI := xs:anyURI("file:/Z:/home/Amsterdam/dbnl/tei")
let $tokenizedURI := xs:anyURI("file:/Z:/home/Amsterdam/tokenized"):)

(:let $teiURI := xs:anyURI("file:/Z:/home/Amsterdam/test directory"):)

(: Linux/Mac URIs :)
(:let $teiURI := xs:anyURI("file:/home/mike/Amsterdam/dbnl/tei")
let $tokenizedURI := xs:anyURI("file:/home/mike/Amsterdam/tokenized"):)

(:let $teiURI := xs:anyURI("file:/home/mike/Amsterdam/test directory"):)

(:let $testLetterPath := "/home/mike/Amsterdam/test directory/test letter 2/test letter 2.xml"
let $testLetter := doc($testLetterPath):)

let $teiPath := utils:get-dir-path($teiURI)
let $tokenizedPath := utils:get-dir-path($tokenizedURI)
return
    element tokenizedFiles {
        attribute userID { $userID },
        if (exists($teiPath) and exists($tokenizedPath)) then
            let $docs := collection(utils:saxon-collection-uri($teiPath))[exists(tei:TEI)]
            return
            (
                attribute transPath { $teiPath },
                attribute tokenizedPath { $tokenizedPath },
                attribute total { count($docs) },
                for $doc in $docs
                let $tokenizedTrans := tok:tokenize($doc/tei:TEI[1], $userID)
                let $newFilePath := utils:write-transformation($teiPath, $tokenizedPath, $doc, $tokenizedTrans)
                order by $newFilePath
                return
                    element file { 
                         element path { 
                             $newFilePath
                         },
                        element wordCount { 
                            data($tokenizedTrans//tei:fileDesc/tei:extent/tei:measure[@unit eq "words"]/@quantity[1]) 
                        }
                    }
            )
        else
        (
            element transDir { attribute invalid { empty($teiPath) }, $teiURI },
            element tokenizedDir { attribute invalid { empty($tokenizedPath) }, $tokenizedURI },
            element error { "The provided directories are invalid!" }
        )
    }


