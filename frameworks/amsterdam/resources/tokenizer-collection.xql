xquery version "3.0";

(:~
 : Tokenize an entire collection of TEI documents
 :
 : @author Mike Olson
 : @version 0.1
 :)

import module namespace tok="http://histei.info/xquery/tei/tokenizer" at "tokenizer.xqm";
import module namespace functx="http://www.functx.com" at "functx.xql";
import module namespace utils="http://histei.info/xquery/utils" at "utils.xqm";

declare namespace file="http://expath.org/ns/file";
declare namespace tei="http://www.tei-c.org/ns/1.0";

declare namespace output="http://www.w3.org/2010/xslt-xquery-serialization";
declare option output:omit-xml-declaration "no";
declare option output:indent "yes";

declare variable $userID as xs:string external := "";
declare variable $transURI as xs:anyURI external;
declare variable $tokenizedURI as xs:anyURI external;

(:let $userID := "MJO"
let $transURI := xs:anyURI("file:/Z:/home/Amsterdam/dbnl/tei/DBNL_bran002leve01_01.xml")
let $tokenizedURI := xs:anyURI("file:/Z:/home/Amsterdam/tokenized/"):)

let $transPath := utils:get-dir-path($transURI)
let $tokenizedPath := utils:get-dir-path($tokenizedURI)
return
    element tokenizedFiles {
        attribute userID { $userID },
        if (exists($transPath) and exists($tokenizedPath)) then
            let $docs := collection(utils:path-to-uri($transPath))[exists(tei:TEI)]
            return
            (
                attribute transPath { $transPath },
                attribute tokenizedPath { $tokenizedPath },
                attribute total { count($docs) },
                for $doc in $docs
                let $fileName := functx:substring-after-last(string(document-uri($doc)), "/")
                let $tokenizedFilePath := concat($tokenizedPath, $fileName)
                order by $fileName
                
                let $trans := $doc/tei:TEI[1]
                let $tokenizedTrans := tok:tokenize($trans, $userID)
                return
                    element file { 
                        file:write($tokenizedFilePath, $tokenizedTrans, 
                           <output:serialization-parameters>
                               <output:omit-xml-declaration value="no"/>
                           </output:serialization-parameters>
                        ),
                        element filename { $fileName }, 
                        element wordCount { 
                            data($tokenizedTrans//tei:fileDesc/tei:extent/tei:measure[@unit eq "words"]/@quantity[1]) 
                        }
                    }
            )
        else
        (
            element transDir { attribute invalid { empty($transPath) }, $transURI },
            element tokenizedDir { attribute invalid { empty($tokenizedPath) }, $tokenizedURI },
            element error { "The provided directories are invalid!" }
        )
    }


